package evacuation.service.graph;

import evacuation.model.graph.Arista;
import evacuation.model.graph.Grafo;
import evacuation.model.graph.Nodo;

import java.util.*;

/**
 * Implementa BFS para encontrar la salida más cercana en número de pasos.
 * No considera pesos; útil para comparar contra Dijkstra.
 * Principio SRP: solo resuelve el problema de camino mínimo sin pesos.
 */
public class BFSService implements RutaService {

    private final Grafo grafo;

    public BFSService(Grafo grafo) {
        this.grafo = grafo;
    }

    @Override
    public List<String> calcularRuta(String origenId, List<String> salidasIds) {
        if (origenId == null || salidasIds == null || salidasIds.isEmpty()) return List.of();

        Set<String> visitados = new HashSet<>();
        Map<String, String> padre = new HashMap<>();  // hijo → padre
        Queue<String> cola = new LinkedList<>();

        cola.add(origenId);
        visitados.add(origenId);
        padre.put(origenId, null);

        while (!cola.isEmpty()) {
            String actual = cola.poll();

            if (salidasIds.contains(actual)) {
                return reconstruirRuta(padre, actual);
            }

            for (Arista arista : grafo.getAristasActivas(actual)) {
                String vecinoId = arista.getDestino().getId();
                if (!visitados.contains(vecinoId)) {
                    visitados.add(vecinoId);
                    padre.put(vecinoId, actual);
                    cola.add(vecinoId);
                }
            }
        }
        return List.of();   // sin ruta disponible
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
    public String getNombreAlgoritmo() { return "BFS (pasos mínimos)"; }
}
