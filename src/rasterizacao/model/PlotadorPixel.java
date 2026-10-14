package rasterizacao.model;

//Esse interface recebe coordenadas do dispositivo, sem aquela conversao Mundo/NDC

@FunctionalInterface
public interface PlotadorPixel {
    void desenhar(int x, int y);
}
