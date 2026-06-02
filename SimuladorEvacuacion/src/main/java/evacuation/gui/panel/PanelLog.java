package evacuation.gui.panel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;

/**
 * Panel de log que muestra los mensajes de la simulación con colores.
 * Principio SRP: solo renderiza texto de log.
 */
public class PanelLog extends JPanel {

    private final JTextPane textPane = new JTextPane();
    private final StyledDocument doc;

    // Estilos
    private final Style estiloBase;
    private final Style estiloTitulo;
    private final Style estiloExito;
    private final Style estiloMov;
    private final Style estiloWarn;

    public PanelLog() {
        setLayout(new BorderLayout());
        setBackground(new Color(10, 14, 26));
        setBorder(titledBorder("LOG DE SIMULACIÓN", new Color(74, 158, 255)));

        textPane.setEditable(false);
        textPane.setBackground(new Color(3, 8, 16));
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 11));
        doc = textPane.getStyledDocument();

        // Definir estilos
        estiloBase   = addEstilo("base",   new Color(160, 190, 220));
        estiloTitulo = addEstilo("titulo", new Color(74, 158, 255),  true);
        estiloExito  = addEstilo("exito",  new Color(34, 197, 94));
        estiloMov    = addEstilo("mov",    new Color(200, 220, 240, 180));
        estiloWarn   = addEstilo("warn",   new Color(245, 158, 11));

        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(0, 140));
        add(scroll, BorderLayout.CENTER);
    }

    public void actualizar(List<String> lineas) {
        try {
            doc.remove(0, doc.getLength());
            for (String linea : lineas) {
                Style estilo = estiloParaLinea(linea);
                doc.insertString(doc.getLength(), linea + "\n", estilo);
            }
            // Auto-scroll
            textPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException ignored) {}
    }

    private Style estiloParaLinea(String linea) {
        if (linea.startsWith("===") || linea.startsWith("Turno") || linea.startsWith("──")) return estiloTitulo;
        if (linea.contains("✅") || linea.contains("completa"))  return estiloExito;
        if (linea.contains("⚠️") || linea.contains("incompleta")) return estiloWarn;
        if (linea.contains("→") || linea.contains("avanzó"))    return estiloMov;
        return estiloBase;
    }

    private Style addEstilo(String nombre, Color color) {
        return addEstilo(nombre, color, false);
    }

    private Style addEstilo(String nombre, Color color, boolean bold) {
        Style s = textPane.addStyle(nombre, null);
        StyleConstants.setForeground(s, color);
        StyleConstants.setBold(s, bold);
        return s;
    }

    private TitledBorder titledBorder(String title, Color color) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60), 1),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Monospaced", Font.BOLD, 10), color);
    }
}
