package transformacoes.controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import rasterizacao.integracao.AdaptadoresRasterizacao;
import transformacoes.model.AlgoritmoRasterizacao;
import transformacoes.model.FormaRasterizacao;
import transformacoes.model.Pixel;
import transformacoes.model.Ponto;
import transformacoes.model.TipoAlgoritmo;
import transformacoes.model.TransformadorCoordenadas;
import transformacoes.view.TransformacoesView;

/** Liga a View ao Model: trata eventos, valida entradas e coordena as transformacoes */
public class TransformacoesController {
    private static final int RESOLUCAO_MINIMA = 2;
    private static final int RESOLUCAO_MAXIMA = 4096;
    private static final String VAZIO = "\u2014";

    private final TransformacoesView view;
    private final Map<TipoAlgoritmo, AlgoritmoRasterizacao> algoritmos =
            new EnumMap<>(TipoAlgoritmo.class);

    public TransformacoesController(TransformacoesView view) {
        this.view = view;
        for (AlgoritmoRasterizacao algoritmo : AdaptadoresRasterizacao.criar()) {
            registrarAlgoritmo(algoritmo);
        }

        view.addMostrarListener(e -> atualizar(true));
        view.addVisualizacaoListener(e -> aplicarVisualizacao());
        view.addAlgoritmoListener(e -> selecionarAlgoritmo());
        view.addFormaListener(e -> selecionarForma());
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

    private void aplicarVisualizacao() {
        view.setVisualizacaoDisplay(view.getAmpliacao(), view.isGradeAtiva());
    }

    private void selecionarAlgoritmo() {
        TipoAlgoritmo algoritmo = view.getAlgoritmoSelecionado();
        if (algoritmo != null) {
            view.setStatus("Selecionado: " + algoritmo + ".", false);
        }
    }

    private void selecionarForma() {
        FormaRasterizacao.Tipo tipo = view.getTipoFormaSelecionado();
        view.setAlgoritmosParaForma(tipo);
        view.setStatus(tipo == FormaRasterizacao.Tipo.PONTO
                ? "Ponto pronto para desenhar."
                : "Forma selecionada. Informe os dados e escolha um algoritmo.", false);
    }

    public void registrarAlgoritmo(AlgoritmoRasterizacao algoritmo) {
        if (algoritmo == null) {
            throw new IllegalArgumentException("O algoritmo nao pode ser nulo.");
        }
        if (algoritmo.getTipo() == null) {
            throw new IllegalArgumentException("O algoritmo deve informar seu tipo.");
        }
        algoritmos.put(algoritmo.getTipo(), algoritmo);
    }

    private static int lerRaio(String texto) {
        try {
            int raio = Integer.parseInt(texto.trim());
            if (raio >= 0 && raio <= RESOLUCAO_MAXIMA) return raio;
        } catch (NumberFormatException ignorada) {
            // tratada abaixo com a mesma mensagem
        }
        throw new IllegalArgumentException("O raio deve ser um inteiro entre 0 e "
                + RESOLUCAO_MAXIMA + " pixels DC.");
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

    private static Pixel converterParaDc(double x, double y, double[] janela, int largura,
                                         int altura, boolean centrado) {
        Ponto ndc = TransformadorCoordenadas.userToNdc(x, y, janela[0], janela[1],
                                                       janela[2], janela[3], centrado);
        return TransformadorCoordenadas.ndcToDc(ndc.x, ndc.y, largura, altura, centrado);
    }

    private static boolean dentroDaJanela(double x, double y, double[] janela) {
        return x >= janela[0] && x <= janela[1] && y >= janela[2] && y <= janela[3];
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

    /* Resolucao pre-definida ou personalizada -> novo frame -> recalcula o pixel */
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
        if (dx < 0 || dy < 0 || dx >= view.getLarguraVisualizacao()
                || dy >= view.getAlturaVisualizacao()) return;
        Pixel dispositivo = view.toDisplayPixel(dx, dy);
        int largura = view.getLarguraTela();
        int altura = view.getAlturaTela();
        try {
            double[] mundo = lerJanela();
            boolean centrado = view.isCentrado();
            Ponto ndc = TransformadorCoordenadas.inpToNdc(dispositivo.x, dispositivo.y,
                                                          largura, altura, centrado);
            Ponto ponto = TransformadorCoordenadas.ndcToUser(ndc.x, ndc.y, mundo[0], mundo[1],
                                                             mundo[2], mundo[3], centrado);
            view.setResultadosInversa(new String[][] {
                {"Clique (mouse)", String.valueOf(dispositivo.x), String.valueOf(dispositivo.y)},
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

    /* Transformacao direta: mundo -> NDC -> dispositivo -> rasterizador -> framebuffer. */
    private void atualizar(boolean mostrarErro) {
        try {
            double[] janela = lerJanela();
            double x = lerNumero(view.getXTexto());
            double y = lerNumero(view.getYTexto());
            int largura = view.getLarguraTela();
            int altura = view.getAlturaTela();
            boolean centrado = view.isCentrado();
            view.limparTela();
            if (!dentroDaJanela(x, y, janela)) {
                throw new IllegalArgumentException("A coordenada inicial está fora da janela do mundo.");
            }
            Pixel inicio = converterParaDc(x, y, janela, largura, altura, centrado);
            FormaRasterizacao.Tipo tipo = view.getTipoFormaSelecionado();
            if (tipo == FormaRasterizacao.Tipo.PONTO) {
                Ponto ndc = TransformadorCoordenadas.userToNdc(x, y, janela[0], janela[1],
                                                               janela[2], janela[3], centrado);
                view.drawPixel(inicio.x, inicio.y);
                view.setResultados(new String[][] {
                    {"Mundo", formatar(x), formatar(y)},
                    {rotuloNdc(centrado), formatar(ndc.x), formatar(ndc.y)},
                    {"Dispositivo (DC)", String.valueOf(inicio.x), String.valueOf(inicio.y)}
                });
                view.setStatus(String.format("Pixel #00FF00 ativado em DC (%d, %d) num display de %d × %d.",
                                             inicio.x, inicio.y, largura, altura), false);
                return;
            }
            FormaRasterizacao forma;
            String descricao;
            if (tipo == FormaRasterizacao.Tipo.RETA) {
                double x2 = lerNumero(view.getX2Texto());
                double y2 = lerNumero(view.getY2Texto());
                if (!dentroDaJanela(x2, y2, janela)) {
                    throw new IllegalArgumentException("A coordenada final está fora da janela do mundo.");
                }
                Pixel fim = converterParaDc(x2, y2, janela, largura, altura, centrado);
                forma = FormaRasterizacao.reta(inicio, fim);
                descricao = String.format("Reta DC (%d, %d) até (%d, %d)",
                                          inicio.x, inicio.y, fim.x, fim.y);
            } else {
                int raio = lerRaio(view.getRaioTexto());
                forma = FormaRasterizacao.circunferencia(inicio, raio);
                descricao = String.format("Circunferência DC centro (%d, %d), raio %d px",
                                          inicio.x, inicio.y, raio);
            }
            TipoAlgoritmo tipoAlgoritmo = view.getAlgoritmoSelecionado();
            if (tipoAlgoritmo == null) {
                throw new IllegalArgumentException("Selecione um algoritmo para essa primitiva.");
            }
            AlgoritmoRasterizacao algoritmo = algoritmos.get(tipoAlgoritmo);
            if (algoritmo == null) {
                view.setResultados(new String[][] {
                    {"Mundo (início/centro)", formatar(x), formatar(y)},
                    {"Primitiva", tipo.toString(), "DC (" + inicio.x + ", " + inicio.y + ")"},
                    {"Algoritmo", tipoAlgoritmo.toString(), "pendente"}
                });
                view.setStatus("O algoritmo selecionado ainda não foi registrado; nenhum pixel foi desenhado.",
                               true);
                return;
            }
            List<Pixel> pixels = algoritmo.rasterizar(forma);
            view.drawPixels(pixels);
            view.setResultados(new String[][] {
                {"Mundo", formatar(x), formatar(y)},
                {"Primitiva", tipo.toString(), descricao},
                {"Pixels desenhados", String.valueOf(pixels.size()), "Algoritmo: " + tipoAlgoritmo}
            });
            view.setStatus(descricao + " desenhada com " + tipoAlgoritmo + ".", false);
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