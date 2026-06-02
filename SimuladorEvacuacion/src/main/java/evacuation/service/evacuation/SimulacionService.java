package evacuation.service.evacuation;

import evacuation.model.graph.Grafo;
import evacuation.model.graph.Nodo;
import evacuation.model.person.Persona;
import evacuation.model.zone.Zona;
import evacuation.util.structure.ArbolRiesgo;
import evacuation.util.structure.ColaPasillo;
import evacuation.util.structure.PilaRuta;
import evacuation.util.structure.TablaHash;

import java.util.*;

/**
 * Ejecuta la simulación por turnos.
 * Cada turno: avanza personas en cola, actualiza hash y árbol de zonas.
 * Principio SRP: solo gestiona el loop de simulación.
 */
public class SimulacionService {

    private final Grafo             grafo;
    private final EvacuacionService evacuacionService;
    private final TablaHash<String, Persona>    registroPersonas;
    private final ArbolRiesgo<Integer, Zona>    arbolZonas;

    // Cola por nodo (escalera/pasillo): simula cuello de botella
    private final Map<String, ColaPasillo<Persona>> colasPorNodo = new HashMap<>();

    private int turnoActual = 0;

    public SimulacionService(Grafo grafo,
                             EvacuacionService evacuacionService,
                             TablaHash<String, Persona> registroPersonas,
                             ArbolRiesgo<Integer, Zona> arbolZonas) {
        this.grafo             = grafo;
        this.evacuacionService = evacuacionService;
        this.registroPersonas  = registroPersonas;
        this.arbolZonas        = arbolZonas;

        // Inicializar cola para cada nodo escalera/pasillo
        grafo.getNodos().forEach(nodo -> {
            if (nodo.esEscalera() || nodo.getTipo() == Nodo.TipoNodo.PASILLO) {
                colasPorNodo.put(nodo.getId(), new ColaPasillo<>());
            }
        });
    }

    /**
     * Inicializa rutas para todas las personas registradas.
     */
    public void iniciar() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║  SIMULACIÓN DE EVACUACIÓN - INICIO   ║");
        System.out.println("╚══════════════════════════════════════╝");

        registroPersonas.valores().forEach(persona -> {
            PilaRuta<String> ruta = evacuacionService.evacuarDesde(persona);
            if (!ruta.estaVacia()) {
                encolarPrimerPaso(persona, ruta);
            }
        });
    }

    /**
     * Avanza la simulación un turno.
     * Cada persona en cola avanza un paso; si llega a salida, se marca evacuada.
     */
    public void avanzarTurno() {
        turnoActual++;
        System.out.printf("%n─── Turno %d ───────────────────────────%n", turnoActual);

        colasPorNodo.forEach((nodoId, cola) -> {
            if (!cola.estaVacia()) {
                Persona persona = cola.desencolar();
                procesarMovimiento(persona, nodoId);
            }
        });

        imprimirEstado();
    }

    /** Ejecuta la simulación completa hasta que todos evacúen o se agoten los turnos. */
    public void ejecutarHastaFin(int maxTurnos) {
        iniciar();
        while (!todosEvacuados() && turnoActual < maxTurnos) {
            avanzarTurno();
        }
        System.out.println(todosEvacuados()
                ? "\n✅ Evacuación completada en " + turnoActual + " turnos."
                : "\n⚠️  Evacuación incompleta tras " + maxTurnos + " turnos.");
    }

    // ── Privados ───────────────────────────────────────────────────

    private void encolarPrimerPaso(Persona persona, PilaRuta<String> ruta) {
        String instruccion = ruta.desapilar();   // primer paso
        String nodoDestino = extraerNodoId(instruccion);
        ColaPasillo<Persona> cola = colasPorNodo.get(nodoDestino);
        if (cola != null) cola.encolar(persona);
    }

    private void procesarMovimiento(Persona persona, String nodoActualId) {
        Nodo nodo = grafo.getNodo(nodoActualId).orElse(null);
        if (nodo == null) return;

        if (nodo.esSalida()) {
            persona.marcarEvacuada();
            registroPersonas.poner(persona.getId(), persona);
            System.out.printf("  ✔ %s evacuado por %s%n", persona.getNombre(), nodoActualId);
        } else {
            persona.moverA(nodoActualId, nodo.getPiso());
            System.out.printf("  → %s avanzó a %s (Piso %d)%n",
                    persona.getNombre(), nodoActualId, nodo.getPiso());
        }
    }

    private boolean todosEvacuados() {
        return registroPersonas.valores().stream().allMatch(Persona::isEvacuado);
    }

    private void imprimirEstado() {
        long evacuados = registroPersonas.valores().stream().filter(Persona::isEvacuado).count();
        long total     = registroPersonas.valores().size();
        System.out.printf("  Estado: %d/%d evacuados%n", evacuados, total);
    }

    private String extraerNodoId(String instruccion) {
        // Formato: "Mover a: HAB_01" o "SALIDA: EXIT_A"
        int idx = instruccion.lastIndexOf(": ");
        return idx >= 0 ? instruccion.substring(idx + 2).trim() : instruccion;
    }

    public int getTurnoActual() { return turnoActual; }
}
