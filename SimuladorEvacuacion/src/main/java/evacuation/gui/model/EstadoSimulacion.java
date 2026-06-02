package evacuation.gui.model;

import evacuation.model.graph.Grafo;
import evacuation.model.person.Persona;
import evacuation.model.zone.Zona;
import evacuation.service.evacuation.EvacuacionService;
import evacuation.service.evacuation.SimulacionService;
import evacuation.service.graph.BFSService;
import evacuation.service.graph.DijkstraService;
import evacuation.model.building.NodoFactory;
import evacuation.model.graph.Nodo;
import evacuation.util.structure.ArbolRiesgo;
import evacuation.util.structure.TablaHash;

import java.util.*;

/**
 * Contiene y gestiona el estado completo de la simulación para la GUI.
 * Construye el mismo grafo y datos que Main.java, sin alterar ninguna clase.
 * Actúa como "Modelo" en el patrón MVC de la GUI.
 */
public class EstadoSimulacion {

    // ── Objetos del dominio (sin cambios) ────────────────────────
    private Grafo                           grafo;
    private TablaHash<String, Persona>      registroPersonas;
    private ArbolRiesgo<Integer, Zona>      arbolZonas;
    private SimulacionService               simulacionService;
    private EvacuacionService               evacuacionService;

    // ── Estado de la GUI ─────────────────────────────────────────
    private String  algoritmoSeleccionado = "DIJKSTRA"; // "BFS" | "DIJKSTRA"
    private boolean bloqueadoEscAExitA    = true;
    private boolean bloqueadoEscBExitB    = false;
    private boolean bloqueadoPas1EscA     = false;
    private boolean bloqueadoPas2EscA     = false;

    private int     turnoActual           = 0;
    private boolean iniciada              = false;
    private boolean finalizada            = false;

    private final List<String> log        = new ArrayList<>();
    private final List<Observer> observadores = new ArrayList<>();

    // ── Rutas calculadas (para visualización) ───────────────────
    private final Map<String, List<String>> rutasPersonas = new LinkedHashMap<>();

    // ── Observer simple ──────────────────────────────────────────
    public interface Observer { void onCambio(); }
    public void agregarObservador(Observer o) { observadores.add(o); }
    private void notificar() { observadores.forEach(Observer::onCambio); }

    // ─────────────────────────────────────────────────────────────
    //  Construcción del grafo (igual que Main.java)
    // ─────────────────────────────────────────────────────────────
    private void construirGrafo() {
        grafo = new Grafo();

        Nodo h1   = NodoFactory.habitacion("HAB_01", "Habitación 1",  1, 5);
        Nodo h2   = NodoFactory.habitacion("HAB_02", "Habitación 2",  1, 5);
        Nodo h3   = NodoFactory.habitacion("HAB_03", "Habitación 3",  1, 5);
        Nodo h4   = NodoFactory.habitacion("HAB_04", "Habitación 4",  2, 5);
        Nodo h5   = NodoFactory.habitacion("HAB_05", "Habitación 5",  2, 5);
        Nodo h6   = NodoFactory.habitacion("HAB_06", "Habitación 6",  2, 5);
        Nodo p1   = NodoFactory.pasillo("PAS_01",    "Pasillo Norte", 1, 2);
        Nodo p2   = NodoFactory.pasillo("PAS_02",    "Pasillo Sur",   2, 2);
        Nodo esc1 = NodoFactory.escalera("ESC_A",    "Escalera A",    1, 2);
        Nodo esc2 = NodoFactory.escalera("ESC_B",    "Escalera B",    1, 2);
        Nodo exA  = NodoFactory.salida("EXIT_A",     "EXIT_A",        0);
        Nodo exB  = NodoFactory.salida("EXIT_B",     "EXIT_B",        0);

        for (Nodo n : new Nodo[]{h1,h2,h3,h4,h5,h6,p1,p2,esc1,esc2,exA,exB})
            grafo.agregarNodo(n);

        grafo.agregarArista(h1,  p1,   5);  grafo.agregarArista(h2,  p1,   5);
        grafo.agregarArista(h3,  p1,   8);  grafo.agregarArista(p1,  esc1, 3);
        grafo.agregarArista(p1,  esc2, 4);  grafo.agregarArista(h4,  p2,   5);
        grafo.agregarArista(h5,  p2,   5);  grafo.agregarArista(h6,  p2,   7);
        grafo.agregarArista(p2,  esc1, 6);
        grafo.agregarArista(esc1, exA, 10); grafo.agregarArista(esc2, exB,  8);

        // Aplicar bloqueos configurados en la GUI
        if (bloqueadoEscAExitA) grafo.bloquearArista("ESC_A", "EXIT_A");
        if (bloqueadoEscBExitB) grafo.bloquearArista("ESC_B", "EXIT_B");
        if (bloqueadoPas1EscA)  grafo.bloquearArista("PAS_01", "ESC_A");
        if (bloqueadoPas2EscA)  grafo.bloquearArista("PAS_02", "ESC_A");
    }

    private void construirPersonas() {
        registroPersonas = new TablaHash<>();
        registroPersonas.poner("P001", new Persona("P001","Ana García",   "HAB_01",1));
        registroPersonas.poner("P002", new Persona("P002","Luis Pérez",   "HAB_02",1));
        registroPersonas.poner("P003", new Persona("P003","María López",  "HAB_03",1));
        registroPersonas.poner("P004", new Persona("P004","Carlos Ruiz",  "HAB_04",2));
        registroPersonas.poner("P005", new Persona("P005","Sofía Torres", "HAB_05",2));
        registroPersonas.poner("P006", new Persona("P006","Juan Morales", "HAB_06",2));
        registroPersonas.poner("P007", new Persona("P007","Laura Vega",   "HAB_01",1));
        registroPersonas.poner("P008", new Persona("P008","Diego Ríos",   "HAB_04",2));
    }

    private void construirZonas() {
        arbolZonas = new ArbolRiesgo<>();
        arbolZonas.insertar(3, new Zona("Z1","Zona Norte Piso 1",1,3));
        arbolZonas.insertar(7, new Zona("Z2","Zona Sur Piso 2",  2,7));
        arbolZonas.insertar(5, new Zona("Z3","Zona Este Piso 1", 1,5));
        arbolZonas.insertar(9, new Zona("Z4","Zona Oeste Piso 2",2,9));
    }

    // ─────────────────────────────────────────────────────────────
    //  API pública para la GUI
    // ─────────────────────────────────────────────────────────────

    /** Inicializa y arranca la simulación con la configuración actual. */
    public void iniciar() {
        construirGrafo();
        construirPersonas();
        construirZonas();

        var rutaService = algoritmoSeleccionado.equals("BFS")
                ? new BFSService(grafo)
                : new DijkstraService(grafo);

        evacuacionService  = new EvacuacionService(grafo, rutaService);
        simulacionService  = new SimulacionService(grafo, evacuacionService,
                                                   registroPersonas, arbolZonas);
        simulacionService.iniciar();

        // Precalcular rutas para el visualizador del grafo
        rutasPersonas.clear();
        List<String> salidasIds = grafo.getSalidas().stream()
                                       .map(Nodo::getId).toList();
        for (Persona p : registroPersonas.valores()) {
            List<String> ruta = rutaService.calcularRuta(p.getHabitacionActual(), salidasIds);
            rutasPersonas.put(p.getId(), ruta);
        }

        turnoActual = 0;
        iniciada    = true;
        finalizada  = false;
        log.clear();
        agregarLog("=== SIMULACIÓN INICIADA (" + algoritmoSeleccionado + ") ===");
        notificar();
    }

    /** Avanza un turno de la simulación. */
    public void avanzarTurno() {
        if (!iniciada || finalizada) return;
        simulacionService.avanzarTurno();
        turnoActual = simulacionService.getTurnoActual();
        agregarLog("Turno " + turnoActual + " completado.");

        long evacuados = registroPersonas.valores().stream()
                                         .filter(Persona::isEvacuado).count();
        if (evacuados == registroPersonas.getTamanio()) {
            finalizada = true;
            agregarLog("✅ Evacuación completa en " + turnoActual + " turnos.");
        }
        notificar();
    }

    /** Reinicia a estado inicial. */
    public void reset() {
        iniciada   = false;
        finalizada = false;
        turnoActual = 0;
        log.clear();
        rutasPersonas.clear();
        grafo            = null;
        registroPersonas = null;
        arbolZonas       = null;
        simulacionService = null;
        evacuacionService = null;
        notificar();
    }

    private void agregarLog(String msg) { log.add(msg); }

    // ── Getters ──────────────────────────────────────────────────
    public Grafo getGrafo()                              { return grafo; }
    public TablaHash<String, Persona> getRegistro()     { return registroPersonas; }
    public ArbolRiesgo<Integer, Zona> getArbol()        { return arbolZonas; }
    public int getTurnoActual()                          { return turnoActual; }
    public boolean isIniciada()                          { return iniciada; }
    public boolean isFinalizada()                        { return finalizada; }
    public List<String> getLog()                         { return Collections.unmodifiableList(log); }
    public Map<String, List<String>> getRutasPersonas() { return Collections.unmodifiableMap(rutasPersonas); }
    public String getAlgoritmo()                         { return algoritmoSeleccionado; }

    // ── Setters de configuración (solo antes de iniciar) ─────────
    public void setAlgoritmo(String alg)           { this.algoritmoSeleccionado = alg; }
    public void setBloqueadoEscAExitA(boolean v)   { this.bloqueadoEscAExitA = v; }
    public void setBloqueadoEscBExitB(boolean v)   { this.bloqueadoEscBExitB = v; }
    public void setBloqueadoPas1EscA(boolean v)    { this.bloqueadoPas1EscA = v; }
    public void setBloqueadoPas2EscA(boolean v)    { this.bloqueadoPas2EscA = v; }
}
