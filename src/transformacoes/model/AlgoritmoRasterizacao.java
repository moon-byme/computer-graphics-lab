package transformacoes.model;

import java.util.List;
import java.util.Objects;

/** Contrato comum: converte uma forma em pixels DC, sem conhecer a renderizacao. */
public interface AlgoritmoRasterizacao {
    TipoAlgoritmo getTipo();

    List<Pixel> calcularPixels(FormaRasterizacao forma);

    default List<Pixel> rasterizar(FormaRasterizacao forma) {
        Objects.requireNonNull(forma, "A forma a rasterizar e obrigatoria.");
        if (forma.getTipo() != getTipo().getTipoForma()) {
            throw new IllegalArgumentException("O algoritmo selecionado nao corresponde a forma.");
        }
        return List.copyOf(Objects.requireNonNull(calcularPixels(forma),
                "O algoritmo deve retornar uma lista de pixels."));
    }
}
