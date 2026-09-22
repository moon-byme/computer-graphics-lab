import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Laboratorio I: transformacoes Mundo -> NDC -> Dispositivo. */
public class TransformacoesCoordenadas extends JFrame {
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

    private static class Ponto {
        final double x;
        final double y;
        Ponto(double x, double y) { this.x = x; this.y = y; }
    }

    private static class Pixel {
        final int x;
        final int y;
        Pixel(int x, int y) { this.x = x; this.y = y; }
    }

    /* Janela de mundo -> coordenadas normalizadas. */
    private static Ponto userToNdc(double x, double y, double xmin, double xmax,
                                   double ymin, double ymax, boolean centrado) {
        double nx = (x - xmin) / (xmax - xmin);
        double ny = (y - ymin) / (ymax - ymin);
        if (centrado) {
            nx = 2 * nx - 1;
            ny = 2 * ny - 1;
        }
        return new Ponto(nx, ny);
    }

    /* Coordenadas normalizadas -> janela de mundo (transformacao inversa). */
    private static Ponto ndcToUser(double nx, double ny, double xmin, double xmax,
                                   double ymin, double ymax, boolean centrado) {
        if (centrado) {
            nx = (nx + 1) / 2;
            ny = (ny + 1) / 2;
        }
        return new Ponto(xmin + nx * (xmax - xmin),
                         ymin + ny * (ymax - ymin));
    }

    /* NDC -> dispositivo. No Swing, a origem da tela fica no canto superior esquerdo. */
    private static Pixel ndcToDc(double nx, double ny, int largura, int altura,
                                 boolean centrado) {
        if (centrado) {
            nx = (nx + 1) / 2;
            ny = (ny + 1) / 2;
        }
        int dx = (int) Math.round(nx * (largura - 1));
        int dy = (int) Math.round((1 - ny) * (altura - 1));
        return new Pixel(dx, dy);
    }

    /* Dispositivo de entrada (mouse) -> NDC. */
    private static Ponto inpToNdc(int dx, int dy, int largura, int altura,
                                  boolean centrado) {
        double nx = (double) dx / (largura - 1);
        double ny = 1 - (double) dy / (altura - 1);
        if (centrado) {
            nx = 2 * nx - 1;
            ny = 2 * ny - 1;
        }
        return new Ponto(nx, ny);
    }

    /* Frame buffer raster, sem grade, ampliacao, circulo ou espessura artificial. */
    private static class Tela extends JPanel {
        private BufferedImage imagem;

        Tela() { setBackground(Color.BLACK); }

        void limpar() {
            int largura = getWidth();
            int altura = getHeight();
            if (largura > 0 && altura > 0) {
                // A imagem nova com TYPE_INT_RGB comeca com todos os pixels pretos.
                imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
            } else {
                imagem = null;
            }
            repaint();
        }

        /* Ativa EXATAMENTE UMA posicao do frame buffer: verde RGB #00FF00. */
        void drawPixel(int x, int y) {
            if (imagem == null || imagem.getWidth() != getWidth()
                    || imagem.getHeight() != getHeight()) {
                limpar();
            }
            if (imagem != null && x >= 0 && y >= 0
                    && x < imagem.getWidth() && y < imagem.getHeight()) {
                imagem.setRGB(x, y, 0x00FF00);
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagem != null && imagem.getWidth() == getWidth()
                    && imagem.getHeight() == getHeight()) {
                // Desenha a imagem na escala original: 1 ponto de imagem por pixel do canvas.
                g.drawImage(imagem, 0, 0, null);
            }
        }
    }

    public TransformacoesCoordenadas() {
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
        JButton mostrar = new JButton("Ativar pixel");
        segundaLinha.add(mostrar);
        controles.add(primeiraLinha, BorderLayout.NORTH);
        controles.add(segundaLinha, BorderLayout.SOUTH);
        add(controles, BorderLayout.NORTH);

        add(tela, BorderLayout.CENTER);
        add(resultado, BorderLayout.SOUTH);
        mostrar.addActionListener(e -> atualizar(true));
        cenario.addActionListener(e -> atualizar(false));
        tela.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { atualizar(false); }
        });
        tela.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                try {
                    double[] mundo = lerJanela();
                    if (tela.getWidth() < 2 || tela.getHeight() < 2) return;
                    boolean centrado = cenario.getSelectedIndex() == 1;
                    Ponto ndc = inpToNdc(e.getX(), e.getY(), tela.getWidth(),
                                          tela.getHeight(), centrado);
                    Ponto ponto = ndcToUser(ndc.x, ndc.y, mundo[0], mundo[1],
                                            mundo[2], mundo[3], centrado);
                    xCampo.setText(String.format(Locale.US, "%.4f", ponto.x));
                    yCampo.setText(String.format(Locale.US, "%.4f", ponto.y));
                    atualizar(false);
                } catch (IllegalArgumentException erro) {
                    JOptionPane.showMessageDialog(TransformacoesCoordenadas.this,
                        erro.getMessage(), "Entrada invalida", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        SwingUtilities.invokeLater(() -> atualizar(false));
    }

    private static double lerNumero(JTextField campo) {
        try {
            double numero = Double.parseDouble(campo.getText().trim().replace(',', '.'));
            if (!Double.isFinite(numero)) throw new NumberFormatException();
            return numero;
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("Informe apenas numeros reais finitos.");
        }
    }

    private double[] lerJanela() {
        double xmin = lerNumero(xminCampo);
        double xmax = lerNumero(xmaxCampo);
        double ymin = lerNumero(yminCampo);
        double ymax = lerNumero(ymaxCampo);
        if (!(xmin < xmax && ymin < ymax)) {
            throw new IllegalArgumentException("Exige-se xmin < xmax e ymin < ymax.");
        }
        return new double[] {xmin, xmax, ymin, ymax};
    }

    private void atualizar(boolean mostrarErro) {
        try {
            double[] janela = lerJanela();
            double x = lerNumero(xCampo);
            double y = lerNumero(yCampo);
            int largura = tela.getWidth();
            int altura = tela.getHeight();
            if (largura < 2 || altura < 2) return;
            tela.limpar();
            if (x < janela[0] || x > janela[1] || y < janela[2] || y > janela[3]) {
                resultado.setText("Ponto fora da janela do mundo: nenhum pixel ativado.");
                return;
            }
            boolean centrado = cenario.getSelectedIndex() == 1;
            Ponto ndc = userToNdc(x, y, janela[0], janela[1], janela[2], janela[3], centrado);
            Pixel dc = ndcToDc(ndc.x, ndc.y, largura, altura, centrado);
            tela.drawPixel(dc.x, dc.y);
            resultado.setText(String.format(Locale.US,
                "Mundo=(%.4f, %.4f) | NDC=(%.4f, %.4f) | DC=(%d, %d) | Display=%d x %d | RGB=#00FF00",
                x, y, ndc.x, ndc.y, dc.x, dc.y, largura, altura));
        } catch (IllegalArgumentException erro) {
            tela.limpar();
            resultado.setText("Entrada invalida: " + erro.getMessage());
            if (mostrarErro) {
                JOptionPane.showMessageDialog(this, erro.getMessage(), "Entrada invalida",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /* Teste matematico sem interface: java TransformacoesCoordenadas --test */
    private static void testar() {
        for (boolean centrado : new boolean[] {false, true}) {
            Ponto ndc = userToNdc(10, 15, -10, 30, 5, 25, centrado);
            double esperado = centrado ? 0 : 0.5;
            if (Math.abs(ndc.x - esperado) > 1e-10
                    || Math.abs(ndc.y - esperado) > 1e-10) throw new AssertionError("NDC");
            Pixel pixel = ndcToDc(ndc.x, ndc.y, 801, 601, centrado);
            if (pixel.x != 400 || pixel.y != 300) throw new AssertionError("Dispositivo");
            Ponto clique = inpToNdc(pixel.x, pixel.y, 801, 601, centrado);
            Ponto mundo = ndcToUser(clique.x, clique.y, -10, 30, 5, 25, centrado);
            if (Math.abs(mundo.x - 10) > 1e-10
                    || Math.abs(mundo.y - 15) > 1e-10) throw new AssertionError("Inversa");
            Pixel inferiorEsquerdo = ndcToDc(centrado ? -1 : 0,
                                             centrado ? -1 : 0, 801, 601, centrado);
            Pixel superiorDireito = ndcToDc(1, 1, 801, 601, centrado);
            if (inferiorEsquerdo.x != 0 || inferiorEsquerdo.y != 600
                    || superiorDireito.x != 800 || superiorDireito.y != 0)
                throw new AssertionError("Extremos");
        }
        Tela telaTeste = new Tela();
        telaTeste.setSize(801, 601);
        telaTeste.limpar();
        telaTeste.drawPixel(400, 300);
        int ativos = 0;
        for (int y = 0; y < 601; y++) {
            for (int x = 0; x < 801; x++) {
                if ((telaTeste.imagem.getRGB(x, y) & 0xFFFFFF) != 0) {
                    ativos++;
                    if (x != 400 || y != 300
                            || (telaTeste.imagem.getRGB(x, y) & 0xFFFFFF) != 0x00FF00) {
                        throw new AssertionError("Pixel/coloracao");
                    }
                }
            }
        }
        if (ativos != 1) throw new AssertionError("Numero de pixels ativos: " + ativos);
        System.out.println("OK: ambos os cenarios, transformacoes inversas e limites.");
        System.out.println("OK: exatamente 1 pixel verde #00FF00 no frame buffer.");
    }

    public static void main(String[] args) {
        if (args.length > 0 && "--test".equals(args[0])) {
            testar();
        } else {
            SwingUtilities.invokeLater(() -> new TransformacoesCoordenadas().setVisible(true));
        }
    }
}
