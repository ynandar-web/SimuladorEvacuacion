package evacuation.gui.panel;

import evacuation.gui.canvas.GrafoCanvas;
import evacuation.gui.model.EstadoSimulacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Ventana principal de la GUI (JFrame).
 * Orquesta todos los sub-paneles y el GrafoCanvas.
 * Se suscribe como Observer del EstadoSimulacion y refresca la vista.
 * Principio SRP: solo gestiona el layout y la coordinación de paneles.
 */
public class VentanaPrincipal extends JFrame implements EstadoSimulacion.Observer {

    private final EstadoSimulacion estado = new EstadoSimulacion();

    // ── Sub-paneles ──────────────────────────────────────────────
    private final GrafoCanvas   canvasGrafo = new GrafoCanvas();
    private final PanelControl  panelControl;
    private final PanelPersonas panelPersonas = new PanelPersonas();
    private final PanelZonas    panelZonas    = new PanelZonas();
    private final PanelLog      panelLog      = new PanelLog();

    public VentanaPrincipal() {
        panelControl = new PanelControl(estado);
        estado.agregarObservador(this);

        construirUI();
        setTitle("Simulador de Evacuación — GUI v1.0");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null);
    }

    // ─────────────────────────────────────────────────────────────
    //  Layout
    // ─────────────────────────────────────────────────────────────
    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(new Color(6, 11, 20));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        // ── Cabecera ─────────────────────────────────────────────
        root.add(cabecera(), BorderLayout.NORTH);

        // ── Centro: grafo + paneles derechos ─────────────────────
        JSplitPane centro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                panelIzquierdo(), panelDerecho());
        centro.setDividerLocation(500);
        centro.setDividerSize(4);
        centro.setBackground(new Color(6, 11, 20));
        centro.setBorder(BorderFactory.createEmptyBorder());
        root.add(centro, BorderLayout.CENTER);

        // ── Log inferior ─────────────────────────────────────────
        root.add(panelLog, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel cabecera() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel sub = new JLabel("SISTEMA DE GESTIÓN DE EMERGENCIAS");
        sub.setFont(new Font("Monospaced", Font.BOLD, 10));
        sub.setForeground(new Color(74, 158, 255, 180));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titulo = new JLabel("SIMULADOR DE EVACUACIÓN");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 22));
        titulo.setForeground(new Color(147, 197, 253));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel version = new JLabel("GUI v1.0 — Clases Java sin modificar · Swing + Java2D");
        version.setFont(new Font("Monospaced", Font.PLAIN, 9));
        version.setForeground(new Color(200, 220, 240, 60));
        version.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(sub);
        p.add(Box.createVerticalStrut(2));
        p.add(titulo);
        p.add(Box.createVerticalStrut(2));
        p.add(version);
        p.add(Box.createVerticalStrut(10));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(74,158,255,40));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        p.add(sep);
        return p;
    }

    private JScrollPane panelIzquierdo() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(new Color(10, 14, 26));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(74,158,255,40), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel lblGrafo = new JLabel("GRAFO DEL EDIFICIO");
        lblGrafo.setFont(new Font("Monospaced", Font.BOLD, 10));
        lblGrafo.setForeground(new Color(74,158,255,180));
        lblGrafo.setBorder(BorderFactory.createEmptyBorder(0,0,6,0));

        p.add(lblGrafo,     BorderLayout.NORTH);
        p.add(canvasGrafo,  BorderLayout.CENTER);
        p.add(leyenda(),    BorderLayout.SOUTH);

        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        return sp;
    }

    private JPanel panelDerecho() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(6, 11, 20));
        p.add(panelControl);
        p.add(Box.createVerticalStrut(8));
        p.add(panelPersonas);
        p.add(Box.createVerticalStrut(8));
        p.add(panelZonas);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel leyenda() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.setOpaque(false);
        String[][] items = {
            {"HABITACIÓN","#1a2744","#4a9eff"},
            {"PASILLO","#1a3030","#2dd4bf"},
            {"ESCALERA","#2a1a44","#a855f7"},
            {"SALIDA","#0f2d1f","#22c55e"},
            {"BLOQUEADO","#330000","#dd4444"},
        };
        for (String[] it : items) {
            JLabel dot = new JLabel("■");
            try { dot.setForeground(Color.decode(it[2])); } catch(Exception ignored){}
            dot.setFont(new Font("Dialog", Font.PLAIN, 12));
            JLabel lbl = new JLabel(it[0]);
            lbl.setFont(new Font("Monospaced", Font.PLAIN, 9));
            lbl.setForeground(new Color(200,220,240,100));
            p.add(dot); p.add(lbl);
        }
        return p;
    }

    // ─────────────────────────────────────────────────────────────
    //  Observer: refrescar todos los paneles cuando cambia el estado
    // ─────────────────────────────────────────────────────────────
    @Override
    public void onCambio() {
        SwingUtilities.invokeLater(() -> {
            canvasGrafo.actualizar(estado.getGrafo(), estado.getRegistro(), estado.getRutasPersonas());
            panelPersonas.actualizar(estado.getRegistro());
            panelZonas.actualizar(estado.getArbol());
            panelLog.actualizar(estado.getLog());
            panelControl.actualizarEstado();
        });
    }
}
