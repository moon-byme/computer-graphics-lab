package transformacoes;

import javax.swing.SwingUtilities;
import transformacoes.controller.TransformacoesController;
import transformacoes.teste.TransformacoesCoordenadasTeste;
import transformacoes.view.TransformacoesView;

/** Laboratorio I: transformacoes Mundo -> NDC -> Dispositivo. Ponto de entrada. */
public class TransformacoesCoordenadas {
    static void main(String[] args) {
        if (args.length > 0 && "--test".equals(args[0])) {
            TransformacoesCoordenadasTeste.testar();
        } else {
            SwingUtilities.invokeLater(() -> {
                TransformacoesView view = new TransformacoesView();
                new TransformacoesController(view);
                view.setVisible(true);
            });
        }
    }
}
