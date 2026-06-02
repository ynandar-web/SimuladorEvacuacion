package evacuation.service.graph;

import evacuation.model.graph.Arista;
import evacuation.model.graph.Grafo;

import java.util.*;

/**
 * Implementa Dijkstra para la ruta de menor tiempo/distancia.
 * Soporta múltiples salidas: elige la de menor costo total.
 * Principio SRP: solo resuelve camino mínimo con pesos.
 */
public class DijkstraService implements RutaService {

    private final Grafo grafo;
    private static final int INFINITO = Integer.MAX_VALUE;

    public DijkstraService(Grafo grafo) {
        this.grafo = grafo;
    }

    @Override
    public List<String> calcularRuta(String origenId, List<String> salidasIds) {
        if (origenId == null || salidasIds == null || salidasIds.isEmpty()) return List.of();

        Map<String, Integer>  distancia = new HashMap<>();
        Map<String, String>   padre     = new HashMap<>();
        Set<String>           visitados = new HashSet<>();

        // Cola de prioridad: (distancia, nodoId)
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e[0]));

        // Inicializar todos los nodos
        grafo.getNodos().forEach(n -> distancia.put(n.getId(), INFINITO));
        distancia.put(origenId, 0);
        pq.offer(new int[]{0, origenId.hashCode()});

        // Mapa auxiliar: hashCode → id (necesario porque PQ usa int[])
        Map<Integer, String> hashAId = new HashMap<>();
        grafo.getNodos().forEach(n -> hashAId.put(n.getId().hashCode(), n.getId()));
        pq.clear();
        pq.offer(new int[]{0, 0});

        // ── Reimplementación con String directa ──────────────────
        PriorityQueue<NodoDistancia> cola = new PriorityQueue<>(Comparator.comparingInt(nd -> nd.dist));
        cola.offer(new NodoDistancia(origenId, 0));

        while (!cola.isEmpty()) {
            NodoDistancia actual = cola.poll();

            if (visitados.contains(actual.id)) continue;
            visitados.add(actual.id);

            // Si llegamos a alguna salida, reconstruimos y retornamos
            if (salidasIds.contains(actual.id)) {
                return reconstruirRuta(padre, actual.id);
            }

            for (Arista arista : grafo.getAristasActivas(actual.id)) {
                String vecinoId  = arista.getDestino().getId();
                int nuevaDist    = distancia.getOrDefault(actual.id, INFINITO) + arista.getPeso();

                if (nuevaDist < distancia.getOrDefault(vecinoId, INFINITO)) {
                    distancia.put(vecinoId, nuevaDist);
                    padre.put(vecinoId, actual.id);
                    cola.offer(new NodoDistancia(vecinoId, nuevaDist));
                }
            }
        }
        return List.of();
    }

    private List<String> reconstruirRuta(Map<String, String> padre, String destino) {
        LinkedList<String> ruta = new LinkedList<>();
        String actual = destino;
        while (actual != null) {
            ruta.addFirst(actual);
            actual = padre.get(actual);
        }
        return ruta;
    }

    @Override
    public String getNombreAlgoritmo() { return "Dijkstra (tiempo mínimo)"; }

    /** Clase interna auxiliar para la cola de prioridad. */
    private static class NodoDistancia {
        final String id;
        final int dist;
        NodoDistancia(String id, int dist) { this.id = id; this.dist = dist; }
    }
}
