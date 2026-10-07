package tabelacores.model;

/** Paleta de 4096 entradas (indice de 12 bits) com cores RGB de 24 bits. */
public class Paleta {
    public static final int QUANTIDADE_CORES = 4096;
    public static final int CORES_POR_PAGINA = 256;
    public static final int QUANTIDADE_PAGINAS = 16;

    private final int[] paleta = criarPaleta();

    public int getCor(int indice) {
        return paleta[indice];
    }

    public void setCor(int indice, int rgb) {
        paleta[indice] = rgb;
    }

    /** Restaura as 4096 entradas para a inicializacao RGB444 -> RGB888. */
    public void restaurar() {
        int[] padrao = criarPaleta();
        System.arraycopy(padrao, 0, paleta, 0, paleta.length);
    }

    private static int expandir4Para8(int valor) {
        return valor * 17;
    }

    public static int[] criarPaleta() {
        int[] tabela = new int[QUANTIDADE_CORES];
        for (int indice = 0; indice < QUANTIDADE_CORES; indice++) {
            int r4 = (indice >> 8) & 0xF;
            int g4 = (indice >> 4) & 0xF;
            int b4 = indice & 0xF;
            int r8 = expandir4Para8(r4);
            int g8 = expandir4Para8(g4);
            int b8 = expandir4Para8(b4);
            tabela[indice] = (r8 << 16) | (g8 << 8) | b8;
        }
        return tabela;
    }
}
