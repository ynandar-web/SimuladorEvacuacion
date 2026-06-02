package evacuation.model.graph;

/**
 * Conexión dirigida entre dos nodos del grafo.
 * Puede bloquearse para simular humo o incendio.
 * Principio SRP: solo modela la relación entre nodos y su peso.
 */
public class Arista {

    private final Nodo origen;
    private final Nodo destino;
    private final int peso;          // tiempo en segundos o distancia
    private boolean bloqueada;       // true = humo/incendio

    public Arista(Nodo origen, Nodo destino, int peso) {
        if (origen == null || destino == null) throw new IllegalArgumentException("Origen y destino no pueden ser nulos.");
        if (peso < 0) throw new IllegalArgumentException("El peso no puede ser negativo.");
        this.origen    = origen;
        this.destino   = destino;
        this.peso      = peso;
        this.bloqueada = false;
    }

    /** Bloquea este pasillo (simula humo o derrumbe). */
    public void bloquear()    { this.bloqueada = true; }

    /** Desbloquea este pasillo (humo despejado). */
    public void desbloquear() { this.bloqueada = false; }

    public Nodo getOrigen()   { return origen; }
    public Nodo getDestino()  { return destino; }
    public int getPeso()      { return peso; }
    public boolean isBloqueada() { return bloqueada; }

    @Override
    public String toString() {
        String estado = bloqueada ? " [BLOQUEADA]" : "";
        return String.format("%s --> %s (peso=%d)%s", origen.getId(), destino.getId(), peso, estado);
    }
}
