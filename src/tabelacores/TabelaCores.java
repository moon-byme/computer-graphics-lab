package tabelacores;

import javax.swing.SwingUtilities;
import tabelacores.controller.TabelaCoresController;
import tabelacores.model.Paleta;
import tabelacores.teste.TabelaCoresTeste;
import tabelacores.view.TabelaCoresView;

/** Exercicio complementar G2: tabela de cores. Ponto de entrada. */
public class TabelaCores {
    static void main(String[] args) {
        if (args.length == 1 && "--test".equals(args[0])) {
            TabelaCoresTeste.testar();
        } else {
            SwingUtilities.invokeLater(() -> {
                TabelaCoresView view = new TabelaCoresView();
                new TabelaCoresController(new Paleta(), view);
                view.setVisible(true);
            });
        }
    }
}
