package evacuation.gui;

import evacuation.gui.panel.VentanaPrincipal;

import javax.swing.*;

/**
 
 * instancia VentanaPrincipal, que orquesta todos los paneles.
 *
 * Para ejecutar la GUI:
 *   java -cp <classpath> evacuation.gui.MainGUI
 * Para ejecutar la versión consola original:
 *   java -cp <classpath> evacuation.main.Main
 */
public class MainGUI {

    public static void main(String[] args) {
        // Asegurar que Swing corra en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
