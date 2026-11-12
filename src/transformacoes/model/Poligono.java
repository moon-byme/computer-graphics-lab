package transformacoes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Polígono definido por vértices cartesianos em ordem (horária ou anti-horária). */
public final class Poligono {
    private final List<Ponto> vertices;

    public Poligono(List<Ponto> vertices) {
        Objects.requireNonNull(vertices, "A lista de vértices é obrigatória.");
        if (vertices.size() < 3) {
            throw new IllegalArgumentException("Um polígono precisa ter pelo menos três vértices.");
        }
        for (Ponto ponto : vertices) {
            Objects.requireNonNull(ponto, "O polígono não pode conter vértices nulos.");
            if (!Double.isFinite(ponto.x) || !Double.isFinite(ponto.y)) {
                throw new IllegalArgumentException("As coordenadas dos vértices devem ser finitas.");
            }
        }
        this.vertices = List.copyOf(vertices);
    }

    /** Retorna os vértices na ordem informada, sem permitir alterações externas. */
    public List<Ponto> getVertices() {
        return vertices;
    }

    public Poligono centralizarNaOrigem() {
        double centroX = 0;
        double centroY = 0;
        for (Ponto ponto : vertices) {
            centroX += ponto.x / vertices.size();
            centroY += ponto.y / vertices.size();
        }

        List<Ponto> verticesCentralizados = new ArrayList<>(vertices.size());
        for (Ponto ponto : vertices) {
            verticesCentralizados.add(new Ponto(ponto.x - centroX, ponto.y - centroY));
        }
        return new Poligono(verticesCentralizados);
    }
}
