package transformacoes.view;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/** Janela do Laboratorio I: apenas componentes visuais, sem regras de negocio. */
public class TransformacoesView extends JFrame {
    /* Paleta clara: cinza na janela, branco no display e nos paineis de informacao. */
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
    private static final int RESOLUCAO_INICIAL = 1; // 640 x 480

    static {
        // Fontes sem negrito no tema padrao (Metal); precisa rodar antes de criar componentes.
        UIManager.put("swing.boldMetal", Boolean.FALSE);
    }

    /* Janela do mundo, ponto e cenario NDC: os mesmos campos de antes. */
    private final JTextField xminCampo = new JTextField("-10", 5);
    private final JTextField xmaxCampo = new JTextField("30", 5);
    private final JTextField yminCampo = new JTextField("5", 5);
    private final JTextField ymaxCampo = new JTextField("25", 5);
    private final JTextField xCampo = new JTextField("10", 5);
    private final JTextField yCampo = new JTextField("15", 5);
    private final JComboBox<String> cenario = new JComboBox<>(new String[] {
        "[0,1] x [0,1]", "[-1,1] x [-1,1]"
    });
    private final JButton mostrar = new JButton("Ativar pixel");

    /* Display e escolha da resolucao. */
    private final Tela tela = new Tela();
    private final JPanel mesa = new JPanel(new GridBagLayout());
    private final JRadioButton[] opcoesResolucao = new JRadioButton[RESOLUCOES.length];
    private final JRadioButton opcaoPersonalizada = new JRadioButton("Personalizada");
    private final JLabel larguraRotulo = new JLabel("Largura");
    private final JLabel alturaRotulo = new JLabel("Altura");
    private final JTextField larguraCampo = new JTextField("1000", 5);
    private final JTextField alturaCampo = new JTextField("700", 5);
    private final JButton aplicarResolucao = new JButton("Aplicar");

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

        opcoesResolucao[RESOLUCAO_INICIAL].setSelected(true);
        setResolucaoDisplay(RESOLUCOES[RESOLUCAO_INICIAL][0], RESOLUCOES[RESOLUCAO_INICIAL][1]);
        setCamposPersonalizadosAtivos(false);
        limparResultados();
        limparInversa();

        Rectangle limite = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1200, limite.width), Math.min(780, limite.height));
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
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
        segundaLinha.add(new JLabel("Ponto no mundo: x"));
        segundaLinha.add(xCampo);
        segundaLinha.add(new JLabel("y"));
        segundaLinha.add(yCampo);
        segundaLinha.add(new JLabel("NDC"));
        segundaLinha.add(cenario);
        segundaLinha.add(mostrar);

        JPanel controles = new JPanel(new BorderLayout());
        controles.setBackground(FUNDO_JANELA);
        controles.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDA),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        controles.add(primeiraLinha, BorderLayout.NORTH);
        controles.add(segundaLinha, BorderLayout.SOUTH);
        return controles;
    }

    private static JPanel linhaDeCampos() {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        linha.setOpaque(false);
        return linha;
    }

    /* O display fica centralizado sobre um fundo cinza; resolucoes grandes ganham rolagem. */
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
        lateral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(0, 0, 10, 0);
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
        // Largura do conteudo + espaco da barra de rolagem: nada fica cortado em telas baixas.
        int largura = lateral.getPreferredSize().width
                + rolagem.getVerticalScrollBar().getPreferredSize().width + 1;
        rolagem.setPreferredSize(new Dimension(largura, 0));
        return rolagem;
    }

    private JPanel criarConteudoResolucao() {
        JPanel conteudo = new JPanel(new GridBagLayout());
        conteudo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        // Resolucoes prontas em duas colunas; "Personalizada" fica por ultimo, com seus campos logo abaixo.
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
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return status;
    }

    /* ----- Pecas reutilizadas ----- */
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

    /* Primeira coluna: nome do sistema em negrito; demais: numeros alinhados a direita. */
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

    /* ----- Registro de listeners (o Controller decide o que fazer) ----- */
    public void addMostrarListener(ActionListener listener) { mostrar.addActionListener(listener); }

    public void addCenarioListener(ActionListener listener) { cenario.addActionListener(listener); }

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

    public boolean isCentrado() { return cenario.getSelectedIndex() == 1; }

    public boolean isResolucaoPersonalizada() { return opcaoPersonalizada.isSelected(); }

    public int[] getResolucaoPredefinida() {
        for (int i = 0; i < opcoesResolucao.length; i++) {
            if (opcoesResolucao[i].isSelected()) return RESOLUCOES[i].clone();
        }
        return RESOLUCOES[RESOLUCAO_INICIAL].clone();
    }

    public String getLarguraPersonalizadaTexto() { return larguraCampo.getText(); }

    public String getAlturaPersonalizadaTexto() { return alturaCampo.getText(); }

    /* Resolucao do frame buffer (nao o tamanho da janela). */
    public int getLarguraTela() { return tela.getLarguraDisplay(); }

    public int getAlturaTela() { return tela.getAlturaDisplay(); }

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

    /* Ativa o pixel e rola o display ate ele, se a resolucao for maior que a area visivel. */
    public void drawPixel(int x, int y) {
        tela.drawPixel(x, y);
        SwingUtilities.invokeLater(() -> tela.scrollRectToVisible(new Rectangle(x - 16, y - 16, 33, 33)));
    }

    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Entrada inválida",
                                      JOptionPane.ERROR_MESSAGE);
    }
}