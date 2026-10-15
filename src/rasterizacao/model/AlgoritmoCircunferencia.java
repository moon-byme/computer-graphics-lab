package rasterizacao.model;

/* Os tres algoritmos de circunferencia, compartilhados pelos controllers e adaptadores. */
public enum AlgoritmoCircunferencia {
    POLINOMIAL("Polinomial") {
        @Override
        public void desenhar(int xc, int yc, int raio, PlotadorPixel plotador) {
            CircunferenciaPolinomial.desenhar(xc, yc, raio, plotador);
        }
    },
    TRIGONOMETRICO("Trigonometrico") {
        @Override
        public void desenhar(int xc, int yc, int raio, PlotadorPixel plotador) {
            CircunferenciaTrigonometrica.desenhar(xc, yc, raio, plotador);
        }
    },
    PONTO_MEDIO("Ponto medio") {
        @Override
        public void desenhar(int xc, int yc, int raio, PlotadorPixel plotador) {
            CircunferenciaPontoMedio.desenhar(xc, yc, raio, plotador);
        }
    };

    private final String nome;

    AlgoritmoCircunferencia(String nome) {
        this.nome = nome;
    }

    public abstract void desenhar(int xc, int yc, int raio, PlotadorPixel plotador);

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}