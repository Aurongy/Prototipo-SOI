package kernel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Polygon;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/** Componentes Swing con el estilo de KernelLab (tarjetas, botones, tablas, combos). */
public final class Widgets {
    private Widgets() { }

    // ------------------------------------------------------------------
    // Tarjeta (panel redondeado con borde sutil)
    // ------------------------------------------------------------------
    public static class Tarjeta extends JPanel {
        private static final long serialVersionUID = 1L;

        public Tarjeta(LayoutManager lm) {
            super(lm);
            setOpaque(false);
            setBorder(new EmptyBorder(14, 16, 14, 16));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            g.setColor(Tema.TARJETA);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g.setColor(Tema.BORDE);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    // ------------------------------------------------------------------
    // Botones
    // ------------------------------------------------------------------
    public static class Boton extends JButton {
        private static final long serialVersionUID = 1L;

        public enum Tipo { PRIMARIO, SECUNDARIO, PELIGRO, EXITO, ADVERTENCIA }

        private final Tipo tipo;

        public Boton(String texto, Tipo tipo) {
            super(texto);
            this.tipo = tipo;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(Tema.fuente(Font.BOLD, 12.5f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(0, 0, 0, 0));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 34, 36);
        }

        @Override
        public Dimension getMinimumSize() { return getPreferredSize(); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            ButtonModel m = getModel();
            boolean on = isEnabled();
            boolean hov = on && m.isRollover();
            boolean pres = on && m.isPressed();
            Color fill;
            Color borde;
            Color fg;
            if (!on) {
                fill = Tema.alfa(Tema.BORDE, 60);
                borde = Tema.alfa(Tema.BORDE, 110);
                fg = Tema.alfa(Tema.TEXTO_SUAVE, 140);
            } else {
                switch (tipo) {
                    case PRIMARIO:
                        fill = pres ? new Color(0x2563EB) : hov ? new Color(0x5B9BFF) : Tema.AZUL;
                        borde = fill;
                        fg = Color.WHITE;
                        break;
                    case PELIGRO:
                        fill = Tema.alfa(Tema.ROJO, pres ? 110 : hov ? 80 : 45);
                        borde = Tema.ROJO;
                        fg = Tema.TEXTO;
                        break;
                    case EXITO:
                        fill = Tema.alfa(Tema.TURQUESA, pres ? 110 : hov ? 80 : 40);
                        borde = Tema.TURQUESA;
                        fg = Tema.TEXTO;
                        break;
                    case ADVERTENCIA:
                        fill = Tema.alfa(Tema.AMARILLO, pres ? 110 : hov ? 80 : 40);
                        borde = Tema.AMARILLO;
                        fg = Tema.TEXTO;
                        break;
                    default:
                        fill = pres ? new Color(0x2E4470) : hov ? new Color(0x2A3E63) : new Color(0x22334F);
                        borde = Tema.BORDE;
                        fg = Tema.TEXTO;
                }
            }
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g.setColor(borde);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g.setFont(getFont());
            g.setColor(fg);
            FontMetrics fm = g.getFontMetrics();
            String t = Tema.ajustar(fm, getText(), getWidth() - 12);
            g.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** Botón de la barra lateral con icono dibujado (seleccionable). */
    public static class BotonNav extends JToggleButton {
        private static final long serialVersionUID = 1L;
        private final String icono;

        public BotonNav(String texto, String icono) {
            super(texto);
            this.icono = icono;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(Tema.fuente(Font.BOLD, 13f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        @Override
        public Dimension getPreferredSize() { return new Dimension(200, 42); }

        @Override
        public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 42); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            boolean sel = isSelected();
            boolean hov = getModel().isRollover();
            if (sel) {
                g.setColor(Tema.alfa(Tema.AZUL, 55));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g.setColor(Tema.AZUL);
                g.fillRoundRect(0, 8, 4, getHeight() - 16, 4, 4);
            } else if (hov) {
                g.setColor(Tema.alfa(Color.WHITE, 12));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
            Iconos.dibujar(g, icono, 16, (getHeight() - 18) / 2, 18, sel ? Tema.AZUL_CLARO : Tema.TEXTO_SUAVE);
            g.setFont(getFont());
            g.setColor(sel || hov ? Tema.TEXTO : Tema.TEXTO_SUAVE);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), 46, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** Filtro tipo "chip" para la consola de eventos. */
    public static class Chip extends JToggleButton {
        private static final long serialVersionUID = 1L;
        private final Color color;

        public Chip(String texto, Color color) {
            super(texto, true);
            this.color = color;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(Tema.fuente(Font.BOLD, 11.5f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getFontMetrics(getFont()).stringWidth(getText()) + 28, 26);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            boolean on = isSelected();
            g.setColor(on ? Tema.alfa(color, 50) : Tema.alfa(Color.WHITE, 6));
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g.setColor(on ? color : Tema.BORDE);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g.setFont(getFont());
            g.setColor(on ? Tema.TEXTO : Tema.TEXTO_SUAVE);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** Etiqueta de estado tipo "píldora" (p. ej. EN LÍNEA). */
    public static class Pildora extends JComponent {
        private static final long serialVersionUID = 1L;
        private String texto;
        private Color color;

        public Pildora(String texto, Color color) {
            this.texto = texto;
            this.color = color;
        }

        public void set(String texto, Color color) {
            this.texto = texto;
            this.color = color;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getFontMetrics(Tema.fuente(Font.BOLD, 11f)).stringWidth(texto) + 34, 24);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            g.setColor(Tema.alfa(color, 40));
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g.setColor(color);
            g.fillOval(10, getHeight() / 2 - 3, 6, 6);
            g.setFont(Tema.fuente(Font.BOLD, 11f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(texto, 22, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Campos, combos, etiquetas, scroll
    // ------------------------------------------------------------------
    public static JLabel etiqueta(String texto, float tam, int estilo, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fuente(estilo, tam));
        l.setForeground(color);
        return l;
    }

    public static JLabel titulo(String texto) { return etiqueta(texto, 14f, Font.BOLD, Tema.TEXTO); }

    public static JLabel pista(String texto) { return etiqueta(texto, 12f, Font.PLAIN, Tema.TEXTO_SUAVE); }

    public static JTextField campo(int columnas) {
        final JTextField f = new JTextField(columnas);
        f.setBackground(Tema.CAMPO);
        f.setForeground(Tema.TEXTO);
        f.setCaretColor(Tema.TEXTO);
        f.setSelectionColor(Tema.AZUL);
        f.setSelectedTextColor(Color.WHITE);
        f.setFont(Tema.fuente(Font.PLAIN, 13f));
        f.setBorder(bordeCampo(Tema.BORDE));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.setBorder(bordeCampo(Tema.AZUL)); }
            @Override public void focusLost(FocusEvent e) { f.setBorder(bordeCampo(Tema.BORDE)); }
        });
        return f;
    }

    private static javax.swing.border.Border bordeCampo(Color c) {
        return BorderFactory.createCompoundBorder(new LineBorder(c, 1, true), new EmptyBorder(6, 10, 6, 10));
    }

    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        estilizarCombo(c, null);
        return c;
    }

    /** Aplica el estilo oscuro a un combo; 'formato' convierte cada elemento a texto (opcional). */
    public static void estilizarCombo(JComboBox<?> c, final Function<Object, String> formato) {
        c.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void paintComponent(Graphics g0) {
                        Graphics2D g = (Graphics2D) g0.create();
                        Tema.calidad(g);
                        g.setColor(Tema.TEXTO_SUAVE);
                        int cx = getWidth() / 2;
                        int cy = getHeight() / 2;
                        g.fill(new Polygon(new int[] { cx - 5, cx + 5, cx }, new int[] { cy - 2, cy - 2, cy + 3 }, 3));
                        g.dispose();
                    }
                };
                b.setBorder(BorderFactory.createEmptyBorder());
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                b.setPreferredSize(new Dimension(26, 20));
                return b;
            }
        });
        c.setBackground(Tema.CAMPO);
        c.setForeground(Tema.TEXTO);
        c.setFont(Tema.fuente(Font.PLAIN, 13f));
        c.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Tema.BORDE, 1, true), new EmptyBorder(3, 8, 3, 2)));
        c.setPreferredSize(new Dimension(Math.max(150, c.getPreferredSize().width), 36));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        c.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean sel, boolean foco) {
                super.getListCellRendererComponent(list, value, index, sel, foco);
                if (value == null) setText("(sin elementos)");
                else if (formato != null) setText(formato.apply(value));
                setBorder(new EmptyBorder(5, 8, 5, 8));
                setBackground(sel ? Tema.AZUL : Tema.CAMPO);
                setForeground(value == null ? Tema.TEXTO_SUAVE : Tema.TEXTO);
                return this;
            }
        });
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(null);
        s.setOpaque(false);
        s.getViewport().setBackground(Tema.TARJETA);
        s.getVerticalScrollBar().setUI(new BarraUI());
        s.getHorizontalScrollBar().setUI(new BarraUI());
        s.getVerticalScrollBar().setPreferredSize(new Dimension(10, 10));
        s.getHorizontalScrollBar().setPreferredSize(new Dimension(10, 10));
        s.getVerticalScrollBar().setBackground(Tema.TARJETA);
        s.getHorizontalScrollBar().setBackground(Tema.TARJETA);
        return s;
    }

    /** Barra de desplazamiento delgada y oscura. */
    private static class BarraUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(0x3A5078);
            trackColor = Tema.TARJETA;
        }

        @Override
        protected JButton createDecreaseButton(int o) { return cero(); }

        @Override
        protected JButton createIncreaseButton(int o) { return cero(); }

        private JButton cero() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle r) {
            g.setColor(Tema.TARJETA);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g0, JComponent c, java.awt.Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            g.setColor(thumbColor);
            g.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 8, 8);
            g.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Disposición que ajusta los controles en varias líneas al redimensionar
    // ------------------------------------------------------------------
    public static class WrapLayout extends FlowLayout {
        private static final long serialVersionUID = 1L;

        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override
        public Dimension preferredLayoutSize(Container target) { return tamano(target, true); }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension m = tamano(target, false);
            m.width -= (getHgap() + 1);
            return m;
        }

        private Dimension tamano(Container target, boolean preferido) {
            synchronized (target.getTreeLock()) {
                Container c = target;
                while (c.getSize().width == 0 && c.getParent() != null) c = c.getParent();
                int ancho = c.getSize().width;
                if (ancho == 0) ancho = Integer.MAX_VALUE;
                int hgap = getHgap();
                int vgap = getVgap();
                Insets in = target.getInsets();
                int extra = in.left + in.right + hgap * 2;
                int max = ancho - extra;
                Dimension dim = new Dimension(0, 0);
                int filaAncho = 0;
                int filaAlto = 0;
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (!m.isVisible()) continue;
                    Dimension d = preferido ? m.getPreferredSize() : m.getMinimumSize();
                    if (filaAncho + d.width > max && filaAncho > 0) {
                        agregarFila(dim, filaAncho, filaAlto);
                        filaAncho = 0;
                        filaAlto = 0;
                    }
                    if (filaAncho != 0) filaAncho += hgap;
                    filaAncho += d.width;
                    filaAlto = Math.max(filaAlto, d.height);
                }
                agregarFila(dim, filaAncho, filaAlto);
                dim.width += extra;
                dim.height += in.top + in.bottom + vgap * 2;
                if (SwingUtilities.getAncestorOfClass(JScrollPane.class, target) != null && target.isValid()) {
                    dim.width -= (hgap + 1);
                }
                return dim;
            }
        }

        private void agregarFila(Dimension dim, int ancho, int alto) {
            dim.width = Math.max(dim.width, ancho);
            if (dim.height > 0) dim.height += getVgap();
            dim.height += alto;
        }
    }

    // ------------------------------------------------------------------
    // Tablas
    // ------------------------------------------------------------------
    public static void estilizarTabla(JTable t) {
        t.setBackground(Tema.TARJETA);
        t.setForeground(Tema.TEXTO);
        t.setRowHeight(38);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(Tema.SELECCION);
        t.setSelectionForeground(Tema.TEXTO);
        t.setFont(Tema.fuente(Font.PLAIN, 13f));
        t.setDefaultRenderer(Object.class, new RenderCelda());
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new RenderEncabezado());
        h.setPreferredSize(new Dimension(100, 34));
        h.setBackground(Tema.CAMPO);
    }

    public static class RenderCelda extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            super.getTableCellRendererComponent(t, v, false, false, fila, col);
            setOpaque(true);
            setBackground(Tema.fondoFila(fila, sel));
            setForeground(Tema.TEXTO);
            setFont(Tema.fuente(Font.PLAIN, 13f));
            setBorder(new EmptyBorder(0, 14, 0, 14));
            return this;
        }
    }

    public static class RenderEncabezado extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            super.getTableCellRendererComponent(t, v == null ? "" : v.toString().toUpperCase(), false, false, fila, col);
            setOpaque(true);
            setBackground(Tema.CAMPO);
            setForeground(Tema.TEXTO_SUAVE);
            setFont(Tema.fuente(Font.BOLD, 11f));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE), new EmptyBorder(0, 14, 0, 14)));
            return this;
        }
    }

    /** Muestra el estado de un proceso como etiqueta de color (píldora). */
    public static class RenderEstado extends JComponent implements TableCellRenderer {
        private static final long serialVersionUID = 1L;
        private String estado = "";
        private Color fondo = Tema.TARJETA;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            estado = String.valueOf(v);
            fondo = Tema.fondoFila(fila, sel);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            g.setColor(fondo);
            g.fillRect(0, 0, getWidth(), getHeight());
            Color c = Tema.colorEstado(estado);
            g.setFont(Tema.fuente(Font.BOLD, 11f));
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(estado) + 26;
            int h = 24;
            int x = 12;
            int y = (getHeight() - h) / 2;
            g.setColor(Tema.alfa(c, 45));
            g.fillRoundRect(x, y, w, h, 24, 24);
            g.setColor(c);
            g.drawRoundRect(x, y, w, h, 24, 24);
            g.fillOval(x + 9, y + h / 2 - 3, 6, 6);
            g.setColor(Tema.TEXTO);
            g.drawString(estado, x + 20, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** Barra de progreso de ráfaga: el valor es int[]{restante, total}. */
    public static class RenderProgreso extends JComponent implements TableCellRenderer {
        private static final long serialVersionUID = 1L;
        private int restante;
        private int total = 1;
        private Color fondo = Tema.TARJETA;
        private boolean abortado;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            int[] d = (int[]) v;
            restante = d[0];
            total = Math.max(1, d[1]);
            abortado = d.length > 2 && d[2] == 1;
            fondo = Tema.fondoFila(fila, sel);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            g.setColor(fondo);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(Tema.fuente(Font.PLAIN, 12f));
            FontMetrics fm = g.getFontMetrics();
            String txt = restante + " / " + total + " u.";
            int tw = fm.stringWidth(txt);
            int x = 14;
            int w = Math.max(30, getWidth() - tw - 44);
            int h = 10;
            int y = (getHeight() - h) / 2;
            g.setColor(new Color(0x0F1A2E));
            g.fillRoundRect(x, y, w, h, 10, 10);
            double hecho = (total - restante) / (double) total;
            g.setColor(abortado ? Tema.ROJO : restante == 0 ? Tema.TURQUESA : Tema.AZUL);
            g.fillRoundRect(x, y, Math.max(hecho > 0 ? 6 : 0, (int) (w * hecho)), h, 10, 10);
            g.setColor(Tema.TEXTO_SUAVE);
            g.drawString(txt, x + w + 10, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Iconos vectoriales simples para la barra lateral
    // ------------------------------------------------------------------
    public static final class Iconos {
        private Iconos() { }

        public static void dibujar(Graphics2D g0, String tipo, int x, int y, int s, Color c) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            g.setColor(c);
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (tipo) {
                case "cpu":
                    g.drawRoundRect(x + 4, y + 4, s - 8, s - 8, 3, 3);
                    g.fillRect(x + 8, y + 8, s - 16, s - 16);
                    for (int i = 0; i < 3; i++) {
                        int p = x + 5 + i * (s - 10) / 2;
                        int q = y + 5 + i * (s - 10) / 2;
                        g.drawLine(p, y + 1, p, y + 4);
                        g.drawLine(p, y + s - 4, p, y + s - 1);
                        g.drawLine(x + 1, q, x + 4, q);
                        g.drawLine(x + s - 4, q, x + s - 1, q);
                    }
                    break;
                case "ram":
                    g.drawRoundRect(x + 1, y + 4, s - 2, s - 9, 3, 3);
                    for (int i = 0; i < 3; i++) g.fillRect(x + 4 + i * ((s - 8) / 3 + 1), y + 7, 3, s - 15);
                    g.drawLine(x + 4, y + s - 4, x + 4, y + s - 1);
                    g.drawLine(x + s - 4, y + s - 4, x + s - 4, y + s - 1);
                    break;
                case "lock":
                    g.drawRoundRect(x + 3, y + 8, s - 6, s - 10, 3, 3);
                    g.drawArc(x + 5, y + 1, s - 10, 14, 0, 180);
                    g.fillOval(x + s / 2 - 2, y + s / 2 + 1, 4, 4);
                    break;
                case "graph":
                    g.drawLine(x + s / 2, y + 3, x + 3, y + s - 4);
                    g.drawLine(x + s / 2, y + 3, x + s - 3, y + s - 4);
                    g.drawLine(x + 3, y + s - 4, x + s - 3, y + s - 4);
                    g.fillOval(x + s / 2 - 3, y, 7, 7);
                    g.fillOval(x, y + s - 7, 7, 7);
                    g.fillOval(x + s - 7, y + s - 7, 7, 7);
                    break;
                case "flask":
                    g.drawLine(x + 6, y + 2, x + 6, y + 8);
                    g.drawLine(x + s - 6, y + 2, x + s - 6, y + 8);
                    g.drawLine(x + 6, y + 8, x + 2, y + s - 2);
                    g.drawLine(x + s - 6, y + 8, x + s - 2, y + s - 2);
                    g.drawLine(x + 2, y + s - 2, x + s - 2, y + s - 2);
                    g.drawLine(x + 4, y + 2, x + s - 4, y + 2);
                    break;
                default:
                    g.drawRect(x + 2, y + 2, s - 4, s - 4);
            }
            g.dispose();
        }
    }
}
