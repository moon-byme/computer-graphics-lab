package rasterizacao.model;

/*
 * Algoritmo do ponto medio para a circunferencia (Bresenham / Foley).
 *
 * Funcao implicita: F(x, y) = x^2 + y^2 - r^2
 *   F < 0 -> ponto dentro da circunferencia
 *   F = 0 -> ponto sobre a circunferencia
 *   F > 0 -> ponto fora da circunferencia
 *
 * No 2o oitante, a partir do pixel P = (x, y), o proximo e
 *   E  = (x + 1, y)       ou       SE = (x + 1, y - 1).
 * A decisao usa o ponto medio M = (x + 1, y - 1/2) entre os dois:
 *   d = F(M) < 0  -> M esta dentro, a curva passa acima dele -> escolhe E
 *   d = F(M) >= 0 -> M esta fora ou sobre a curva            -> escolhe SE
 *
 * Forma incremental (diferenca entre o d novo e o d antigo):
 *   escolheu E : d += 2x + 3
 *   escolheu SE: d += 2(x - y) + 5
 * Valor inicial em (0, r): d = F(1, r - 1/2) = 5/4 - r. Como d so muda por
 * inteiros e so o sinal importa, usa-se d = 1 - r e tudo fica em inteiros.
 */
public final class CircunferenciaPontoMedio {
    private CircunferenciaPontoMedio() { }

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

        int x = 0;
        int y = raio;
        long d = 1L - raio;            // 5/4 - r arredondado (long evita overflow em raios enormes)

        while (x <= y) {
            pontoDoOitante.desenhar(x, y);
            if (d < 0) {               // escolhe E
                d += 2L * x + 3;
            } else {                   // escolhe SE
                d += 2L * (x - y) + 5;
                y--;
            }
            x++;
        }
    }
}