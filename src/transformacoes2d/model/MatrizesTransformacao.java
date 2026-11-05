
package transformacoes2d.model;

/**
 * Matrizes homogêneas 3x3 das transformações geométricas 2D.
 *
 * As transformações seguem a convenção de vetores coluna:
 * P' = M * P.
 *
 * Cada método retorna uma nova matriz 3x3.
 */
public final class MatrizesTransformacao {

    private MatrizesTransformacao() {
        // Impede a instanciação da classe.
    }

    /**
     * Cria a matriz de translação.
     *
     * @param tx deslocamento no eixo X
     * @param ty deslocamento no eixo Y
     * @return matriz de translação 3x3
     */
    public static double[][] translacao(double tx, double ty) {
        validarFinitos(tx, ty);

        return new double[][] {
            {1, 0, tx},
            {0, 1, ty},
            {0, 0, 1}
        };
    }

    /**
     * Cria a matriz de escala.
     *
     * @param sx fator de escala no eixo X
     * @param sy fator de escala no eixo Y
     * @return matriz de escala 3x3
     */
    public static double[][] escala(double sx, double sy) {
        validarFinitos(sx, sy);

        return new double[][] {
            {sx, 0, 0},
            {0, sy, 0},
            {0, 0, 1}
        };
    }

    /**
     * Cria a matriz de rotação em torno da origem.
     *
     * O ângulo deve ser informado em graus.
     *
     * @param anguloGraus ângulo de rotação em graus
     * @return matriz de rotação 3x3
     */
    public static double[][] rotacao(double anguloGraus) {
        validarFinitos(anguloGraus);

        double angulo = Math.toRadians(anguloGraus);

        double cos = Math.cos(angulo);
        double sen = Math.sin(angulo);

        return new double[][] {
            {cos, -sen, 0},
            {sen, cos, 0},
            {0, 0, 1}
        };
    }

    /**
     * Reflexão em relação ao eixo X.
     */
    public static double[][] reflexaoEixoX() {
        return new double[][] {
            {1, 0, 0},
            {0, -1, 0},
            {0, 0, 1}
        };
    }

    /**
     * Reflexão em relação ao eixo Y.
     */
    public static double[][] reflexaoEixoY() {
        return new double[][] {
            {-1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        };
    }

    /**
     * Reflexão em relação à origem.
     */
    public static double[][] reflexaoOrigem() {
        return new double[][] {
            {-1, 0, 0},
            {0, -1, 0},
            {0, 0, 1}
        };
    }

    /**
     * Reflexão em relação à reta Y = X.
     */
    public static double[][] reflexaoRetaYX() {
        return new double[][] {
            {0, 1, 0},
            {1, 0, 0},
            {0, 0, 1}
        };
    }

    /**
     * Cria a matriz de cisalhamento em X.
     *
     * X novo = X antigo + a * Y antigo.
     *
     * @param a fator de cisalhamento em X
     * @return matriz de cisalhamento em X
     */
    public static double[][] cisalhamentoX(double a) {
        validarFinitos(a);

        return new double[][] {
            {1, a, 0},
            {0, 1, 0},
            {0, 0, 1}
        };
    }

    /**
     * Cria a matriz de cisalhamento em Y.
     *
     * Y novo = Y antigo + b * X antigo.
     *
     * @param b fator de cisalhamento em Y
     * @return matriz de cisalhamento em Y
     */
    public static double[][] cisalhamentoY(double b) {
        validarFinitos(b);

        return new double[][] {
            {1, 0, 0},
            {b, 1, 0},
            {0, 0, 1}
        };
    }

    /**
     * Verifica se os parâmetros são números finitos.
     */
    private static void validarFinitos(double... valores) {
        for (double valor : valores) {
            if (!Double.isFinite(valor)) {
                throw new IllegalArgumentException(
                    "Os parâmetros devem ser números finitos."
                );
            }
        }
    }
}
