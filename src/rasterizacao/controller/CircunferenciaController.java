package rasterizacao.controller;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import rasterizacao.model.AlgoritmoCircunferencia;
import rasterizacao.model.PlotadorPixel;

/*
 * Controller do desenho de circunferencias: valida a entrada, escolhe o algoritmo
 * e manda os pixels para o destino.
 *
 * Ainda nao ha View. Quem cria o Controller informa para onde vao os pixels
 * (um PlotadorPixel): nos testes e uma lista; na futura tela sera tela::drawPixel.
 */
public class CircunferenciaController {
    public static final int RAIO_MAXIMO = 10_000;

    private final PlotadorPixel destino;

    public CircunferenciaController(PlotadorPixel destino) {
        this.destino = Objects.requireNonNull(destino, "O destino dos pixels nao pode ser nulo.");
    }

    /*
     * Entrada em texto, como recebida de campos de uma interface.
     * Retorna quantos pixels foram ativados.
     */
    public int desenhar(String algoritmo, String xc, String yc, String raio) {
        return desenhar(lerAlgoritmo(algoritmo),
                lerInteiro(xc, "coordenada x do centro"),
                lerInteiro(yc, "coordenada y do centro"),
                lerInteiro(raio, "raio"));
    }

    // Entrada ja convertida. Retorna quantos pixels foram ativados.
    public int desenhar(AlgoritmoCircunferencia algoritmo, int xc, int yc, int raio) {
        if (algoritmo == null) {
            throw new IllegalArgumentException("Escolha um algoritmo de circunferencia.");
        }
        if (raio < 0 || raio > RAIO_MAXIMO) {
            throw new IllegalArgumentException(
                    "O raio deve ser um inteiro entre 0 e " + RAIO_MAXIMO + ".");
        }

        int[] quantidade = {0};
        algoritmo.desenhar(xc, yc, raio, (x, y) -> {
            destino.desenhar(x, y);
            quantidade[0]++;
        });
        return quantidade[0];
    }

    // Aceita "polinomial", "trigonometrico" e "ponto medio" (sem diferenciar maiusculas ou acentos).
    public static AlgoritmoCircunferencia lerAlgoritmo(String texto) {
        if (texto != null) {
            String chave = Normalizer.normalize(texto, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .replaceAll("[\\s_-]", "")
                    .toLowerCase(Locale.ROOT);
            switch (chave) {
                case "polinomial":     return AlgoritmoCircunferencia.POLINOMIAL;
                case "trigonometrico": return AlgoritmoCircunferencia.TRIGONOMETRICO;
                case "pontomedio":     return AlgoritmoCircunferencia.PONTO_MEDIO;
                default: break;
            }
        }
        throw new IllegalArgumentException(
                "Algoritmo desconhecido. Use: polinomial, trigonometrico ou ponto medio.");
    }

    private static int lerInteiro(String texto, String nome) {
        try {
            if (texto != null) return Integer.parseInt(texto.trim());
        } catch (NumberFormatException ignorada) {
        }
        throw new IllegalArgumentException("O campo '" + nome + "' deve ser um numero inteiro.");
    }
}