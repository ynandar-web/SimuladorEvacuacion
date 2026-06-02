package evacuation.gui.panel;

import evacuation.gui.model.EstadoSimulacion;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Panel de control: botones Iniciar / Avanzar Turno / Auto / Reset,
 * selector de algoritmo y checkboxes de bloqueo de aristas.
 * Principio SRP: solo gestiona la entrada del usuario.
 */
public class PanelControl extends JPanel {

    private final EstadoSimulacion estado;

    // ── Widgets ──────────────────────────────────────────────────
    private final JRadioButton rbDijkstra   = new JRadioButton("Dijkstra", true);
    private final JRadioButton rbBFS        = new JRadioButton("BFS");
    private final JCheckBox    chkEscAExitA = new JCheckBox("ESC_A → EXIT_A (humo)", true);
    private final JCheckBox    chkEscBExitB = new JCheckBox("ESC_B → EXIT_B");
    private final JCheckBox    chkPas1EscA  = new JCheckBox("PAS_01 → ESC_A");
    private final JCheckBox    chkPas2EscA  = new JCheckBox("PAS_02 → ESC_A");
    private final JButton      btnIniciar   = new JButton("▶  INICIAR");
    private final JButton      btnTurno     = new JButton("⏭  TURNO");
    private final JButton      btnAuto      = new JButton("⚡  AUTO");
    private final JButton      btnReset     = new JButton("↺  RESET");
    private final JLabel       lblTurno     = new JLabel("Turno: 0");
    private final JProgressBar barProgreso  = new JProgressBar(0, 100);

    private Timer timerAuto;
    private boolean autoActivo = false;

    public PanelControl(EstadoSimulacion estado) {
        this.estado = estado;
        construirUI();
        conectarEventos();
        actualizarEstado();
    }

    // ─────────────────────────────────────────────────────────────
    //  Construcción de la UI
    // ─────────────────────────────────────────────────────────────
    private void construirUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(10, 14, 26));
        setBorder(titledBorder("CONTROL DE SIMULACIÓN", new Color(168, 85, 247)));

        // ── Algoritmo ────────────────────────────────────────────
        JPanel pAlg = seccion("Algoritmo de ruta");
        ButtonGroup bg = new ButtonGroup(); bg.add(rbDijkstra); bg.add(rbBFS);
        estilizarRadio(rbDijkstra); estilizarRadio(rbBFS);
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.setOpaque(false); fila.add(rbDijkstra); fila.add(rbBFS);
        pAlg.add(fila);
        add(pAlg); add(Box.createVerticalStrut(8));

        // ── Bloqueos ─────────────────────────────────────────────
        JPanel pBlq = seccion("Simular humo / incendio");
        for (JCheckBox chk : new JCheckBox[]{chkEscAExitA, chkEscBExitB, chkPas1EscA, chkPas2EscA}) {
            estilizarCheck(chk);
            pBlq.add(chk);
        }
        add(pBlq); add(Box.createVerticalStrut(8));

        // ── Botones ───────────────────────────────────────────────
        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pBtn.setOpaque(false);
        estilizarBoton(btnIniciar, new Color(34, 197, 94));
        estilizarBoton(btnTurno,  new Color(74, 158, 255));
        estilizarBoton(btnAuto,   new Color(99, 102, 241));
        estilizarBoton(btnReset,  new Color(239, 68, 68));
        pBtn.add(btnIniciar); pBtn.add(btnTurno); pBtn.add(btnAuto); pBtn.add(btnReset);
        add(pBtn); add(Box.createVerticalStrut(8));

        // ── Progreso ─────────────────────────────────────────────
        JPanel pProg = seccion("Progreso");
        lblTurno.setForeground(new Color(74, 158, 255));
        lblTurno.setFont(new Font("Monospaced", Font.BOLD, 11));
        barProgreso.setStringPainted(true);
        barProgreso.setForeground(new Color(34, 197, 94));
        barProgreso.setBackground(new Color(13, 27, 46));
        barProgreso.setBorder(BorderFactory.createEmptyBorder());
        barProgreso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
        pProg.add(lblTurno);
        pProg.add(Box.createVerticalStrut(4));
        pProg.add(barProgreso);
        add(pProg);
    }

    // ─────────────────────────────────────────────────────────────
    //  Eventos
    // ─────────────────────────────────────────────────────────────
    private void conectarEventos() {
        rbDijkstra.addActionListener(e -> estado.setAlgoritmo("DIJKSTRA"));
        rbBFS.addActionListener(e      -> estado.setAlgoritmo("BFS"));

        chkEscAExitA.addActionListener(e -> estado.setBloqueadoEscAExitA(chkEscAExitA.isSelected()));
        chkEscBExitB.addActionListener(e -> estado.setBloqueadoEscBExitB(chkEscBExitB.isSelected()));
        chkPas1EscA.addActionListener(e  -> estado.setBloqueadoPas1EscA(chkPas1EscA.isSelected()));
        chkPas2EscA.addActionListener(e  -> estado.setBloqueadoPas2EscA(chkPas2EscA.isSelected()));

        btnIniciar.addActionListener(e -> { estado.iniciar(); actualizarEstado(); });
        btnTurno.addActionListener(e   -> { estado.avanzarTurno(); actualizarEstado(); });
        btnReset.addActionListener(e   -> {
            detenerAuto();
            estado.reset();
            actualizarEstado();
        });
        btnAuto.addActionListener(e -> toggleAuto());

        timerAuto = new Timer(800, e -> {
            if (estado.isFinalizada() || !estado.isIniciada()) {
                detenerAuto();
            } else {
                estado.avanzarTurno();
                actualizarEstado();
            }
        });
    }

    private void toggleAuto() {
        if (autoActivo) {
            detenerAuto();
        } else {
            autoActivo = true;
            btnAuto.setText("⏸  PAUSAR");
            timerAuto.start();
        }
    }

    private void detenerAuto() {
        autoActivo = false;
        timerAuto.stop();
        btnAuto.setText("⚡  AUTO");
    }

    // ─────────────────────────────────────────────────────────────
    //  Actualizar estado visual
    // ─────────────────────────────────────────────────────────────
    public void actualizarEstado() {
        boolean iniciada   = estado.isIniciada();
        boolean finalizada = estado.isFinalizada();

        btnIniciar.setEnabled(!iniciada);
        btnTurno.setEnabled(iniciada && !finalizada);
        btnAuto.setEnabled(iniciada && !finalizada);
        btnReset.setEnabled(iniciada);
        rbDijkstra.setEnabled(!iniciada);
        rbBFS.setEnabled(!iniciada);
        for (JCheckBox chk : new JCheckBox[]{chkEscAExitA, chkEscBExitB, chkPas1EscA, chkPas2EscA}) {
            chk.setEnabled(!iniciada);
        }

        lblTurno.setText("Turno: " + estado.getTurnoActual());

        if (iniciada && estado.getRegistro() != null) {
            long total     = estado.getRegistro().getTamanio();
            long evacuados = estado.getRegistro().valores().stream()
                             .filter(p -> p.isEvacuado()).count();
            int pct = total > 0 ? (int)(evacuados * 100 / total) : 0;
            barProgreso.setValue(pct);
            barProgreso.setString(evacuados + "/" + total + " evacuados");
        } else {
            barProgreso.setValue(0);
            barProgreso.setString("0%");
        }
    }

    // ── Estilos ──────────────────────────────────────────────────
    private void estilizarBoton(JButton btn, Color color) {
        btn.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 35));
        btn.setForeground(color);
        btn.setFont(new Font("Monospaced", Font.BOLD, 11));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 140), 1),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void estilizarRadio(JRadioButton rb) {
        rb.setForeground(new Color(147, 197, 253));
        rb.setFont(new Font("Monospaced", Font.PLAIN, 11));
        rb.setOpaque(false);
    }

    private void estilizarCheck(JCheckBox chk) {
        chk.setForeground(new Color(239, 68, 68));
        chk.setFont(new Font("Monospaced", Font.PLAIN, 10));
        chk.setOpaque(false);
    }

    private JPanel seccion(String titulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255,255,255,20), 1),
                titulo,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Monospaced", Font.BOLD, 9),
                new Color(200, 220, 240, 100));
        p.setBorder(BorderFactory.createCompoundBorder(tb,
                BorderFactory.createEmptyBorder(4, 6, 6, 6)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        return p;
    }

    private TitledBorder titledBorder(String title, Color color) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60), 1),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Monospaced", Font.BOLD, 10),
                color);
    }
}
