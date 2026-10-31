package kernel;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.UIManager;

/**
 * Paleta y utilidades de dibujo del tema oscuro de KernelLab.
 * Azul = predominante, turquesa = funcionamiento correcto,
 * amarillo = advertencias/ocupado, rojo = errores y deadlock.
 */
public final class Tema {
    private Tema() { }

    public static final Color FONDO        = new Color(0x0B1120);
    public static final Color SIDEBAR      = new Color(0x0D1526);
    public static final Color TARJETA      = new Color(0x17243A);
    public static final Color FILA_ALT     = new Color(0x1B2A44);
    public static final Color CAMPO        = new Color(0x0F1A2E);
    public static final Color BORDE        = new Color(0x2B3E5F);
    public static final Color AZUL         = new Color(0x3B82F6);
    public static final Color AZUL_CLARO   = new Color(0x60A5FA);
    public static final Color TURQUESA     = new Color(0x2DD4BF);
    public static final Color AMARILLO     = new Color(0xFBBF24);
    public static final Color ROJO         = new Color(0xF87171);
    public static final Color TEXTO        = new Color(0xF8FAFC);
    public static final Color TEXTO_SUAVE  = new Color(0x94A3B8);
    public static final Color GRIS         = new Color(0x64748B);
    public static final Color CONSOLA      = new Color(0x070C18);
    public static final Color SELECCION    = new Color(0x23406E);

    /** Colores para distinguir procesos (memoria, Gantt, grafo). */
    private static final Color[] PALETA_PID = {
        new Color(0x60A5FA), new Color(0x34D399), new Color(0xA78BFA), new Color(0xF472B6),
        new Color(0x38BDF8), new Color(0xFB923C), new Color(0x818CF8), new Color(0x4ADE80),
        new Color(0xE879F9), new Color(0x22D3EE)
    };

    public static Color colorPid(int pid) {
        return PALETA_PID[Math.floorMod(pid - 1, PALETA_PID.length)];
    }

    public static Color colorEstado(String estado) {
        switch (estado) {
            case "LISTO":      return TURQUESA;
            case "EJECUTANDO": return AZUL_CLARO;
            case "BLOQUEADO":  return AMARILLO;
            case "ABORTADO":   return ROJO;
            case "TERMINADO":  return GRIS;
            default:           return TEXTO_SUAVE;
        }
    }

    public static Color fondoFila(int fila, boolean seleccionada) {
        return seleccionada ? SELECCION : (fila % 2 == 0 ? TARJETA : FILA_ALT);
    }

    public static Color alfa(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    public static Font fuente(int estilo, float tam) {
        return new Font(Font.SANS_SERIF, estilo, 12).deriveFont(tam);
    }

    public static Font mono(float tam) {
        return new Font(Font.MONOSPACED, Font.PLAIN, 12).deriveFont(tam);
    }

    public static void calidad(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /** Texto centrado horizontalmente en [x, x+w] con la línea base en y. */
    public static void textoCentrado(Graphics2D g, String s, int x, int w, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, x + (w - fm.stringWidth(s)) / 2, y);
    }

    /** Recorta con puntos suspensivos si el texto no cabe en 'ancho'. */
    public static String ajustar(FontMetrics fm, String s, int ancho) {
        if (fm.stringWidth(s) <= ancho) return s;
        String t = s;
        while (t.length() > 1 && fm.stringWidth(t + "...") > ancho) t = t.substring(0, t.length() - 1);
        return t + "...";
    }

    /** Aplica colores del tema a los componentes estándar de Swing que usan UIManager. */
    public static void instalar() {
        UIManager.put("ComboBox.selectionBackground", AZUL);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.background", CAMPO);
        UIManager.put("ComboBox.foreground", TEXTO);
        UIManager.put("ToolTip.background", new Color(0x0F1A2E));
        UIManager.put("ToolTip.foreground", TEXTO);
        UIManager.put("ToolTip.border", javax.swing.BorderFactory.createLineBorder(BORDE));
    }
}
