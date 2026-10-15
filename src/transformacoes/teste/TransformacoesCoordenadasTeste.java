package transformacoes.teste;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import rasterizacao.integracao.AdaptadoresRasterizacao;
import transformacoes.model.AlgoritmoRasterizacao;
import transformacoes.model.FormaRasterizacao;
import transformacoes.model.Pixel;
import transformacoes.model.Ponto;
import transformacoes.model.TipoAlgoritmo;
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
        telaTeste.limpar();
        if ((telaTeste.getImagem().getRGB(400, 300) & 0xFFFFFF) != Tela.COR_FUNDO) {
            throw new AssertionError("Limpar tela deve restaurar o fundo branco");
        }
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
        if (TipoAlgoritmo.values().length != 5) {
            throw new AssertionError("Catalogo dos cinco algoritmos");
        }
        Map<TipoAlgoritmo, AlgoritmoRasterizacao> algoritmos = new EnumMap<>(TipoAlgoritmo.class);
        for (AlgoritmoRasterizacao algoritmo : AdaptadoresRasterizacao.criar()) {
            algoritmos.put(algoritmo.getTipo(), algoritmo);
        }
        if (algoritmos.size() != TipoAlgoritmo.values().length) {
            throw new AssertionError("Todos os algoritmos devem estar integrados");
        }
        Tela telaAlgoritmos = new Tela();
        telaAlgoritmos.setResolucao(32, 32);
        validarReta(algoritmos.get(TipoAlgoritmo.DDA_RETA), TipoAlgoritmo.DDA_RETA, telaAlgoritmos);
        validarReta(algoritmos.get(TipoAlgoritmo.BRESENHAM_RETA),
                    TipoAlgoritmo.BRESENHAM_RETA, telaAlgoritmos);
        validarCircunferencia(algoritmos.get(TipoAlgoritmo.CIRCUNFERENCIA_POLINOMIAL), telaAlgoritmos);
        validarCircunferencia(algoritmos.get(TipoAlgoritmo.CIRCUNFERENCIA_TRIGONOMETRICA), telaAlgoritmos);
        validarCircunferencia(algoritmos.get(TipoAlgoritmo.CIRCUNFERENCIA_PONTO_MEDIO), telaAlgoritmos);
        AlgoritmoRasterizacao algoritmoTeste = new AlgoritmoRasterizacao() {
            @Override public TipoAlgoritmo getTipo() { return TipoAlgoritmo.DDA_RETA; }
            @Override public List<Pixel> calcularPixels(FormaRasterizacao forma) {
                return List.of(forma.getInicio(), forma.getFim());
            }
        };
        List<Pixel> pixelsReta = algoritmoTeste.rasterizar(
                FormaRasterizacao.reta(new Pixel(1, 2), new Pixel(6, 4)));
        telaAmpliada.limpar();
        telaAmpliada.drawPixels(pixelsReta);
        if ((telaAmpliada.getImagem().getRGB(1, 2) & 0xFFFFFF) != Tela.COR_PIXEL
                || (telaAmpliada.getImagem().getRGB(6, 4) & 0xFFFFFF) != Tela.COR_PIXEL) {
            throw new AssertionError("Saida do algoritmo deve usar a renderizacao comum");
        }
        telaAmpliada.drawPixel(3, 3);
        FormaRasterizacao ponto = FormaRasterizacao.ponto(new Pixel(3, 3));
        if (ponto.getTipo() != FormaRasterizacao.Tipo.PONTO
                || (telaAmpliada.getImagem().getRGB(3, 3) & 0xFFFFFF) != Tela.COR_PIXEL) {
            throw new AssertionError("A primitiva ponto deve continuar usando drawPixel");
        }
        try {
            algoritmoTeste.rasterizar(FormaRasterizacao.circunferencia(new Pixel(3, 3), 2));
            throw new AssertionError("Algoritmo de reta nao deve aceitar circunferencia");
        } catch (IllegalArgumentException esperado) {
            // A interface comum rejeita formas incompatíveis antes da renderização.
        }
        System.out.println("OK: ambos os cenarios, transformacoes inversas e limites.");
        System.out.println("OK: exatamente 1 pixel verde #00FF00 no frame buffer.");
        System.out.println("OK: ampliacao, grade e cliques preservam as coordenadas DC originais.");
        System.out.println("OK: cinco algoritmos adaptados e conectados a saida rasterizada da tela.");
    }

    private static void validarReta(
            AlgoritmoRasterizacao algoritmo, TipoAlgoritmo tipo, Tela tela) {
        List<Pixel> pixels = algoritmo.rasterizar(
                FormaRasterizacao.reta(new Pixel(2, 3), new Pixel(8, 6)));
        if (pixels.size() != 7 || !contem(pixels, 2, 3) || !contem(pixels, 8, 6)) {
            throw new AssertionError("Adaptador da reta " + tipo);
        }
        tela.limpar();
        tela.drawPixels(pixels);
        verificarPixel(tela, 2, 3);
        verificarPixel(tela, 8, 6);
    }

    private static void validarCircunferencia(AlgoritmoRasterizacao algoritmo, Tela tela) {
        List<Pixel> pixels = algoritmo.rasterizar(
                FormaRasterizacao.circunferencia(new Pixel(10, 12), 5));
        if (pixels.size() < 8 || !contem(pixels, 10, 7) || !contem(pixels, 10, 17)
                || !contem(pixels, 5, 12) || !contem(pixels, 15, 12)) {
            throw new AssertionError("Adaptador de circunferencia " + algoritmo.getTipo());
        }
        tela.limpar();
        tela.drawPixels(pixels);
        verificarPixel(tela, 10, 7);
        verificarPixel(tela, 10, 17);
        verificarPixel(tela, 5, 12);
        verificarPixel(tela, 15, 12);
    }

    private static boolean contem(List<Pixel> pixels, int x, int y) {
        return pixels.stream().anyMatch(pixel -> pixel.x == x && pixel.y == y);
    }

    private static void verificarPixel(Tela tela, int x, int y) {
        if ((tela.getImagem().getRGB(x, y) & 0xFFFFFF) != Tela.COR_PIXEL) {
            throw new AssertionError("O algoritmo nao chegou a Tela em DC (" + x + ", " + y + ")");
        }
    }
}