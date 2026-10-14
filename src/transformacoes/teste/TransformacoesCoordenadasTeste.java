package transformacoes.teste;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import transformacoes.model.Pixel;
import transformacoes.model.Ponto;
import static transformacoes.model.TransformadorCoordenadas.*;
import transformacoes.view.Tela;

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
        telaTeste.setResolucao(801, 601);
        telaTeste.drawPixel(400, 300);
        if (telaTeste.getImagem().getWidth() != 801 || telaTeste.getImagem().getHeight() != 601)
            throw new AssertionError("Resolucao do frame buffer");
        int ativos = 0;
        for (int y = 0; y < 601; y++) {
            for (int x = 0; x < 801; x++) {
                int rgb = telaTeste.getImagem().getRGB(x, y) & 0xFFFFFF;
                if (rgb != Tela.COR_FUNDO) {
                    ativos++;
                    if (x != 400 || y != 300 || rgb != Tela.COR_PIXEL) {
                        throw new AssertionError("Pixel/coloracao");
                    }
                }
            }
        }
        if (ativos != 1) throw new AssertionError("Numero de pixels ativos: " + ativos);
        Tela telaAmpliada = new Tela();
        telaAmpliada.setResolucao(8, 6);
        telaAmpliada.drawPixel(4, 3);
        telaAmpliada.setVisualizacao(4, true);
        if (telaAmpliada.getImagem().getWidth() != 8
                || telaAmpliada.getImagem().getHeight() != 6
                || telaAmpliada.getPreferredSize().width != 32
                || telaAmpliada.getPreferredSize().height != 24) {
            throw new AssertionError("A ampliacao nao deve alterar o frame buffer original");
        }
        Pixel cliqueAmpliado = telaAmpliada.toDisplayPixel(17, 13);
        Pixel cantoInferiorDireito = telaAmpliada.toDisplayPixel(31, 23);
        if (cliqueAmpliado.x != 4 || cliqueAmpliado.y != 3
                || cantoInferiorDireito.x != 7 || cantoInferiorDireito.y != 5) {
            throw new AssertionError("Conversao do clique ampliado para coordenadas DC");
        }
        BufferedImage visualizacao = new BufferedImage(32, 24, BufferedImage.TYPE_INT_RGB);
        telaAmpliada.setSize(32, 24);
        Graphics2D graficos = visualizacao.createGraphics();
        telaAmpliada.paint(graficos);
        graficos.dispose();
        if ((visualizacao.getRGB(17, 13) & 0xFFFFFF) != Tela.COR_PIXEL
                || (visualizacao.getRGB(4, 1) & 0xFFFFFF) == Tela.COR_FUNDO
                || (telaAmpliada.getImagem().getRGB(1, 0) & 0xFFFFFF) != Tela.COR_FUNDO) {
            throw new AssertionError("Ampliacao/grade deve ser somente visualizacao");
        }
        telaAmpliada.setVisualizacao(4, false);
        graficos = visualizacao.createGraphics();
        telaAmpliada.paint(graficos);
        graficos.dispose();
        if ((visualizacao.getRGB(4, 1) & 0xFFFFFF) != Tela.COR_FUNDO) {
            throw new AssertionError("A grade deve poder ser desativada");
        }
        System.out.println("OK: ambos os cenarios, transformacoes inversas e limites.");
        System.out.println("OK: exatamente 1 pixel verde #00FF00 no frame buffer.");
        System.out.println("OK: ampliacao, grade e cliques preservam as coordenadas DC originais.");
    }
}