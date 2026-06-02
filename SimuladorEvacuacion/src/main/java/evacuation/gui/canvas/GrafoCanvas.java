package evacuation.gui.canvas;

import evacuation.model.graph.Arista;
import evacuation.model.graph.Grafo;
import evacuation.model.graph.Nodo;
import evacuation.model.person.Persona;
import evacuation.util.structure.TablaHash;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Componente Swing que dibuja el grafo del edificio con Java2D.
 * Solo lee datos del Grafo y Persona — no los modifica.
 * Principio SRP: única responsabilidad es la visualización del grafo.
 */
public class GrafoCanvas extends JPanel {

    // ── Posiciones visuales de cada nodo ────────────────────────
    private static final Map<String, Point> POSICIONES = new LinkedHashMap<>();
    static {
        POSICIONES.put("HAB_01", new Point( 60,  40));
        POSICIONES.put("HAB_02", new Point(200,  40));
        POSICIONES.put("HAB_03", new Point(340,  40));
        POSICIONES.put("PAS_01", new Point(200, 130));
        POSICIONES.put("ESC_A",  new Point(120, 230));
        POSICIONES.put("ESC_B",  new Point(280, 230));
        POSICIONES.put("EXIT_A", new Point( 60, 330));
        POSICIONES.put("EXIT_B", new Point(340, 330));
        POSICIONES.put("HAB_04", new Point( 60, 450));
        POSICIONES.put("HAB_05", new Point(200, 450));
        POSICIONES.put("HAB_06", new Point(340, 450));
        POSICIONES.put("PAS_02", new Point(200, 360));
    }

    // ── Paleta de colores por tipo ───────────────────────────────
    private static final Map<Nodo.TipoNodo, Color[]> COLORES = new EnumMap<>(Nodo.TipoNodo.class);
    static {
        COLORES.put(Nodo.TipoNodo.HABITACION, new Color[]{ new Color(20,35,70),  new Color(74,158,255) });
        COLORES.put(Nodo.TipoNodo.PASILLO,    new Color[]{ new Color(20,50,48),  new Color(45,212,191) });
        COLORES.put(Nodo.TipoNodo.ESCALERA,   new Color[]{ new Color(42,26,68),  new Color(168,85,247) });
        COLORES.put(Nodo.TipoNodo.SALIDA,     new Color[]{ new Color(10,45,30),  new Color(34,197,94)  });
    }

    private static final Color COLOR_BLOQUEADO = new Color(220, 40, 40);
    private static final Color COLOR_RUTA      = new Color(34, 197, 94);
    private static final Color COLOR_FONDO     = new Color(8, 14, 26);
    private static final Color COLOR_GRID      = new Color(255, 255, 255, 8);
    private static final Color COLOR_SEPARADOR = new Color(255, 255, 255, 18);

    // Dimensiones del nodo
    private static final int NW = 80, NH = 36, NR = 7;

    // ── Estado de visualización ──────────────────────────────────
    private Grafo                          grafo;
    private TablaHash<String, Persona>     personas;
    private Map<String, List<String>>      rutasActivas = new HashMap<>();
    private String                         nodoResaltado;

    public GrafoCanvas() {
        setPreferredSize(new Dimension(480, 560));
        setBackground(COLOR_FONDO);
    }

    /** Actualiza los datos y repinta. */
    public void actualizar(Grafo grafo,
                           TablaHash<String, Persona> personas,
                           Map<String, List<String>> rutas) {
        this.grafo        = grafo;
        this.personas     = personas;
        this.rutasActivas = rutas != null ? rutas : new HashMap<>();
        repaint();
    }

    public void resaltarNodo(String nodoId) {
        this.nodoResaltado = nodoId;
        repaint();
    }

    // ─────────────────────────────────────────────────────────────
    //  Pintado
    // ─────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();

        // Fondo
        g2.setColor(COLOR_FONDO);
        g2.fillRect(0, 0, W, H);

        // Grid
        g2.setColor(COLOR_GRID);
        g2.setStroke(new BasicStroke(1f));
        for (int x = 0; x < W; x += 40) { g2.drawLine(x, 0, x, H); }
        for (int y = 0; y < H; y += 40) { g2.drawLine(0, y, W, y); }

        // Línea separadora de pisos
        g2.setColor(COLOR_SEPARADOR);
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                     10f, new float[]{6,6}, 0));
        g2.drawLine(10, 295, W-10, 295);
        g2.setStroke(new BasicStroke(1f));
        g2.setFont(new Font("Monospaced", Font.BOLD, 9));
        g2.setColor(new Color(255,255,255,35));
        g2.drawString("PISO 1", 10, 290);
        g2.drawString("PISO 2", 10, 315);

        if (grafo == null) {
            g2.setColor(new Color(100,150,200,120));
            g2.setFont(new Font("Monospaced", Font.BOLD, 13));
            g2.drawString("Presiona INICIAR para comenzar", 80, H/2);
            return;
        }

        pintarAristas(g2);
        pintarNodos(g2);
    }

    private void pintarAristas(Graphics2D g2) {
        for (var entry : grafo.getNodos()) {
            for (var arista : getAllAristas(grafo, entry.getId())) {
                Point from = POSICIONES.get(arista.getOrigen().getId());
                Point to   = POSICIONES.get(arista.getDestino().getId());
                if (from == null || to == null) continue;

                int fx = from.x + NW/2, fy = from.y + NH/2;
                int tx = to.x   + NW/2, ty = to.y   + NH/2;

                boolean enRuta = enRutaActiva(arista.getOrigen().getId(), arista.getDestino().getId());

                if (arista.isBloqueada()) {
                    g2.setColor(COLOR_BLOQUEADO);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                 0f, new float[]{5,4}, 0));
                } else if (enRuta) {
                    g2.setColor(COLOR_RUTA);
                    g2.setStroke(new BasicStroke(2.8f));
                    // glow
                    g2.setColor(new Color(34,197,94,40));
                    g2.setStroke(new BasicStroke(6f));
                    g2.drawLine(fx, fy, tx, ty);
                    g2.setColor(COLOR_RUTA);
                    g2.setStroke(new BasicStroke(2.2f));
                } else {
                    g2.setColor(new Color(100,160,255,60));
                    g2.setStroke(new BasicStroke(1.5f));
                }

                g2.drawLine(fx, fy, tx, ty);
                g2.setStroke(new BasicStroke(1f));

                // Peso o símbolo de bloqueo
                int mx = (fx+tx)/2, my = (fy+ty)/2;
                g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
                if (arista.isBloqueada()) {
                    g2.setColor(COLOR_BLOQUEADO);
                    g2.drawString("BLOQ", mx-14, my-3);
                } else {
                    g2.setColor(new Color(180,210,255,140));
                    g2.drawString(arista.getPeso()+"s", mx+3, my-3);
                }
            }
        }
    }

    private void pintarNodos(Graphics2D g2) {
        for (Nodo nodo : grafo.getNodos()) {
            Point pos = POSICIONES.get(nodo.getId());
            if (pos == null) continue;

            Color[] col    = COLORES.getOrDefault(nodo.getTipo(), new Color[]{ Color.DARK_GRAY, Color.GRAY });
            boolean caliente = nodo.getId().equals(nodoResaltado);

            // Glow si está resaltado
            if (caliente) {
                for (int r = 16; r > 0; r -= 4) {
                    g2.setColor(new Color(col[1].getRed(), col[1].getGreen(), col[1].getBlue(), 20));
                    g2.fill(roundRect(pos.x - r/2, pos.y - r/2, NW + r, NH + r, NR + r/2));
                }
            }

            // Cuerpo del nodo
            g2.setColor(col[0]);
            g2.fill(roundRect(pos.x, pos.y, NW, NH, NR));
            g2.setColor(caliente ? Color.WHITE : col[1]);
            g2.setStroke(new BasicStroke(caliente ? 2.2f : 1.4f));
            g2.draw(roundRect(pos.x, pos.y, NW, NH, NR));
            g2.setStroke(new BasicStroke(1f));

            // Texto ID
            g2.setFont(new Font("Monospaced", Font.BOLD, 9));
            g2.setColor(col[1]);
            drawCentered(g2, nodo.getId(), pos.x, pos.y + 14, NW);

            // Texto tipo
            g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
            g2.setColor(new Color(255,255,255,90));
            drawCentered(g2, nodo.getTipo().name(), pos.x, pos.y + 26, NW);

            // Badge de personas presentes
            if (personas != null) {
                long count = personas.valores().stream()
                        .filter(p -> !p.isEvacuado() && p.getHabitacionActual().equals(nodo.getId()))
                        .count();
                if (count > 0) {
                    int bx = pos.x + NW - 10, by = pos.y + 2;
                    g2.setColor(new Color(245,158,11));
                    g2.fillOval(bx, by, 16, 16);
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Monospaced", Font.BOLD, 9));
                    g2.drawString(String.valueOf(count), bx + 4, by + 11);
                }
            }
        }
    }

    // ── Utilidades ───────────────────────────────────────────────

    private List<Arista> getAllAristas(Grafo g, String nodoId) {
        // Accedemos a todas las aristas (bloqueadas + activas) usando reflexión mínima
        // o bien exponemos un método en Grafo — aquí usamos getAristasActivas + isBloqueada
        // Para obtener también las bloqueadas, iteramos directamente sobre el grafo.
        // Grafo expone getNodos() y getAristasActivas(). Para las bloqueadas hacemos
        // un truco: pedimos activas y además revisamos via toString el campo bloqueada.
        // La solución limpia: agregar getTodasAristas() en Grafo. Como NO modificamos
        // clases, usamos reflexión para acceder al campo "adyacencia".
        try {
            var field = Grafo.class.getDeclaredField("adyacencia");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            var map = (Map<String, List<Arista>>) field.get(g);
            return map.getOrDefault(nodoId, Collections.emptyList());
        } catch (Exception e) {
            return g.getAristasActivas(nodoId); // fallback: solo activas
        }
    }

    private boolean enRutaActiva(String origen, String destino) {
        for (List<String> ruta : rutasActivas.values()) {
            for (int i = 0; i < ruta.size() - 1; i++) {
                if (ruta.get(i).equals(origen) && ruta.get(i+1).equals(destino)) return true;
            }
        }
        return false;
    }

    private static RoundRectangle2D roundRect(int x, int y, int w, int h, int r) {
        return new RoundRectangle2D.Float(x, y, w, h, r, r);
    }

    private static void drawCentered(Graphics2D g2, String text, int x, int y, int width) {
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (width - fm.stringWidth(text)) / 2;
        g2.drawString(text, tx, y);
    }
}
