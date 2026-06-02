package evacuation.model.person;

/**
 * Ocupante del edificio.
 * Almacenado en TablaHash<String, Persona> con clave = idPersona.
 * Principio SRP: solo modela el estado de una persona en la evacuación.
 */
public class Persona {

    private final String id;
    private final String nombre;
    private String habitacionActual;
    private int    piso;
    private boolean evacuado;

    public Persona(String id, String nombre, String habitacionActual, int piso) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID de persona requerido.");
        this.id               = id;
        this.nombre           = nombre;
        this.habitacionActual = habitacionActual;
        this.piso             = piso;
        this.evacuado         = false;
    }

    // ── Consulta ───────────────────────────────────────────────────
    public String getId()               { return id; }
    public String getNombre()           { return nombre; }
    public String getHabitacionActual() { return habitacionActual; }
    public int getPiso()                { return piso; }
    public boolean isEvacuado()         { return evacuado; }

    // ── Mutación ───────────────────────────────────────────────────
    public void moverA(String nuevaHabitacion, int nuevoPiso) {
        this.habitacionActual = nuevaHabitacion;
        this.piso             = nuevoPiso;
    }

    /** Marca la persona como evacuada exitosamente. */
    public void marcarEvacuada() { this.evacuado = true; }

    @Override
    public String toString() {
        String estado = evacuado ? "EVACUADO" : "EN EDIFICIO";
        return String.format("Persona[%s | %s | Hab:%s | P%d | %s]",
                id, nombre, habitacionActual, piso, estado);
    }
}
