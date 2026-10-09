package transformacoes.controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import javax.swing.*;
import transformacoes.model.Pixel;
import transformacoes.model.Ponto;
import transformacoes.model.TransformadorCoordenadas;
import transformacoes.view.TransformacoesView;

/** Liga a View ao Model: trata eventos, valida entradas e coordena as transformacoes. */
public class TransformacoesController {
    private static final int RESOLUCAO_MINIMA = 2;
    private static final int RESOLUCAO_MAXIMA = 4096;
    private static final String VAZIO = "\u2014";

    private final TransformacoesView view;

    public TransformacoesController(TransformacoesView view) {
        this.view = view;

        view.addMostrarListener(e -> atualizar(true));
        view.addCenarioListener(e -> {
            view.limparInversa();
            atualizar(false);
        });
        view.addResolucaoListener(e -> aplicarResolucao());
        view.addTelaMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { transformarClique(e.getX(), e.getY()); }
        });
        SwingUtilities.invokeLater(() -> atualizar(false));
    }

    private static double lerNumero(String texto) {
        try {
            double numero = Double.parseDouble(texto.trim().replace(',', '.'));
            if (!Double.isFinite(numero)) throw new NumberFormatException();
            return numero;
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("Informe apenas números reais finitos.");
        }
    }

    private static int lerDimensao(String texto, String nome) {
        try {
            int valor = Integer.parseInt(texto.trim());
            if (valor >= RESOLUCAO_MINIMA && valor <= RESOLUCAO_MAXIMA) return valor;
        } catch (NumberFormatException ignorada) {
            // tratada abaixo com a mesma mensagem
        }
        throw new IllegalArgumentException(String.format(
                "A %s do display deve ser um número inteiro entre %d e %d.",
                nome, RESOLUCAO_MINIMA, RESOLUCAO_MAXIMA));
    }

    private static String formatar(double valor) {
        return String.format(Locale.US, "%.4f", valor);
    }

    private static String rotuloNdc(boolean centrado) {
        return centrado ? "NDC [-1, 1]" : "NDC [0, 1]";
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

    /* Resolucao pre-definida ou personalizada -> novo frame buffer -> recalcula o pixel. */
    private void aplicarResolucao() {
        boolean personalizada = view.isResolucaoPersonalizada();
        view.setCamposPersonalizadosAtivos(personalizada);
        try {
            int largura;
            int altura;
            if (personalizada) {
                largura = lerDimensao(view.getLarguraPersonalizadaTexto(), "largura");
                altura = lerDimensao(view.getAlturaPersonalizadaTexto(), "altura");
            } else {
                int[] resolucao = view.getResolucaoPredefinida();
                largura = resolucao[0];
                altura = resolucao[1];
            }
            view.setResolucaoDisplay(largura, altura);
            view.limparInversa();
            atualizar(false);
        } catch (IllegalArgumentException erro) {
            view.setStatus("Resolução inválida: " + erro.getMessage(), true);
            view.mostrarErro(erro.getMessage());
        }
    }

    /* Transformacao inversa: dispositivo de entrada (mouse) -> NDC -> mundo. */
    private void transformarClique(int dx, int dy) {
        int largura = view.getLarguraTela();
        int altura = view.getAlturaTela();
        if (dx < 0 || dy < 0 || dx >= largura || dy >= altura) return;
        try {
            double[] mundo = lerJanela();
            boolean centrado = view.isCentrado();
            Ponto ndc = TransformadorCoordenadas.inpToNdc(dx, dy, largura, altura, centrado);
            Ponto ponto = TransformadorCoordenadas.ndcToUser(ndc.x, ndc.y, mundo[0], mundo[1],
                                                             mundo[2], mundo[3], centrado);
            view.setResultadosInversa(new String[][] {
                {"Clique (mouse)", String.valueOf(dx), String.valueOf(dy)},
                {rotuloNdc(centrado), formatar(ndc.x), formatar(ndc.y)},
                {"Mundo", formatar(ponto.x), formatar(ponto.y)}
            });
            view.setXTexto(formatar(ponto.x));
            view.setYTexto(formatar(ponto.y));
            atualizar(false);
        } catch (IllegalArgumentException erro) {
            view.mostrarErro(erro.getMessage());
        }
    }

    /* Transformacao direta: mundo -> NDC -> dispositivo, ativando um unico pixel. */
    private void atualizar(boolean mostrarErro) {
        try {
            double[] janela = lerJanela();
            double x = lerNumero(view.getXTexto());
            double y = lerNumero(view.getYTexto());
            int largura = view.getLarguraTela();
            int altura = view.getAlturaTela();
            boolean centrado = view.isCentrado();
            view.limparTela();
            if (x < janela[0] || x > janela[1] || y < janela[2] || y > janela[3]) {
                view.setResultados(new String[][] {
                    {"Mundo", formatar(x), formatar(y)},
                    {rotuloNdc(centrado), VAZIO, VAZIO},
                    {"Dispositivo (DC)", VAZIO, VAZIO}
                });
                view.setStatus("Ponto fora da janela do mundo: nenhum pixel ativado.", true);
                return;
            }
            Ponto ndc = TransformadorCoordenadas.userToNdc(x, y, janela[0], janela[1],
                                                           janela[2], janela[3], centrado);
            Pixel dc = TransformadorCoordenadas.ndcToDc(ndc.x, ndc.y, largura, altura, centrado);
            view.drawPixel(dc.x, dc.y);
            view.setResultados(new String[][] {
                {"Mundo", formatar(x), formatar(y)},
                {rotuloNdc(centrado), formatar(ndc.x), formatar(ndc.y)},
                {"Dispositivo (DC)", String.valueOf(dc.x), String.valueOf(dc.y)}
            });
            view.setStatus(String.format("Pixel #00FF00 ativado em DC (%d, %d) num display de %d × %d.",
                                         dc.x, dc.y, largura, altura), false);
        } catch (IllegalArgumentException erro) {
            view.limparTela();
            view.limparResultados();
            view.setStatus("Entrada inválida: " + erro.getMessage(), true);
            if (mostrarErro) {
                view.mostrarErro(erro.getMessage());
            }
        }
    }
}