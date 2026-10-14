package rasterizacao.teste;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import rasterizacao.controller.CircunferenciaController;
import rasterizacao.model.CircunferenciaPolinomial;
import rasterizacao.model.CircunferenciaPontoMedio;
import rasterizacao.model.CircunferenciaTrigonometrica;
import rasterizacao.model.PlotadorPixel;
import rasterizacao.model.SimetriaCircunferencia;

/*
 * Testes sem interface dos algoritmos de circunferencia.
 *
 * Todos os testes:
 *   java -cp out rasterizacao.teste.AlgoritmosCircunferenciaTeste
 * Um teste individual (ou varios, separados por espaco):
 *   java -cp out rasterizacao.teste.AlgoritmosCircunferenciaTeste polinomial
 *   opcoes: simetria, polinomial, trigonometrico, pontomedio, controller
 */
public class AlgoritmosCircunferenciaTeste {
    private AlgoritmosCircunferenciaTeste() { }

    private static final int RAIO_MAXIMO_PROPRIEDADES = 200;
    private static final double ERRO_ARREDONDAMENTO = 0.5;            // um eixo arredondado
    private static final double ERRO_DOIS_EIXOS = Math.sqrt(2) / 2;   // x e y arredondados

    private record Pixel(int x, int y) {
        @Override public String toString() { return "(" + x + "," + y + ")"; }
    }

    @FunctionalInterface
    private interface Desenho {
        void desenhar(int xc, int yc, int raio, PlotadorPixel plotador);
    }

    @FunctionalInterface
    private interface Oitante {
        void percorrer(int raio, PlotadorPixel plotador);
    }

    // ------------------------------------------------------------------ utilitarios

    private static List<Pixel> desenhar(Desenho desenho, int xc, int yc, int raio) {
        List<Pixel> pixels = new ArrayList<>();
        desenho.desenhar(xc, yc, raio, (x, y) -> pixels.add(new Pixel(x, y)));
        return pixels;
    }

    private static List<Pixel> oitante(Oitante oitante, int raio) {
        List<Pixel> pixels = new ArrayList<>();
        oitante.percorrer(raio, (x, y) -> pixels.add(new Pixel(x, y)));
        return pixels;
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    private static void exigirExcecao(Class<? extends Throwable> tipo, Runnable acao, String nome) {
        try {
            acao.run();
        } catch (Throwable erro) {
            exigir(tipo.isInstance(erro), nome + ": esperava " + tipo.getSimpleName()
                    + ", recebeu " + erro.getClass().getSimpleName());
            return;
        }
        throw new AssertionError(nome + ": esperava " + tipo.getSimpleName() + ", nada foi lancado");
    }

    private static List<Pixel> pixels(int[][] coordenadas) {
        List<Pixel> lista = new ArrayList<>();
        for (int[] c : coordenadas) lista.add(new Pixel(c[0], c[1]));
        return lista;
    }

    private static void conferirSequencia(String nome, List<Pixel> atual, int[][] esperado) {
        exigir(atual.equals(pixels(esperado)),
                nome + ": recebido " + atual + ", esperado " + pixels(esperado));
    }

    private static void conferirConjunto(String nome, List<Pixel> atual, int[][] esperado) {
        Set<Pixel> esperados = new HashSet<>(pixels(esperado));
        exigir(atual.size() == esperados.size() && esperados.equals(new HashSet<>(atual)),
                nome + ": recebido " + atual + ", esperado " + esperados);
    }

    // ------------------------------------------------------------------ propriedades

    /*
     * Pontos do 2o oitante: comecam em (0, r), ficam em 0 <= x <= y, andam no maximo
     * 1 pixel por vez (x nao diminui, y nao aumenta) e terminam encostados na diagonal.
     */
    private static void verificarOitante(String nome, List<Pixel> pontos, int raio, boolean umPorColuna) {
        exigir(!pontos.isEmpty(), nome + ": oitante vazio");
        exigir(pontos.get(0).equals(new Pixel(0, raio)), nome + ": nao comeca em (0," + raio + ")");
        for (int i = 0; i < pontos.size(); i++) {
            Pixel p = pontos.get(i);
            exigir(0 <= p.x() && p.x() <= p.y(), nome + ": ponto fora do oitante " + p);
            if (i > 0) {
                Pixel anterior = pontos.get(i - 1);
                int passoX = p.x() - anterior.x();
                int passoY = p.y() - anterior.y();
                exigir((passoX == 0 || passoX == 1) && (passoY == 0 || passoY == -1)
                                && (passoX != 0 || passoY != 0),
                        nome + ": passo invalido " + anterior + " -> " + p);
                exigir(!umPorColuna || passoX == 1,
                        nome + ": esperava exatamente um pixel por coluna em " + p);
            }
        }
        Pixel ultimo = pontos.get(pontos.size() - 1);
        exigir(ultimo.x() == ultimo.y() || ultimo.x() == ultimo.y() - 1,
                nome + ": o oitante nao termina junto a diagonal: " + ultimo);
    }

    private static void verificarCircunferencia(String nome, List<Pixel> pixels,
                                                int xc, int yc, int raio, double erroMaximo) {
        Set<Pixel> conjunto = new HashSet<>(pixels);
        exigir(conjunto.size() == pixels.size(), nome + ": pixels repetidos");

        if (raio == 0) {
            exigir(pixels.equals(List.of(new Pixel(xc, yc))), nome + ": raio nulo deve gerar so o centro");
            return;
        }

        // Os quatro extremos (topo, base, esquerda, direita) precisam estar presentes.
        for (Pixel extremo : List.of(new Pixel(xc, yc + raio), new Pixel(xc, yc - raio),
                new Pixel(xc + raio, yc), new Pixel(xc - raio, yc))) {
            exigir(conjunto.contains(extremo), nome + ": falta o extremo " + extremo);
        }

        for (Pixel p : pixels) {
            int dx = p.x() - xc;
            int dy = p.y() - yc;

            double erro = Math.abs(Math.hypot(dx, dy) - raio);
            exigir(erro <= erroMaximo + 1e-9,
                    nome + ": pixel " + p + " longe da circunferencia (erro " + erro + ")");

            // Simetria de oito pontos: todos os refletidos tambem foram desenhados.
            int[][] reflexoes = {{dx, dy}, {-dx, dy}, {dx, -dy}, {-dx, -dy},
                                 {dy, dx}, {-dy, dx}, {dy, -dx}, {-dy, -dx}};
            for (int[] r : reflexoes) {
                exigir(conjunto.contains(new Pixel(xc + r[0], yc + r[1])),
                        nome + ": falta o simetrico de " + p);
            }

            // Curva fechada: cada pixel tem pelo menos dois vizinhos (8-vizinhanca).
            exigir(contarVizinhos(conjunto, p) >= 2, nome + ": pixel solto ou ponta aberta em " + p);
        }

        exigir(componentesConexos(conjunto) == 1, nome + ": a circunferencia tem interrupcoes");
    }

    private static int contarVizinhos(Set<Pixel> conjunto, Pixel p) {
        int vizinhos = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if ((dx != 0 || dy != 0) && conjunto.contains(new Pixel(p.x() + dx, p.y() + dy))) {
                    vizinhos++;
                }
            }
        }
        return vizinhos;
    }

    // Busca em largura com 8-vizinhanca: 1 componente = curva continua.
    private static int componentesConexos(Set<Pixel> conjunto) {
        Set<Pixel> visitados = new HashSet<>();
        int componentes = 0;
        for (Pixel inicio : conjunto) {
            if (!visitados.add(inicio)) continue;
            componentes++;
            Deque<Pixel> fila = new ArrayDeque<>(List.of(inicio));
            while (!fila.isEmpty()) {
                Pixel atual = fila.poll();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        Pixel vizinho = new Pixel(atual.x() + dx, atual.y() + dy);
                        if (conjunto.contains(vizinho) && visitados.add(vizinho)) fila.add(vizinho);
                    }
                }
            }
        }
        return componentes;
    }

    private static void verificarPropriedades(String nome, Desenho desenho, Oitante oitante,
                                              double erroMaximo, boolean umPorColuna) {
        for (int raio = 0; raio <= RAIO_MAXIMO_PROPRIEDADES; raio++) {
            verificarOitante(nome + " (oitante, r=" + raio + ")", oitante(oitante, raio), raio, umPorColuna);
            verificarCircunferencia(nome + " (r=" + raio + ")", desenhar(desenho, 0, 0, raio),
                    0, 0, raio, erroMaximo);
        }
    }

    // Mudar o centro so desloca o desenho: mesmos pixels, mesma ordem, somando (xc, yc).
    private static void verificarDiferentesCentros(String nome, Desenho desenho) {
        int[][] centros = {{0, 0}, {320, 240}, {-50, 30}, {7, -13}, {-100, -100},
                           {Integer.MAX_VALUE - 25, Integer.MIN_VALUE + 25}};
        int[] raios = {0, 1, 2, 7, 25};
        for (int raio : raios) {
            List<Pixel> naOrigem = desenhar(desenho, 0, 0, raio);
            for (int[] c : centros) {
                List<Pixel> deslocado = desenhar(desenho, c[0], c[1], raio);
                List<Pixel> esperado = new ArrayList<>();
                for (Pixel p : naOrigem) esperado.add(new Pixel(p.x() + c[0], p.y() + c[1]));
                exigir(deslocado.equals(esperado), nome + ": centro (" + c[0] + "," + c[1]
                        + "), r=" + raio + " nao e a translacao do desenho na origem");
                verificarCircunferencia(nome + " centro (" + c[0] + "," + c[1] + ")",
                        deslocado, c[0], c[1], raio, 1.0);
            }
        }
    }

    private static void verificarEntradasInvalidas(String nome, Desenho desenho) {
        List<Pixel> plotados = new ArrayList<>();
        PlotadorPixel lista = (x, y) -> plotados.add(new Pixel(x, y));

        exigirExcecao(IllegalArgumentException.class,
                () -> desenho.desenhar(0, 0, -1, lista), nome + " raio negativo");
        exigirExcecao(NullPointerException.class,
                () -> desenho.desenhar(0, 0, 5, null), nome + " plotador nulo");
        exigirExcecao(IllegalArgumentException.class,
                () -> desenho.desenhar(Integer.MAX_VALUE, 0, 1, lista), nome + " estouro de inteiro");
        exigir(plotados.isEmpty(), nome + ": entrada invalida nao pode plotar nenhum pixel");
    }

    // ------------------------------------------------------------------ testes individuais

    private static void testarSimetria() {
        List<Pixel> p = new ArrayList<>();
        SimetriaCircunferencia.plotarOitoPontos(10, 20, 0, 0, (x, y) -> p.add(new Pixel(x, y)));
        conferirConjunto("Simetria (0,0)", p, new int[][] {{10, 20}});

        p.clear();
        SimetriaCircunferencia.plotarOitoPontos(10, 20, 0, 5, (x, y) -> p.add(new Pixel(x, y)));
        conferirConjunto("Simetria sobre os eixos (0,5)", p,
                new int[][] {{10, 25}, {10, 15}, {15, 20}, {5, 20}});

        p.clear();
        SimetriaCircunferencia.plotarOitoPontos(10, 20, 3, 3, (x, y) -> p.add(new Pixel(x, y)));
        conferirConjunto("Simetria sobre a diagonal (3,3)", p,
                new int[][] {{13, 23}, {7, 23}, {13, 17}, {7, 17}});

        p.clear();
        SimetriaCircunferencia.plotarOitoPontos(10, 20, 2, 5, (x, y) -> p.add(new Pixel(x, y)));
        conferirConjunto("Simetria geral (2,5)", p,
                new int[][] {{12, 25}, {8, 25}, {12, 15}, {8, 15},
                             {15, 22}, {5, 22}, {15, 18}, {5, 18}});

        exigirExcecao(IllegalArgumentException.class,
                () -> SimetriaCircunferencia.plotarOitoPontos(0, 0, -1, 2, (x, y) -> { }),
                "Simetria com deslocamento negativo");
        System.out.println("OK: simetria de oito pontos (sem pixels repetidos em x = 0 e x = y).");
    }

    private static void testarPolinomial() {
        String nome = "Polinomial";
        Desenho desenho = CircunferenciaPolinomial::desenhar;

        // r = 10: y = round(sqrt(100 - x^2)) para x = 0..7 -> 10; 9,95; 9,80; 9,54; 9,17; 8,66; 8; 7,14
        conferirSequencia(nome + " - oitante r=10", oitante(CircunferenciaPolinomial::percorrerOitante, 10),
                new int[][] {{0, 10}, {1, 10}, {2, 10}, {3, 10}, {4, 9}, {5, 9}, {6, 8}, {7, 7}});
        conferirConjunto(nome + " - r=1", desenhar(desenho, 5, 5, 1),
                new int[][] {{5, 6}, {5, 4}, {6, 5}, {4, 5}});
        conferirSequencia(nome + " - raio nulo", desenhar(desenho, -7, 12, 0), new int[][] {{-7, 12}});
        exigir(desenhar(desenho, 0, 0, 5).size() == 28, nome + ": r=5 deveria ter 28 pixels");

        verificarPropriedades(nome, desenho, CircunferenciaPolinomial::percorrerOitante,
                ERRO_ARREDONDAMENTO, true);
        verificarDiferentesCentros(nome, desenho);
        verificarEntradasInvalidas(nome, desenho);
        System.out.println("OK: polinomial (r = 0.." + RAIO_MAXIMO_PROPRIEDADES + ", varios centros).");
    }

    private static void testarTrigonometrico() {
        String nome = "Trigonometrico";
        Desenho desenho = CircunferenciaTrigonometrica::desenhar;

        conferirSequencia(nome + " - oitante r=10",
                oitante(CircunferenciaTrigonometrica::percorrerOitante, 10),
                new int[][] {{0, 10}, {1, 10}, {2, 10}, {3, 10}, {4, 9}, {5, 9}, {6, 8}, {7, 7}});
        // Com r = 1 o passo e 1 rad: a amostra em 90 - 57,3 graus arredonda para (1,1).
        conferirConjunto(nome + " - r=1", desenhar(desenho, 5, 5, 1),
                new int[][] {{5, 6}, {5, 4}, {6, 5}, {4, 5}, {6, 6}, {4, 6}, {6, 4}, {4, 4}});
        conferirSequencia(nome + " - raio nulo", desenhar(desenho, -7, 12, 0), new int[][] {{-7, 12}});

        verificarPropriedades(nome, desenho, CircunferenciaTrigonometrica::percorrerOitante,
                ERRO_DOIS_EIXOS, false);
        verificarDiferentesCentros(nome, desenho);
        verificarEntradasInvalidas(nome, desenho);
        System.out.println("OK: trigonometrico (r = 0.." + RAIO_MAXIMO_PROPRIEDADES + ", varios centros).");
    }

    private static void testarPontoMedio() {
        String nome = "Ponto medio";
        Desenho desenho = CircunferenciaPontoMedio::desenhar;

        // Exemplo classico (Hearn & Baker), r = 10. Variavel de decisao a cada passo:
        // d = -9 (E), -6 (E), -1 (E), 6 (SE), -3 (E), 8 (SE), 5 (SE)
        conferirSequencia(nome + " - oitante r=10", oitante(CircunferenciaPontoMedio::percorrerOitante, 10),
                new int[][] {{0, 10}, {1, 10}, {2, 10}, {3, 10}, {4, 9}, {5, 9}, {6, 8}, {7, 7}});
        conferirConjunto(nome + " - r=1", desenhar(desenho, 5, 5, 1),
                new int[][] {{5, 6}, {5, 4}, {6, 5}, {4, 5}});
        conferirSequencia(nome + " - raio nulo", desenhar(desenho, -7, 12, 0), new int[][] {{-7, 12}});
        exigir(desenhar(desenho, 0, 0, 5).size() == 28, nome + ": r=5 deveria ter 28 pixels");

        verificarPropriedades(nome, desenho, CircunferenciaPontoMedio::percorrerOitante,
                ERRO_ARREDONDAMENTO, true);

        // O teste do ponto medio escolhe o mesmo y que o arredondamento do polinomial,
        // so que usando apenas inteiros (sem raiz quadrada).
        for (int raio = 0; raio <= 1000; raio++) {
            exigir(oitante(CircunferenciaPontoMedio::percorrerOitante, raio)
                            .equals(oitante(CircunferenciaPolinomial::percorrerOitante, raio)),
                    nome + ": difere do polinomial em r=" + raio);
        }

        verificarDiferentesCentros(nome, desenho);
        verificarEntradasInvalidas(nome, desenho);
        System.out.println("OK: ponto medio (r = 0.." + RAIO_MAXIMO_PROPRIEDADES
                + ", varios centros, igual ao polinomial ate r = 1000).");
    }

    private static void testarController() {
        List<Pixel> destino = new ArrayList<>();
        CircunferenciaController controller =
                new CircunferenciaController((x, y) -> destino.add(new Pixel(x, y)));

        int quantidade = controller.desenhar("Ponto médio", " 10 ", "20", "5");
        exigir(quantidade == 28 && destino.equals(desenhar(CircunferenciaPontoMedio::desenhar, 10, 20, 5)),
                "Controller: deveria repassar os 28 pixels do ponto medio");

        destino.clear();
        exigir(controller.desenhar("polinomial", "-3", "4", "0") == 1
                        && destino.equals(List.of(new Pixel(-3, 4))),
                "Controller: raio nulo deveria ativar so o centro");

        exigir(CircunferenciaController.lerAlgoritmo("Trigonométrico").name().equals("TRIGONOMETRICO")
                        && CircunferenciaController.lerAlgoritmo("PONTO_MEDIO").name().equals("PONTO_MEDIO")
                        && CircunferenciaController.lerAlgoritmo(" polinomial ").name().equals("POLINOMIAL"),
                "Controller: nomes de algoritmo nao reconhecidos");

        destino.clear();
        exigirExcecao(IllegalArgumentException.class,
                () -> controller.desenhar("bresenham", "0", "0", "5"), "Controller algoritmo invalido");
        exigirExcecao(IllegalArgumentException.class,
                () -> controller.desenhar("polinomial", "0", "0", "-1"), "Controller raio negativo");
        exigirExcecao(IllegalArgumentException.class,
                () -> controller.desenhar("polinomial", "0", "0", "abc"), "Controller raio nao numerico");
        exigirExcecao(IllegalArgumentException.class,
                () -> controller.desenhar("polinomial", "", "0", "5"), "Controller centro vazio");
        exigirExcecao(IllegalArgumentException.class,
                () -> controller.desenhar("polinomial", "0", "0",
                        String.valueOf(CircunferenciaController.RAIO_MAXIMO + 1)), "Controller raio grande");
        exigirExcecao(NullPointerException.class,
                () -> new CircunferenciaController(null), "Controller sem destino");
        exigir(destino.isEmpty(), "Controller: entrada invalida nao pode plotar pixels");
        System.out.println("OK: controller (validacao da entrada e escolha do algoritmo).");
    }

    public static void main(String[] args) {
        List<String> escolhidos = args.length == 0
                ? List.of("simetria", "polinomial", "trigonometrico", "pontomedio", "controller")
                : Arrays.asList(args);

        for (String escolhido : escolhidos) {
            switch (escolhido.toLowerCase(Locale.ROOT).replaceAll("[\\s_-]", "")) {
                case "simetria":       testarSimetria(); break;
                case "polinomial":     testarPolinomial(); break;
                case "trigonometrico": testarTrigonometrico(); break;
                case "pontomedio":     testarPontoMedio(); break;
                case "controller":     testarController(); break;
                default:
                    System.err.println("Teste desconhecido: " + escolhido
                            + ". Use: simetria, polinomial, trigonometrico, pontomedio ou controller.");
                    System.exit(2);
            }
        }
        System.out.println("Testes de circunferencia concluidos com sucesso.");
    }
}