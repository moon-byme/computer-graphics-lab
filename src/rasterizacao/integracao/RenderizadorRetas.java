package rasterizacao.integracao;

import java.util.Objects;
import rasterizacao.model.Bresenham;
import rasterizacao.model.DDA;
import transformacoes.view.Tela;

public final class RenderizadorRetas {
    private RenderizadorRetas() { }

    public static void desenharDDA(Tela tela, int x1, int y1, int x2, int y2) {
        Objects.requireNonNull(tela, "A tela nao pode ser nula.");
        DDA.desenhar(x1, y1, x2, y2, tela::drawPixel);
    }

    public static void desenharBresenham(Tela tela, int x1, int y1, int x2, int y2) {
        Objects.requireNonNull(tela, "A tela nao pode ser nula.");
        Bresenham.desenhar(x1, y1, x2, y2, tela::drawPixel);
    }
}
