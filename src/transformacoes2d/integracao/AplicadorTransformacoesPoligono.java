package transformacoes2d.integracao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import rasterizacao.integracao.RasterizadorPoligono;
import transformacoes.model.Pixel;
import transformacoes.model.Poligono;
import transformacoes.model.Ponto;
import transformacoes.view.Tela;
import transformacoes2d.model.CoordenadasHomogeneas;
import transformacoes2d.model.MatrizesTransformacao;
import transformacoes2d.model.OperacoesMatriciais;
import transformacoes2d.model.PontoHomogeneo;

/** Integra o polígono homogêneo à rasterização e à tela existentes. */
public final class AplicadorTransformacoesPoligono {
    private final Tela tela;
    private final Function<Ponto, Pixel> conversorParaDispositivo;
    private List<PontoHomogeneo> verticesOriginais = List.of();
    private List<PontoHomogeneo> verticesAtuais = List.of();
    private boolean inicializado;

    public AplicadorTransformacoesPoligono(
            Tela tela, Function<Ponto, Pixel> conversorParaDispositivo) {
        this.tela = Objects.requireNonNull(tela, "A tela é obrigatória.");
        this.conversorParaDispositivo = Objects.requireNonNull(
                conversorParaDispositivo, "O conversor para dispositivo é obrigatório.");
    }

    /** Armazena uma cópia homogênea como base imutável para as transformações. */
    public void inicializarPoligono(Poligono poligonoOriginal) {
        Objects.requireNonNull(poligonoOriginal, "O polígono original é obrigatório.");

        List<PontoHomogeneo> vertices = new ArrayList<>(poligonoOriginal.getVertices().size());
        for (Ponto ponto : poligonoOriginal.getVertices()) {
            vertices.add(CoordenadasHomogeneas.paraHomogeneas(ponto.x, ponto.y));
        }
        verticesOriginais = List.copyOf(vertices);
        verticesAtuais = List.copyOf(verticesOriginais);
        inicializado = true;
    }

    /** Converte os vértices atuais e redesenha seu contorno verde pelo ponto médio. */
    public void atualizarExibicao() {
        exigirInicializado();

        List<Ponto> verticesCartesianos = new ArrayList<>(verticesAtuais.size());
        for (PontoHomogeneo ponto : verticesAtuais) {
            verticesCartesianos.add(CoordenadasHomogeneas.paraCartesianas(ponto));
        }

        Poligono poligono = new Poligono(verticesCartesianos);
        List<Pixel> pixels = RasterizadorPoligono.rasterizar(
                poligono, conversorParaDispositivo);
        tela.limpar();
        tela.drawPixels(pixels);
    }

    /** Translada os vértices originais e redesenha o polígono. */
    public void transladarPoligono(double tx, double ty) {
        exigirInicializado();
        aplicarTransformacao(MatrizesTransformacao.translacao(tx, ty));
    }

    /** Escala os vértices originais e redesenha o polígono. */
    public void escalarPoligono(double sx, double sy) {
        exigirInicializado();
        aplicarTransformacao(MatrizesTransformacao.escala(sx, sy));
    }

    /** Rotaciona os vértices originais e redesenha o polígono. */
    public void rotacionarPoligono(double angulo) {
        exigirInicializado();
        aplicarTransformacao(MatrizesTransformacao.rotacao(angulo));
    }

    /** Reflete os vértices originais em relação ao eixo X e redesenha o polígono. */
    public void aplicarReflexaoX() {
        exigirInicializado();
        aplicarTransformacao(MatrizesTransformacao.reflexaoX());
    }

    /** Reflete os vértices originais em relação ao eixo Y e redesenha o polígono. */
    public void aplicarReflexaoY() {
        exigirInicializado();
        aplicarTransformacao(MatrizesTransformacao.reflexaoY());
    }

    /** Reflete os vértices originais em relação à origem e redesenha o polígono. */
    public void aplicarReflexaoOrigem() {
        exigirInicializado();
        aplicarTransformacao(MatrizesTransformacao.reflexaoOrigem());
    }

    /** Reflete os vértices originais em relação à reta Y = X e redesenha o polígono. */
    public void aplicarReflexaoRetaXY() {
        exigirInicializado();
        aplicarTransformacao(MatrizesTransformacao.reflexaoRetaXY());
    }

    /** Cisalha os vértices originais no eixo X e redesenha o polígono. */
    public void aplicarCisalhamentoX(double fator) {
        exigirInicializado();
        aplicarTransformacao(MatrizesTransformacao.cisalhamentoX(fator));
    }

    /** Cisalha os vértices originais no eixo Y e redesenha o polígono. */
    public void aplicarCisalhamentoY(double fator) {
        exigirInicializado();
        aplicarTransformacao(MatrizesTransformacao.cisalhamentoY(fator));
    }

    /** Retorna os vértices originais homogêneos, sem permitir alterações externas. */
    public List<PontoHomogeneo> getVerticesOriginais() {
        exigirInicializado();
        return verticesOriginais;
    }

    /** Retorna os vértices atuais homogêneos, sem permitir alterações externas. */
    public List<PontoHomogeneo> getVerticesAtuais() {
        exigirInicializado();
        return verticesAtuais;
    }

    private void aplicarTransformacao(double[][] matriz) {
        exigirInicializado();

        List<PontoHomogeneo> verticesTransformados =
                new ArrayList<>(verticesOriginais.size());
        for (PontoHomogeneo ponto : verticesOriginais) {
            verticesTransformados.add(
                    OperacoesMatriciais.multiplicarMatrizPorPonto(matriz, ponto));
        }
        verticesAtuais = List.copyOf(verticesTransformados);
        atualizarExibicao();
    }

    private void exigirInicializado() {
        if (!inicializado) {
            throw new IllegalStateException("Inicialize o polígono antes de usá-lo.");
        }
    }
}
