package rasterizacao.model;

import java.util.Objects;

public final class Bresenham {
    private Bresenham() { }

    //Rasteriza uma reta entre dois pixels, incluindo ambos os extremos.
    public static void desenhar(int x1, int y1, int x2, int y2, PlotadorPixel plotador) {
        Objects.requireNonNull(plotador, "O plotador de pixels nao pode ser nulo.");

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sentidoX = Integer.compare(x2, x1); // reflexao no eixo vertical
        int sentidoY = Integer.compare(y2, y1); // reflexao no eixo horizontal

        // Quando dy > dx, refletimos em y = x (troca-se os eixos)
        boolean trocaEixos = dy > dx;
        if (trocaEixos) {
            int auxiliar = dx;
            dx = dy;
            dy = auxiliar;
        }

        int d = 2 * dy - dx;    
        int incE = 2 * dy;     
        int incNE = 2 * (dy - dx); 
        int x = 0;
        int y = 0;

        while (true) {
            int pixelX = x1 + sentidoX * (trocaEixos ? y : x);
            int pixelY = y1 + sentidoY * (trocaEixos ? x : y);
            plotador.desenhar(pixelX, pixelY);

            if (x == dx) {
                break;
            }

            x++;
            if (d <= 0) {  
                d += incE;
            } else {
                y++;
                d += incNE;
            }
        }
    }
}