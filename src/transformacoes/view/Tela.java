package transformacoes.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/*
 * Frame buffer raster com a resolucao escolhida para o display.
 * O buffer guarda apenas o fundo branco e, no maximo, UM pixel verde #00FF00,
 * sem grade, ampliacao, circulo, mira ou espessura artificial.
 */
public class Tela extends JPanel {
    public static final int COR_FUNDO = 0xFFFFFF;
    public static final int COR_PIXEL = 0x00FF00;

    private int largura;
    private int altura;
    private BufferedImage imagem;

    public Tela() {
        setBackground(Color.WHITE);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        setResolucao(640, 480);
    }

    public BufferedImage getImagem() { return imagem; }

    public int getLarguraDisplay() { return largura; }

    public int getAlturaDisplay() { return altura; }

    /* O componente passa a ter exatamente largura x altura: 1 pixel do buffer = 1 pixel na tela. */
    public void setResolucao(int largura, int altura) {
        if (largura < 2 || altura < 2) {
            throw new IllegalArgumentException("A resolucao minima do display e 2 x 2.");
        }
        this.largura = largura;
        this.altura = altura;
        Dimension tamanho = new Dimension(largura, altura);
        setPreferredSize(tamanho);
        setMinimumSize(tamanho);
        setMaximumSize(tamanho);
        limpar();
        revalidate();
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

    /* Ativa EXATAMENTE UMA posicao do frame buffer: verde RGB #00FF00. */
    public void drawPixel(int x, int y) {
        if (x >= 0 && y >= 0 && x < largura && y < altura) {
            imagem.setRGB(x, y, COR_PIXEL);
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Escala original: 1 ponto da imagem por pixel do componente, nada desenhado por cima.
        g.drawImage(imagem, 0, 0, null);
    }
}