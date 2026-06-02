package evacuation.gui.panel;

import evacuation.model.zone.Zona;
import evacuation.util.structure.ArbolRiesgo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * Muestra las zonas del ArbolRiesgo ordenadas de mayor a menor peligro.
 * Solo lee el ArbolRiesgo<Integer, Zona> — no lo modifica.
 */
public class PanelZonas extends JPanel {

    private final JPanel contenedor = new JPanel();

    public PanelZonas() {
        setLayout(new BorderLayout());
        setBackground(new Color(10, 14, 26));
        setBorder(titledBorder("ZONAS DE RIESGO", new Color(245, 158, 11)));

        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setBackground(new Color(10, 14, 26));
        contenedor.setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));
        add(contenedor, BorderLayout.CENTER);
    }

    public void actualizar(ArbolRiesgo<Integer, Zona> arbol) {
        contenedor.removeAll();
        if (arbol == null) { repaint(); return; }

        List<Zona> zonas = arbol.inOrden();
        // inOrden = ascendente → invertir para mostrar mayor riesgo primero
        for (int i = zonas.size() - 1; i >= 0; i--) {
            contenedor.add(filaZona(zonas.get(i)));
            contenedor.add(Box.createVerticalStrut(5));
        }
        revalidate();
        repaint();
    }

    private JPanel filaZona(Zona z) {
        JPanel fila = new JPanel(new BorderLayout(6, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        // Etiqueta nombre
        JLabel lbl = new JLabel(z.getNombre());
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 9));
        lbl.setForeground(new Color(200, 220, 240, 160));
        lbl.setPreferredSize(new Dimension(130, 16));

        // Barra de riesgo
        Color barColor = z.getNivelRiesgo() >= 7
                ? new Color(239,68,68)
                : z.getNivelRiesgo() >= 4
                ? new Color(245,158,11)
                : new Color(34,197,94);

        JProgressBar bar = new JProgressBar(0, 10);
        bar.setValue(z.getNivelRiesgo());
        bar.setBackground(new Color(13, 27, 46));
        bar.setForeground(barColor);
        bar.setBorderPainted(false);
        bar.setStringPainted(false);
        bar.setPreferredSize(new Dimension(80, 10));

        // Valor numérico
        JLabel valor = new JLabel(String.valueOf(z.getNivelRiesgo()));
        valor.setFont(new Font("Monospaced", Font.BOLD, 10));
        valor.setForeground(barColor);
        valor.setPreferredSize(new Dimension(16, 16));

        fila.add(lbl,   BorderLayout.WEST);
        fila.add(bar,   BorderLayout.CENTER);
        fila.add(valor, BorderLayout.EAST);
        return fila;
    }

    private TitledBorder titledBorder(String title, Color color) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60), 1),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Monospaced", Font.BOLD, 10), color);
    }
}
