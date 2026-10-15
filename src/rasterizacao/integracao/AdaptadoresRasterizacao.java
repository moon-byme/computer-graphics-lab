package rasterizacao.integracao;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import rasterizacao.model.AlgoritmoCircunferencia;
import rasterizacao.model.Bresenham;
import rasterizacao.model.DDA;
import rasterizacao.model.PlotadorPixel;
import transformacoes.model.AlgoritmoRasterizacao;
import transformacoes.model.FormaRasterizacao;
import transformacoes.model.Pixel;
import transformacoes.model.TipoAlgoritmo;

/** Adapta os algoritmos de rasterizacao ao contrato usado pelo controller da interface. */
public final class AdaptadoresRasterizacao {
    private AdaptadoresRasterizacao() { }

    public static List<AlgoritmoRasterizacao> criar() {
        return List.of(
                adaptar(TipoAlgoritmo.DDA_RETA, forma -> rasterizarReta(forma, false)),
                adaptar(TipoAlgoritmo.BRESENHAM_RETA, forma -> rasterizarReta(forma, true)),
                adaptar(TipoAlgoritmo.CIRCUNFERENCIA_POLINOMIAL,
                        forma -> rasterizarCircunferencia(forma, AlgoritmoCircunferencia.POLINOMIAL)),
                adaptar(TipoAlgoritmo.CIRCUNFERENCIA_TRIGONOMETRICA,
                        forma -> rasterizarCircunferencia(forma, AlgoritmoCircunferencia.TRIGONOMETRICO)),
                adaptar(TipoAlgoritmo.CIRCUNFERENCIA_PONTO_MEDIO,
                        forma -> rasterizarCircunferencia(forma, AlgoritmoCircunferencia.PONTO_MEDIO)));
    }

    private static AlgoritmoRasterizacao adaptar(
            TipoAlgoritmo tipo, Function<FormaRasterizacao, List<Pixel>> calcular) {
        return new AlgoritmoRasterizacao() {
            @Override
            public TipoAlgoritmo getTipo() {
                return tipo;
            }

            @Override
            public List<Pixel> calcularPixels(FormaRasterizacao forma) {
                return calcular.apply(forma);
            }
        };
    }

    private static List<Pixel> rasterizarReta(FormaRasterizacao forma, boolean bresenham) {
        Pixel inicio = forma.getInicio();
        Pixel fim = forma.getFim();
        List<Pixel> pixels = new ArrayList<>();
        PlotadorPixel plotador = (x, y) -> pixels.add(new Pixel(x, y));
        if (bresenham) {
            Bresenham.desenhar(inicio.x, inicio.y, fim.x, fim.y, plotador);
        } else {
            DDA.desenhar(inicio.x, inicio.y, fim.x, fim.y, plotador);
        }
        return pixels;
    }

    private static List<Pixel> rasterizarCircunferencia(
            FormaRasterizacao forma, AlgoritmoCircunferencia algoritmo) {
        Pixel centro = forma.getCentro();
        List<Pixel> pixels = new ArrayList<>();
        algoritmo.desenhar(centro.x, centro.y, forma.getRaio(),
                           (x, y) -> pixels.add(new Pixel(x, y)));
        return pixels;
    }
}
