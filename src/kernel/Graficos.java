package kernel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/** Visualizaciones personalizadas (Java2D) del monitor: RAM, Round Robin, semáforo y deadlock. */
public final class Graficos {
    private Graficos() { }

    /** Dibuja un "chip" de proceso: nombre en grande y una línea de detalle. */
    private static void chip(Graphics2D g, int x, int y, int w, int h, Color c, String nombre, String detalle) {
        g.setColor(Tema.alfa(c, 55));
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(c);
        g.drawRoundRect(x, y, w, h, 12, 12);
        g.setFont(Tema.fuente(Font.BOLD, 14f));
        g.setColor(Tema.TEXTO);
        Tema.textoCentrado(g, nombre, x, w, y + 18);
        g.setFont(Tema.fuente(Font.PLAIN, 10.5f));
        g.setColor(Tema.TEXTO_SUAVE);
        Tema.textoCentrado(g, Tema.ajustar(g.getFontMetrics(), detalle, w - 6), x, w, y + h - 7);
    }

    // ------------------------------------------------------------------
    /** Barra de progreso redondeada (tarjeta de RAM). */
    public static class BarraProgreso extends JComponent {
        private static final long serialVersionUID = 1L;
        private double valor = 0;
        private Color color = Tema.TURQUESA;

        public void set(double valor, Color color) {
            this.valor = Math.max(0, Math.min(1, valor));
            this.color = color;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() { return new Dimension(100, 10); }

        @Override
        public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 10); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            g.setColor(new Color(0x2A3A57));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g.setColor(color);
            g.fillRoundRect(0, 0, Math.max(valor > 0 ? 8 : 0, (int) (getWidth() * valor)), getHeight(), 10, 10);
            g.dispose();
        }
    }

    // ------------------------------------------------------------------
    /** Barra segmentada de la RAM de 100 MB: una partición por proceso. */
    public static class PanelRAM extends JComponent {
        private static final long serialVersionUID = 1L;
        private List<int[]> particiones = new ArrayList<>();

        public void actualizar(List<int[]> p) {
            this.particiones = p;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() { return new Dimension(500, 104); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            int total = AdministradorMemoria.TOTAL_MB;
            int x0 = 2;
            int w = getWidth() - 6;
            int y0 = 6;
            int h = 60;
            g.setClip(new RoundRectangle2D.Float(x0, y0, w, h, 16, 16));
            g.setColor(Tema.CAMPO);
            g.fillRect(x0, y0, w, h);
            for (int[] b : particiones) {
                int x = x0 + b[0] * w / total;
                int bw = Math.max(1, b[1] * w / total);
                g.setFont(Tema.fuente(Font.BOLD, 12.5f));
                FontMetrics fm = g.getFontMetrics();
                if (b[2] >= 0) {
                    g.setColor(Tema.colorPid(b[2]));
                    g.fillRect(x, y0, bw, h);
                    g.setColor(new Color(0x0B1120));
                    String t = "P" + b[2] + "  " + b[1] + " MB";
                    if (fm.stringWidth(t) < bw - 8) Tema.textoCentrado(g, t, x, bw, y0 + h / 2 + 5);
                    g.setColor(Tema.alfa(Tema.FONDO, 160));
                    g.fillRect(x + bw - 2, y0, 2, h);
                } else {
                    g.setColor(Tema.TEXTO_SUAVE);
                    String t = "Libre  " + b[1] + " MB";
                    if (fm.stringWidth(t) < bw - 8) Tema.textoCentrado(g, t, x, bw, y0 + h / 2 + 5);
                }
            }
            g.setClip(null);
            g.setColor(Tema.BORDE);
            g.draw(new RoundRectangle2D.Float(x0, y0, w, h, 16, 16));
            g.setFont(Tema.fuente(Font.PLAIN, 11f));
            FontMetrics fm = g.getFontMetrics();
            for (int mb = 0; mb <= total; mb += 25) {
                int x = x0 + mb * w / total;
                g.setColor(Tema.TEXTO_SUAVE);
                g.drawLine(x, y0 + h + 2, x, y0 + h + 7);
                String t = mb + " MB";
                int tx = Math.min(Math.max(x - fm.stringWidth(t) / 2, 0), getWidth() - fm.stringWidth(t));
                g.drawString(t, tx, y0 + h + 22);
            }
            g.dispose();
        }
    }

    // ------------------------------------------------------------------
    /** CPU, cola de listos (FIFO) y procesos bloqueados. */
    public static class PanelCola extends JComponent {
        private static final long serialVersionUID = 1L;
        private List<PCB> cola = new ArrayList<>();
        private List<PCB> bloqueados = new ArrayList<>();
        private List<String> motivos = new ArrayList<>();
        private PCB ultimo;

        public void actualizar(List<PCB> cola, PCB ultimo, List<PCB> bloqueados, List<String> motivos) {
            this.cola = cola;
            this.ultimo = ultimo;
            this.bloqueados = bloqueados;
            this.motivos = motivos;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() { return new Dimension(500, 134); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            int by = 18;
            int bw = 108;
            int bh = 50;
            g.setColor(Tema.alfa(Tema.AZUL, 45));
            g.fillRoundRect(0, by, bw, bh, 12, 12);
            g.setColor(Tema.AZUL);
            g.drawRoundRect(0, by, bw, bh, 12, 12);
            g.setFont(Tema.fuente(Font.BOLD, 10.5f));
            g.setColor(Tema.TEXTO_SUAVE);
            g.drawString(ultimo == null ? "CPU" : "CPU (ÚLTIMO)", 10, by + 15);
            g.setFont(Tema.fuente(Font.BOLD, 18f));
            g.setColor(Tema.TEXTO);
            g.drawString(ultimo == null ? "ocioso" : ultimo.getNombre(), 10, by + 39);

            int qx = bw + 38;
            g.setColor(Tema.TEXTO_SUAVE);
            g.setStroke(new BasicStroke(1.6f));
            g.drawLine(bw + 8, by + bh / 2, bw + 26, by + bh / 2);
            g.fill(new Polygon(new int[] { bw + 26, bw + 20, bw + 20 }, new int[] { by + bh / 2, by + bh / 2 - 4, by + bh / 2 + 4 }, 3));

            g.setFont(Tema.fuente(Font.BOLD, 10.5f));
            g.drawString("COLA DE LISTOS (FIFO) - sale primero el de la izquierda", qx, by - 5);
            fila(g, qx, by, cola, null, Tema.TURQUESA, "r=");
            g.setFont(Tema.fuente(Font.BOLD, 10.5f));
            g.setColor(Tema.TEXTO_SUAVE);
            g.drawString("BLOQUEADOS", 0, 98);
            fila(g, qx, 84, bloqueados, motivos, Tema.AMARILLO, null);
            g.dispose();
        }

        private void fila(Graphics2D g, int x0, int y, List<PCB> lista, List<String> motivos, Color color, String prefijo) {
            int cw = 92;
            int ch = 44;
            int gap = 8;
            if (lista.isEmpty()) {
                g.setFont(Tema.fuente(Font.ITALIC, 12f));
                g.setColor(Tema.TEXTO_SUAVE);
                g.drawString(prefijo != null ? "vacía" : "ninguno", x0, y + 26);
                return;
            }
            int max = Math.max(1, (getWidth() - x0) / (cw + gap));
            for (int i = 0; i < lista.size(); i++) {
                int x = x0 + i * (cw + gap);
                if (i == max - 1 && lista.size() > max) {
                    g.setFont(Tema.fuente(Font.BOLD, 13f));
                    g.setColor(Tema.TEXTO_SUAVE);
                    g.drawString("+" + (lista.size() - i), x + 6, y + 26);
                    break;
                }
                PCB p = lista.get(i);
                String det = prefijo != null ? prefijo + p.getRafagaRestante() + " u." : (motivos != null ? motivos.get(i) : "");
                chip(g, x, y, cw, ch, color, p.getNombre(), det);
            }
        }
    }

    // ------------------------------------------------------------------
    /** Diagrama de Gantt de Round Robin (segmentos {pid, inicio, fin}). */
    public static class PanelGantt extends JComponent {
        private static final long serialVersionUID = 1L;
        private List<int[]> seg = new ArrayList<>();

        public void actualizar(List<int[]> seg) {
            this.seg = seg;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() { return new Dimension(500, 82); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            g.setFont(Tema.fuente(Font.BOLD, 10.5f));
            g.setColor(Tema.TEXTO_SUAVE);
            g.drawString("DIAGRAMA DE GANTT (unidades de CPU, q = " + PlanificadorRR.QUANTUM + ")", 0, 11);
            if (seg.isEmpty()) {
                g.setFont(Tema.fuente(Font.ITALIC, 12f));
                g.drawString("Aún no se ha ejecutado ningún turno.", 0, 44);
                g.dispose();
                return;
            }
            int total = Math.max(1, seg.get(seg.size() - 1)[2]);
            int ancho = getWidth() - 2;
            double esc = ancho / (double) total;
            int y = 20;
            int h = 32;
            int ultimoTick = -100;
            g.setFont(Tema.fuente(Font.BOLD, 12f));
            FontMetrics fm = g.getFontMetrics();
            for (int[] s : seg) {
                int x = (int) (s[1] * esc);
                int w = Math.max(2, (int) ((s[2] - s[1]) * esc) - 2);
                g.setColor(Tema.colorPid(s[0]));
                g.fillRoundRect(x, y, w, h, 8, 8);
                String t = "P" + s[0];
                if (fm.stringWidth(t) < w - 4) {
                    g.setColor(new Color(0x0B1120));
                    Tema.textoCentrado(g, t, x, w, y + h / 2 + 5);
                }
                if (x - ultimoTick >= 24) {
                    g.setColor(Tema.TEXTO_SUAVE);
                    g.setFont(Tema.fuente(Font.PLAIN, 10.5f));
                    g.drawString(String.valueOf(s[1]), x, y + h + 14);
                    g.setFont(Tema.fuente(Font.BOLD, 12f));
                    ultimoTick = x;
                }
            }
            g.setColor(Tema.TEXTO_SUAVE);
            g.setFont(Tema.fuente(Font.PLAIN, 10.5f));
            String fin = String.valueOf(total);
            g.drawString(fin, Math.max(0, ancho - g.getFontMetrics().stringWidth(fin)), y + h + 14);
            g.dispose();
        }
    }

    // ------------------------------------------------------------------
    /** Semáforo binario -> sección crítica -> cola de espera. */
    public static class PanelSemaforo extends JComponent {
        private static final long serialVersionUID = 1L;
        private int valor = 1;
        private PCB propietario;
        private List<PCB> cola = new ArrayList<>();

        public void actualizar(int valor, PCB propietario, List<PCB> cola) {
            this.valor = valor;
            this.propietario = propietario;
            this.cola = cola;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() { return new Dimension(700, 220); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            boolean libre = propietario == null;
            Color est = libre ? Tema.TURQUESA : Tema.AMARILLO;
            int W = getWidth();
            int H = getHeight();
            int top = 10;
            int h = Math.min(H - 20, 190);
            int gap = 46;
            int w1 = (int) ((W - 2 * gap) * 0.28);
            int w2 = (int) ((W - 2 * gap) * 0.30);
            int w3 = W - 2 * gap - w1 - w2;
            int x1 = 0;
            int x2 = w1 + gap;
            int x3 = x2 + w2 + gap;

            // Zona 1: semáforo
            caja(g, x1, top, w1, h, est);
            g.setFont(Tema.fuente(Font.BOLD, 11f));
            g.setColor(Tema.TEXTO_SUAVE);
            Tema.textoCentrado(g, "SEMÁFORO BINARIO", x1, w1, top + 24);
            g.setFont(Tema.fuente(Font.BOLD, 58f));
            g.setColor(est);
            Tema.textoCentrado(g, String.valueOf(valor), x1, w1, top + h / 2 + 22);
            g.setFont(Tema.fuente(Font.BOLD, 12f));
            String e = libre ? "LIBRE" : "OCUPADO";
            int pw = g.getFontMetrics().stringWidth(e) + 28;
            g.setColor(Tema.alfa(est, 45));
            g.fillRoundRect(x1 + (w1 - pw) / 2, top + h - 40, pw, 24, 24, 24);
            g.setColor(Tema.TEXTO);
            Tema.textoCentrado(g, e, x1, w1, top + h - 23);

            // Zona 2: sección crítica
            caja(g, x2, top, w2, h, libre ? Tema.BORDE : Tema.AMARILLO);
            g.setFont(Tema.fuente(Font.BOLD, 11f));
            g.setColor(Tema.TEXTO_SUAVE);
            Tema.textoCentrado(g, "SECCIÓN CRÍTICA", x2, w2, top + 24);
            g.setFont(Tema.fuente(Font.PLAIN, 10.5f));
            Tema.textoCentrado(g, "variable global compartida", x2, w2, top + 40);
            if (libre) {
                g.setFont(Tema.fuente(Font.ITALIC, 14f));
                Tema.textoCentrado(g, "vacía", x2, w2, top + h / 2 + 18);
            } else {
                chip(g, x2 + (w2 - 100) / 2, top + h / 2 - 8, 100, 52, Tema.colorPid(propietario.getPid()),
                    propietario.getNombre(), "dentro");
            }

            // Zona 3: cola
            caja(g, x3, top, w3, h, Tema.BORDE);
            g.setFont(Tema.fuente(Font.BOLD, 11f));
            g.setColor(Tema.TEXTO_SUAVE);
            Tema.textoCentrado(g, "COLA DE ESPERA (FIFO)", x3, w3, top + 24);
            if (cola.isEmpty()) {
                g.setFont(Tema.fuente(Font.ITALIC, 13f));
                Tema.textoCentrado(g, "sin procesos esperando", x3, w3, top + h / 2 + 12);
            } else {
                int cw = 78;
                int ch = 46;
                int porFila = Math.max(1, (w3 - 24) / (cw + 8));
                for (int i = 0; i < cola.size() && i < porFila * 3; i++) {
                    PCB p = cola.get(i);
                    int cx = x3 + 12 + (i % porFila) * (cw + 8);
                    int cy = top + 44 + (i / porFila) * (ch + 8);
                    chip(g, cx, cy, cw, ch, Tema.AMARILLO, p.getNombre(), i == 0 ? "siguiente" : "bloqueado");
                }
            }

            // Flechas entre zonas
            flecha(g, x1 + w1 + 6, top + h / 2, x2 - 6, "wait()");
            flecha(g, x2 + w2 + 6, top + h / 2, x3 - 6, null);
            g.dispose();
        }

        private void caja(Graphics2D g, int x, int y, int w, int h, Color borde) {
            g.setColor(Tema.CAMPO);
            g.fillRoundRect(x, y, w, h, 16, 16);
            g.setColor(borde);
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(x, y, w, h, 16, 16);
        }

        private void flecha(Graphics2D g, int x1, int y, int x2, String etiqueta) {
            g.setColor(Tema.TEXTO_SUAVE);
            g.setStroke(new BasicStroke(1.6f));
            g.drawLine(x1, y, x2 - 6, y);
            g.fill(new Polygon(new int[] { x2, x2 - 8, x2 - 8 }, new int[] { y, y - 4, y + 4 }, 3));
            if (etiqueta != null) {
                g.setFont(Tema.fuente(Font.PLAIN, 10f));
                Tema.textoCentrado(g, etiqueta, x1, x2 - x1, y - 6);
            }
        }
    }

    // ------------------------------------------------------------------
    /** Grafo de asignación de recursos: R1 y R2, procesos y la espera circular. */
    public static class PanelGrafo extends JComponent {
        private static final long serialVersionUID = 1L;
        private Recurso[] recursos = new Recurso[0];
        private List<PCB> ciclo = new ArrayList<>();

        public void actualizar(Recurso r1, Recurso r2, List<PCB> ciclo) {
            this.recursos = new Recurso[] { r1, r2 };
            this.ciclo = ciclo;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() { return new Dimension(700, 300); }

        private static final int RADIO = 28;
        private static final int RW = 118;
        private static final int RH = 62;

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.calidad(g);
            if (recursos.length < 2) { g.dispose(); return; }
            int W = getWidth();
            int H = getHeight() - 26;

            // Procesos involucrados (retienen o esperan algún recurso)
            List<PCB> procs = new ArrayList<>();
            for (Recurso r : recursos) {
                if (r.getPropietario() != null && !procs.contains(r.getPropietario())) procs.add(r.getPropietario());
                for (PCB q : r.getColaEspera()) if (!procs.contains(q)) procs.add(q);
            }
            procs.sort((a, b) -> Integer.compare(a.getPid(), b.getPid()));

            Point2D[] rc = { new Point2D.Double(W * 0.40, H / 2.0), new Point2D.Double(W * 0.60, H / 2.0) };
            List<Point2D> pc = new ArrayList<>();
            int arriba = (procs.size() + 1) / 2;
            int abajo = procs.size() / 2;
            for (int i = 0; i < procs.size(); i++) {
                boolean top = i % 2 == 0;
                int j = i / 2;
                int n = top ? arriba : abajo;
                pc.add(new Point2D.Double(W * (j + 1) / (double) (n + 1), top ? H * 0.17 : H * 0.83));
            }

            // Aristas
            for (int i = 0; i < 2; i++) {
                Recurso r = recursos[i];
                PCB dueno = r.getPropietario();
                boolean hayEsperaEnCiclo = false;
                for (PCB q : r.getColaEspera()) if (ciclo.contains(q)) hayEsperaEnCiclo = true;
                if (dueno != null) {
                    boolean crit = ciclo.contains(dueno) && hayEsperaEnCiclo;
                    arista(g, rc[i], false, pc.get(procs.indexOf(dueno)), true, crit ? Tema.ROJO : Tema.AZUL_CLARO,
                        false, crit, "retiene");
                }
                for (PCB q : r.getColaEspera()) {
                    boolean crit = ciclo.contains(q);
                    arista(g, pc.get(procs.indexOf(q)), true, rc[i], false, crit ? Tema.ROJO : Tema.AMARILLO,
                        true, crit, "solicita");
                }
            }

            // Nodos recurso
            for (int i = 0; i < 2; i++) {
                Recurso r = recursos[i];
                boolean ocupado = r.getPropietario() != null;
                Color c = ocupado ? Tema.AMARILLO : Tema.TURQUESA;
                int x = (int) rc[i].getX() - RW / 2;
                int y = (int) rc[i].getY() - RH / 2;
                g.setColor(Tema.CAMPO);
                g.fillRoundRect(x, y, RW, RH, 14, 14);
                g.setColor(c);
                g.setStroke(new BasicStroke(2f));
                g.drawRoundRect(x, y, RW, RH, 14, 14);
                g.setFont(Tema.fuente(Font.BOLD, 17f));
                g.setColor(Tema.TEXTO);
                Tema.textoCentrado(g, "R" + (i + 1), x, RW, y + 26);
                g.setFont(Tema.fuente(Font.PLAIN, 11.5f));
                g.setColor(c);
                Tema.textoCentrado(g, ocupado ? "ocupado por " + r.getPropietario().getNombre() : "libre", x, RW, y + 46);
            }

            // Nodos proceso
            for (int i = 0; i < procs.size(); i++) {
                PCB p = procs.get(i);
                boolean enCiclo = ciclo.contains(p);
                int x = (int) pc.get(i).getX() - RADIO;
                int y = (int) pc.get(i).getY() - RADIO;
                g.setColor(Tema.colorPid(p.getPid()));
                g.fillOval(x, y, RADIO * 2, RADIO * 2);
                g.setStroke(new BasicStroke(enCiclo ? 4f : 2f));
                g.setColor(enCiclo ? Tema.ROJO : Tema.alfa(Color.WHITE, 90));
                g.drawOval(x, y, RADIO * 2, RADIO * 2);
                g.setFont(Tema.fuente(Font.BOLD, 16f));
                g.setColor(new Color(0x0B1120));
                Tema.textoCentrado(g, p.getNombre(), x, RADIO * 2, y + RADIO + 6);
            }

            if (procs.isEmpty()) {
                g.setFont(Tema.fuente(Font.ITALIC, 13f));
                g.setColor(Tema.TEXTO_SUAVE);
                Tema.textoCentrado(g, "Ningún proceso usa recursos. Solicite recursos o use «Forzar deadlock».", 0, W, 28);
            } else if (!ciclo.isEmpty()) {
                g.setFont(Tema.fuente(Font.BOLD, 13f));
                g.setColor(Tema.ROJO);
                Tema.textoCentrado(g, "ESPERA CIRCULAR DETECTADA", 0, W, 20);
            }

            // Leyenda
            int ly = getHeight() - 8;
            g.setFont(Tema.fuente(Font.PLAIN, 11f));
            leyenda(g, 6, ly, Tema.AZUL_CLARO, false, "retiene");
            leyenda(g, 110, ly, Tema.AMARILLO, true, "solicita (espera)");
            leyenda(g, 264, ly, Tema.ROJO, false, "ciclo de deadlock");
            g.dispose();
        }

        private void leyenda(Graphics2D g, int x, int y, Color c, boolean punteada, String t) {
            g.setColor(c);
            g.setStroke(trazo(2f, punteada));
            g.drawLine(x, y - 4, x + 26, y - 4);
            g.setColor(Tema.TEXTO_SUAVE);
            g.drawString(t, x + 32, y);
        }

        private BasicStroke trazo(float w, boolean punteada) {
            return punteada
                ? new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] { 7f, 5f }, 0f)
                : new BasicStroke(w);
        }

        /** Arista con flecha entre dos nodos (círculo de proceso o caja de recurso). */
        private void arista(Graphics2D g, Point2D a, boolean aEsProceso, Point2D b, boolean bEsProceso,
                            Color c, boolean punteada, boolean critica, String etiqueta) {
            Point2D p1 = borde(a, aEsProceso, b);
            Point2D p2 = borde(b, bEsProceso, a);
            g.setColor(c);
            g.setStroke(trazo(critica ? 3.2f : 2f, punteada));
            double ang = Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX());
            double fx = p2.getX() - 9 * Math.cos(ang);
            double fy = p2.getY() - 9 * Math.sin(ang);
            g.drawLine((int) p1.getX(), (int) p1.getY(), (int) fx, (int) fy);
            g.setStroke(new BasicStroke(1f));
            Polygon flecha = new Polygon();
            flecha.addPoint((int) p2.getX(), (int) p2.getY());
            flecha.addPoint((int) (p2.getX() - 12 * Math.cos(ang - 0.4)), (int) (p2.getY() - 12 * Math.sin(ang - 0.4)));
            flecha.addPoint((int) (p2.getX() - 12 * Math.cos(ang + 0.4)), (int) (p2.getY() - 12 * Math.sin(ang + 0.4)));
            g.fill(flecha);
            g.setFont(Tema.fuente(Font.PLAIN, 10.5f));
            double mx = (p1.getX() + p2.getX()) / 2;
            double my = (p1.getY() + p2.getY()) / 2;
            int nx = (int) (-Math.sin(ang) * 11);
            int ny = (int) (Math.cos(ang) * 11);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(etiqueta, (int) mx + nx - fm.stringWidth(etiqueta) / 2 + (nx >= 0 ? 8 : -8), (int) my + ny + 4);
        }

        /** Punto donde la recta centro->destino corta el borde del nodo. */
        private Point2D borde(Point2D c, boolean esProceso, Point2D hacia) {
            double dx = hacia.getX() - c.getX();
            double dy = hacia.getY() - c.getY();
            double len = Math.max(0.001, Math.hypot(dx, dy));
            double t;
            if (esProceso) {
                t = RADIO + 3;
            } else {
                double tx = dx == 0 ? Double.MAX_VALUE : (RW / 2.0 + 3) * len / Math.abs(dx);
                double ty = dy == 0 ? Double.MAX_VALUE : (RH / 2.0 + 3) * len / Math.abs(dy);
                t = Math.min(tx, ty);
            }
            return new Point2D.Double(c.getX() + dx / len * t, c.getY() + dy / len * t);
        }
    }
}
