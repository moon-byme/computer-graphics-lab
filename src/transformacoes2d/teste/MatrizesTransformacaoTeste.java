
package transformacoes2d.teste;

import transformacoes2d.model.MatrizesTransformacao;
import transformacoes2d.model.OperacoesMatriciais;
import transformacoes2d.model.PontoHomogeneo;

/**
 * Testes das matrizes das transformacoes geometricas 2D.
 *
 * Verifica os valores das matrizes e os resultados obtidos
 * ao aplica-las a pontos em coordenadas homogeneas.
 */
public class MatrizesTransformacaoTeste {

    private static final double TOLERANCIA = 1e-9;

    private static int testesExecutados = 0;

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println(" TESTES - MATRIZES DE TRANSFORMACAO 2D");
        System.out.println("========================================");

        testarTranslacao();
        testarEscala();
        testarRotacao();
        testarReflexaoEixoX();
        testarReflexaoEixoY();
        testarReflexaoOrigem();
        testarReflexaoRetaYX();
        testarCisalhamentoX();
        testarCisalhamentoY();
        testarDimensoesDasMatrizes();
        testarParametrosInvalidos();

        System.out.println("\n========================================");
        System.out.println(" RESULTADO: " + testesExecutados + " testes aprovados!");
        System.out.println("========================================");
    }

    /**
     * Verifica a translação de um ponto.
     */
    private static void testarTranslacao() {

        double[][] matriz =
                MatrizesTransformacao.translacao(2, 3);

        verificarMatriz(matriz, new double[][] {
            {1, 0, 2},
            {0, 1, 3},
            {0, 0, 1}
        }, "Translação - matriz");

        verificarPonto(matriz, 1, 1, 3, 4,
                "Translação - aplicação");
    }

    /**
     * Verifica a escala nos eixos X e Y.
     */
    private static void testarEscala() {

        double[][] matriz =
                MatrizesTransformacao.escala(2, 3);

        verificarMatriz(matriz, new double[][] {
            {2, 0, 0},
            {0, 3, 0},
            {0, 0, 1}
        }, "Escala - matriz");

        verificarPonto(matriz, 2, 2, 4, 6,
                "Escala - aplicação");
    }

    /**
     * Verifica rotações de 90, 180 e 270 graus.
     */
    private static void testarRotacao() {

        double[][] rotacao90 =
                MatrizesTransformacao.rotacao(90);

        verificarPonto(rotacao90, 1, 0, 0, 1,
                "Rotação de 90 graus");

        double[][] rotacao180 =
                MatrizesTransformacao.rotacao(180);

        verificarPonto(rotacao180, 1, 0, -1, 0,
                "Rotação de 180 graus");

        double[][] rotacao270 =
                MatrizesTransformacao.rotacao(270);

        verificarPonto(rotacao270, 1, 0, 0, -1,
                "Rotação de 270 graus");
    }

    /**
     * Verifica a reflexão em relação ao eixo X.
     */
    private static void testarReflexaoEixoX() {

        double[][] matriz =
                MatrizesTransformacao.reflexaoEixoX();

        verificarMatriz(matriz, new double[][] {
            {1, 0, 0},
            {0, -1, 0},
            {0, 0, 1}
        }, "Reflexão eixo X - matriz");

        verificarPonto(matriz, 2, 3, 2, -3,
                "Reflexão eixo X - aplicação");
    }

    /**
     * Verifica a reflexão em relação ao eixo Y.
     */
    private static void testarReflexaoEixoY() {

        double[][] matriz =
                MatrizesTransformacao.reflexaoEixoY();

        verificarMatriz(matriz, new double[][] {
            {-1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        }, "Reflexão eixo Y - matriz");

        verificarPonto(matriz, 2, 3, -2, 3,
                "Reflexão eixo Y - aplicação");
    }

    /**
     * Verifica a reflexão em relação à origem.
     */
    private static void testarReflexaoOrigem() {

        double[][] matriz =
                MatrizesTransformacao.reflexaoOrigem();

        verificarMatriz(matriz, new double[][] {
            {-1, 0, 0},
            {0, -1, 0},
            {0, 0, 1}
        }, "Reflexão origem - matriz");

        verificarPonto(matriz, 2, 3, -2, -3,
                "Reflexão origem - aplicação");
    }

    /**
     * Verifica a reflexão em relação à reta Y = X.
     */
    private static void testarReflexaoRetaYX() {

        double[][] matriz =
                MatrizesTransformacao.reflexaoRetaYX();

        verificarMatriz(matriz, new double[][] {
            {0, 1, 0},
            {1, 0, 0},
            {0, 0, 1}
        }, "Reflexão Y = X - matriz");

        verificarPonto(matriz, 2, 3, 3, 2,
                "Reflexão Y = X - aplicação");
    }

    /**
     * Verifica o cisalhamento no eixo X.
     */
    private static void testarCisalhamentoX() {

        double[][] matriz =
                MatrizesTransformacao.cisalhamentoX(2);

        verificarMatriz(matriz, new double[][] {
            {1, 2, 0},
            {0, 1, 0},
            {0, 0, 1}
        }, "Cisalhamento X - matriz");

        verificarPonto(matriz, 1, 3, 7, 3,
                "Cisalhamento X - aplicação");
    }

    /**
     * Verifica o cisalhamento no eixo Y.
     */
    private static void testarCisalhamentoY() {

        double[][] matriz =
                MatrizesTransformacao.cisalhamentoY(2);

        verificarMatriz(matriz, new double[][] {
            {1, 0, 0},
            {2, 1, 0},
            {0, 0, 1}
        }, "Cisalhamento Y - matriz");

        verificarPonto(matriz, 1, 3, 1, 5,
                "Cisalhamento Y - aplicação");
    }

    /**
     * Verifica se todas as matrizes possuem dimensao 3x3.
     */
    private static void testarDimensoesDasMatrizes() {

        verificarDimensao(MatrizesTransformacao.translacao(2, 3),
                "Dimensão translação");

        verificarDimensao(MatrizesTransformacao.escala(2, 3),
                "Dimensão escala");

        verificarDimensao(MatrizesTransformacao.rotacao(45),
                "Dimensão rotação");

        verificarDimensao(MatrizesTransformacao.reflexaoEixoX(),
                "Dimensão reflexão X");

        verificarDimensao(MatrizesTransformacao.reflexaoEixoY(),
                "Dimensão reflexão Y");

        verificarDimensao(MatrizesTransformacao.reflexaoOrigem(),
                "Dimensão reflexão origem");

        verificarDimensao(MatrizesTransformacao.reflexaoRetaYX(),
                "Dimensão reflexão Y=X");

        verificarDimensao(MatrizesTransformacao.cisalhamentoX(2),
                "Dimensão cisalhamento X");

        verificarDimensao(MatrizesTransformacao.cisalhamentoY(2),
                "Dimensão cisalhamento Y");
    }

    /**
     * Verifica se parametros invalidos sao rejeitados.
     */
    private static void testarParametrosInvalidos() {

        verificarExcecao(
                () -> MatrizesTransformacao.translacao(Double.NaN, 2),
                "Translação com NaN"
        );

        verificarExcecao(
                () -> MatrizesTransformacao.escala(2, Double.POSITIVE_INFINITY),
                "Escala com infinito"
        );

        verificarExcecao(
                () -> MatrizesTransformacao.rotacao(Double.NaN),
                "Rotação com NaN"
        );

        verificarExcecao(
                () -> MatrizesTransformacao.cisalhamentoX(Double.POSITIVE_INFINITY),
                "Cisalhamento X com infinito"
        );

        verificarExcecao(
                () -> MatrizesTransformacao.cisalhamentoY(Double.NaN),
                "Cisalhamento Y com NaN"
        );
    }

    /**
     * Aplica uma matriz a um ponto e verifica o resultado cartesiano.
     */
    private static void verificarPonto(
            double[][] matriz,
            double x,
            double y,
            double esperadoX,
            double esperadoY,
            String descricao) {

        PontoHomogeneo ponto = new PontoHomogeneo(x, y, 1);

        PontoHomogeneo resultado =
                OperacoesMatriciais.multiplicarMatrizPorPonto(
                        matriz, ponto
                );

        if (resultado.w == 0.0) {
            throw new AssertionError(
                    descricao + ": resultado com W igual a zero."
            );
        }

        double xCalculado = resultado.x / resultado.w;
        double yCalculado = resultado.y / resultado.w;

        verificarIgualdade(xCalculado, esperadoX,
                descricao + " - coordenada X");

        verificarIgualdade(yCalculado, esperadoY,
                descricao + " - coordenada Y");
    }

    /**
     * Compara os valores de duas matrizes 3x3.
     */
    private static void verificarMatriz(
            double[][] obtida,
            double[][] esperada,
            String descricao) {

        verificarDimensao(obtida, descricao);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {

                verificarIgualdade(
                        obtida[i][j],
                        esperada[i][j],
                        descricao + " [" + i + "][" + j + "]"
                );
            }
        }
    }

    /**
     * Verifica se a matriz possui exatamente tres linhas e colunas.
     */
    private static void verificarDimensao(
            double[][] matriz,
            String descricao) {

        if (matriz == null || matriz.length != 3) {
            throw new AssertionError(
                    descricao + ": a matriz deve ter 3 linhas."
            );
        }

        for (double[] linha : matriz) {
            if (linha == null || linha.length != 3) {
                throw new AssertionError(
                        descricao + ": cada linha deve ter 3 colunas."
                );
            }
        }

        testesExecutados++;
        System.out.println("[OK] " + descricao);
    }

    /**
     * Compara dois valores reais utilizando uma tolerancia.
     */
    private static void verificarIgualdade(
            double obtido,
            double esperado,
            String descricao) {

        if (Math.abs(obtido - esperado) > TOLERANCIA) {
            throw new AssertionError(
                    descricao
                    + " | Esperado: " + esperado
                    + " | Obtido: " + obtido
            );
        }

        testesExecutados++;
        System.out.println("[OK] " + descricao);
    }

    /**
     * Verifica se uma operacao lanca IllegalArgumentException.
     */
    private static void verificarExcecao(
            Runnable operacao,
            String descricao) {

        try {
            operacao.run();

            throw new AssertionError(
                    descricao + ": era esperada uma excecao."
            );

        } catch (IllegalArgumentException e) {
            testesExecutados++;
            System.out.println("[OK] " + descricao);
        }
    }
}
