package rasterizacao.teste;

import rasterizacao.model.Bresenham;
import rasterizacao.model.DDA;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlgoritmosRetaTeste {
    private AlgoritmosRetaTeste() { }

    private static final class Pixel {
        final int x;
        final int y;

        Pixel(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object outro) {
            if (!(outro instanceof Pixel)) return false;
            Pixel p = (Pixel) outro;
            return x == p.x && y == p.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }

        @Override
        public String toString() { return "(" + x + "," + y + ")"; }
    }

    private static List<Pixel> dda(int x1, int y1, int x2, int y2) {
        List<Pixel> pixels = new ArrayList<>();
        DDA.desenhar(x1, y1, x2, y2, (x, y) -> pixels.add(new Pixel(x, y)));
        return pixels;
    }

    private static List<Pixel> bresenham(int x1, int y1, int x2, int y2) {
        List<Pixel> pixels = new ArrayList<>();
        Bresenham.desenhar(x1, y1, x2, y2, (x, y) -> pixels.add(new Pixel(x, y)));
        return pixels;
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    private static void conferirSequencia(String nome, List<Pixel> atual, int[][] esperado) {
        exigir(atual.size() == esperado.length,
                nome + ": quantidade incorreta: " + atual);
        for (int i = 0; i < esperado.length; i++) {
            exigir(atual.get(i).equals(new Pixel(esperado[i][0], esperado[i][1])),
                    nome + ": passo " + i + ", recebido " + atual.get(i)
                            + ", esperado (" + esperado[i][0] + "," + esperado[i][1] + ")");
        }
    }

    private static void verificarPropriedades(String nome, List<Pixel> pixels,
                                               int x1, int y1, int x2, int y2) {
        int passos = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        exigir(pixels.size() == passos + 1, nome + ": quantidade de pixels incorreta " + pixels);
        exigir(pixels.get(0).equals(new Pixel(x1, y1)), nome + ": inicio incorreto");
        exigir(pixels.get(pixels.size() - 1).equals(new Pixel(x2, y2)), nome + ": fim incorreto");
        Set<Pixel> unicos = new HashSet<>(pixels);
        exigir(unicos.size() == pixels.size(), nome + ": pixels repetidos: " + pixels);

        for (int i = 0; i < pixels.size(); i++) {
            Pixel p = pixels.get(i);
            exigir(p.x >= Math.min(x1, x2) && p.x <= Math.max(x1, x2)
                    && p.y >= Math.min(y1, y2) && p.y <= Math.max(y1, y2),
                    nome + ": pixel fora dos extremos: " + p);
            if (i > 0) {
                Pixel anterior = pixels.get(i - 1);
                int difX = Math.abs(p.x - anterior.x);
                int difY = Math.abs(p.y - anterior.y);
                exigir(difX <= 1 && difY <= 1 && difX + difY >= 1,
                        nome + ": interrupcao/descontinuidade: " + anterior + " -> " + p);
            }
        }
    }

    private static void testarDDA() {
        conferirSequencia("DDA - exemplo", dda(6, 9, 11, 12),
                new int[][] {{6,9}, {7,10}, {8,10}, {9,11}, {10,11}, {11,12}});
        conferirSequencia("DDA - um pixel", dda(5, 7, 5, 7), new int[][] {{5,7}});
        conferirSequencia("DDA - horizontal", dda(2, 4, 5, 4),
                new int[][] {{2,4}, {3,4}, {4,4}, {5,4}});
        conferirSequencia("DDA - vertical inversa", dda(2, 4, 2, 1),
                new int[][] {{2,4}, {2,3}, {2,2}, {2,1}});
        verificarDiferentesDirecoes("DDA", true);
    }

    private static void testarBresenham() {
        conferirSequencia("Bresenham - exemplo", bresenham(20, 10, 30, 18),
                new int[][] {{20,10}, {21,11}, {22,12}, {23,12}, {24,13}, {25,14},
                        {26,15}, {27,16}, {28,16}, {29,17}, {30,18}});
        conferirSequencia("Bresenham - um pixel", bresenham(5, 7, 5, 7),
                new int[][] {{5,7}});
        conferirSequencia("Bresenham - horizontal inversa", bresenham(5, 4, 2, 4),
                new int[][] {{5,4}, {4,4}, {3,4}, {2,4}});
        conferirSequencia("Bresenham - vertical", bresenham(2, 1, 2, 4),
                new int[][] {{2,1}, {2,2}, {2,3}, {2,4}});
        verificarDiferentesDirecoes("Bresenham", false);
        verificarSimetriaOitoOitantes();
    }

    private static void verificarDiferentesDirecoes(String nome, boolean usaDDA) {
        int quantidade = 0;
        for (int dx = -20; dx <= 20; dx++) {
            for (int dy = -20; dy <= 20; dy++) {
                List<Pixel> pixels = usaDDA
                        ? dda(75, 60, 75 + dx, 60 + dy)
                        : bresenham(75, 60, 75 + dx, 60 + dy);
                verificarPropriedades(nome + " (dx=" + dx + ",dy=" + dy + ")",
                        pixels, 75, 60, 75 + dx, 60 + dy);
                quantidade++;
            }
        }
        exigir(quantidade == 1681, nome + ": numero de testes de direcoes incorreto");
    }

    private static void verificarSimetriaOitoOitantes() {
        int[][] canonica = {{0,0}, {1,0}, {2,1}, {3,1}, {4,2}, {5,2}, {6,3}, {7,3}, {8,4}};
        int centroX = 100, centroY = 100;
        for (int troca = 0; troca <= 1; troca++) {
            for (int sx : new int[] {-1, 1}) {
                for (int sy : new int[] {-1, 1}) {
                    int dx = sx * (troca == 1 ? 4 : 8);
                    int dy = sy * (troca == 1 ? 8 : 4);
                    List<Pixel> obtidos = bresenham(centroX, centroY,
                            centroX + dx, centroY + dy);
                    int[][] esperados = new int[canonica.length][2];
                    for (int i = 0; i < canonica.length; i++) {
                        esperados[i][0] = centroX + sx * canonica[i][troca == 1 ? 1 : 0];
                        esperados[i][1] = centroY + sy * canonica[i][troca == 1 ? 0 : 1];
                    }
                    conferirSequencia("Simetria do octante (" + dx + "," + dy + ")",
                            obtidos, esperados);
                }
            }
        }
    }
    public static void main(String[] args) {
        testarDDA();
        testarBresenham();
        System.out.println("Testes concluídos com sucesso.");
    }
}
