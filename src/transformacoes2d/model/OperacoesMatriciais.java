
package transformacoes2d.model;

/**
 * Operacoes matematicas com matrizes homogeneas 3 x 3.
 *
 * Convencao: pontos sao vetores coluna, e a matriz fica a esquerda
 * do ponto (M * P). Oferece produto matriz por ponto, produto entre duas matrizes e produto de N matrizes.
 */
public final class OperacoesMatriciais {
    private static final int DIMENSAO = 3;

    private OperacoesMatriciais() { }

    /**
     * Multiplica uma matriz 3 x 3 pelo vetor coluna [x, y, w]^T.
     * O resultado permanece em coordenadas homogeneas, sem normalizacao.
     */
    public static PontoHomogeneo multiplicarMatrizPorPonto(
            double[][] matriz, PontoHomogeneo ponto) {
        validarMatriz(matriz);
        if (ponto == null) {
            throw new IllegalArgumentException("O ponto homogeneo nao pode ser nulo.");
        }

        double[] p = {ponto.x, ponto.y, ponto.w};
        double[] resultado = new double[DIMENSAO];

        for (int linha = 0; linha < DIMENSAO; linha++) {
            for (int coluna = 0; coluna < DIMENSAO; coluna++) {
                resultado[linha] += matriz[linha][coluna] * p[coluna];
            }
            exigirResultadoFinito(resultado[linha]);
        }

        return new PontoHomogeneo(resultado[0], resultado[1], resultado[2]);
    }

    /**
     * Multiplica duas matrizes 3 x 3: esquerda * direita.
     * Retorna uma nova matriz sem alterar as matrizes de entrada.
     * Com vetores coluna, (A * B) * P aplica B antes de A.
     */
    public static double[][] multiplicarMatrizes(double[][] esquerda, double[][] direita) {
        validarMatriz(esquerda);
        validarMatriz(direita);

        double[][] resultado = new double[DIMENSAO][DIMENSAO];

        for (int linha = 0; linha < DIMENSAO; linha++) {
            for (int coluna = 0; coluna < DIMENSAO; coluna++) {
                double soma = 0.0;
                for (int k = 0; k < DIMENSAO; k++) {
                    soma += esquerda[linha][k] * direita[k][coluna];
                }
                exigirResultadoFinito(soma);
                resultado[linha][coluna] = soma;
            }
        }

        return resultado;
    }

    /**
     * Calcula o produto de N matrizes 3 x 3, preservando a ordem recebida.
     * Exemplo: multiplicar(A, B, C) retorna A * B * C.
     * Com vetores coluna, C atua primeiro sobre o ponto, depois B e A.
     *
     * Exige pelo menos uma matriz. Se receber apenas uma, retorna uma copia.
     */
    public static double[][] multiplicar(double[][]... matrizes) {
        if (matrizes == null || matrizes.length == 0) {
            throw new IllegalArgumentException("Informe pelo menos uma matriz 3 x 3.");
        }

        validarMatriz(matrizes[0]);
        double[][] acumulado = new double[DIMENSAO][DIMENSAO];
        for (int linha = 0; linha < DIMENSAO; linha++) {
            System.arraycopy(matrizes[0][linha], 0, acumulado[linha], 0, DIMENSAO);
        }

        for (int indice = 1; indice < matrizes.length; indice++) {
            acumulado = multiplicarMatrizes(acumulado, matrizes[indice]);
        }
        return acumulado;
    }

    /** Verifica se a matriz e 3 x 3 e contem apenas numeros finitos. */
    private static void validarMatriz(double[][] matriz) {
        if (matriz == null || matriz.length != DIMENSAO) {
            throw new IllegalArgumentException("A matriz deve possuir exatamente 3 linhas.");
        }
        for (double[] linha : matriz) {
            if (linha == null || linha.length != DIMENSAO) {
                throw new IllegalArgumentException("Cada linha da matriz deve ter 3 colunas.");
            }
            for (double valor : linha) {
                if (!Double.isFinite(valor)) {
                    throw new IllegalArgumentException("A matriz deve conter apenas valores finitos.");
                }
            }
        }
    }

    /** Interrompe a operacao se o resultado exceder o intervalo de double. */
    private static void exigirResultadoFinito(double valor) {
        if (!Double.isFinite(valor)) {
            throw new ArithmeticException("O resultado excedeu o intervalo numerico de double.");
        }
    }
}
