package evacuation.util.export;

import evacuation.model.person.Persona;
import evacuation.model.zone.Zona;
import evacuation.util.structure.ArbolRiesgo;
import evacuation.util.structure.TablaHash;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serializa el plan de evacuación completo a un archivo .txt.
 * Principio SRP: única responsabilidad es la exportación de texto plano.
 */
public class ExportadorTxt {

    private static final String SEPARADOR = "═".repeat(45);

    private ExportadorTxt() { /* utilidad estática */ }

    /**
     * Exporta el reporte completo al archivo indicado.
     *
     * @param rutaArchivo     Ruta del .txt de destino, p.ej. "plan_evacuacion.txt"
     * @param personas        Registro hash de personas
     * @param arbolZonas      Árbol BST de zonas por riesgo
     * @param turnosEjecutados Número de turnos que duró la simulación
     */
    public static void exportar(String rutaArchivo,
                                TablaHash<String, Persona> personas,
                                ArbolRiesgo<Integer, Zona> arbolZonas,
                                int turnosEjecutados) throws IOException {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {

            bw.write(SEPARADOR); bw.newLine();
            bw.write("   PLAN DE EVACUACIÓN - SIMULADOR v1.0");  bw.newLine();
            bw.write("   Generado: " + timestamp());             bw.newLine();
            bw.write(SEPARADOR); bw.newLine();
            bw.newLine();

            // ── Sección 1: Zonas de riesgo ─────────────────────
            bw.write("ZONAS POR NIVEL DE RIESGO (mayor → menor)"); bw.newLine();
            bw.write("-".repeat(45)); bw.newLine();

            List<Zona> zonas = arbolZonas.inOrden();
            for (int i = zonas.size() - 1; i >= 0; i--) {
                Zona z = zonas.get(i);
                String alerta = z.getNivelRiesgo() >= 7 ? " [!!! CRITICO]" : "";
                bw.write(String.format("  Riesgo %2d | %-20s | Piso %d%s%n",
                        z.getNivelRiesgo(), z.getNombre(), z.getPiso(), alerta));
            }
            bw.newLine();

            // ── Sección 2: Estado de ocupantes ─────────────────
            bw.write("ESTADO DE OCUPANTES"); bw.newLine();
            bw.write("-".repeat(45));       bw.newLine();

            long evacuados   = personas.valores().stream().filter(Persona::isEvacuado).count();
            long pendientes  = personas.getTamanio() - evacuados;

            for (Persona p : personas.valores()) {
                String estado = p.isEvacuado() ? "[EVACUADO]" : "[PENDIENTE]";
                bw.write(String.format("  %s %-20s Hab:%-8s Piso:%d%n",
                        estado, p.getNombre(), p.getHabitacionActual(), p.getPiso()));
            }
            bw.newLine();
            bw.write(String.format("  Total evacuados : %d%n", evacuados));
            bw.write(String.format("  Total pendientes: %d%n", pendientes));
            bw.newLine();

            // ── Sección 3: Resumen ──────────────────────────────
            bw.write("RESUMEN FINAL"); bw.newLine();
            bw.write("-".repeat(45)); bw.newLine();
            bw.write(String.format("  Turnos ejecutados : %d%n", turnosEjecutados));
            bw.write(String.format("  Personas registradas: %d%n", personas.getTamanio()));
            bw.write(SEPARADOR); bw.newLine();
        }

        System.out.println("📄 Plan exportado a: " + rutaArchivo);
    }

    private static String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}
