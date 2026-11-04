
package transformacoes2d.teste;

import transformacoes.model.Ponto;
import transformacoes2d.model.CoordenadasHomogeneas;
import transformacoes2d.model.OperacoesMatriciais;
import transformacoes2d.model.PontoHomogeneo;

/** Testes automaticos sem GUI e sem bibliotecas externas (java -cp out ...). */
public final class OperacoesMatriciaisTeste {
    private static final double TOLERANCIA = 1e-10;
    private static int verificacoes;

    private OperacoesMatriciaisTeste() { }

    public static void main(String[] args) {
        testarPontosEConversoes();
        testarMatrizPorPonto();
        testarMatrizesDuasEN();
        testarEntradasInvalidas();
        System.out.println("OK: " + verificacoes
                + " verificacoes de coordenadas homogeneas e operacoes matriciais.");
    }

    private static void testarPontosEConversoes() {
        PontoHomogeneo homog = CoordenadasHomogeneas.paraHomogeneas(2, 3);
        conferirPonto(homog, 2, 3, 1, "conversao cartesiano -> homogeneo");
        Ponto origem = new Ponto(0, 0);
        conferirPonto(CoordenadasHomogeneas.paraHomogeneas(origem), 0, 0, 1,
                "conversao de Ponto existente");
        conferirCartesiano(CoordenadasHomogeneas.paraCartesianas(homog), 2, 3,
                "conversao homogeneo -> cartesiano");
        conferirCartesiano(CoordenadasHomogeneas.paraCartesianas(
                new PontoHomogeneo(4, 6, 2)), 2, 3, "triplas proporcionais");
        conferirCartesiano(CoordenadasHomogeneas.paraCartesianas(
                new PontoHomogeneo(-4, -6, -2)), 2, 3, "w negativo");
        conferirPonto(CoordenadasHomogeneas.normalizar(new PontoHomogeneo(8, 12, 4)),
                2, 3, 1, "normalizacao w = 1");
        conferirPonto(CoordenadasHomogeneas.normalizar(new PontoHomogeneo(1, 3, 2)),
                0.5, 1.5, 1, "normalizacao com fracoes");
        conferirPonto(homog, 2, 3, 1, "ponto original permanece imutavel");
        conferirPonto(CoordenadasHomogeneas.paraHomogeneas(-4.5, 1.25),
                -4.5, 1.25, 1, "coordenadas reais e negativas");
    }

    private static void testarMatrizPorPonto() {
        double[][] identidade = identidade();
        conferirPonto(OperacoesMatriciais.multiplicarMatrizPorPonto(
                identidade, new PontoHomogeneo(2, -3, 1)),
                2, -3, 1, "matriz identidade aplicada ao ponto");

        double[][] matriz = {
            {2, 1, 4},
            {-1, 3, -2},
            {1, 0, 2}
        };
        PontoHomogeneo resultado = OperacoesMatriciais.multiplicarMatrizPorPonto(
                matriz, new PontoHomogeneo(2, 3, 1));
        conferirPonto(resultado, 11, 5, 4, "produto por vetor coluna, w sem normalizar");
        conferirCartesiano(CoordenadasHomogeneas.paraCartesianas(resultado),
                2.75, 1.25, "conversao depois do produto");
    }

    private static void testarMatrizesDuasEN() {
        double[][] a = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        double[][] b = {{9, 8, 7}, {6, 5, 4}, {3, 2, 1}};
        conferirMatriz(OperacoesMatriciais.multiplicarMatrizes(a, b),
                new double[][] {{30, 24, 18}, {84, 69, 54}, {138, 114, 90}},
                "produto de duas matrizes (linha x coluna)");

        double[][] escalaTeste = {{2, 0, 0}, {0, 2, 0}, {0, 0, 1}};
        double[][] deslocamentoXTeste = {{1, 0, 5}, {0, 1, 0}, {0, 0, 1}};
        double[][] deslocamentoYTeste = {{1, 0, 0}, {0, 1, -3}, {0, 0, 1}};

        conferirMatriz(OperacoesMatriciais.multiplicar(escalaTeste, deslocamentoXTeste),
                new double[][] {{2, 0, 10}, {0, 2, 0}, {0, 0, 1}},
                "produto de duas via metodo N");
        conferirMatriz(OperacoesMatriciais.multiplicar(deslocamentoXTeste, escalaTeste),
                new double[][] {{2, 0, 5}, {0, 2, 0}, {0, 0, 1}},
                "ordem diferente, produto diferente");
        double[][] composto = OperacoesMatriciais.multiplicar(
                escalaTeste, deslocamentoXTeste, deslocamentoYTeste);
        conferirMatriz(composto,
                new double[][] {{2, 0, 10}, {0, 2, -6}, {0, 0, 1}},
                "produto de tres matrizes na ordem recebida");
        conferirPonto(OperacoesMatriciais.multiplicarMatrizPorPonto(
                composto, CoordenadasHomogeneas.paraHomogeneas(1, 1)),
                12, -4, 1, "composicao aplicada ao ponto");
        conferirMatriz(OperacoesMatriciais.multiplicar(
                OperacoesMatriciais.multiplicar(escalaTeste, deslocamentoXTeste),
                deslocamentoYTeste),
                composto, "associatividade do produto");
        conferirMatriz(OperacoesMatriciais.multiplicar(
                escalaTeste, OperacoesMatriciais.multiplicar(
                        deslocamentoXTeste, deslocamentoYTeste)),
                composto, "associatividade com agrupamento a direita");
        conferirMatriz(OperacoesMatriciais.multiplicar(identidade(), escalaTeste,
                deslocamentoXTeste, deslocamentoYTeste),
                composto, "produto de quatro matrizes");

        double[][] copia = OperacoesMatriciais.multiplicar(a);
        conferirMatriz(copia, a, "N = 1 devolve valores identicos");
        conferir(copia != a && copia[0] != a[0], "N = 1 deve retornar copia independente");
        copia[0][0] = -100;
        conferirNumero(a[0][0], 1, "matriz original nao pode ser alterada");
        conferirNumero(escalaTeste[0][0], 2, "composicao nao altera as entradas");
        conferirNumero(deslocamentoXTeste[0][2], 5, "composicao nao altera as entradas (2)");
    }

    private static void testarEntradasInvalidas() {
        exigirExcecao(IllegalArgumentException.class,
                () -> new PontoHomogeneo(Double.NaN, 2, 1), "ponto com NaN");
        exigirExcecao(IllegalArgumentException.class,
                () -> new PontoHomogeneo(2, Double.POSITIVE_INFINITY, 1), "ponto infinito");
        exigirExcecao(IllegalArgumentException.class,
                () -> new PontoHomogeneo(0, 0, 0), "tripla homogenea nula");
        exigirExcecao(IllegalArgumentException.class,
                () -> CoordenadasHomogeneas.paraCartesianas(new PontoHomogeneo(3, 2, 0)),
                "w = 0 nao tem correspondente cartesiano finito");
        exigirExcecao(IllegalArgumentException.class,
                () -> CoordenadasHomogeneas.normalizar(new PontoHomogeneo(1, 0, -0.0)),
                "normalizacao com w = -0");
        exigirExcecao(IllegalArgumentException.class,
                () -> CoordenadasHomogeneas.paraCartesianas(null), "ponto nulo");
        exigirExcecao(IllegalArgumentException.class,
                () -> CoordenadasHomogeneas.paraHomogeneas((Ponto) null), "cartesiano nulo");
        exigirExcecao(IllegalArgumentException.class,
                () -> CoordenadasHomogeneas.paraHomogeneas(new Ponto(Double.NaN, 0)),
                "cartesiano nao finito");
        exigirExcecao(IllegalArgumentException.class,
                () -> OperacoesMatriciais.multiplicar(), "lista de matrizes vazia");
        exigirExcecao(IllegalArgumentException.class,
                () -> OperacoesMatriciais.multiplicar((double[][][]) null),
                "lista de matrizes nula");
        exigirExcecao(IllegalArgumentException.class,
                () -> OperacoesMatriciais.multiplicar(new double[][] {{1, 2}, {3, 4}}),
                "matriz 2 x 2");
        exigirExcecao(IllegalArgumentException.class,
                () -> OperacoesMatriciais.multiplicar(new double[][] {{1, 2, 3}, {4, 5}, {6, 7, 8}}),
                "matriz com linha incompleta");
        exigirExcecao(IllegalArgumentException.class,
                () -> OperacoesMatriciais.multiplicar(new double[][] {{1, 2, 3}, null, {7, 8, 9}}),
                "matriz com linha nula");
        exigirExcecao(IllegalArgumentException.class,
                () -> OperacoesMatriciais.multiplicar(new double[][] {{1, 2, 3},
                        {4, Double.NaN, 6}, {7, 8, 9}}), "matriz com NaN");
        exigirExcecao(IllegalArgumentException.class,
                () -> OperacoesMatriciais.multiplicar(identidade(), null),
                "matriz nula no conjunto");
        exigirExcecao(IllegalArgumentException.class,
                () -> OperacoesMatriciais.multiplicarMatrizes(identidade(), new double[4][3]),
                "produto com matriz fora de dimensao");
        exigirExcecao(IllegalArgumentException.class,
                () -> OperacoesMatriciais.multiplicarMatrizPorPonto(new double[3][2],
                        new PontoHomogeneo(1, 1, 1)), "matriz por ponto com dimensao errada");
        exigirExcecao(IllegalArgumentException.class,
                () -> OperacoesMatriciais.multiplicarMatrizPorPonto(identidade(), null),
                "multiplicacao com ponto nulo");
        exigirExcecao(ArithmeticException.class,
                () -> OperacoesMatriciais.multiplicarMatrizes(
                        new double[][] {{Double.MAX_VALUE, 0, 0}, {0, 1, 0}, {0, 0, 1}},
                        new double[][] {{2, 0, 0}, {0, 1, 0}, {0, 0, 1}}),
                "transbordamento numerico");
    }

    private static double[][] identidade() {
        return new double[][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    }

    private static void conferirPonto(PontoHomogeneo ponto, double x, double y, double w,
                                      String mensagem) {
        conferirNumero(ponto.x, x, mensagem + ": x");
        conferirNumero(ponto.y, y, mensagem + ": y");
        conferirNumero(ponto.w, w, mensagem + ": w");
    }

    private static void conferirCartesiano(Ponto ponto, double x, double y, String mensagem) {
        conferirNumero(ponto.x, x, mensagem + ": x");
        conferirNumero(ponto.y, y, mensagem + ": y");
    }

    private static void conferirMatriz(double[][] atual, double[][] esperado, String mensagem) {
        conferir(atual.length == 3, mensagem + ": numero de linhas");
        for (int i = 0; i < 3; i++) {
            conferir(atual[i].length == 3, mensagem + ": colunas da linha " + i);
            for (int j = 0; j < 3; j++) {
                conferirNumero(atual[i][j], esperado[i][j], mensagem + " [" + i + "][" + j + "]");
            }
        }
    }

    private static void conferirNumero(double atual, double esperado, String mensagem) {
        conferir(Double.isFinite(atual) && Math.abs(atual - esperado) <= TOLERANCIA,
                mensagem + ": esperado " + esperado + ", obtido " + atual);
    }

    private static void conferir(boolean condicao, String mensagem) {
        verificacoes++;
        if (!condicao) throw new AssertionError(mensagem);
    }

    private static void exigirExcecao(Class<? extends Throwable> tipo, Runnable operacao,
                                      String mensagem) {
        boolean lancou = false;
        try {
            operacao.run();
        } catch (Throwable erro) {
            if (!tipo.isInstance(erro)) {
                throw new AssertionError(mensagem + ": excecao incorreta: " + erro, erro);
            }
            lancou = true;
        }
        conferir(lancou, mensagem + ": deveria rejeitar entrada invalida");
    }
}
