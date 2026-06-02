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
 * Cada turno: avanza personas según su ruta, usa ColaPasillo como
 * cuello de botella en escaleras y pasillos.
 * Principio SRP: solo gestiona el loop de simulación.
 *
 * CORRECCIÓN: se almacena la ruta completa de cada persona (PilaRuta)
 * para que pueda avanzar un paso por turno hasta llegar a la salida.
 * El bug original descartaba la ruta tras el primer paso y solo
 * encolaba personas en nodos ESCALERA/PASILLO, por lo que quienes
 * empezaban en HABITACION nunca avanzaban.
 */
public class SimulacionService {

    private final Grafo                          grafo;
    private final EvacuacionService              evacuacionService;
    private final TablaHash<String, Persona>     registroPersonas;
    private final ArbolRiesgo<Integer, Zona>     arbolZonas;

    // Ruta pendiente de cada persona (clave = idPersona)
    private final Map<String, PilaRuta<String>>  rutasPendientes  = new HashMap<>();

    // Cola por nodo (escalera/pasillo): simula cuello de botella
    private final Map<String, ColaPasillo<Persona>> colasPorNodo  = new HashMap<>();

    // Personas listas para moverse este turno (no están en cuello de botella)
    private final ColaPasillo<Persona>           colaGeneral      = new ColaPasillo<>();

    private int turnoActual = 0;

    public SimulacionService(Grafo grafo,
                             EvacuacionService evacuacionService,
                             TablaHash<String, Persona> registroPersonas,
                             ArbolRiesgo<Integer, Zona> arbolZonas) {
        this.grafo            = grafo;
        this.evacuacionService = evacuacionService;
        this.registroPersonas  = registroPersonas;
        this.arbolZonas        = arbolZonas;

        // Cola de cuello de botella solo para escaleras y pasillos
        grafo.getNodos().forEach(nodo -> {
            if (nodo.esEscalera() || nodo.getTipo() == Nodo.TipoNodo.PASILLO) {
                colasPorNodo.put(nodo.getId(), new ColaPasillo<>());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  Iniciar: calcular rutas y encolar todas las personas
    // ─────────────────────────────────────────────────────────────
    public void iniciar() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║  SIMULACIÓN DE EVACUACIÓN - INICIO   ║");
        System.out.println("╚══════════════════════════════════════╝");

        registroPersonas.valores().forEach(persona -> {
            PilaRuta<String> ruta = evacuacionService.evacuarDesde(persona);
            if (!ruta.estaVacia()) {
                // Descartamos la instrucción "INICIO: HAB_XX" (primer elemento)
                ruta.desapilar();
                rutasPendientes.put(persona.getId(), ruta);
                // Todas las personas arrancan listas para moverse
                colaGeneral.encolar(persona);
            } else {
                System.out.printf("[ALERTA] Sin ruta para %s — permanece en %s%n",
                        persona.getNombre(), persona.getHabitacionActual());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  Avanzar un turno
    // ─────────────────────────────────────────────────────────────
    public void avanzarTurno() {
        turnoActual++;
        System.out.printf("%n─── Turno %d ───────────────────────────%n", turnoActual);

        // 1. Mover personas de la cola general (una por turno c/u)
        int enCola = colaGeneral.getTamanio();
        for (int i = 0; i < enCola; i++) {
            Persona persona = colaGeneral.desencolar();
            if (persona.isEvacuado()) continue;
            moverPersona(persona);
        }

        // 2. Liberar un lugar por cuello de botella (escalera/pasillo)
        colasPorNodo.forEach((nodoId, cola) -> {
            if (!cola.estaVacia()) {
                Persona persona = cola.desencolar();
                if (!persona.isEvacuado()) moverPersona(persona);
            }
        });

        imprimirEstado();
    }

    /** Ejecuta hasta que todos evacúen o se agoten los turnos. */
    public void ejecutarHastaFin(int maxTurnos) {
        iniciar();
        while (!todosEvacuados() && turnoActual < maxTurnos) {
            avanzarTurno();
        }
        System.out.println(todosEvacuados()
                ? "\n✅ Evacuación completada en " + turnoActual + " turnos."
                : "\n⚠️  Evacuación incompleta tras " + maxTurnos + " turnos.");
    }

    // ─────────────────────────────────────────────────────────────
    //  Privados
    // ─────────────────────────────────────────────────────────────

    /**
     * Avanza a la persona un paso en su ruta.
     * Si el siguiente nodo es un cuello de botella, la mete en su cola.
     * Si no hay más pasos, es porque llegó a la salida.
     */
    private void moverPersona(Persona persona) {
        PilaRuta<String> ruta = rutasPendientes.get(persona.getId());
        if (ruta == null || ruta.estaVacia()) return;

        String instruccion  = ruta.desapilar();
        String nodoDestinoId = extraerNodoId(instruccion);
        Nodo   nodo          = grafo.getNodo(nodoDestinoId).orElse(null);
        if (nodo == null) return;

        if (nodo.esSalida()) {
            persona.marcarEvacuada();
            registroPersonas.poner(persona.getId(), persona);
            System.out.printf("  ✔ %s evacuado por %s%n", persona.getNombre(), nodoDestinoId);
        } else {
            persona.moverA(nodoDestinoId, nodo.getPiso());
            System.out.printf("  → %s avanzó a %s (Piso %d)%n",
                    persona.getNombre(), nodoDestinoId, nodo.getPiso());

            // ¿Es un cuello de botella?
            ColaPasillo<Persona> colaNodo = colasPorNodo.get(nodoDestinoId);
            if (colaNodo != null) {
                colaNodo.encolar(persona);   // espera su turno en ese nodo
            } else {
                colaGeneral.encolar(persona); // sigue libremente el próximo turno
            }
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
        // Formato: "Mover a: PAS_01" | "SALIDA: EXIT_B"
        int idx = instruccion.lastIndexOf(": ");
        return idx >= 0 ? instruccion.substring(idx + 2).trim() : instruccion;
    }

    public int getTurnoActual() { return turnoActual; }
}
