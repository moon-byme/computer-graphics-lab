package transformacoes.model;

/** Algoritmos selecionaveis; as implementacoes serao adicionadas separadamente. */
public enum TipoAlgoritmo {
    DDA_RETA("Reta — DDA", FormaRasterizacao.Tipo.RETA),
    BRESENHAM_RETA("Reta — Bresenham", FormaRasterizacao.Tipo.RETA),
    CIRCUNFERENCIA_POLINOMIAL("Circunferência — Polinomial", FormaRasterizacao.Tipo.CIRCUNFERENCIA),
    CIRCUNFERENCIA_TRIGONOMETRICA("Circunferência — Trigonométrico",
                                  FormaRasterizacao.Tipo.CIRCUNFERENCIA),
    CIRCUNFERENCIA_PONTO_MEDIO("Circunferência — Ponto médio",
                               FormaRasterizacao.Tipo.CIRCUNFERENCIA);

    private final String nome;
    private final FormaRasterizacao.Tipo tipoForma;

    TipoAlgoritmo(String nome, FormaRasterizacao.Tipo tipoForma) {
        this.nome = nome;
        this.tipoForma = tipoForma;
    }

    public FormaRasterizacao.Tipo getTipoForma() { return tipoForma; }

    @Override
    public String toString() { return nome; }
}
