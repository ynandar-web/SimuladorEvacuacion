package evacuation.model.zone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Agrupa habitaciones de un mismo piso/sector.
 * Almacenada en ArbolBinario<Integer, Zona> con clave = nivelRiesgo.
 * Principio SRP: solo modela zona y su peligrosidad.
 */
public class Zona {

    private final String  id;
    private final String  nombre;
    private final int     piso;
    private int           nivelRiesgo;     // 1 (bajo) … 10 (crítico)
    private final List<String> habitacionIds = new ArrayList<>();

    public Zona(String id, String nombre, int piso, int nivelRiesgo) {
        this.id          = id;
        this.nombre      = nombre;
        this.piso        = piso;
        this.nivelRiesgo = nivelRiesgo;
    }

    public void agregarHabitacion(String habitacionId) {
        habitacionIds.add(habitacionId);
    }

    public void actualizarRiesgo(int nuevoNivel) {
        if (nuevoNivel < 1 || nuevoNivel > 10)
            throw new IllegalArgumentException("Nivel de riesgo debe estar entre 1 y 10.");
        this.nivelRiesgo = nuevoNivel;
    }

    public String getId()                       { return id; }
    public String getNombre()                   { return nombre; }
    public int getPiso()                        { return piso; }
    public int getNivelRiesgo()                 { return nivelRiesgo; }
    public List<String> getHabitacionIds()      { return Collections.unmodifiableList(habitacionIds); }

    @Override
    public String toString() {
        return String.format("Zona[%s | %s | P%d | Riesgo:%d]", id, nombre, piso, nivelRiesgo);
    }
}
