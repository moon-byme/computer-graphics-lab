package rasterizacao.model;

/*
 * Algoritmo polinomial (equacao explicita) da circunferencia.
 *
 * Da equacao x^2 + y^2 = r^2 tira-se y = sqrt(r^2 - x^2). No 2o oitante x avanca
 * de 1 em 1 a partir de 0 e, para cada x, y e calculado e arredondado. O laco para
 * quando cruza a diagonal (x > y); o resto vem da simetria de oito pontos.
 *
 * Desvantagem: uma raiz quadrada e um arredondamento em ponto flutuante por pixel.
 */
public final class CircunferenciaPolinomial {
    private CircunferenciaPolinomial() { }

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
        if (raio == 0) {               // raio nulo: a circunferencia e so o centro
            pontoDoOitante.desenhar(0, 0);
            return;
        }

        long raioQuadrado = (long) raio * raio;
        for (int x = 0; ; x++) {
            double restante = Math.max(0, raioQuadrado - (long) x * x);
            int y = (int) Math.round(Math.sqrt(restante));
            if (x > y) {               // passou da diagonal: fim do oitante
                break;
            }
            pontoDoOitante.desenhar(x, y);
        }
    }
}