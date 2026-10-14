package rasterizacao.model;

/*
 * Algoritmo trigonometrico (forma parametrica) da circunferencia.
 *
 *   x = r * cos(theta)      y = r * sin(theta)
 *
 * O angulo comeca em 90 graus (ponto (0, r)) e diminui em direcao a 45 graus,
 * percorrendo o 2o oitante. O passo e 1/r radiano: assim cada amostra fica a
 * menos de 1 unidade da anterior (corda = 2r*sen(1/2r) < 1) e a curva rasterizada
 * nao tem buracos. O laco para quando o ponto arredondado cruza a diagonal (x > y).
 *
 * Com esse passo, duas amostras seguidas podem cair no mesmo pixel; a repeticao
 * e descartada. Desvantagem: seno, cosseno e arredondamento a cada amostra.
 */
public final class CircunferenciaTrigonometrica {
    private CircunferenciaTrigonometrica() { }

    // Rasteriza a circunferencia de centro (xc, yc) e raio dado, em coordenadas do dispositivo.
    public static void desenhar(int xc, int yc, int raio, PlotadorPixel plotador) {
        SimetriaCircunferencia.validar(xc, yc, raio, plotador);
        percorrerOitante(raio, (x, y) -> SimetriaCircunferencia.plotarOitoPontos(xc, yc, x, y, plotador));
    }

    // Entrega so os pontos (x, y) do 2o oitante, relativos ao centro, com 0 <= x <= y.
    public static void percorrerOitante(int raio, PlotadorPixel pontoDoOitante) {
        if (raio < 0) {
            throw new IllegalArgumentException("O raio nao pode ser negativo: " + raio);
        }
        if (raio == 0) {               // raio nulo: o passo 1/r nao existe; so o centro
            pontoDoOitante.desenhar(0, 0);
            return;
        }

        double passo = 1.0 / raio;
        int anteriorX = -1;
        int anteriorY = -1;
        for (int i = 0; ; i++) {
            // theta e calculado a partir de i (e nao somando o passo) para nao acumular erro.
            double theta = Math.PI / 2 - i * passo;
            int x = (int) Math.round(raio * Math.cos(theta));
            int y = (int) Math.round(raio * Math.sin(theta));
            if (x > y) {               // passou da diagonal: fim do oitante
                break;
            }
            if (x != anteriorX || y != anteriorY) {
                pontoDoOitante.desenhar(x, y);
                anteriorX = x;
                anteriorY = y;
            }
        }
    }
}