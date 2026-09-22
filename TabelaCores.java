import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

public class TabelaCores extends JFrame {
    private static final int QUANTIDADE_CORES = 4096;
    private static final int CORES_POR_PAGINA = 256;
    private static final int QUANTIDADE_PAGINAS = 16;

    private final int[] paleta = criarPaleta();
    private final JButton[] amostras = new JButton[CORES_POR_PAGINA];
    private final Border bordaNormal = BorderFactory.createLineBorder(new Color(90, 90, 90));
    private final Border bordaSelecionada = BorderFactory.createLineBorder(Color.YELLOW, 3);
    private final JTextField campoIndice = new JTextField("F00", 5);
    private final JTextField campoRGB = new JTextField("FF0000", 7);
    private final JLabel paginaTexto = new JLabel();
    private final JLabel detalhes = new JLabel();
    private final JPanel amostraGrande = new JPanel();
    private final JButton anterior = new JButton("< Pagina anterior");
    private final JButton proxima = new JButton("Proxima pagina >");

    private int pagina = 0;
    private int indiceSelecionado = 0x000;

    private static int expandir4Para8(int valor) {
        return valor * 17;
    }

    private static int[] criarPaleta() {
        int[] tabela = new int[QUANTIDADE_CORES];
        for (int indice = 0; indice < QUANTIDADE_CORES; indice++) {
            int r4 = (indice >> 8) & 0xF;
            int g4 = (indice >> 4) & 0xF;
            int b4 = indice & 0xF;
            int r8 = expandir4Para8(r4);
            int g8 = expandir4Para8(g4);
            int b8 = expandir4Para8(b4);
            tabela[indice] = (r8 << 16) | (g8 << 8) | b8;
        }
        return tabela;
    }

    private static int lerHexadecimal(String texto, int digitosMaximos, String nome) {
        String valor = texto.trim().replaceFirst("^(?i:0x|#)", "");
        if (valor.isEmpty() || valor.length() > digitosMaximos || !valor.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException(nome + " deve conter de 1 a "
                    + digitosMaximos + " digitos hexadecimais.");
        }
        return Integer.parseInt(valor, 16);
    }

    private TabelaCores() {
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
        JButton botaoSelecionar = new JButton("Selecionar");
        comandos.add(botaoSelecionar);
        comandos.add(new JLabel("Cor RGB (hex): #"));
        comandos.add(campoRGB);
        JButton botaoAlterar = new JButton("Alterar entrada");
        comandos.add(botaoAlterar);
        JButton botaoRestaurar = new JButton("Restaurar paleta");
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
        for (int i = 0; i < CORES_POR_PAGINA; i++) {
            final int posicao = i;
            JButton amostra = new JButton();
            amostra.setPreferredSize(new Dimension(26, 26));
            amostra.setOpaque(true);
            amostra.setContentAreaFilled(true);
            amostra.setFocusPainted(false);
            amostra.setBorder(bordaNormal);
            amostra.addActionListener(e -> selecionar(pagina * CORES_POR_PAGINA + posicao));
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

        botaoSelecionar.addActionListener(e -> {
            try {
                int indice = lerHexadecimal(campoIndice.getText(), 3, "Indice de 12 bits");
                selecionar(indice);
            } catch (IllegalArgumentException erro) {
                mostrarErro(erro.getMessage());
            }
        });
        campoIndice.addActionListener(e -> botaoSelecionar.doClick());

        botaoAlterar.addActionListener(e -> {
            try {
                int indice = lerHexadecimal(campoIndice.getText(), 3, "Indice de 12 bits");
                int rgb = lerHexadecimal(campoRGB.getText(), 6, "Cor RGB de 24 bits");
                // Exigir os seis digitos evita ambiguidade entre #ABC e #000ABC.
                String limpa = campoRGB.getText().trim().replaceFirst("^(?i:0x|#)", "");
                if (limpa.length() != 6) {
                    throw new IllegalArgumentException("A cor RGB deve ter exatamente 6 digitos (RRGGBB).");
                }
                paleta[indice] = rgb;
                selecionar(indice);
            } catch (IllegalArgumentException erro) {
                mostrarErro(erro.getMessage());
            }
        });
        campoRGB.addActionListener(e -> botaoAlterar.doClick());

        botaoRestaurar.addActionListener(e -> {
            int resposta = JOptionPane.showConfirmDialog(this,
                    "Restaurar as 4096 entradas RGB444 -> RGB888?", "Restaurar paleta",
                    JOptionPane.YES_NO_OPTION);
            if (resposta == JOptionPane.YES_OPTION) {
                int[] padrao = criarPaleta();
                System.arraycopy(padrao, 0, paleta, 0, paleta.length);
                selecionar(indiceSelecionado);
            }
        });
        anterior.addActionListener(e -> mudarPagina(pagina - 1));
        proxima.addActionListener(e -> mudarPagina(pagina + 1));

        selecionar(indiceSelecionado);
        setSize(850, 780);
        setMinimumSize(new Dimension(650, 600));
        setLocationRelativeTo(null);
    }

    private void mudarPagina(int novaPagina) {
        if (novaPagina >= 0 && novaPagina < QUANTIDADE_PAGINAS) {
            pagina = novaPagina;
            selecionar(pagina * CORES_POR_PAGINA);
        }
    }

    private void selecionar(int indice) {
        indiceSelecionado = indice;
        pagina = indice / CORES_POR_PAGINA;
        int rgb = paleta[indice];
        int vermelho = (rgb >> 16) & 0xFF;
        int verde = (rgb >> 8) & 0xFF;
        int azul = rgb & 0xFF;
        campoIndice.setText(String.format(Locale.US, "%03X", indice));
        campoRGB.setText(String.format(Locale.US, "%06X", rgb));
        paginaTexto.setText(String.format(Locale.US, "Pagina %d de 16 (indices %03X a %03X)",
                pagina + 1, pagina * 256, pagina * 256 + 255));
        anterior.setEnabled(pagina > 0);
        proxima.setEnabled(pagina < QUANTIDADE_PAGINAS - 1);
        amostraGrande.setBackground(new Color(rgb));
        String binario = String.format(Locale.US, "%12s", Integer.toBinaryString(indice)).replace(' ', '0');
        detalhes.setText(String.format(Locale.US,
                "<html><b>Indice do pixel (12 bits):</b> 0x%03X (%d)<br>"
                + "<b>Binario:</b> %s<br>"
                + "<b>Paleta[indice] (24 bits):</b> #%06X<br>"
                + "<b>Canais RGB:</b> R=%d, G=%d, B=%d (8 bits cada)</html>",
                indice, indice, binario, rgb, vermelho, verde, azul));
        for (int i = 0; i < CORES_POR_PAGINA; i++) {
            int indiceAmostra = pagina * CORES_POR_PAGINA + i;
            int cor = paleta[indiceAmostra];
            amostras[i].setBackground(new Color(cor));
            amostras[i].setBorder(indiceAmostra == indiceSelecionado
                    ? bordaSelecionada : bordaNormal);
            amostras[i].setToolTipText(String.format(Locale.US, "Indice %03X -> #%06X",
                    indiceAmostra, cor));
        }
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Valor invalido", JOptionPane.ERROR_MESSAGE);
    }

    /** Testes independentes da interface: java TabelaCores --test */
    private static void testar() {
        int[] tabela = criarPaleta();
        if (tabela.length != 4096 || tabela[0] != 0x000000 || tabela[0xFFF] != 0xFFFFFF
                || tabela[0xF00] != 0xFF0000 || tabela[0x0F0] != 0x00FF00
                || tabela[0x00F] != 0x0000FF) {
            throw new AssertionError("Paleta RGB444 -> RGB888 incorreta.");
        }
        HashSet<Integer> unicas = new HashSet<>();
        for (int cor : tabela) unicas.add(cor);
        if (unicas.size() != 4096) throw new AssertionError("Ha cores iniciais repetidas.");
        tabela[0x123] = 0xABCDEF;
        if (tabela[0x123] != 0xABCDEF) {
            throw new AssertionError("Falha ao associar uma cor RGB de 24 bits ao indice.");
        }
        if (lerHexadecimal("FFF", 3, "Indice") != 4095
                || lerHexadecimal("#00FF00", 6, "RGB") != 0x00FF00) {
            throw new AssertionError("Leitura hexadecimal incorreta.");
        }
        System.out.println("OK: 4096 entradas enderecadas por 12 bits (000..FFF).");
        System.out.println("OK: cores RGB de 24 bits, mapeamento RGB444 e edicao da paleta.");
    }

    public static void main(String[] args) {
        if (args.length == 1 && "--test".equals(args[0])) {
            testar();
        } else {
            SwingUtilities.invokeLater(() -> new TabelaCores().setVisible(true));
        }
    }
}
