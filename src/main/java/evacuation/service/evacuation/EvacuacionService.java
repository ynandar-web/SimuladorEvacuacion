package evacuation.service.evacuation;

import evacuation.model.graph.Grafo;
import evacuation.model.graph.Nodo;
import evacuation.model.person.Persona;
import evacuation.service.graph.RutaService;
import evacuation.util.structure.PilaRuta;

import java.util.*;

/**
 * Orquesta la evacuación de una persona desde su habitación.
 * Principio DIP: recibe RutaService por inyección (BFS o Dijkstra).
 * Principio SRP: solo calcula y apila la ruta de evacuación.
 */
public class EvacuacionService {

    private final Grafo        grafo;
    private final RutaService  rutaService;

    public EvacuacionService(Grafo grafo, RutaService rutaService) {
        this.grafo        = grafo;
        this.rutaService  = rutaService;
    }

    /**
     * Calcula la ruta de evacuación para una persona y la apila en una PilaRuta.
     *
     * @param persona La persona a evacuar.
     * @return PilaRuta con los pasos ordenados (tope = primer paso).
     *         Vacía si no hay ruta disponible.
     */
    public PilaRuta<String> evacuarDesde(Persona persona) {
        PilaRuta<String> pila = new PilaRuta<>();

        List<String> salidasIds = grafo.getSalidas()
                                       .stream()
                                       .map(Nodo::getId)
                                       .toList();

        List<String> ruta = rutaService.calcularRuta(persona.getHabitacionActual(), salidasIds);

        if (ruta.isEmpty()) {
            System.out.printf("[ALERTA] Sin ruta para %s desde %s. Busque ruta alternativa.%n",
                    persona.getNombre(), persona.getHabitacionActual());
            return pila;
        }

        // Apilamos en orden inverso → el tope será el primer paso
        for (int i = ruta.size() - 1; i >= 0; i--) {
            pila.apilar(instrucion(ruta, i));
        }
        return pila;
    }

    /**
     * Compara BFS vs Dijkstra para la misma habitación e imprime el resultado.
     */
    public void compararAlgoritmos(String habitacionId,
                                   RutaService bfs,
                                   RutaService dijkstra) {
        List<String> salidas = grafo.getSalidas().stream().map(Nodo::getId).toList();

        List<String> rutaBFS      = bfs.calcularRuta(habitacionId, salidas);
        List<String> rutaDijkstra = dijkstra.calcularRuta(habitacionId, salidas);

        System.out.println("═══════════════════════════════════════");
        System.out.println("Comparación de algoritmos desde: " + habitacionId);
        System.out.printf("  BFS      (%d pasos): %s%n", rutaBFS.size() - 1, rutaBFS);
        System.out.printf("  Dijkstra (%d pasos): %s%n", rutaDijkstra.size() - 1, rutaDijkstra);
        System.out.println("═══════════════════════════════════════");
    }

    // ── Privado ────────────────────────────────────────────────────

    private String instrucion(List<String> ruta, int indice) {
        if (indice == 0) return "INICIO: " + ruta.get(0);
        if (indice == ruta.size() - 1) return "SALIDA: " + ruta.get(indice);
        return "Mover a: " + ruta.get(indice);
    }
}
