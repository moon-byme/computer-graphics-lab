package rasterizacao.model;

import java.util.Objects;

/*
 * Simetria de oito pontos da circunferencia.
 *
 * Os tres algoritmos calculam apenas o 2o oitante, o arco que vai de 90 graus
 * (ponto (0, r)) ate 45 graus (diagonal x = y). Nesse trecho vale 0 <= x <= y.
 * Os outros sete oitantes saem por reflexao em relacao aos eixos e a reta y = x:
 *
 *   ( x,  y)  ( y,  x)  (-x,  y)  (-y,  x)
 *   ( x, -y)  ( y, -x)  (-x, -y)  (-y, -x)
 *
 * Quando x = 0 ou x = y, alguns desses pontos coincidem; eles sao plotados uma
 * unica vez para nenhum pixel ser ativado duas vezes.
 */
public final class SimetriaCircunferencia {
    private SimetriaCircunferencia() { }

    // Plota (xc + x, yc + y) e os simetricos nos oito oitantes, sem repetir pixel.
    public static void plotarOitoPontos(int xc, int yc, int x, int y, PlotadorPixel plotador) {
        Objects.requireNonNull(plotador, "O plotador de pixels nao pode ser nulo.");
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Os deslocamentos x e y devem ser maiores ou iguais a zero.");
        }

        plotarQuatroPontos(xc, yc, x, y, plotador);    // reflexoes nos eixos
        if (x != y) {
            plotarQuatroPontos(xc, yc, y, x, plotador); // reflexao em y = x
        }
    }

    // (+-a, +-b) em torno do centro; se a ou b for zero, o sinal nao muda o pixel.
    private static void plotarQuatroPontos(int xc, int yc, int a, int b, PlotadorPixel plotador) {
        plotador.desenhar(xc + a, yc + b);
        if (a != 0) plotador.desenhar(xc - a, yc + b);
        if (b != 0) plotador.desenhar(xc + a, yc - b);
        if (a != 0 && b != 0) plotador.desenhar(xc - a, yc - b);
    }

    // Validacao comum aos tres algoritmos, feita antes de qualquer pixel ser plotado.
    static void validar(int xc, int yc, int raio, PlotadorPixel plotador) {
        Objects.requireNonNull(plotador, "O plotador de pixels nao pode ser nulo.");
        if (raio < 0) {
            throw new IllegalArgumentException("O raio nao pode ser negativo: " + raio);
        }
        if ((long) xc - raio < Integer.MIN_VALUE || (long) xc + raio > Integer.MAX_VALUE
                || (long) yc - raio < Integer.MIN_VALUE || (long) yc + raio > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("A circunferencia ultrapassa o intervalo de coordenadas inteiras.");
        }
    }
}