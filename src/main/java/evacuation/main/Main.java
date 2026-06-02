package evacuation.main;

import evacuation.model.building.NodoFactory;
import evacuation.model.graph.Grafo;
import evacuation.model.graph.Nodo;
import evacuation.model.person.Persona;
import evacuation.model.zone.Zona;
import evacuation.service.evacuation.EvacuacionService;
import evacuation.service.evacuation.SimulacionService;
import evacuation.service.graph.BFSService;
import evacuation.service.graph.DijkstraService;
import evacuation.service.report.ReporteService;
import evacuation.util.export.ExportadorTxt;
import evacuation.util.structure.ArbolRiesgo;
import evacuation.util.structure.ColaPasillo;
import evacuation.util.structure.PilaRuta;
import evacuation.util.structure.TablaHash;

import java.io.IOException;

/**
 * Punto de entrada del Simulador de Evacuación. Responsabilidad: construir el
 * grafo, registrar personas/zonas y lanzar la simulación.
 *
 * Fase 1: Conceptos simples ✓ Fase 2: Evacuación integrada ✓ Fase 3:
 * Exportación .txt ✓
 */
public class Main {

    public static void main(String[] args) throws IOException {

        // ─────────────────────────────────────────────────────────
        // FASE 1 — Construcción del grafo (12 nodos)
        // ─────────────────────────────────────────────────────────
        Grafo grafo = new Grafo();

        // Habitaciones (piso 1 y 2)
        Nodo h1 = NodoFactory.habitacion("HAB_01", "Habitación 1", 1, 5);
        Nodo h2 = NodoFactory.habitacion("HAB_02", "Habitación 2", 1, 5);
        Nodo h3 = NodoFactory.habitacion("HAB_03", "Habitación 3", 1, 5);
        Nodo h4 = NodoFactory.habitacion("HAB_04", "Habitación 4", 2, 5);
        Nodo h5 = NodoFactory.habitacion("HAB_05", "Habitación 5", 2, 5);
        Nodo h6 = NodoFactory.habitacion("HAB_06", "Habitación 6", 2, 5);
        Nodo p1 = NodoFactory.pasillo("PAS_01", "Pasillo Norte", 1, 2);
        Nodo p2 = NodoFactory.pasillo("PAS_02", "Pasillo Sur", 2, 2);
        Nodo esc1 = NodoFactory.escalera("ESC_A", "Escalera A", 1, 2);
        Nodo esc2 = NodoFactory.escalera("ESC_B", "Escalera B", 1, 2);
        Nodo exitA = NodoFactory.salida("EXIT_A", "EXIT_A", 0);
        Nodo exitB = NodoFactory.salida("EXIT_B", "EXIT_B", 0);

        // Registrar en el grafo
        for (Nodo n : new Nodo[]{h1, h2, h3, h4, h5, h6, p1, p2, esc1, esc2, exitA, exitB}) {
            grafo.agregarNodo(n);
        }

        // Aristas con pesos (tiempo en segundos)
        grafo.agregarArista(h1, p1, 5);
        grafo.agregarArista(h2, p1, 5);
        grafo.agregarArista(h3, p1, 8);
        grafo.agregarArista(p1, esc1, 3);
        grafo.agregarArista(p1, esc2, 4);
        grafo.agregarArista(h4, p2, 5);
        grafo.agregarArista(h5, p2, 5);
        grafo.agregarArista(h6, p2, 7);
        grafo.agregarArista(p2, esc1, 6);
        grafo.agregarArista(esc1, exitA, 10);
        grafo.agregarArista(esc2, exitB, 8);

        // Simular humo en ESC_A → EXIT_A
        System.out.println("🔥 Bloqueando ESC_A → EXIT_A (humo)");
        grafo.bloquearArista("ESC_A", "EXIT_A");

        // ─────────────────────────────────────────────────────────
        // FASE 1 — Estructuras de datos manuales
        // ─────────────────────────────────────────────────────────
        // TablaHash: 8 personas
        TablaHash<String, Persona> registro = new TablaHash<>();
        registro.poner("P001", new Persona("P001", "Ana García", "HAB_01", 1));
        registro.poner("P002", new Persona("P002", "Luis Pérez", "HAB_02", 1));
        registro.poner("P003", new Persona("P003", "María López", "HAB_03", 1));
        registro.poner("P004", new Persona("P004", "Carlos Ruiz", "HAB_04", 2));
        registro.poner("P005", new Persona("P005", "Sofía Torres", "HAB_05", 2));
        registro.poner("P006", new Persona("P006", "Juan Morales", "HAB_06", 2));
        registro.poner("P007", new Persona("P007", "Laura Vega", "HAB_01", 1));
        registro.poner("P008", new Persona("P008", "Diego Ríos", "HAB_04", 2));

        // Cola: simular paso de una persona en puerta
        ColaPasillo<String> colaDemo = new ColaPasillo<>();
        colaDemo.encolar("Baje escalera B");
        colaDemo.encolar("Cruce Pasillo 1");
        colaDemo.encolar("Llegue a Salida A");
        System.out.println("\nDemo ColaPasillo: " + colaDemo);
        System.out.println("Procesando: " + colaDemo.desencolar());

        // Pila: instrucciones de evacuación
        PilaRuta<String> pilaDemo = new PilaRuta<>();
        pilaDemo.apilar("Salida A");
        pilaDemo.apilar("Pasillo 2");
        pilaDemo.apilar("Escalera B");
        System.out.println("Demo PilaRuta: " + pilaDemo);

        // ArbolBinario: zonas por riesgo
        ArbolRiesgo<Integer, Zona> arbol = new ArbolRiesgo<>();
        arbol.insertar(3, new Zona("Z1", "Zona Norte Piso 1", 1, 3));
        arbol.insertar(7, new Zona("Z2", "Zona Sur Piso 2", 2, 7));
        arbol.insertar(5, new Zona("Z3", "Zona Este Piso 1", 1, 5));
        arbol.insertar(9, new Zona("Z4", "Zona Oeste Piso 2", 2, 9));

        // ─────────────────────────────────────────────────────────
        // FASE 2 — Evacuación integrada
        // ─────────────────────────────────────────────────────────
        DijkstraService dijkstra = new DijkstraService(grafo);
        BFSService bfs = new BFSService(grafo);

        EvacuacionService evacuacionService = new EvacuacionService(grafo, dijkstra);

        // Comparar BFS vs Dijkstra desde HAB_01
        evacuacionService.compararAlgoritmos("HAB_01", bfs, dijkstra);

        // Simulación completa por turnos
        SimulacionService simulacion = new SimulacionService(
                grafo, evacuacionService, registro, arbol);
        simulacion.ejecutarHastaFin(20);

        // ─────────────────────────────────────────────────────────
        // FASE 3 — Reporte y exportación
        // ─────────────────────────────────────────────────────────
        ReporteService reporte = new ReporteService(arbol, registro);
        reporte.reporteCompleto();

        ExportadorTxt.exportar(
                "plan_evacuacion.txt",
                registro,
                arbol,
                simulacion.getTurnoActual()
        );
    }
}
