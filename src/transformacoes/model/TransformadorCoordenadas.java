package transformacoes.model;

/** Transformacoes Mundo -> NDC -> Dispositivo (e inversas). */
public final class TransformadorCoordenadas {
    private TransformadorCoordenadas() { }

    /* Janela de mundo -> coordenadas normalizadas. */
    public static Ponto userToNdc(double x, double y, double xmin, double xmax,
                                  double ymin, double ymax, boolean centrado) {
        double nx = (x - xmin) / (xmax - xmin);
        double ny = (y - ymin) / (ymax - ymin);
        if (centrado) {
            nx = 2 * nx - 1;
            ny = 2 * ny - 1;
        }
        return new Ponto(nx, ny);
    }

    /* Coordenadas normalizadas -> janela de mundo (transformacao inversa). */
    public static Ponto ndcToUser(double nx, double ny, double xmin, double xmax,
                                  double ymin, double ymax, boolean centrado) {
        if (centrado) {
            nx = (nx + 1) / 2;
            ny = (ny + 1) / 2;
        }
        return new Ponto(xmin + nx * (xmax - xmin),
                         ymin + ny * (ymax - ymin));
    }

    /* NDC -> dispositivo. No Swing, a origem da tela fica no canto superior esquerdo. */
    public static Pixel ndcToDc(double nx, double ny, int largura, int altura,
                                boolean centrado) {
        if (centrado) {
            nx = (nx + 1) / 2;
            ny = (ny + 1) / 2;
        }
        int dx = (int) Math.round(nx * (largura - 1));
        int dy = (int) Math.round((1 - ny) * (altura - 1));
        return new Pixel(dx, dy);
    }

    /* Dispositivo de entrada (mouse) -> NDC. */
    public static Ponto inpToNdc(int dx, int dy, int largura, int altura,
                                 boolean centrado) {
        double nx = (double) dx / (largura - 1);
        double ny = 1 - (double) dy / (altura - 1);
        if (centrado) {
            nx = 2 * nx - 1;
            ny = 2 * ny - 1;
        }
        return new Ponto(nx, ny);
    }
}
