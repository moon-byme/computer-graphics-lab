package transformacoes.view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/* Frame buffer raster, sem grade, ampliacao, circulo ou espessura artificial. */
public class Tela extends JPanel {
    private BufferedImage imagem;

    public Tela() { setBackground(Color.BLACK); }

    public BufferedImage getImagem() { return imagem; }

    public void limpar() {
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
    public void drawPixel(int x, int y) {
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
