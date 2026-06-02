package evacuation.model.building;

import evacuation.model.graph.Nodo;

/**
 * Subtipos semánticos de Nodo para mayor expresividad del dominio.
 * Principio LSP: son intercambiables donde se espera un Nodo.
 */

// ── Habitacion ─────────────────────────────────────────────────────────
class Habitacion extends Nodo {
    private final int capacidadMaxima;

    public Habitacion(String id, String nombre, int piso, int capacidadMaxima) {
        super(id, nombre, TipoNodo.HABITACION, piso);
        this.capacidadMaxima = capacidadMaxima;
    }

    public int getCapacidadMaxima() { return capacidadMaxima; }
}

// ── Escalera ───────────────────────────────────────────────────────────
class Escalera extends Nodo {
    private final int pisoCima;
    private final int pisoBase;

    public Escalera(String id, String nombre, int pisoBase, int pisoCima) {
        super(id, nombre, TipoNodo.ESCALERA, pisoBase);
        this.pisoBase = pisoBase;
        this.pisoCima = pisoCima;
    }

    public int getPisoBase() { return pisoBase; }
    public int getPisoCima() { return pisoCima; }
}

// ── Pasillo ────────────────────────────────────────────────────────────
class Pasillo extends Nodo {
    private final int anchoMetros;   // influye en el cuello de botella

    public Pasillo(String id, String nombre, int piso, int anchoMetros) {
        super(id, nombre, TipoNodo.PASILLO, piso);
        this.anchoMetros = anchoMetros;
    }

    public int getAnchoMetros() { return anchoMetros; }
}

// ── Salida ─────────────────────────────────────────────────────────────
class Salida extends Nodo {
    private final String codigoEmergencia;   // p. ej. "EXIT_A", "EXIT_B"

    public Salida(String id, String nombre, int piso, String codigoEmergencia) {
        super(id, nombre, TipoNodo.SALIDA, piso);
        this.codigoEmergencia = codigoEmergencia;
    }

    public String getCodigoEmergencia() { return codigoEmergencia; }

    @Override
    public String toString() {
        return String.format("[SALIDA %s | %s | Piso %d]", codigoEmergencia, getId(), getPiso());
    }
}
