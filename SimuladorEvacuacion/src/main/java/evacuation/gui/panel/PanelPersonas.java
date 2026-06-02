package evacuation.gui.panel;

import evacuation.model.person.Persona;
import evacuation.util.structure.TablaHash;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Collection;

/**
 * Panel que muestra el estado de cada Persona registrada.
 * Solo lee la TablaHash<String, Persona> — no la modifica.
 */
public class PanelPersonas extends JPanel {

    private final JPanel contenedor = new JPanel();

    public PanelPersonas() {
        setLayout(new BorderLayout());
        setBackground(new Color(10, 14, 26));
        setBorder(titledBorder("OCUPANTES", new Color(34, 197, 94)));

        contenedor.setLayout(new GridLayout(0, 2, 5, 5));
        contenedor.setBackground(new Color(10, 14, 26));
        contenedor.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JScrollPane scroll = new JScrollPane(contenedor);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll, BorderLayout.CENTER);
    }

    public void actualizar(TablaHash<String, Persona> registro) {
        contenedor.removeAll();
        if (registro == null) { repaint(); return; }

        Collection<Persona> personas = registro.valores();
        for (Persona p : personas) {
            contenedor.add(tarjetaPersona(p));
        }
        revalidate();
        repaint();
    }

    private JPanel tarjetaPersona(Persona p) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        Color acento = p.isEvacuado() ? new Color(34,197,94) : new Color(74,158,255);
        card.setBackground(new Color(acento.getRed(), acento.getGreen(), acento.getBlue(), 18));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(acento.getRed(), acento.getGreen(), acento.getBlue(), 100), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        JLabel nombre = new JLabel((p.isEvacuado() ? "✅ " : "👤 ") + p.getNombre());
        nombre.setFont(new Font("Monospaced", Font.BOLD, 9));
        nombre.setForeground(acento);

        String info = p.isEvacuado()
                ? "EVACUADO"
                : p.getHabitacionActual() + " · P" + p.getPiso();
        JLabel estado = new JLabel(info);
        estado.setFont(new Font("Monospaced", Font.PLAIN, 9));
        estado.setForeground(new Color(200, 220, 240, 130));

        card.add(nombre);
        card.add(Box.createVerticalStrut(2));
        card.add(estado);
        return card;
    }

    private TitledBorder titledBorder(String title, Color color) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60), 1),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Monospaced", Font.BOLD, 10), color);
    }
}
