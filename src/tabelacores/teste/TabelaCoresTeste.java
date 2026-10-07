package tabelacores.teste;

import tabelacores.model.Paleta;

import java.util.HashSet;

import static tabelacores.util.Hexadecimal.lerHexadecimal;

/** Testes independentes da interface: java -cp out tabelacores.TabelaCores --test */
public final class TabelaCoresTeste {
    private TabelaCoresTeste() { }

    public static void testar() {
        int[] tabela = Paleta.criarPaleta();
        if (tabela.length != 4096 || tabela[0] != 0x000000 || tabela[0xFFF] != 0xFFFFFF
                || tabela[0xF00] != 0xFF0000 || tabela[0x0F0] != 0x00FF00
                || tabela[0x00F] != 0x0000FF) {
            throw new AssertionError("Paleta RGB444 -> RGB888 incorreta.");
        }
        HashSet<Integer> unicas = new HashSet<>();
        for (int cor : tabela) unicas.add(cor);
        if (unicas.size() != 4096) throw new AssertionError("Ha cores iniciais repetidas.");
        Paleta paleta = new Paleta();
        paleta.setCor(0x123, 0xABCDEF);
        if (paleta.getCor(0x123) != 0xABCDEF) {
            throw new AssertionError("Falha ao associar uma cor RGB de 24 bits ao indice.");
        }
        paleta.restaurar();
        if (paleta.getCor(0x123) != tabela[0x123]) {
            throw new AssertionError("Falha ao restaurar a paleta.");
        }
        if (lerHexadecimal("FFF", 3, "Indice") != 4095
                || lerHexadecimal("#00FF00", 6, "RGB") != 0x00FF00) {
            throw new AssertionError("Leitura hexadecimal incorreta.");
        }
        System.out.println("OK: 4096 entradas enderecadas por 12 bits (000..FFF).");
        System.out.println("OK: cores RGB de 24 bits, mapeamento RGB444 e edicao da paleta.");
    }
}
