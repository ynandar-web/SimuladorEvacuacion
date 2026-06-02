package evacuation.service.graph;

import java.util.List;

/**
 * Contrato para algoritmos de búsqueda de ruta.
 * Principio DIP: EvacuacionService depende de esta abstracción,
 * no de BFSService ni DijkstraService directamente.
 * Principio ISP: interfaz mínima, solo lo que los clientes necesitan.
 */
public interface RutaService {

    /**
     * Calcula la ruta óptima desde un nodo origen hasta la salida más cercana.
     *
     * @param origenId   ID del nodo de partida.
     * @param salidasIds IDs de los nodos de salida disponibles.
     * @return Lista ordenada de IDs de nodos que forman la ruta,
     *         o lista vacía si no existe camino.
     */
    List<String> calcularRuta(String origenId, List<String> salidasIds);

    /**
     * Nombre descriptivo del algoritmo (para logs y reportes).
     */
    String getNombreAlgoritmo();
}
