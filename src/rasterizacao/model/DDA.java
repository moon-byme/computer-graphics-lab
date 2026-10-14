package rasterizacao.model;

import java.util.Objects;

public final class DDA {
    private DDA() { }
        //Rasteriza a reta entre os dois pixels, incluindo ambos os extremos.
        
    public static void desenhar(int x1, int y1, int x2, int y2, PlotadorPixel plotador) {
        Objects.requireNonNull(plotador, "O plotador de pixels nao pode ser nulo.");

        int dx = x2 - x1;
        int dy = y2 - y1;
        int passos = Math.max(Math.abs(dx), Math.abs(dy));

        if (passos == 0) {
            plotador.desenhar(x1, y1); 
            return;
        }

        double incrementoX = (double) dx / passos;
        double incrementoY = (double) dy / passos;
        double x = x1;
        double y = y1;

        plotador.desenhar(x1, y1);
        for (int i = 0; i < passos; i++) {
            x += incrementoX;
            y += incrementoY;
            plotador.desenhar((int) Math.round(x), (int) Math.round(y));
        }
    }
}