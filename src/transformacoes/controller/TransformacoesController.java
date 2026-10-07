package transformacoes.controller;

import transformacoes.model.Pixel;
import transformacoes.model.Ponto;
import transformacoes.model.TransformadorCoordenadas;
import transformacoes.view.TransformacoesView;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

/** Liga a View ao Model: trata eventos, valida entradas e coordena as transformacoes. */
public class TransformacoesController {
    private final TransformacoesView view;

    public TransformacoesController(TransformacoesView view) {
        this.view = view;

        view.addMostrarListener(e -> atualizar(true));
        view.addCenarioListener(e -> atualizar(false));
        view.addTelaComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { atualizar(false); }
        });
        view.addTelaMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                try {
                    double[] mundo = lerJanela();
                    if (view.getLarguraTela() < 2 || view.getAlturaTela() < 2) return;
                    boolean centrado = view.isCentrado();
                    Ponto ndc = TransformadorCoordenadas.inpToNdc(e.getX(), e.getY(),
                            view.getLarguraTela(), view.getAlturaTela(), centrado);
                    Ponto ponto = TransformadorCoordenadas.ndcToUser(ndc.x, ndc.y, mundo[0], mundo[1],
                                            mundo[2], mundo[3], centrado);
                    view.setXTexto(String.format(Locale.US, "%.4f", ponto.x));
                    view.setYTexto(String.format(Locale.US, "%.4f", ponto.y));
                    atualizar(false);
                } catch (IllegalArgumentException erro) {
                    view.mostrarErro(erro.getMessage());
                }
            }
        });
        SwingUtilities.invokeLater(() -> atualizar(false));
    }

    private static double lerNumero(String texto) {
        try {
            double numero = Double.parseDouble(texto.trim().replace(',', '.'));
            if (!Double.isFinite(numero)) throw new NumberFormatException();
            return numero;
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("Informe apenas numeros reais finitos.");
        }
    }

    private double[] lerJanela() {
        double xmin = lerNumero(view.getXminTexto());
        double xmax = lerNumero(view.getXmaxTexto());
        double ymin = lerNumero(view.getYminTexto());
        double ymax = lerNumero(view.getYmaxTexto());
        if (!(xmin < xmax && ymin < ymax)) {
            throw new IllegalArgumentException("Exige-se xmin < xmax e ymin < ymax.");
        }
        return new double[] {xmin, xmax, ymin, ymax};
    }

    private void atualizar(boolean mostrarErro) {
        try {
            double[] janela = lerJanela();
            double x = lerNumero(view.getXTexto());
            double y = lerNumero(view.getYTexto());
            int largura = view.getLarguraTela();
            int altura = view.getAlturaTela();
            if (largura < 2 || altura < 2) return;
            view.limparTela();
            if (x < janela[0] || x > janela[1] || y < janela[2] || y > janela[3]) {
                view.setResultado("Ponto fora da janela do mundo: nenhum pixel ativado.");
                return;
            }
            boolean centrado = view.isCentrado();
            Ponto ndc = TransformadorCoordenadas.userToNdc(x, y, janela[0], janela[1], janela[2], janela[3], centrado);
            Pixel dc = TransformadorCoordenadas.ndcToDc(ndc.x, ndc.y, largura, altura, centrado);
            view.drawPixel(dc.x, dc.y);
            view.setResultado(String.format(Locale.US,
                "Mundo=(%.4f, %.4f) | NDC=(%.4f, %.4f) | DC=(%d, %d) | Display=%d x %d | RGB=#00FF00",
                x, y, ndc.x, ndc.y, dc.x, dc.y, largura, altura));
        } catch (IllegalArgumentException erro) {
            view.limparTela();
            view.setResultado("Entrada invalida: " + erro.getMessage());
            if (mostrarErro) {
                view.mostrarErro(erro.getMessage());
            }
        }
    }
}
