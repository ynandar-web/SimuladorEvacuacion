package evacuation.service.report;

import evacuation.model.person.Persona;
import evacuation.model.zone.Zona;
import evacuation.util.structure.ArbolRiesgo;
import evacuation.util.structure.TablaHash;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Genera reportes de evacuación. Recorre ArbolRiesgo inOrden para listar zonas
 * de mayor a menor peligro. Principio SRP: solo produce reportes; no modifica
 * estado.
 */
public class ReporteService {

    private static final int ANCHO_LINEA = 45;
    private static final String SEP = "─".repeat(ANCHO_LINEA);
    private static final int UMBRAL_CRITICO = 7;
    private static final int UMBRAL_MODERADO = 4;

    private final ArbolRiesgo<Integer, Zona> arbolZonas;
    private final TablaHash<String, Persona> registroPersonas;

    public ReporteService(ArbolRiesgo<Integer, Zona> arbolZonas,
            TablaHash<String, Persona> registroPersonas) {
        this.arbolZonas = arbolZonas;
        this.registroPersonas = registroPersonas;
    }

    // ── Zonas ─────────────────────────────────────────────────────
    /**
     * Imprime zonas ordenadas por nivelRiesgo descendente (más peligrosas
     * primero).
     */
    public void reporteZonasPorRiesgo() {
        encabezado("ZONAS POR NIVEL DE RIESGO (mayor → menor)");

        List<Zona> zonas = arbolZonas.inOrden();
        if (zonas.isEmpty()) {
            System.out.println("  (sin zonas registradas)");
            pie();
            return;
        }

        // inOrden = ascendente → invertimos para mostrar mayor riesgo primero
        for (int i = zonas.size() - 1; i >= 0; i--) {
            Zona z = zonas.get(i);
            System.out.printf("  Riesgo %2d | %-20s | Piso %d %s%n",
                    z.getNivelRiesgo(),
                    z.getNombre(),
                    z.getPiso(),
                    etiquetaRiesgo(z.getNivelRiesgo()));
        }
        pie();
    }

    // ── Personas ──────────────────────────────────────────────────
    /**
     * Imprime el estado final de cada persona agrupado por piso.
     */
    public void reportePersonas() {
        encabezado("ESTADO DE OCUPANTES");

        List<Persona> todas = registroPersonas.valores();
        if (todas.isEmpty()) {
            System.out.println("  (sin ocupantes registrados)");
            pie();
            return;
        }

        // Agrupar por piso para lectura más clara
        Map<Integer, List<Persona>> porPiso = todas.stream()
                .collect(Collectors.groupingBy(Persona::getPiso,
                        Collectors.toList()));

        porPiso.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    System.out.printf("  Piso %d:%n", entry.getKey());
                    entry.getValue().stream()
                            .sorted(Comparator.comparing(Persona::getNombre))
                            .forEach(p -> System.out.printf(
                            "    [%-9s] %-22s Hab:%-8s%n",
                            p.isEvacuado() ? "EVACUADO" : "PENDIENTE",
                            p.getNombre(),
                            p.getHabitacionActual()));
                });

        long evacuados = todas.stream().filter(Persona::isEvacuado).count();
        long pendientes = todas.size() - evacuados;
        double pct = todas.isEmpty() ? 0 : evacuados * 100.0 / todas.size();

        System.out.println();
        System.out.printf("  Total evacuados : %d%n", evacuados);
        System.out.printf("  Total pendientes: %d%n", pendientes);
        System.out.printf("  Progreso        : %.0f%%%n", pct);
        pie();
    }

    // ── Resumen ───────────────────────────────────────────────────
    /**
     * Resumen ejecutivo con timestamp.
     */
    public void reporteCompleto(int turnosEjecutados) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        System.out.println("\n" + "═".repeat(ANCHO_LINEA));
        System.out.printf("   PLAN DE EVACUACIÓN - SIMULADOR v1.0%n");
        System.out.printf("   Generado: %s%n", timestamp);
        System.out.println("═".repeat(ANCHO_LINEA));

        reporteZonasPorRiesgo();
        reportePersonas();

        encabezado("RESUMEN FINAL");
        List<Persona> todas = registroPersonas.valores();
        long evacuados = todas.stream().filter(Persona::isEvacuado).count();

        System.out.printf("  Turnos ejecutados   : %d%n", turnosEjecutados);
        System.out.printf("  Personas registradas: %d%n", todas.size());
        System.out.printf("  Evacuadas           : %d%n", evacuados);
        System.out.printf("  Sin evacuar         : %d%n", todas.size() - evacuados);

        // Zona más crítica (si existe)
        List<Zona> zonas = arbolZonas.inOrden();
        if (!zonas.isEmpty()) {
            Zona peor = zonas.get(zonas.size() - 1);
            System.out.printf("  Zona más crítica    : %s (riesgo %d)%n",
                    peor.getNombre(), peor.getNivelRiesgo());
        }
        pie();
    }

    /**
     * Sobrecarga sin turnos para compatibilidad con llamadas existentes.
     */
    public void reporteCompleto() {
        reporteCompleto(0);
    }

    // ── Utilidades privadas ───────────────────────────────────────
    private void encabezado(String titulo) {
        System.out.println("\n" + SEP);
        System.out.printf("  %s%n", titulo);
        System.out.println(SEP);
    }

    private void pie() {
        System.out.println(SEP);
    }

    private String etiquetaRiesgo(int nivel) {
        if (nivel >= UMBRAL_CRITICO) {
            return "[!!! CRITICO]";
        }
        if (nivel >= UMBRAL_MODERADO) {
            return "[! MODERADO]";
        }
        return "";
    }
}
