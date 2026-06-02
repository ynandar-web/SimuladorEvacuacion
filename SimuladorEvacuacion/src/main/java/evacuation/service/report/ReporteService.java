package evacuation.service.report;

import evacuation.model.person.Persona;
import evacuation.model.zone.Zona;
import evacuation.util.structure.ArbolRiesgo;
import evacuation.util.structure.TablaHash;

import java.util.List;

/**
 * Genera reportes de evacuación.
 * Recorre ArbolRiesgo inOrden para listar zonas de mayor a menor peligro.
 * Principio SRP: solo produce reportes; no modifica estado.
 */
public class ReporteService {

    private final ArbolRiesgo<Integer, Zona>   arbolZonas;
    private final TablaHash<String, Persona>   registroPersonas;

    public ReporteService(ArbolRiesgo<Integer, Zona> arbolZonas,
                          TablaHash<String, Persona> registroPersonas) {
        this.arbolZonas       = arbolZonas;
        this.registroPersonas = registroPersonas;
    }

    /** Imprime zonas ordenadas por nivelRiesgo (inOrden = ascendente). */
    public void reporteZonasPorRiesgo() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║   REPORTE: ZONAS POR NIVEL DE RIESGO ║");
        System.out.println("╚══════════════════════════════════════╝");

        List<Zona> zonas = arbolZonas.inOrden();
        if (zonas.isEmpty()) {
            System.out.println("  (sin zonas registradas)");
            return;
        }
        // inOrden = ascendente; imprimimos desde el final para ver las más peligrosas primero
        for (int i = zonas.size() - 1; i >= 0; i--) {
            Zona z = zonas.get(i);
            String alerta = z.getNivelRiesgo() >= 7 ? " ⚠️ CRÍTICO" : "";
            System.out.printf("  Riesgo %2d | %s | Piso %d%s%n",
                    z.getNivelRiesgo(), z.getNombre(), z.getPiso(), alerta);
        }
    }

    /** Imprime el estado final de cada persona. */
    public void reportePersonas() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║     REPORTE: ESTADO DE OCUPANTES     ║");
        System.out.println("╚══════════════════════════════════════╝");

        long evacuados   = registroPersonas.valores().stream().filter(Persona::isEvacuado).count();
        long noEvacuados = registroPersonas.valores().size() - evacuados;

        registroPersonas.valores().forEach(p -> System.out.println("  " + p));

        System.out.printf("%n  ✅ Evacuados : %d%n", evacuados);
        System.out.printf("  ⛔ Pendientes: %d%n", noEvacuados);
    }

    /** Resumen ejecutivo completo. */
    public void reporteCompleto() {
        reporteZonasPorRiesgo();
        reportePersonas();
    }
}
