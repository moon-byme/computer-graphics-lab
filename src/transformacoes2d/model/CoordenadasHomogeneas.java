
package transformacoes2d.model;

import transformacoes.model.Ponto;

/** Conversoes matematicas entre pontos cartesianos 2D e homogeneos. */
public final class CoordenadasHomogeneas {
    private CoordenadasHomogeneas() { }

    /** Adiciona w = 1 ao ponto cartesiano (x, y). */
    public static PontoHomogeneo paraHomogeneas(double x, double y) {
        return new PontoHomogeneo(x, y, 1.0);
    }

    /** Converte um Ponto do modulo ja existente, sem modifica-lo. */
    public static PontoHomogeneo paraHomogeneas(Ponto ponto) {
        if (ponto == null) {
            throw new IllegalArgumentException("O ponto cartesiano nao pode ser nulo.");
        }
        return paraHomogeneas(ponto.x, ponto.y);
    }

    /**
     * Normaliza (x, y, w) para (x / w, y / w, 1).
     * A conversao nao e definida para w = 0.
     */
    public static PontoHomogeneo normalizar(PontoHomogeneo ponto) {
        if (ponto == null) {
            throw new IllegalArgumentException("O ponto homogeneo nao pode ser nulo.");
        }
        if (ponto.w == 0.0) {
            throw new IllegalArgumentException("Nao e possivel normalizar um ponto com w = 0.");
        }
        return new PontoHomogeneo(ponto.x / ponto.w, ponto.y / ponto.w, 1.0);
    }

    /** Divide x e y por w e retorna o Ponto cartesiano usado no projeto. */
    public static Ponto paraCartesianas(PontoHomogeneo ponto) {
        PontoHomogeneo normalizado = normalizar(ponto);
        return new Ponto(normalizado.x, normalizado.y);
    }
}
