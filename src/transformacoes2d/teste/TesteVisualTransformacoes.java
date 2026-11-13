package transformacoes2d.teste;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import javax.swing.*;
import transformacoes.model.Pixel;
import transformacoes.model.Poligono;
import transformacoes.model.Ponto;
import transformacoes.view.Tela;
import transformacoes2d.integracao.AplicadorTransformacoesPoligono;
import transformacoes2d.model.MatrizesTransformacao;

/** Interface de demonstração ligada ao integrador de transformações 2D. */
public class TesteVisualTransformacoes extends JPanel {
    private static final int LARGURA_TELA = 850;
    private static final int ALTURA_TELA = 600;
    private static final int ESCALA_TELA = 2;

    private final Tela tela = new Tela();
    private final JTextField translacaoX = new JTextField("100", 5);
    private final JTextField translacaoY = new JTextField("50", 5);
    private final JTextField escalaX = new JTextField("1.5", 5);
    private final JTextField escalaY = new JTextField("0.7", 5);
    private final JTextField angulo = new JTextField("45", 5);
    private final JTextField cisalhamento = new JTextField("0.7", 5);
    private final JTextField pontoFixoX = new JTextField("0", 5);
    private final JTextField pontoFixoY = new JTextField("0", 5);
    private final JLabel status = new JLabel("Polígono original pronto.");
    private final AplicadorTransformacoesPoligono aplicador;

    public TesteVisualTransformacoes() {
        super(new BorderLayout(8, 8));
        tela.setResolucao(LARGURA_TELA, ALTURA_TELA);
        aplicador = new AplicadorTransformacoesPoligono(tela, ponto ->
                new Pixel(
                        (int) Math.round(LARGURA_TELA / 2.0 + ponto.x * ESCALA_TELA),
                        (int) Math.round(ALTURA_TELA / 2.0 - ponto.y * ESCALA_TELA)));
        aplicador.inicializarPoligono(new Poligono(List.of(
                new Ponto(-80, -50),
                new Ponto(80, -50),
                new Ponto(80, 50),
                new Ponto(-80, 50))));

        add(criarPainelControles(), BorderLayout.NORTH);
        add(tela, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        add(status, BorderLayout.SOUTH);
        aplicador.atualizarDesenhoNaTela();
    }

    /** Mantém um ponto de entrada simples para aplicar uma matriz composta externa. */
    public void aplicarTransformacao(double[][] matriz) {
        executar(() -> aplicador.aplicarMatrizComposta(matriz));
    }

    private JPanel criarPainelControles() {
        JPanel controles = new JPanel(new GridBagLayout());
        controles.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 2));

        JPanel linhaTranslacao = linha("Translação", rotuloCampo("tx", translacaoX),
                rotuloCampo("ty", translacaoY));
        adicionarBotao(linhaTranslacao, "Aplicar", () ->
                aplicador.transladarPoligono(numero(translacaoX), numero(translacaoY)));
        controles.add(linhaTranslacao);

        JPanel linhaEscala = linha("Escala", rotuloCampo("sx", escalaX),
                rotuloCampo("sy", escalaY));
        adicionarBotao(linhaEscala, "Aplicar", () ->
                aplicador.escalarPoligono(numero(escalaX), numero(escalaY)));
        controles.add(linhaEscala);

        JPanel linhaRotacao = linha("Rotação (graus)", angulo);
        adicionarBotao(linhaRotacao, "Aplicar", () ->
                aplicador.rotacionarPoligono(numero(angulo)));
        controles.add(linhaRotacao);

        JPanel linhaReflexoes = linha("Reflexões");
        adicionarBotao(linhaReflexoes, "Eixo X", aplicador::aplicarReflexaoX);
        adicionarBotao(linhaReflexoes, "Eixo Y", aplicador::aplicarReflexaoY);
        adicionarBotao(linhaReflexoes, "Origem", aplicador::aplicarReflexaoOrigem);
        adicionarBotao(linhaReflexoes, "Reta Y = X", aplicador::aplicarReflexaoRetaXY);
        controles.add(linhaReflexoes);

        JPanel linhaCisalhamento = linha("Cisalhamento", cisalhamento);
        adicionarBotao(linhaCisalhamento, "Eixo X", () ->
                aplicador.aplicarCisalhamentoX(numero(cisalhamento)));
        adicionarBotao(linhaCisalhamento, "Eixo Y", () ->
                aplicador.aplicarCisalhamentoY(numero(cisalhamento)));
        controles.add(linhaCisalhamento);

        JPanel linhaPontoFixo = linha("Ponto fixo",
                rotuloCampo("xf", pontoFixoX), rotuloCampo("yf", pontoFixoY));
        adicionarBotao(linhaPontoFixo, "Escalar", () ->
                aplicador.escalarEmPontoFixo(numero(pontoFixoX), numero(pontoFixoY),
                        numero(escalaX), numero(escalaY)));
        adicionarBotao(linhaPontoFixo, "Rotacionar", () ->
                aplicador.rotacionarEmPontoFixo(numero(pontoFixoX), numero(pontoFixoY),
                        numero(angulo)));
        controles.add(linhaPontoFixo);

        JPanel linhaComposicao = linha("Composição", new JLabel("Translação → rotação → escala"));
        adicionarBotao(linhaComposicao, "Aplicar", () -> {
            double[][] matriz = aplicador.comporTransformacoes(
                    MatrizesTransformacao.translacao(
                            numero(translacaoX), numero(translacaoY)),
                    MatrizesTransformacao.rotacao(numero(angulo)),
                    MatrizesTransformacao.escala(numero(escalaX), numero(escalaY)));
            aplicador.aplicarMatrizComposta(matriz);
        });
        adicionarBotao(linhaComposicao, "Restaurar original", () -> {
            aplicador.inicializarPoligono(new Poligono(List.of(
                    new Ponto(-80, -50),
                    new Ponto(80, -50),
                    new Ponto(80, 50),
                    new Ponto(-80, 50))));
            aplicador.atualizarDesenhoNaTela();
        });
        controles.add(linhaComposicao);
        organizarGrupos(controles);
        controles.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evento) {
                organizarGrupos(controles);
            }
        });
        return controles;
    }

    private void organizarGrupos(JPanel controles) {
        int larguraGrupo = 0;
        for (java.awt.Component grupo : controles.getComponents()) {
            larguraGrupo = Math.max(larguraGrupo, grupo.getPreferredSize().width);
        }
        int largura = controles.getWidth() > 0
                ? controles.getWidth() : tela.getPreferredSize().width;
        int colunas = Math.max(1, Math.min(3, (largura - 4) / (larguraGrupo + 8)));
        if (Integer.valueOf(colunas).equals(controles.getClientProperty("colunas"))) {
            return;
        }
        controles.putClientProperty("colunas", colunas);

        GridBagLayout layout = (GridBagLayout) controles.getLayout();
        layout.columnWidths = new int[colunas];
        java.util.Arrays.fill(layout.columnWidths, larguraGrupo + 8);
        GridBagConstraints restricoes = new GridBagConstraints();
        restricoes.fill = GridBagConstraints.BOTH;
        restricoes.weightx = 1;
        restricoes.insets = new Insets(4, 4, 4, 4);
        for (int i = 0; i < controles.getComponentCount(); i++) {
            restricoes.gridx = i % colunas;
            restricoes.gridy = i / colunas;
            restricoes.gridwidth = i == controles.getComponentCount() - 1
                    ? colunas - restricoes.gridx : 1;
            layout.setConstraints(controles.getComponent(i), restricoes);
        }
        controles.revalidate();
        controles.repaint();
    }

    private JPanel linha(String titulo, JComponent... componentes) {
        JPanel linha = new JPanel(new BorderLayout(6, 6));
        linha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(titulo),
                BorderFactory.createEmptyBorder(2, 4, 4, 4)));
        JPanel campos = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        for (JComponent componente : componentes) {
            campos.add(componente);
        }
        if (componentes.length > 0) {
            linha.add(campos, BorderLayout.CENTER);
        }
        JPanel botoes = new JPanel(componentes.length == 0
                ? new GridLayout(0, 2, 6, 6)
                : new FlowLayout(FlowLayout.CENTER, 6, 2));
        linha.add(botoes, BorderLayout.SOUTH);
        return linha;
    }

    private static JPanel rotuloCampo(String rotulo, JTextField campo) {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        painel.add(new JLabel(rotulo));
        painel.add(campo);
        return painel;
    }

    private void adicionarBotao(JPanel painel, String rotulo, Runnable acao) {
        JButton botao = new JButton(rotulo);
        botao.addActionListener(evento -> executar(acao));
        JPanel botoes = (JPanel) ((BorderLayout) painel.getLayout())
                .getLayoutComponent(BorderLayout.SOUTH);
        botoes.add(botao);
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
            status.setText("Transformação aplicada a partir do polígono original.");
        } catch (IllegalArgumentException | IllegalStateException erro) {
            status.setText("Não foi possível aplicar a transformação: " + erro.getMessage());
        }
    }

    private static double numero(JTextField campo) {
        try {
            double valor = Double.parseDouble(campo.getText().trim().replace(',', '.'));
            if (Double.isFinite(valor)) {
                return valor;
            }
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("Informe um número real finito.");
        }
        throw new IllegalArgumentException("Informe um número real finito.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Transformações geométricas 2D");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setContentPane(new TesteVisualTransformacoes());
            janela.pack();
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);
        });
    }
}
