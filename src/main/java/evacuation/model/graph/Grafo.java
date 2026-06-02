package evacuation.model.graph;

import java.util.*;

/**
 * Grafo dirigido con lista de adyacencia.
 * Gestiona nodos y aristas; expone bloquearArista(u,v) para simular humo.
 * Principio OCP: acepta cualquier subtipo de Nodo sin modificarse.
 */
public class Grafo {

    private final Map<String, Nodo>         nodos   = new LinkedHashMap<>();
    private final Map<String, List<Arista>> adyacencia = new HashMap<>();

    // Mutación

    public void agregarNodo(Nodo nodo) {
        nodos.put(nodo.getId(), nodo);
        adyacencia.putIfAbsent(nodo.getId(), new ArrayList<>());
    }

    public void agregarArista(Nodo origen, Nodo destino, int peso) {
        validarNodoExistente(origen);
        validarNodoExistente(destino);
        Arista arista = new Arista(origen, destino, peso);
        adyacencia.get(origen.getId()).add(arista);
    }

    /**
     * Bloquea la arista entre u y v (simulación de humo/incendio).
     * @return true si se encontró y bloqueó, false si no existe.
     */
    public boolean bloquearArista(String uId, String vId) {
        return setEstadoArista(uId, vId, true);
    }

    public boolean desbloquearArista(String uId, String vId) {
        return setEstadoArista(uId, vId, false);
    }

    // ── Consulta ───────────────────────────────────────────────────

    public Optional<Nodo> getNodo(String id) {
        return Optional.ofNullable(nodos.get(id));
    }

    /** Devuelve solo aristas NO bloqueadas desde un nodo. */
    public List<Arista> getAristasActivas(String nodoId) {
        return adyacencia.getOrDefault(nodoId, Collections.emptyList())
                         .stream()
                         .filter(a -> !a.isBloqueada())
                         .toList();
    }

    public Collection<Nodo> getNodos() { return Collections.unmodifiableCollection(nodos.values()); }

    /** Devuelve todos los nodos marcados como SALIDA. */
    public List<Nodo> getSalidas() {
        return nodos.values().stream()
                    .filter(Nodo::esSalida)
                    .toList();
    }

    // ── Internos ───────────────────────────────────────────────────

    private boolean setEstadoArista(String uId, String vId, boolean bloquear) {
        List<Arista> aristas = adyacencia.getOrDefault(uId, Collections.emptyList());
        for (Arista a : aristas) {
            if (a.getDestino().getId().equals(vId)) {
                if (bloquear) a.bloquear(); else a.desbloquear();
                return true;
            }
        }
        return false;
    }

    private void validarNodoExistente(Nodo nodo) {
        if (!nodos.containsKey(nodo.getId()))
            throw new IllegalArgumentException("Nodo no registrado en el grafo: " + nodo.getId());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("=== Grafo de Evacuación ===\n");
        adyacencia.forEach((id, aristas) -> {
            sb.append(id).append(":\n");
            aristas.forEach(a -> sb.append("  ").append(a).append("\n"));
        });
        return sb.toString();
    }
}
