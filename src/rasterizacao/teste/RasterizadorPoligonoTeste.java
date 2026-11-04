package rasterizacao.teste;

import java.util.List;
import rasterizacao.integracao.RasterizadorPoligono;
import transformacoes.model.Pixel;
import transformacoes.model.Poligono;
import transformacoes.model.Ponto;

/** Testes automatizados básicos da representação e rasterização de polígonos. */
public final class RasterizadorPoligonoTeste {
    private RasterizadorPoligonoTeste() { }

    public static void main(String[] args) {
        testarQuantidadeMinimaDeVertices();
        testarRetanguloFechado();
        testarPoligonoIrregular();
        System.out.println("Todos os testes de polígono passaram.");
    }

    private static void testarQuantidadeMinimaDeVertices() {
        esperarExcecao(() -> new Poligono(List.of(new Ponto(0, 0), new Ponto(1, 0))),
                "Polígono com menos de três vértices deve ser rejeitado.");
    }

    private static void testarRetanguloFechado() {
        Poligono retangulo = new Poligono(List.of(
                new Ponto(-2, -1), new Ponto(2, -1),
                new Ponto(2, 1), new Ponto(-2, 1)));
        List<Pixel> pixels = RasterizadorPoligono.rasterizar(retangulo,
                ponto -> new Pixel((int) ponto.x + 5, (int) ponto.y + 5));

        exigir(contem(pixels, 3, 4), "Aresta inferior não foi rasterizada.");
        exigir(contem(pixels, 7, 4), "Aresta direita não foi rasterizada.");
        exigir(contem(pixels, 7, 6), "Aresta superior não foi rasterizada.");
        exigir(contem(pixels, 3, 6), "Aresta esquerda/fechamento não foi rasterizada.");
        exigir(pixels.size() == 12, "Pixels repetidos nos vértices devem ser eliminados.");
    }

    private static void testarPoligonoIrregular() {
        Poligono triangulo = new Poligono(List.of(
                new Ponto(0, 0), new Ponto(4, 0), new Ponto(1, 3)));
        List<Pixel> pixels = RasterizadorPoligono.rasterizar(triangulo,
                ponto -> new Pixel((int) ponto.x, (int) ponto.y));
        exigir(contem(pixels, 0, 0) && contem(pixels, 4, 0) && contem(pixels, 1, 3),
                "O contorno deve incluir todos os vértices do triângulo.");
        exigir(pixels.size() > 0, "O polígono deve produzir pixels.");
    }

    private static boolean contem(List<Pixel> pixels, int x, int y) {
        return pixels.stream().anyMatch(pixel -> pixel.x == x && pixel.y == y);
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    private static void esperarExcecao(Runnable acao, String mensagem) {
        try {
            acao.run();
        } catch (IllegalArgumentException esperado) {
            return;
        }
        throw new AssertionError(mensagem);
    }
}
