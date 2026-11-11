package transformacoes2d.teste;

import java.util.ArrayList;
import java.util.List;
import transformacoes.model.Pixel;
import transformacoes.model.Poligono;
import transformacoes.model.Ponto;
import transformacoes.view.Tela;
import transformacoes2d.integracao.AplicadorTransformacoesPoligono;
import transformacoes2d.model.PontoHomogeneo;

/** Testes de integração entre o polígono homogêneo, rasterizador e tela. */
public final class AplicadorTransformacoesPoligonoTeste {
    private AplicadorTransformacoesPoligonoTeste() { }

    public static void main(String[] args) {
        Tela tela = new Tela();
        tela.setResolucao(16, 16);
        AplicadorTransformacoesPoligono aplicador = new AplicadorTransformacoesPoligono(
                tela, ponto -> new Pixel((int) Math.round(ponto.x), (int) Math.round(ponto.y)));

        esperarExcecao(IllegalStateException.class, aplicador::atualizarExibicao,
                "Não deve atualizar a tela antes de inicializar o polígono.");

        List<Ponto> verticesRecebidos = new ArrayList<>(List.of(
                new Ponto(2, 2), new Ponto(8, 2), new Ponto(2, 8)));
        aplicador.inicializarPoligono(new Poligono(verticesRecebidos));
        verticesRecebidos.clear();

        List<PontoHomogeneo> esperado = List.of(
                new PontoHomogeneo(2, 2, 1),
                new PontoHomogeneo(8, 2, 1),
                new PontoHomogeneo(2, 8, 1));
        exigir(mesmosVertices(aplicador.getVerticesOriginais(), esperado),
                "Os vértices originais devem ser copiados em coordenadas homogêneas.");
        exigir(mesmosVertices(aplicador.getVerticesAtuais(), esperado),
                "Os vértices atuais devem iniciar como cópia do polígono original.");

        esperarExcecao(UnsupportedOperationException.class,
                () -> aplicador.getVerticesOriginais().clear(),
                "A lista original não deve poder ser alterada externamente.");
        esperarExcecao(UnsupportedOperationException.class,
                () -> aplicador.getVerticesAtuais().clear(),
                "A lista atual não deve poder ser alterada externamente.");

        aplicador.atualizarExibicao();
        exigir((tela.getImagem().getRGB(4, 2) & 0xFFFFFF) == Tela.COR_PIXEL,
                "A aresta do polígono deve ser rasterizada em verde #00FF00.");
        exigir((tela.getImagem().getRGB(5, 5) & 0xFFFFFF) == Tela.COR_PIXEL,
                "A aresta diagonal deve ser rasterizada pelo ponto médio.");
        exigir((tela.getImagem().getRGB(12, 12) & 0xFFFFFF) == Tela.COR_FUNDO,
                "A tela deve manter o fundo branco fora do polígono.");

        aplicador.inicializarPoligono(new Poligono(List.of(
                new Ponto(2, 10), new Ponto(8, 10), new Ponto(2, 14))));
        aplicador.atualizarExibicao();
        exigir((tela.getImagem().getRGB(4, 2) & 0xFFFFFF) == Tela.COR_FUNDO,
                "A atualização deve remover o desenho anterior.");
        exigir((tela.getImagem().getRGB(4, 10) & 0xFFFFFF) == Tela.COR_PIXEL,
                "A atualização deve rasterizar o polígono recebido mais recentemente.");

        System.out.println("Todos os testes de integração do polígono passaram.");
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    private static boolean mesmosVertices(
            List<PontoHomogeneo> atuais, List<PontoHomogeneo> esperados) {
        if (atuais.size() != esperados.size()) {
            return false;
        }
        for (int i = 0; i < atuais.size(); i++) {
            PontoHomogeneo atual = atuais.get(i);
            PontoHomogeneo esperado = esperados.get(i);
            if (atual.x != esperado.x || atual.y != esperado.y || atual.w != esperado.w) {
                return false;
            }
        }
        return true;
    }

    private static void esperarExcecao(
            Class<? extends RuntimeException> tipo, Runnable acao, String mensagem) {
        try {
            acao.run();
        } catch (RuntimeException erro) {
            if (tipo.isInstance(erro)) {
                return;
            }
            throw new AssertionError(mensagem, erro);
        }
        throw new AssertionError(mensagem);
    }
}
