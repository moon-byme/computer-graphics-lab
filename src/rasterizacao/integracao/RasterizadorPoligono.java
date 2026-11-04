package rasterizacao.integracao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import rasterizacao.model.Bresenham;
import transformacoes.model.Pixel;
import transformacoes.model.Poligono;
import transformacoes.model.Ponto;

/** Rasteriza todas as arestas de um polígono com Bresenham (algoritmo do ponto médio). */
public final class RasterizadorPoligono {
    private RasterizadorPoligono() { }

    /**
     * Converte cada vértice para coordenadas de dispositivo e rasteriza as arestas,
     * incluindo a aresta que liga o último vértice ao primeiro.
     */
    public static List<Pixel> rasterizar(Poligono poligono, Function<Ponto, Pixel> conversor) {
        Objects.requireNonNull(poligono, "O polígono é obrigatório.");
        Objects.requireNonNull(conversor, "O conversor de coordenadas é obrigatório.");

        List<Pixel> vertices = new ArrayList<>(poligono.getVertices().size());
        for (Ponto ponto : poligono.getVertices()) {
            vertices.add(Objects.requireNonNull(conversor.apply(ponto),
                    "A conversão não pode retornar um pixel nulo."));
        }

        List<Pixel> pixels = new ArrayList<>();
        Set<Long> visitados = new HashSet<>();
        for (int i = 0; i < vertices.size(); i++) {
            Pixel inicio = vertices.get(i);
            Pixel fim = vertices.get((i + 1) % vertices.size());
            Bresenham.desenhar(inicio.x, inicio.y, fim.x, fim.y, (x, y) -> {
                long chave = (((long) x) << 32) ^ (y & 0xffffffffL);
                if (visitados.add(chave)) {
                    pixels.add(new Pixel(x, y));
                }
            });
        }
        return List.copyOf(pixels);
    }
}
