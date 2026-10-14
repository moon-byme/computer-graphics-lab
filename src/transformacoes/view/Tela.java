package transformacoes.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import transformacoes.model.Pixel;

/* Frame buffer na resolucao original; ampliacao e grade existem apenas na pintura. */
public class Tela extends JPanel {
    public static final int COR_FUNDO = 0xFFFFFF;
    public static final int COR_PIXEL = 0x00FF00;

    private int largura;
    private int altura;
    private int escala = 1;
    private boolean gradeAtiva;
    private BufferedImage imagem;

    public Tela() {
        setBackground(Color.WHITE);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        setResolucao(640, 480);
    }

    public BufferedImage getImagem() { return imagem; }

    public int getLarguraDisplay() { return largura; }

    public int getAlturaDisplay() { return altura; }

    public int getEscala() { return escala; }

    public int getLarguraVisualizacao() { return largura * escala; }

    public int getAlturaVisualizacao() { return altura * escala; }

    /* A resolucao do buffer permanece independente da escala de visualizacao. */
    public void setResolucao(int largura, int altura) {
        if (largura < 2 || altura < 2) {
            throw new IllegalArgumentException("A resolucao minima do display e 2 x 2.");
        }
        this.largura = largura;
        this.altura = altura;
        atualizarTamanhoVisualizacao();
        limpar();
        revalidate();
    }

    public void setVisualizacao(int escala, boolean gradeAtiva) {
        if (escala != 1 && escala != 2 && escala != 4 && escala != 8) {
            throw new IllegalArgumentException("A ampliacao deve ser 1x, 2x, 4x ou 8x.");
        }
        this.escala = escala;
        this.gradeAtiva = gradeAtiva;
        atualizarTamanhoVisualizacao();
        revalidate();
        repaint();
    }

    private void atualizarTamanhoVisualizacao() {
        Dimension tamanho = new Dimension(getLarguraVisualizacao(), getAlturaVisualizacao());
        setPreferredSize(tamanho);
        setMinimumSize(tamanho);
        setMaximumSize(tamanho);
    }

    public Pixel toDisplayPixel(int x, int y) {
        if (x < 0 || y < 0 || x >= getLarguraVisualizacao() || y >= getAlturaVisualizacao()) {
            throw new IllegalArgumentException("O ponto está fora da área visualizada.");
        }
        return new Pixel(x / escala, y / escala);
    }

    /* Recria o frame buffer com todos os pixels na cor de fundo (branco). */
    public void limpar() {
        imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagem.createGraphics();
        g.setColor(new Color(COR_FUNDO));
        g.fillRect(0, 0, largura, altura);
        g.dispose();
        repaint();
    }

    /* Ativa uma posicao do frame buffer, mantendo drawPixel como atalho para pontos. */
    public void drawPixel(int x, int y) {
        drawPixels(List.of(new Pixel(x, y)));
    }

    public void drawPixels(Iterable<Pixel> pixels) {
        Objects.requireNonNull(pixels, "A colecao de pixels e obrigatoria.");
        for (Pixel pixel : pixels) {
            Objects.requireNonNull(pixel, "A colecao nao pode conter pixels nulos.");
            if (pixel.x >= 0 && pixel.y >= 0 && pixel.x < largura && pixel.y < altura) {
                imagem.setRGB(pixel.x, pixel.y, COR_PIXEL);
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(imagem, 0, 0, getLarguraVisualizacao(), getAlturaVisualizacao(), null);
        if (gradeAtiva && escala > 1) {
            g2.setColor(new Color(0, 0, 0, 80));
            for (int x = escala; x < getLarguraVisualizacao(); x += escala) {
                g2.drawLine(x, 0, x, getAlturaVisualizacao());
            }
            for (int y = escala; y < getAlturaVisualizacao(); y += escala) {
                g2.drawLine(0, y, getLarguraVisualizacao(), y);
            }
        }
        g2.dispose();
    }
}