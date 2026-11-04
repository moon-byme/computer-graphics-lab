
package transformacoes2d.model;

/**
 * Representa um ponto 2D em coordenadas homogeneas (x, y, w).
 *
 * <p>Para w diferente de zero, o ponto cartesiano correspondente e
 * (x / w, y / w). Triplas proporcionais nao nulas representam o mesmo
 * ponto. Um ponto com w = 0 pode ser representado, mas nao convertido
 * em um ponto cartesiano finito.</p>
 *
 * <p>Assim como {@code transformacoes.model.Ponto}, esta classe e imutavel.</p>
 */
public final class PontoHomogeneo {
    public final double x;
    public final double y;
    public final double w;

    public PontoHomogeneo(double x, double y, double w) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(w)) {
            throw new IllegalArgumentException("As coordenadas devem ser numeros finitos.");
        }
        if (x == 0.0 && y == 0.0 && w == 0.0) {
            throw new IllegalArgumentException("(0, 0, 0) nao representa um ponto homogeneo.");
        }
        this.x = x;
        this.y = y;
        this.w = w;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + w + ")";
    }
}
