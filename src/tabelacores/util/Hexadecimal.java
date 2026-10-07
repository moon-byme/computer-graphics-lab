package tabelacores.util;

/** Leitura e validacao de valores hexadecimais digitados pelo usuario. */
public final class Hexadecimal {
    private Hexadecimal() { }

    public static int lerHexadecimal(String texto, int digitosMaximos, String nome) {
        String valor = texto.trim().replaceFirst("^(?i:0x|#)", "");
        if (valor.isEmpty() || valor.length() > digitosMaximos || !valor.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException(nome + " deve conter de 1 a "
                    + digitosMaximos + " digitos hexadecimais.");
        }
        return Integer.parseInt(valor, 16);
    }
}
