package tabelacores.view;

import tabelacores.model.Paleta;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.function.IntConsumer;

/** Janela da tabela de cores: apenas componentes visuais, sem regras de negocio. */
public class TabelaCoresView extends JFrame {
    private final JButton[] amostras = new JButton[Paleta.CORES_POR_PAGINA];
    private final Border bordaNormal = BorderFactory.createLineBorder(new Color(90, 90, 90));
    private final Border bordaSelecionada = BorderFactory.createLineBorder(Color.YELLOW, 3);
    private final JTextField campoIndice = new JTextField("F00", 5);
    private final JTextField campoRGB = new JTextField("FF0000", 7);
    private final JLabel paginaTexto = new JLabel();
    private final JLabel detalhes = new JLabel();
    private final JPanel amostraGrande = new JPanel();
    private final JButton anterior = new JButton("< Pagina anterior");
    private final JButton proxima = new JButton("Proxima pagina >");
    private final JButton botaoSelecionar = new JButton("Selecionar");
    private final JButton botaoAlterar = new JButton("Alterar entrada");
    private final JButton botaoRestaurar = new JButton("Restaurar paleta");

    public TabelaCoresView() {
        super("Exercicio extra G2 - Tabela de cores (12 bits / RGB 24 bits)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel cabecalho = new JPanel(new BorderLayout(4, 4));
        JLabel explicacao = new JLabel("Pixel: indice de 12 bits (0x000..0xFFF) | Paleta: 4096 entradas RGB de 24 bits");
        explicacao.setBorder(BorderFactory.createEmptyBorder(8, 10, 3, 10));
        cabecalho.add(explicacao, BorderLayout.NORTH);

        JPanel comandos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comandos.add(new JLabel("Indice (hex):"));
        comandos.add(campoIndice);
        comandos.add(botaoSelecionar);
        comandos.add(new JLabel("Cor RGB (hex): #"));
        comandos.add(campoRGB);
        comandos.add(botaoAlterar);
        comandos.add(botaoRestaurar);
        cabecalho.add(comandos, BorderLayout.SOUTH);
        add(cabecalho, BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(8, 8));
        corpo.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JPanel navegacao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        navegacao.add(anterior);
        navegacao.add(paginaTexto);
        navegacao.add(proxima);
        corpo.add(navegacao, BorderLayout.NORTH);

        JPanel grade = new JPanel(new GridLayout(16, 16, 2, 2));
        grade.setBackground(Color.DARK_GRAY);
        grade.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        for (int i = 0; i < Paleta.CORES_POR_PAGINA; i++) {
            JButton amostra = new JButton();
            amostra.setPreferredSize(new Dimension(26, 26));
            amostra.setOpaque(true);
            amostra.setContentAreaFilled(true);
            amostra.setFocusPainted(false);
            amostra.setBorder(bordaNormal);
            amostras[i] = amostra;
            grade.add(amostra);
        }
        corpo.add(grade, BorderLayout.CENTER);

        JPanel painelDetalhes = new JPanel(new BorderLayout(8, 8));
        amostraGrande.setPreferredSize(new Dimension(125, 125));
        amostraGrande.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        painelDetalhes.add(amostraGrande, BorderLayout.WEST);
        detalhes.setVerticalAlignment(SwingConstants.CENTER);
        painelDetalhes.add(detalhes, BorderLayout.CENTER);
        corpo.add(painelDetalhes, BorderLayout.SOUTH);
        add(corpo, BorderLayout.CENTER);

        // Atalhos de teclado (Enter) internos a View.
        campoIndice.addActionListener(e -> botaoSelecionar.doClick());
        campoRGB.addActionListener(e -> botaoAlterar.doClick());

        setSize(850, 780);
        setMinimumSize(new Dimension(650, 600));
        setLocationRelativeTo(null);
    }

    /* ----- Registro de listeners (o Controller decide o que fazer) ----- */
    public void addSelecionarListener(ActionListener listener) { botaoSelecionar.addActionListener(listener); }

    public void addAlterarListener(ActionListener listener) { botaoAlterar.addActionListener(listener); }

    public void addRestaurarListener(ActionListener listener) { botaoRestaurar.addActionListener(listener); }

    public void addAnteriorListener(ActionListener listener) { anterior.addActionListener(listener); }

    public void addProximaListener(ActionListener listener) { proxima.addActionListener(listener); }

    /** Informa a posicao (0..255) da amostra clicada dentro da pagina atual. */
    public void addAmostraListener(IntConsumer listener) {
        for (int i = 0; i < Paleta.CORES_POR_PAGINA; i++) {
            final int posicao = i;
            amostras[i].addActionListener(e -> listener.accept(posicao));
        }
    }

    /* ----- Leitura dos campos ----- */
    public String getIndiceTexto() { return campoIndice.getText(); }

    public String getRgbTexto() { return campoRGB.getText(); }

    /* ----- Atualizacao da interface ----- */
    public void exibirSelecao(Paleta paleta, int indice, int pagina) {
        int rgb = paleta.getCor(indice);
        int vermelho = (rgb >> 16) & 0xFF;
        int verde = (rgb >> 8) & 0xFF;
        int azul = rgb & 0xFF;
        campoIndice.setText(String.format(Locale.US, "%03X", indice));
        campoRGB.setText(String.format(Locale.US, "%06X", rgb));
        paginaTexto.setText(String.format(Locale.US, "Pagina %d de 16 (indices %03X a %03X)",
                pagina + 1, pagina * 256, pagina * 256 + 255));
        anterior.setEnabled(pagina > 0);
        proxima.setEnabled(pagina < Paleta.QUANTIDADE_PAGINAS - 1);
        amostraGrande.setBackground(new Color(rgb));
        String binario = String.format(Locale.US, "%12s", Integer.toBinaryString(indice)).replace(' ', '0');
        detalhes.setText(String.format(Locale.US,
                "<html><b>Indice do pixel (12 bits):</b> 0x%03X (%d)<br>"
                + "<b>Binario:</b> %s<br>"
                + "<b>Paleta[indice] (24 bits):</b> #%06X<br>"
                + "<b>Canais RGB:</b> R=%d, G=%d, B=%d (8 bits cada)</html>",
                indice, indice, binario, rgb, vermelho, verde, azul));
        for (int i = 0; i < Paleta.CORES_POR_PAGINA; i++) {
            int indiceAmostra = pagina * Paleta.CORES_POR_PAGINA + i;
            int cor = paleta.getCor(indiceAmostra);
            amostras[i].setBackground(new Color(cor));
            amostras[i].setBorder(indiceAmostra == indice
                    ? bordaSelecionada : bordaNormal);
            amostras[i].setToolTipText(String.format(Locale.US, "Indice %03X -> #%06X",
                    indiceAmostra, cor));
        }
    }

    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Valor invalido", JOptionPane.ERROR_MESSAGE);
    }

    public boolean confirmarRestauracao() {
        int resposta = JOptionPane.showConfirmDialog(this,
                "Restaurar as 4096 entradas RGB444 -> RGB888?", "Restaurar paleta",
                JOptionPane.YES_NO_OPTION);
        return resposta == JOptionPane.YES_OPTION;
    }
}
