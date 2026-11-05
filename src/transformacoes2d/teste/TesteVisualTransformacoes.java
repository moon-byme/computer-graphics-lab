
package transformacoes2d.teste;

import java.awt.*;
import javax.swing.*;
import transformacoes2d.model.MatrizesTransformacao;
import transformacoes2d.model.OperacoesMatriciais;
import transformacoes2d.model.PontoHomogeneo;


public class TesteVisualTransformacoes extends JPanel {

    private static final int ESCALA_TELA = 2;

    private final double[][] vertices = {
        {-80, -50},
        {80, -50},
        {80, 50},
        {-80, 50}
    };

    private double[][] matriz = {
        {1, 0, 0},
        {0, 1, 0},
        {0, 0, 1}
    };

    public TesteVisualTransformacoes() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(850, 600));
    }

    public void aplicarTransformacao(double[][] novaMatriz) {
        matriz = novaMatriz;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        int centroX = getWidth() / 2;
        int centroY = getHeight() / 2;

        desenharEixos(g2, centroX, centroY);

        // Polígono original em azul.
        desenharPoligono(g2, vertices, centroX, centroY, Color.BLUE);

        // Aplica a matriz aos vértices.
        double[][] transformados = new double[vertices.length][2];

        for (int i = 0; i < vertices.length; i++) {
            PontoHomogeneo ponto = new PontoHomogeneo(
                vertices[i][0], vertices[i][1], 1
            );

            PontoHomogeneo resultado =
                OperacoesMatriciais.multiplicarMatrizPorPonto(
                    matriz, ponto
                );

            PontoHomogeneo normalizado =
                new PontoHomogeneo(
                    resultado.x / resultado.w,
                    resultado.y / resultado.w,
                    1
                );

            transformados[i][0] = normalizado.x;
            transformados[i][1] = normalizado.y;
        }

        // Polígono transformado em verde.
        desenharPoligono(
            g2, transformados, centroX, centroY, Color.GREEN.darker()
        );

        g2.dispose();
    }

    private void desenharEixos(Graphics2D g2, int cx, int cy) {
        g2.setColor(Color.LIGHT_GRAY);

        g2.drawLine(0, cy, getWidth(), cy);
        g2.drawLine(cx, 0, cx, getHeight());

        g2.setColor(Color.BLACK);
        g2.drawString("X", getWidth() - 20, cy - 10);
        g2.drawString("Y", cx + 10, 20);
        g2.drawString("(0,0)", cx + 5, cy + 15);
    }

    private void desenharPoligono(
        Graphics2D g2,
        double[][] pontos,
        int cx,
        int cy,
        Color cor
    ) {
        Polygon poligono = new Polygon();

        for (double[] ponto : pontos) {
            int x = cx + (int) Math.round(
                ponto[0] * ESCALA_TELA
            );

            int y = cy - (int) Math.round(
                ponto[1] * ESCALA_TELA
            );

            poligono.addPoint(x, y);
        }

        g2.setColor(cor);
        g2.setStroke(new BasicStroke(3));
        g2.drawPolygon(poligono);

        for (int i = 0; i < poligono.npoints; i++) {
            g2.fillOval(
                poligono.xpoints[i] - 4,
                poligono.ypoints[i] - 4,
                8, 8
            );
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            TesteVisualTransformacoes painel =
                new TesteVisualTransformacoes();

            String[] opcoes = {
                "Identidade",
                "Translação",
                "Escala",
                "Rotação 45°",
                "Reflexão eixo X",
                "Reflexão eixo Y",
                "Reflexão origem",
                "Reflexão Y = X",
                "Cisalhamento X",
                "Cisalhamento Y"
            };

            JComboBox<String> seletor =
                new JComboBox<>(opcoes);

            seletor.addActionListener(e -> {

                String escolha = (String) seletor.getSelectedItem();

                double[][] m = switch (escolha) {
                    case "Translação" ->
                        MatrizesTransformacao.translacao(100, 50);

                    case "Escala" ->
                        MatrizesTransformacao.escala(1.5, 0.7);

                    case "Rotação 45°" ->
                        MatrizesTransformacao.rotacao(45);

                    case "Reflexão eixo X" ->
                        MatrizesTransformacao.reflexaoEixoX();

                    case "Reflexão eixo Y" ->
                        MatrizesTransformacao.reflexaoEixoY();

                    case "Reflexão origem" ->
                        MatrizesTransformacao.reflexaoOrigem();

                    case "Reflexão Y = X" ->
                        MatrizesTransformacao.reflexaoRetaYX();

                    case "Cisalhamento X" ->
                        MatrizesTransformacao.cisalhamentoX(0.7);

                    case "Cisalhamento Y" ->
                        MatrizesTransformacao.cisalhamentoY(0.7);

                    default -> new double[][] {
                        {1, 0, 0},
                        {0, 1, 0},
                        {0, 0, 1}
                    };
                };

                painel.aplicarTransformacao(m);
            });

            JFrame janela = new JFrame(
                "Teste visual - Transformações Geométricas 2D"
            );

            janela.setLayout(new BorderLayout());

            JPanel topo = new JPanel();
            topo.add(new JLabel("Escolha a transformação:"));
            topo.add(seletor);

            janela.add(topo, BorderLayout.NORTH);
            janela.add(painel, BorderLayout.CENTER);

            JLabel legenda = new JLabel(
                "Azul: original | Verde: transformado",
                SwingConstants.CENTER
            );

            janela.add(legenda, BorderLayout.SOUTH);

            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.pack();
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);
        });
    }
}
