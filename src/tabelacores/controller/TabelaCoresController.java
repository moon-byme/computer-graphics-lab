package tabelacores.controller;

import tabelacores.model.Paleta;
import tabelacores.view.TabelaCoresView;

import static tabelacores.util.Hexadecimal.lerHexadecimal;

/** Liga a View ao Model: trata eventos, valida entradas e atualiza a paleta. */
public class TabelaCoresController {
    private final Paleta paleta;
    private final TabelaCoresView view;

    private int pagina = 0;
    private int indiceSelecionado = 0x000;

    public TabelaCoresController(Paleta paleta, TabelaCoresView view) {
        this.paleta = paleta;
        this.view = view;

        view.addAmostraListener(posicao -> selecionar(pagina * Paleta.CORES_POR_PAGINA + posicao));

        view.addSelecionarListener(e -> {
            try {
                int indice = lerHexadecimal(view.getIndiceTexto(), 3, "Indice de 12 bits");
                selecionar(indice);
            } catch (IllegalArgumentException erro) {
                view.mostrarErro(erro.getMessage());
            }
        });

        view.addAlterarListener(e -> {
            try {
                int indice = lerHexadecimal(view.getIndiceTexto(), 3, "Indice de 12 bits");
                int rgb = lerHexadecimal(view.getRgbTexto(), 6, "Cor RGB de 24 bits");
                // Exigir os seis digitos evita ambiguidade entre #ABC e #000ABC.
                String limpa = view.getRgbTexto().trim().replaceFirst("^(?i:0x|#)", "");
                if (limpa.length() != 6) {
                    throw new IllegalArgumentException("A cor RGB deve ter exatamente 6 digitos (RRGGBB).");
                }
                paleta.setCor(indice, rgb);
                selecionar(indice);
            } catch (IllegalArgumentException erro) {
                view.mostrarErro(erro.getMessage());
            }
        });

        view.addRestaurarListener(e -> {
            if (view.confirmarRestauracao()) {
                paleta.restaurar();
                selecionar(indiceSelecionado);
            }
        });
        view.addAnteriorListener(e -> mudarPagina(pagina - 1));
        view.addProximaListener(e -> mudarPagina(pagina + 1));

        selecionar(indiceSelecionado);
    }

    private void mudarPagina(int novaPagina) {
        if (novaPagina >= 0 && novaPagina < Paleta.QUANTIDADE_PAGINAS) {
            pagina = novaPagina;
            selecionar(pagina * Paleta.CORES_POR_PAGINA);
        }
    }

    private void selecionar(int indice) {
        indiceSelecionado = indice;
        pagina = indice / Paleta.CORES_POR_PAGINA;
        view.exibirSelecao(paleta, indice, pagina);
    }
}
