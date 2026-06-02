package evacuation.model.graph;

/**
 * Representa cualquier ubicación del edificio en el grafo.
 * Principio SRP: solo contiene datos estructurales del nodo.
 */
public class Nodo {

    public enum TipoNodo {
        HABITACION,
        PASILLO,
        ESCALERA,
        SALIDA
    }

    private final String id;
    private final String nombre;
    private final TipoNodo tipo;
    private final int piso;

    public Nodo(String id, String nombre, TipoNodo tipo, int piso) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El ID del nodo no puede ser nulo o vacío.");
        this.id     = id;
        this.nombre = nombre;
        this.tipo   = tipo;
        this.piso   = piso;
    }

    public String getId()      { return id; }
    public String getNombre()  { return nombre; }
    public TipoNodo getTipo()  { return tipo; }
    public int getPiso()       { return piso; }

    public boolean esSalida()   { return tipo == TipoNodo.SALIDA; }
    public boolean esEscalera() { return tipo == TipoNodo.ESCALERA; }

    @Override
    public String toString() {
        return String.format("[%s | %s | Piso %d]", id, tipo, piso);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nodo)) return false;
        return id.equals(((Nodo) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
