package transformacoes.teste;

import transformacoes.model.Pixel;
import transformacoes.model.Ponto;
import transformacoes.view.Tela;

import static transformacoes.model.TransformadorCoordenadas.*;

/* Teste matematico sem interface: java -cp out transformacoes.TransformacoesCoordenadas --test */
public final class TransformacoesCoordenadasTeste {
    private TransformacoesCoordenadasTeste() { }

    public static void testar() {
        for (boolean centrado : new boolean[] {false, true}) {
            Ponto ndc = userToNdc(10, 15, -10, 30, 5, 25, centrado);
            double esperado = centrado ? 0 : 0.5;
            if (Math.abs(ndc.x - esperado) > 1e-10
                    || Math.abs(ndc.y - esperado) > 1e-10) throw new AssertionError("NDC");
            Pixel pixel = ndcToDc(ndc.x, ndc.y, 801, 601, centrado);
            if (pixel.x != 400 || pixel.y != 300) throw new AssertionError("Dispositivo");
            Ponto clique = inpToNdc(pixel.x, pixel.y, 801, 601, centrado);
            Ponto mundo = ndcToUser(clique.x, clique.y, -10, 30, 5, 25, centrado);
            if (Math.abs(mundo.x - 10) > 1e-10
                    || Math.abs(mundo.y - 15) > 1e-10) throw new AssertionError("Inversa");
            Pixel inferiorEsquerdo = ndcToDc(centrado ? -1 : 0,
                                             centrado ? -1 : 0, 801, 601, centrado);
            Pixel superiorDireito = ndcToDc(1, 1, 801, 601, centrado);
            if (inferiorEsquerdo.x != 0 || inferiorEsquerdo.y != 600
                    || superiorDireito.x != 800 || superiorDireito.y != 0)
                throw new AssertionError("Extremos");
        }
        Tela telaTeste = new Tela();
        telaTeste.setSize(801, 601);
        telaTeste.limpar();
        telaTeste.drawPixel(400, 300);
        int ativos = 0;
        for (int y = 0; y < 601; y++) {
            for (int x = 0; x < 801; x++) {
                if ((telaTeste.getImagem().getRGB(x, y) & 0xFFFFFF) != 0) {
                    ativos++;
                    if (x != 400 || y != 300
                            || (telaTeste.getImagem().getRGB(x, y) & 0xFFFFFF) != 0x00FF00) {
                        throw new AssertionError("Pixel/coloracao");
                    }
                }
            }
        }
        if (ativos != 1) throw new AssertionError("Numero de pixels ativos: " + ativos);
        System.out.println("OK: ambos os cenarios, transformacoes inversas e limites.");
        System.out.println("OK: exatamente 1 pixel verde #00FF00 no frame buffer.");
    }
}
