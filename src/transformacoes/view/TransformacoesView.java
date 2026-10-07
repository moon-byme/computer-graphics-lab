package transformacoes.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;

/** Janela do Laboratorio I: apenas componentes visuais, sem regras de negocio. */
public class TransformacoesView extends JFrame {
    private final JTextField xminCampo = new JTextField("-10", 5);
    private final JTextField xmaxCampo = new JTextField("30", 5);
    private final JTextField yminCampo = new JTextField("5", 5);
    private final JTextField ymaxCampo = new JTextField("25", 5);
    private final JTextField xCampo = new JTextField("10", 5);
    private final JTextField yCampo = new JTextField("15", 5);
    private final JComboBox<String> cenario = new JComboBox<>(new String[] {
        "[0,1] x [0,1]", "[-1,1] x [-1,1]"
    });
    private final JLabel resultado = new JLabel("Informe a janela do mundo e o ponto.");
    private final Tela tela = new Tela();
    private final JButton mostrar = new JButton("Ativar pixel");

    public TransformacoesView() {
        super("Laboratorio I - Transformacoes de coordenadas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel controles = new JPanel(new BorderLayout());
        JPanel primeiraLinha = new JPanel(new FlowLayout(FlowLayout.LEFT));
        primeiraLinha.add(new JLabel("Janela: xmin"));
        primeiraLinha.add(xminCampo);
        primeiraLinha.add(new JLabel("xmax"));
        primeiraLinha.add(xmaxCampo);
        primeiraLinha.add(new JLabel("ymin"));
        primeiraLinha.add(yminCampo);
        primeiraLinha.add(new JLabel("ymax"));
        primeiraLinha.add(ymaxCampo);

        JPanel segundaLinha = new JPanel(new FlowLayout(FlowLayout.LEFT));
        segundaLinha.add(new JLabel("Ponto no mundo: x"));
        segundaLinha.add(xCampo);
        segundaLinha.add(new JLabel("y"));
        segundaLinha.add(yCampo);
        segundaLinha.add(new JLabel("NDC"));
        segundaLinha.add(cenario);
        segundaLinha.add(mostrar);
        controles.add(primeiraLinha, BorderLayout.NORTH);
        controles.add(segundaLinha, BorderLayout.SOUTH);
        add(controles, BorderLayout.NORTH);

        add(tela, BorderLayout.CENTER);
        add(resultado, BorderLayout.SOUTH);
    }

    /* ----- Registro de listeners (o Controller decide o que fazer) ----- */
    public void addMostrarListener(ActionListener listener) { mostrar.addActionListener(listener); }

    public void addCenarioListener(ActionListener listener) { cenario.addActionListener(listener); }

    public void addTelaComponentListener(ComponentListener listener) { tela.addComponentListener(listener); }

    public void addTelaMouseListener(MouseListener listener) { tela.addMouseListener(listener); }

    /* ----- Leitura dos campos ----- */
    public String getXminTexto() { return xminCampo.getText(); }

    public String getXmaxTexto() { return xmaxCampo.getText(); }

    public String getYminTexto() { return yminCampo.getText(); }

    public String getYmaxTexto() { return ymaxCampo.getText(); }

    public String getXTexto() { return xCampo.getText(); }

    public String getYTexto() { return yCampo.getText(); }

    public boolean isCentrado() { return cenario.getSelectedIndex() == 1; }

    /* ----- Atualizacao da interface ----- */
    public void setXTexto(String texto) { xCampo.setText(texto); }

    public void setYTexto(String texto) { yCampo.setText(texto); }

    public void setResultado(String texto) { resultado.setText(texto); }

    public int getLarguraTela() { return tela.getWidth(); }

    public int getAlturaTela() { return tela.getHeight(); }

    public void limparTela() { tela.limpar(); }

    public void drawPixel(int x, int y) { tela.drawPixel(x, y); }

    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Entrada invalida",
                                      JOptionPane.ERROR_MESSAGE);
    }
}
