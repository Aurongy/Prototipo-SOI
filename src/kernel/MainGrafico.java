package kernel;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada de la interfaz gráfica moderna del simulador. */
public class MainGrafico {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new VentanaKernel().setVisible(true));
    }
}
