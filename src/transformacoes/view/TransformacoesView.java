package transformacoes.view;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import transformacoes.model.FormaRasterizacao;
import transformacoes.model.TipoAlgoritmo;
import transformacoes.model.Pixel;

/** apenas componentes visuais, sem regras de negocio */
public class TransformacoesView extends JFrame {
    private static final Color FUNDO_JANELA = new Color(0xEEF0F3);
    private static final Color FUNDO_MESA = new Color(0xD9DDE3);
    private static final Color FUNDO_PAINEL = Color.WHITE;
    private static final Color FUNDO_LINHA_ALTERNADA = new Color(0xF5F6F8);
    private static final Color BORDA = new Color(0xC3C9D1);
    private static final Color BORDA_DISPLAY = new Color(0x8C95A1);
    private static final Color TEXTO_SECUNDARIO = new Color(0x5B6675);
    private static final Color COR_OK = new Color(0x1E6B34);
    private static final Color COR_ERRO = new Color(0xB3261E);
    private static final String VAZIO = "\u2014";

    /* Resolucoes pre-definidas do display: {largura, altura} e nome usual. */
    private static final int[][] RESOLUCOES = {
            {320, 240}, {640, 480}, {800, 600}, {1024, 768},
            {1280, 720}, {1366, 768}, {1920, 1080}
    };
    private static final String[] NOMES_RESOLUCOES = {
            "QVGA", "VGA", "SVGA", "XGA", "HD", "WXGA", "Full HD"
    };
    private static final int RESOLUCAO_INICIAL = 1;

    static {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
    }

    /* Janela do mundo, ponto e cenario NDC */
    private final JTextField xminCampo = new JTextField("-10", 5);
    private final JTextField xmaxCampo = new JTextField("10", 5);
    private final JTextField yminCampo = new JTextField("-10", 5);
    private final JTextField ymaxCampo = new JTextField("10", 5);
    private final JTextField xCampo = new JTextField("10", 5);
    private final JTextField yCampo = new JTextField("15", 5);
    private final JLabel xRotulo = new JLabel("x");
    private final JLabel yRotulo = new JLabel("y");
    private final JComboBox<String> cenario = new JComboBox<>(new String[] {
            "[0,1] x [0,1]", "[-1,1] x [-1,1]"
    });
    private final JButton mostrar = new JButton("Desenhar");
    private final JButton limparTela = new JButton("Limpar tela");
    private final JComboBox<FormaRasterizacao.Tipo> tipoForma =
            new JComboBox<>(FormaRasterizacao.Tipo.values());
    private final JLabel x2Rotulo = new JLabel("x final");
    private final JTextField x2Campo = new JTextField("20", 5);
    private final JLabel y2Rotulo = new JLabel("y final");
    private final JTextField y2Campo = new JTextField("20", 5);
    private final JLabel raioRotulo = new JLabel("Raio (px DC)");
    private final JTextField raioCampo = new JTextField("50", 5);
    private final JLabel verticesRotulo = new JLabel("Vértices (x,y; ...)");
    private final JTextField verticesCampo = new JTextField("-4,-3; 4,-3; 4,3; -4,3", 24);

    /* Transformações do polígono. */
    private final JTextField translacaoXCampo = new JTextField("2", 4);
    private final JTextField translacaoYCampo = new JTextField("2", 4);
    private final JTextField escalaXCampo = new JTextField("1.5", 4);
    private final JTextField escalaYCampo = new JTextField("1.5", 4);
    private final JTextField rotacaoCampo = new JTextField("45", 4);
    private final JTextField cisalhamentoCampo = new JTextField("0.5", 4);
    private final JTextField pontoFixoXCampo = new JTextField("0", 4);
    private final JTextField pontoFixoYCampo = new JTextField("0", 4);
    private final JButton transladarPoligono = new JButton("Transladar");
    private final JButton escalarPoligono = new JButton("Escalar");
    private final JButton rotacionarPoligono = new JButton("Rotacionar");
    private final JButton reflexaoX = new JButton("Eixo X");
    private final JButton reflexaoY = new JButton("Eixo Y");
    private final JButton reflexaoOrigem = new JButton("Origem");
    private final JButton reflexaoRetaXY = new JButton("Reta Y = X");
    private final JButton cisalhamentoX = new JButton("Cisalhar X");
    private final JButton cisalhamentoY = new JButton("Cisalhar Y");
    private final JButton escalarPontoFixo = new JButton("Escalar no ponto");
    private final JButton rotacionarPontoFixo = new JButton("Girar no ponto");
    private final JButton comporTransformacoes = new JButton("Compor");
    private final JButton restaurarPoligono = new JButton("Restaurar original");

    /* Display e escolha da resolucao. */
    private final JPanel painelTransformacoes = new JPanel(new BorderLayout(4, 4));
    private final Tela tela = new Tela();
    private final JPanel mesa = new JPanel(new GridBagLayout());
    private final JRadioButton[] opcoesResolucao = new JRadioButton[RESOLUCOES.length];
    private final JRadioButton opcaoPersonalizada = new JRadioButton("Personalizada");
    private final JLabel larguraRotulo = new JLabel("Largura");
    private final JLabel alturaRotulo = new JLabel("Altura");
    private final JTextField larguraCampo = new JTextField("1000", 5);
    private final JTextField alturaCampo = new JTextField("700", 5);
    private final JButton aplicarResolucao = new JButton("Aplicar");
    private final JComboBox<String> ampliacao = new JComboBox<>(new String[] {"1×", "2×", "4×", "8×"});
    private final JCheckBox grade = new JCheckBox("Grade");
    private final JComboBox<TipoAlgoritmo> algoritmos = new JComboBox<>(TipoAlgoritmo.values());

    /* Resultados. */
    private final DefaultTableModel resultados = criarModelo();
    private final DefaultTableModel inversa = criarModelo();
    private final JLabel displayRotulo = new JLabel();
    private final JLabel status = new JLabel(" ");

    public TransformacoesView() {
        super("Laboratório I - Transformações de coordenadas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(FUNDO_JANELA);
        setLayout(new BorderLayout());

        add(criarControles(), BorderLayout.NORTH);
        add(criarAreaDisplay(), BorderLayout.CENTER);
        add(criarPainelLateral(), BorderLayout.EAST);
        add(criarBarraStatus(), BorderLayout.SOUTH);

        tipoForma.addActionListener(e -> atualizarCamposForma(getTipoFormaSelecionado()));
        setAlgoritmosParaForma(FormaRasterizacao.Tipo.PONTO);
        opcoesResolucao[RESOLUCAO_INICIAL].setSelected(true);
        atualizarCamposForma(FormaRasterizacao.Tipo.PONTO);
        setResolucaoDisplay(RESOLUCOES[RESOLUCAO_INICIAL][0], RESOLUCOES[RESOLUCAO_INICIAL][1]);
        setCamposPersonalizadosAtivos(false);
        limparResultados();
        limparInversa();

        Rectangle limite = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        pack();
        setMinimumSize(new Dimension(
                Math.min(900, limite.width), Math.min(560, limite.height)));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    /* ----- Montagem da janela ----- */
    private JPanel criarControles() {
        JPanel primeiraLinha = linhaDeCampos();
        primeiraLinha.add(new JLabel("Janela: xmin"));
        primeiraLinha.add(xminCampo);
        primeiraLinha.add(new JLabel("xmax"));
        primeiraLinha.add(xmaxCampo);
        primeiraLinha.add(new JLabel("ymin"));
        primeiraLinha.add(yminCampo);
        primeiraLinha.add(new JLabel("ymax"));
        primeiraLinha.add(ymaxCampo);

        JPanel segundaLinha = linhaDeCampos();
        segundaLinha.add(xRotulo);
        segundaLinha.add(xCampo);
        segundaLinha.add(yRotulo);
        segundaLinha.add(yCampo);
        segundaLinha.add(new JLabel("NDC"));
        segundaLinha.add(cenario);
        segundaLinha.add(mostrar);
        segundaLinha.add(limparTela);

        JPanel terceiraLinha = linhaDeCampos();
        terceiraLinha.add(new JLabel("Primitiva"));
        terceiraLinha.add(tipoForma);
        terceiraLinha.add(x2Rotulo);
        terceiraLinha.add(x2Campo);
        terceiraLinha.add(y2Rotulo);
        terceiraLinha.add(y2Campo);
        terceiraLinha.add(raioRotulo);
        terceiraLinha.add(raioCampo);
        terceiraLinha.add(verticesRotulo);
        terceiraLinha.add(verticesCampo);

        JPanel camposDesenho = new JPanel();
        camposDesenho.setLayout(new BoxLayout(camposDesenho, BoxLayout.Y_AXIS));
        camposDesenho.setOpaque(false);
        camposDesenho.add(segundaLinha);
        camposDesenho.add(terceiraLinha);
        painelTransformacoes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Transformações do polígono"),
                BorderFactory.createEmptyBorder(0, 4, 2, 4)));
        painelTransformacoes.add(criarConteudoTransformacoes(), BorderLayout.CENTER);
        camposDesenho.add(painelTransformacoes);

        JPanel controles = new JPanel();
        controles.setLayout(new BoxLayout(controles, BoxLayout.Y_AXIS));
        controles.setBackground(FUNDO_JANELA);
        controles.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDA),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        controles.add(primeiraLinha);
        controles.add(camposDesenho);
        return controles;
    }

    private void atualizarCamposForma(FormaRasterizacao.Tipo tipo) {
        boolean reta = tipo == FormaRasterizacao.Tipo.RETA;
        boolean circunferencia = tipo == FormaRasterizacao.Tipo.CIRCUNFERENCIA;
        boolean poligono = tipo == FormaRasterizacao.Tipo.POLIGONO;
        painelTransformacoes.setVisible(poligono);
        xRotulo.setText(reta ? "Início x" : circunferencia ? "Centro x" : "Ponto x");
        yRotulo.setText(reta ? "Início y" : circunferencia ? "Centro y" : "Ponto y");
        xRotulo.setVisible(!poligono);
        xCampo.setVisible(!poligono);
        yRotulo.setVisible(!poligono);
        yCampo.setVisible(!poligono);
        x2Rotulo.setVisible(reta);
        x2Campo.setVisible(reta);
        y2Rotulo.setVisible(reta);
        y2Campo.setVisible(reta);
        raioRotulo.setVisible(circunferencia);
        raioCampo.setVisible(circunferencia);
        verticesRotulo.setVisible(poligono);
        verticesCampo.setVisible(poligono);
        revalidate();
        repaint();
    }

    private static JPanel linhaDeCampos() {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        linha.setOpaque(false);
        return linha;
    }

    /* dispay centralizado */
    private JScrollPane criarAreaDisplay() {
        JPanel moldura = new JPanel(new BorderLayout());
        moldura.setBorder(BorderFactory.createLineBorder(BORDA_DISPLAY));
        moldura.add(tela, BorderLayout.CENTER);

        mesa.setBackground(FUNDO_MESA);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(16, 16, 16, 16);
        mesa.add(moldura, c);

        JScrollPane rolagem = new JScrollPane(mesa);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.getViewport().setBackground(FUNDO_MESA);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        rolagem.getHorizontalScrollBar().setUnitIncrement(16);
        return rolagem;
    }

    private JScrollPane criarPainelLateral() {
        JPanel lateral = new JPanel(new GridBagLayout());
        lateral.setBackground(FUNDO_JANELA);
        lateral.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(0, 0, 10, 0);
        lateral.add(painel("Algoritmo de rasterização", criarConteudoAlgoritmos()), c);
        lateral.add(painel("Resolução do display", criarConteudoResolucao()), c);
        lateral.add(painel("Resultados", criarConteudoResultados()), c);
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 0);
        lateral.add(Box.createGlue(), c);

        JScrollPane rolagem = new JScrollPane(lateral,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rolagem.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDA));
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        int largura = lateral.getPreferredSize().width
                + rolagem.getVerticalScrollBar().getPreferredSize().width + 1;
        rolagem.setPreferredSize(new Dimension(largura, 0));
        return rolagem;
    }

    private JPanel criarConteudoTransformacoes() {
        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setOpaque(false);

        JPanel linhaGeometria = linhaTransformacoes();
        linhaGeometria.add(new JLabel("Translação"));
        linhaGeometria.add(new JLabel("tx"));
        linhaGeometria.add(translacaoXCampo);
        linhaGeometria.add(new JLabel("ty"));
        linhaGeometria.add(translacaoYCampo);
        linhaGeometria.add(transladarPoligono);
        linhaGeometria.add(new JLabel("Escala"));
        linhaGeometria.add(new JLabel("sx"));
        linhaGeometria.add(escalaXCampo);
        linhaGeometria.add(new JLabel("sy"));
        linhaGeometria.add(escalaYCampo);
        linhaGeometria.add(escalarPoligono);
        linhaGeometria.add(new JLabel("Ângulo (graus)"));
        linhaGeometria.add(rotacaoCampo);
        linhaGeometria.add(rotacionarPoligono);
        conteudo.add(linhaGeometria);

        JPanel linhaReflexoes = linhaTransformacoes();
        linhaReflexoes.add(new JLabel("Reflexão"));
        linhaReflexoes.add(reflexaoX);
        linhaReflexoes.add(reflexaoY);
        linhaReflexoes.add(reflexaoOrigem);
        linhaReflexoes.add(reflexaoRetaXY);
        linhaReflexoes.add(new JLabel("Cisalhamento"));
        linhaReflexoes.add(new JLabel("Fator"));
        linhaReflexoes.add(cisalhamentoCampo);
        linhaReflexoes.add(cisalhamentoX);
        linhaReflexoes.add(cisalhamentoY);
        conteudo.add(linhaReflexoes);

        JPanel linhaComposicao = linhaTransformacoes();
        linhaComposicao.add(new JLabel("Ponto fixo"));
        linhaComposicao.add(new JLabel("xf"));
        linhaComposicao.add(pontoFixoXCampo);
        linhaComposicao.add(new JLabel("yf"));
        linhaComposicao.add(pontoFixoYCampo);
        linhaComposicao.add(escalarPontoFixo);
        linhaComposicao.add(rotacionarPontoFixo);
        linhaComposicao.add(comporTransformacoes);
        linhaComposicao.add(restaurarPoligono);
        conteudo.add(linhaComposicao);
        return conteudo;
    }

    private static JPanel linhaTransformacoes() {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        linha.setOpaque(false);
        return linha;
    }

    private JPanel criarConteudoResolucao() {
        JPanel conteudo = new JPanel(new GridBagLayout());
        conteudo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        JPanel prontas = new JPanel(new GridLayout(0, 2, 8, 0));
        prontas.setOpaque(false);
        ButtonGroup grupo = new ButtonGroup();
        for (int i = 0; i < RESOLUCOES.length; i++) {
            JRadioButton opcao = new JRadioButton(String.format("%d × %d (%s)",
                    RESOLUCOES[i][0], RESOLUCOES[i][1], NOMES_RESOLUCOES[i]));
            opcao.setOpaque(false);
            grupo.add(opcao);
            opcoesResolucao[i] = opcao;
            prontas.add(opcao);
        }
        conteudo.add(prontas, c);
        opcaoPersonalizada.setOpaque(false);
        grupo.add(opcaoPersonalizada);
        conteudo.add(opcaoPersonalizada, c);

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        GridBagConstraints f = new GridBagConstraints();
        f.anchor = GridBagConstraints.WEST;
        f.insets = new Insets(2, 0, 2, 6);
        f.gridy = 0;
        f.gridx = 0; campos.add(larguraRotulo, f);
        f.gridx = 1; campos.add(larguraCampo, f);
        f.gridx = 2; campos.add(rotuloSecundario("px"), f);
        f.gridy = 1;
        f.gridx = 0; campos.add(alturaRotulo, f);
        f.gridx = 1; campos.add(alturaCampo, f);
        f.gridx = 2; campos.add(rotuloSecundario("px"), f);
        f.gridy = 0;
        f.gridx = 3;
        f.gridheight = 2;
        f.insets = new Insets(2, 8, 2, 0);
        campos.add(aplicarResolucao, f);
        c.insets = new Insets(0, 26, 2, 0);
        conteudo.add(campos, c);

        JPanel controlesVisualizacao = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        controlesVisualizacao.setOpaque(false);
        controlesVisualizacao.add(new JLabel("Ampliação"));
        controlesVisualizacao.add(ampliacao);
        grade.setOpaque(false);
        controlesVisualizacao.add(grade);
        c.insets = new Insets(6, 0, 0, 0);
        conteudo.add(controlesVisualizacao, c);
        return conteudo;
    }

    private JPanel criarConteudoAlgoritmos() {
        JPanel conteudo = new JPanel(new GridBagLayout());
        conteudo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        conteudo.add(new JLabel("Algoritmo selecionado"), c);
        c.insets = new Insets(4, 0, 0, 0);
        conteudo.add(algoritmos, c);
        c.insets = new Insets(6, 0, 0, 0);
        conteudo.add(rotuloSecundario("Escolha uma primitiva e o algoritmo correspondente."), c);
        return conteudo;
    }

    private JPanel criarConteudoResultados() {
        JPanel conteudo = new JPanel(new GridBagLayout());
        conteudo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        conteudo.add(criarTabela(resultados), c);

        displayRotulo.setForeground(TEXTO_SECUNDARIO);
        c.insets = new Insets(8, 0, 0, 0);
        conteudo.add(displayRotulo, c);
        JLabel cor = new JLabel("Cor do pixel: #00FF00 (R 0, G 255, B 0)",
                amostraDeCor(new Color(Tela.COR_PIXEL)), SwingConstants.LEFT);
        cor.setForeground(TEXTO_SECUNDARIO);
        cor.setIconTextGap(6);
        c.insets = new Insets(4, 0, 0, 0);
        conteudo.add(cor, c);

        JLabel subtitulo = new JLabel("Transformação inversa");
        subtitulo.setFont(subtitulo.getFont().deriveFont(Font.BOLD));
        c.insets = new Insets(16, 0, 0, 0);
        conteudo.add(subtitulo, c);
        c.insets = new Insets(2, 0, 6, 0);
        conteudo.add(rotuloSecundario("Clique no display para calcular."), c);
        c.insets = new Insets(0, 0, 0, 0);
        conteudo.add(criarTabela(inversa), c);
        return conteudo;
    }

    private JLabel criarBarraStatus() {
        status.setOpaque(true);
        status.setBackground(new Color(0xE3E6EA));
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDA),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return status;
    }

    private static JPanel painel(String titulo, JComponent conteudo) {
        JLabel cabecalho = new JLabel(titulo);
        cabecalho.setFont(cabecalho.getFont().deriveFont(Font.BOLD, 13f));
        cabecalho.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(FUNDO_PAINEL);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(10, 12, 12, 12)));
        painel.add(cabecalho, BorderLayout.NORTH);
        painel.add(conteudo, BorderLayout.CENTER);
        return painel;
    }

    private static JLabel rotuloSecundario(String texto) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setForeground(TEXTO_SECUNDARIO);
        return rotulo;
    }

    private static DefaultTableModel criarModelo() {
        return new DefaultTableModel(new Object[] {"Sistema", "X", "Y"}, 0) {
            @Override public boolean isCellEditable(int linha, int coluna) { return false; }
        };
    }

    private static JComponent criarTabela(DefaultTableModel modelo) {
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(24);
        tabela.setFocusable(false);
        tabela.setRowSelectionAllowed(false);
        tabela.setShowGrid(true);
        tabela.setGridColor(BORDA);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setResizingAllowed(false);
        for (int i = 0; i < 3; i++) {
            TableColumn coluna = tabela.getColumnModel().getColumn(i);
            coluna.setPreferredWidth(i == 0 ? 134 : 84);
            coluna.setCellRenderer(new Celula(i == 0));
        }
        JPanel caixa = new JPanel(new BorderLayout());
        caixa.setBorder(BorderFactory.createLineBorder(BORDA));
        caixa.add(tabela.getTableHeader(), BorderLayout.NORTH);
        caixa.add(tabela, BorderLayout.CENTER);
        return caixa;
    }

    private static final class Celula extends DefaultTableCellRenderer {
        private final boolean rotulo;

        Celula(boolean rotulo) { this.rotulo = rotulo; }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                                                       boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, false, false, linha, coluna);
            setHorizontalAlignment(rotulo ? SwingConstants.LEFT : SwingConstants.RIGHT);
            setFont(rotulo ? tabela.getFont().deriveFont(Font.BOLD) : tabela.getFont());
            setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            setBackground(linha % 2 == 0 ? FUNDO_PAINEL : FUNDO_LINHA_ALTERNADA);
            return this;
        }
    }

    private static Icon amostraDeCor(Color cor) {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(cor);
                g.fillRect(x, y, 14, 14);
                g.setColor(BORDA_DISPLAY);
                g.drawRect(x, y, 13, 13);
            }
            @Override public int getIconWidth() { return 14; }
            @Override public int getIconHeight() { return 14; }
        };
    }

    private static void preencher(DefaultTableModel modelo, String[][] linhas) {
        modelo.setRowCount(0);
        for (String[] linha : linhas) {
            modelo.addRow(linha);
        }
    }

    public void addMostrarListener(ActionListener listener) { mostrar.addActionListener(listener); }

    public void addLimparTelaListener(ActionListener listener) {
        limparTela.addActionListener(listener);
    }

    public void addTransformacaoPoligonoListener(ActionListener listener) {
        transladarPoligono.addActionListener(listener);
        escalarPoligono.addActionListener(listener);
        rotacionarPoligono.addActionListener(listener);
        reflexaoX.addActionListener(listener);
        reflexaoY.addActionListener(listener);
        reflexaoOrigem.addActionListener(listener);
        reflexaoRetaXY.addActionListener(listener);
        cisalhamentoX.addActionListener(listener);
        cisalhamentoY.addActionListener(listener);
        escalarPontoFixo.addActionListener(listener);
        rotacionarPontoFixo.addActionListener(listener);
        comporTransformacoes.addActionListener(listener);
        restaurarPoligono.addActionListener(listener);
    }

    public void addCenarioListener(ActionListener listener) { cenario.addActionListener(listener); }

    public void addVisualizacaoListener(ActionListener listener) {
        ampliacao.addActionListener(listener);
        grade.addActionListener(listener);
    }

    public void addAlgoritmoListener(ActionListener listener) {
        algoritmos.addActionListener(listener);
    }

    public void addFormaListener(ActionListener listener) {
        tipoForma.addActionListener(listener);
    }

    public void setAlgoritmosParaForma(FormaRasterizacao.Tipo tipo) {
        DefaultComboBoxModel<TipoAlgoritmo> modelo = new DefaultComboBoxModel<>();
        for (TipoAlgoritmo algoritmo : TipoAlgoritmo.values()) {
            if (algoritmo.getTipoForma() == tipo) {
                modelo.addElement(algoritmo);
            }
        }
        algoritmos.setModel(modelo);
        algoritmos.setEnabled(tipo != FormaRasterizacao.Tipo.PONTO
                && tipo != FormaRasterizacao.Tipo.POLIGONO);
    }

    /* Disparado ao escolher uma resolucao, ao clicar em Aplicar ou ao teclar Enter nos campos. */
    public void addResolucaoListener(ActionListener listener) {
        for (JRadioButton opcao : opcoesResolucao) {
            opcao.addActionListener(listener);
        }
        opcaoPersonalizada.addActionListener(listener);
        aplicarResolucao.addActionListener(listener);
        larguraCampo.addActionListener(listener);
        alturaCampo.addActionListener(listener);
    }

    public void addTelaMouseListener(MouseListener listener) { tela.addMouseListener(listener); }

    /* ----- Leitura dos campos ----- */
    public String getXminTexto() { return xminCampo.getText(); }

    public String getXmaxTexto() { return xmaxCampo.getText(); }

    public String getYminTexto() { return yminCampo.getText(); }

    public String getYmaxTexto() { return ymaxCampo.getText(); }

    public String getXTexto() { return xCampo.getText(); }

    public String getYTexto() { return yCampo.getText(); }

    public String getX2Texto() { return x2Campo.getText(); }

    public String getY2Texto() { return y2Campo.getText(); }

    public String getRaioTexto() { return raioCampo.getText(); }

    public String getVerticesTexto() { return verticesCampo.getText(); }

    public String getTranslacaoXTexto() { return translacaoXCampo.getText(); }

    public String getTranslacaoYTexto() { return translacaoYCampo.getText(); }

    public String getEscalaXTexto() { return escalaXCampo.getText(); }

    public String getEscalaYTexto() { return escalaYCampo.getText(); }

    public String getRotacaoTexto() { return rotacaoCampo.getText(); }

    public String getCisalhamentoTexto() { return cisalhamentoCampo.getText(); }

    public String getPontoFixoXTexto() { return pontoFixoXCampo.getText(); }

    public String getPontoFixoYTexto() { return pontoFixoYCampo.getText(); }

    public Tela getTela() { return tela; }

    public FormaRasterizacao.Tipo getTipoFormaSelecionado() {
        return (FormaRasterizacao.Tipo) tipoForma.getSelectedItem();
    }

    public boolean isCentrado() { return cenario.getSelectedIndex() == 1; }

    public int getAmpliacao() { return 1 << ampliacao.getSelectedIndex(); }

    public boolean isGradeAtiva() { return grade.isSelected(); }

    public TipoAlgoritmo getAlgoritmoSelecionado() {
        return (TipoAlgoritmo) algoritmos.getSelectedItem();
    }

    public boolean isResolucaoPersonalizada() { return opcaoPersonalizada.isSelected(); }

    public int[] getResolucaoPredefinida() {
        for (int i = 0; i < opcoesResolucao.length; i++) {
            if (opcoesResolucao[i].isSelected()) return RESOLUCOES[i].clone();
        }
        return RESOLUCOES[RESOLUCAO_INICIAL].clone();
    }

    public String getLarguraPersonalizadaTexto() { return larguraCampo.getText(); }

    public String getAlturaPersonalizadaTexto() { return alturaCampo.getText(); }

    public int getLarguraTela() { return tela.getLarguraDisplay(); }

    public int getAlturaTela() { return tela.getAlturaDisplay(); }

    public int getLarguraVisualizacao() { return tela.getLarguraVisualizacao(); }

    public int getAlturaVisualizacao() { return tela.getAlturaVisualizacao(); }

    public Pixel toDisplayPixel(int x, int y) { return tela.toDisplayPixel(x, y); }

    /* ----- Atualizacao da interface ----- */
    public void setXTexto(String texto) { xCampo.setText(texto); }

    public void setYTexto(String texto) { yCampo.setText(texto); }

    public void setCamposPersonalizadosAtivos(boolean ativos) {
        larguraRotulo.setEnabled(ativos);
        alturaRotulo.setEnabled(ativos);
        larguraCampo.setEnabled(ativos);
        alturaCampo.setEnabled(ativos);
        aplicarResolucao.setEnabled(ativos);
    }

    public void setResolucaoDisplay(int largura, int altura) {
        tela.setResolucao(largura, altura);
        displayRotulo.setText(String.format("Display: %d × %d px", largura, altura));
        mesa.revalidate();
        mesa.repaint();
    }

    public void setVisualizacaoDisplay(int escala, boolean gradeAtiva) {
        tela.setVisualizacao(escala, gradeAtiva);
        mesa.revalidate();
        mesa.repaint();
    }

    /* Cada linha: {sistema, x, y}. */
    public void setResultados(String[][] linhas) { preencher(resultados, linhas); }

    public void setResultadosInversa(String[][] linhas) { preencher(inversa, linhas); }

    public void limparResultados() {
        preencher(resultados, new String[][] {
                {"Mundo", VAZIO, VAZIO}, {"NDC", VAZIO, VAZIO}, {"Dispositivo (DC)", VAZIO, VAZIO}
        });
    }

    public void limparInversa() {
        preencher(inversa, new String[][] {
                {"Clique (mouse)", VAZIO, VAZIO}, {"NDC", VAZIO, VAZIO}, {"Mundo", VAZIO, VAZIO}
        });
    }

    public void setStatus(String texto, boolean erro) {
        status.setText(texto);
        status.setForeground(erro ? COR_ERRO : COR_OK);
    }

    public void limparTela() { tela.limpar(); }

    public void drawPixel(int x, int y) {
        tela.drawPixel(x, y);
        int escala = tela.getEscala();
        SwingUtilities.invokeLater(() -> tela.scrollRectToVisible(
                new Rectangle(x * escala - 16, y * escala - 16, 33, 33)));
    }

    public void drawPixels(Iterable<Pixel> pixels) {
        tela.drawPixels(pixels);
    }

    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Entrada inválida",
                JOptionPane.ERROR_MESSAGE);
    }
}