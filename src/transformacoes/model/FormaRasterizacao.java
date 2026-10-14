package transformacoes.model;

import java.util.Objects;

/** Entrada geometrica em coordenadas originais do dispositivo para um algoritmo raster. */
public final class FormaRasterizacao {
    public enum Tipo {
        PONTO("Ponto"),
        RETA("Reta"),
        CIRCUNFERENCIA("Circunferência");

        private final String nome;

        Tipo(String nome) { this.nome = nome; }

        @Override public String toString() { return nome; }
    }

    private final Tipo tipo;
    private final Pixel inicio;
    private final Pixel fim;
    private final Pixel centro;
    private final int raio;

    private FormaRasterizacao(Tipo tipo, Pixel inicio, Pixel fim, Pixel centro, int raio) {
        this.tipo = tipo;
        this.inicio = inicio;
        this.fim = fim;
        this.centro = centro;
        this.raio = raio;
    }

    public static FormaRasterizacao ponto(Pixel posicao) {
        return new FormaRasterizacao(Tipo.PONTO, null, null,
                                     Objects.requireNonNull(posicao), 0);
    }

    public static FormaRasterizacao reta(Pixel inicio, Pixel fim) {
        return new FormaRasterizacao(Tipo.RETA, Objects.requireNonNull(inicio),
                                     Objects.requireNonNull(fim), null, 0);
    }

    public static FormaRasterizacao circunferencia(Pixel centro, int raio) {
        if (raio < 0) {
            throw new IllegalArgumentException("O raio da circunferência não pode ser negativo.");
        }
        return new FormaRasterizacao(Tipo.CIRCUNFERENCIA, null, null,
                                     Objects.requireNonNull(centro), raio);
    }

    public Tipo getTipo() { return tipo; }

    public Pixel getInicio() { return inicio; }

    public Pixel getFim() { return fim; }

    public Pixel getCentro() { return centro; }

    public int getRaio() { return raio; }
}
