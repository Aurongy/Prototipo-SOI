package kernel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Interfaz Swing para operar el mismo motor Kernel que usa la CLI. */
public class VentanaKernel extends JFrame {
    private static final Color BG = new Color(13, 18, 34);
    private static final Color PANEL = new Color(23, 30, 51);
    private static final Color PANEL2 = new Color(30, 39, 65);
    private static final Color TEXT = new Color(235, 240, 255);
    private static final Color MUTED = new Color(153, 166, 197);
    private static final Color BLUE = new Color(75, 130, 255);
    private static final Color CYAN = new Color(45, 212, 191);
    private static final Color PURPLE = new Color(167, 139, 250);
    private static final Color RED = new Color(251, 113, 133);
    private static final Color ORANGE = new Color(251, 191, 36);

    private Kernel kernel = new Kernel();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"PID", "Proceso", "Estado", "RAM", "Base", "CPU total", "CPU restante"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(model);
    private final JTextArea consola = new JTextArea();
    private final JProgressBar ramBar = new JProgressBar(0, 100);
    private final JLabel ramLabel = new JLabel("0 / 100 MB");
    private final JLabel relojLabel = new JLabel("t = 0");
    private final JLabel contextosLabel = new JLabel("0");
    private final JLabel semaforoLabel = new JLabel("LIBRE");
    private final JLabel r1Label = new JLabel("Disponible");
    private final JLabel r2Label = new JLabel("Disponible");
    private final JLabel procesosLabel = new JLabel("0");
    private final JTextField memoriaInput = new JTextField("20", 6);
    private final JTextField rafagaInput = new JTextField("7", 6);
    private final JTextField pidInput = new JTextField("1", 6);
    private final JTextField pidAInput = new JTextField("1", 4);
    private final JTextField pidBInput = new JTextField("2", 4);
    private final JTextField abortInput = new JTextField("1", 5);
    private boolean redirigiendo = false;

    public VentanaKernel() {
        super("KernelLab | Simulador de Sistema Operativo");
          // Estilo visual de ventanas emergentes
        UIManager.put("OptionPane.background", BG);
        UIManager.put("Panel.background", PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.foreground", TEXT);

        UIManager.put("Button.background", PANEL2);
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("Button.select", BLUE);
        UIManager.put("Button.focus", BLUE);

// Colores de ventanas de selección
        UIManager.put("OptionPane.buttonFont", new Font("SansSerif", Font.BOLD, 12));
        UIManager.put("OptionPane.messageFont", new Font("SansSerif", Font.PLAIN, 13));
    

        UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 800));
        setSize(1450, 940);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        add(crearBarraLateral(), BorderLayout.WEST);
        add(crearContenido(), BorderLayout.CENTER);
        estilizarTabla();
        estilizarConsola();
        estilizarCampos();
        redirigirSalida();
        actualizarVista();
        log("KernelLab listo. Crea procesos para comenzar la simulación.");
    }

    private JPanel crearBarraLateral() {
        JPanel side = new JPanel();
        side.setBackground(new Color(17, 23, 42));
        side.setPreferredSize(new Dimension(255, 0));
        side.setBorder(new EmptyBorder(24, 16, 20, 16));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel("◈  KernelLab");
        logo.setFont(new Font("SansSerif", Font.BOLD, 24)); logo.setForeground(TEXT);
        JLabel sub = new JLabel("OS CORE SIMULATOR"); sub.setForeground(CYAN); sub.setFont(new Font("SansSerif", Font.BOLD, 10));
        sub.setBorder(new EmptyBorder(7, 3, 28, 0));
        side.add(logo); side.add(sub);
        side.add(navLabel("PANEL DE CONTROL"));
        side.add(navButton("▦   Resumen general", () -> { actualizarVista(); log("Vista general actualizada."); }));
        side.add(navButton("＋   Crear proceso", this::crearProceso));
        side.add(navButton("▶   Ejecutar un turno", this::ejecutarTurno));
        side.add(navButton("»   Ejecutar hasta terminar", this::ejecutarTodo));
        side.add(Box.createVerticalStrut(18));
        side.add(navLabel("SINCRONIZACIÓN"));
        side.add(navButton("⌁   wait() / signal()", this::operarSemaforo));
        side.add(navButton("⚠   Simular deadlock", this::forzarDeadlock));
        side.add(navButton("✕   Abortar proceso", this::abortarProceso));
        side.add(Box.createVerticalGlue());
        JLabel ram = new JLabel("RAM FÍSICA"); ram.setForeground(MUTED); ram.setFont(new Font("SansSerif", Font.BOLD, 11));
        side.add(ram);
        JProgressBar smallBar = new JProgressBar(0,100); smallBar.setName("sideRam"); smallBar.setStringPainted(true); smallBar.setString("100 MB");
        smallBar.setForeground(CYAN); smallBar.setBackground(PANEL2); smallBar.setBorderPainted(false); smallBar.setMaximumSize(new Dimension(190, 18));
        side.add(Box.createVerticalStrut(8)); side.add(smallBar);
        side.add(Box.createVerticalStrut(16));
        side.add(navButton("↻   Reiniciar kernel", this::reiniciar));
        side.add(navButton("⏻   Salir", () -> dispose()));
        return side;
    }

    private JLabel navLabel(String text) {
        JLabel l = new JLabel(text); l.setForeground(new Color(115, 132, 170));
        l.setFont(new Font("SansSerif", Font.BOLD, 10)); l.setBorder(new EmptyBorder(0, 3, 10, 0)); return l;
    }
    private JButton navButton(String text, Runnable action) {
        JButton b = new JButton(text); 
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setAlignmentX(Component.LEFT_ALIGNMENT); b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setForeground(TEXT); b.setBackground(new Color(28, 38, 62)); b.setOpaque(true); b.setContentAreaFilled(true); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(47, 62, 94), 1, true), new EmptyBorder(11, 10, 11, 8))); b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setMaximumSize(new Dimension(225, 46)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        b.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(new Color(45, 65, 103)); } public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(new Color(28,38,62)); }});
        return b;
    }

    private JPanel crearContenido() {
        JPanel root = new JPanel(new BorderLayout(0, 18)); root.setBackground(BG); root.setBorder(new EmptyBorder(24, 26, 22, 26));
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JPanel title = new JPanel(); title.setOpaque(false); title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        JLabel h = new JLabel("Simulador de funciones del Kernel"); h.setFont(new Font("SansSerif", Font.BOLD, 27)); h.setForeground(TEXT);
        JLabel desc = new JLabel("Planificación de procesos · Memoria · Sincronización · Deadlock"); desc.setForeground(MUTED); desc.setFont(new Font("SansSerif", Font.PLAIN, 13)); desc.setBorder(new EmptyBorder(7,0,0,0));
        title.add(h); title.add(desc); top.add(title, BorderLayout.WEST);
        JLabel status = new JLabel("●  SISTEMA ACTIVO"); status.setForeground(CYAN); status.setFont(new Font("SansSerif", Font.BOLD, 11));
        status.setBorder(new EmptyBorder(10, 14, 10, 14)); status.setOpaque(true); status.setBackground(new Color(18, 58, 63)); top.add(status, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 16)); center.setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(1,4,13,0)); cards.setOpaque(false);
        cards.add(card("MEMORIA RAM", ramLabel, "Límite físico simulado", CYAN));
        cards.add(card("PROCESOS", procesosLabel, "Registrados en el kernel", BLUE));
        cards.add(card("RELOJ DE CPU", relojLabel, "Unidades de tiempo", PURPLE));
        cards.add(card("CAMBIOS CONTEXTO", contextosLabel, "Quantum fijo: 3", ORANGE));
        JPanel tablePanel = panel("Tabla de procesos (PCB)", "Estado actual de cada proceso y su ráfaga de CPU");
        tablePanel.setLayout(new BorderLayout(0,12));
        JScrollPane sp = new JScrollPane(tabla); sp.setBorder(BorderFactory.createEmptyBorder()); sp.getViewport().setBackground(PANEL); tablePanel.add(sp, BorderLayout.CENTER);
        JPanel memRow = new JPanel(new BorderLayout(12,0)); memRow.setOpaque(false); memRow.add(ramBar, BorderLayout.CENTER);
        JLabel total = new JLabel("100 MB"); total.setForeground(MUTED); memRow.add(total, BorderLayout.EAST); tablePanel.add(memRow, BorderLayout.SOUTH);
        JPanel lower = new JPanel(new GridLayout(1,2,14,0)); lower.setOpaque(false);
        lower.add(crearAccionesPanel()); lower.add(crearEstadoPanel());
        JPanel upper = new JPanel(new BorderLayout(0,14)); upper.setOpaque(false); upper.add(cards, BorderLayout.NORTH); upper.add(tablePanel, BorderLayout.CENTER);
        center.add(upper, BorderLayout.CENTER); center.add(lower, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        JPanel logsPanel = panel("Bitácora del kernel", "Eventos, validaciones y resultados de los algoritmos");
        logsPanel.setPreferredSize(new Dimension(0, 190)); logsPanel.setLayout(new BorderLayout(0,8));
        JScrollPane logScroll = new JScrollPane(consola); logScroll.setBorder(BorderFactory.createEmptyBorder()); logScroll.getViewport().setBackground(new Color(9,13,25)); logsPanel.add(logScroll, BorderLayout.CENTER);
        JButton limpiar = boton("Limpiar bitácora", PANEL2, TEXT); limpiar.addActionListener(e -> consola.setText(""));
        JPanel logHead = new JPanel(new BorderLayout()); logHead.setOpaque(false); logHead.add(new JLabel("Salida del sistema"), BorderLayout.WEST); logHead.add(limpiar, BorderLayout.EAST); logsPanel.add(logHead, BorderLayout.NORTH);
        root.add(logsPanel, BorderLayout.SOUTH);
        return root;
    }

    private JPanel card(String title, JLabel value, String detail, Color accent) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(PANEL); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,3,0,0,accent), new EmptyBorder(15,15,14,12)));
        JLabel t = new JLabel(title); t.setForeground(MUTED); t.setFont(new Font("SansSerif", Font.BOLD, 10));
        value.setForeground(TEXT); value.setFont(new Font("SansSerif", Font.BOLD, 24));
        JLabel d = new JLabel(detail); d.setForeground(MUTED); d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        p.add(t); p.add(Box.createVerticalStrut(9)); p.add(value); p.add(Box.createVerticalStrut(4)); p.add(d); return p;
    }

    private JPanel crearAccionesPanel() {
        JPanel p = panel("Acciones rápidas", "Operaciones principales del simulador"); p.setLayout(new BorderLayout(0,10));
        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false); GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(4,4,4,8); c.anchor = GridBagConstraints.WEST;
        c.gridx=0;c.gridy=0; fields.add(label("Memoria (MB)"),c); c.gridx=1;fields.add(memoriaInput,c); c.gridx=2;fields.add(label("Ráfaga CPU"),c); c.gridx=3;fields.add(rafagaInput,c);
        JPanel buttons = new JPanel(new GridLayout(2,2,8,8)); buttons.setOpaque(false);
        buttons.add(boton("＋ Crear proceso", BLUE, Color.WHITE)); buttons.getComponent(0).addMouseListener(new java.awt.event.MouseAdapter(){ public void mouseClicked(java.awt.event.MouseEvent e){} });
        ((JButton)buttons.getComponent(0)).addActionListener(e -> crearProceso());
        JButton one=boton("▶ Ejecutar turno", new Color(27,89,100), Color.WHITE); one.addActionListener(e->ejecutarTurno()); buttons.add(one);
        JButton all=boton("» Ejecutar todo", new Color(67,52,118), Color.WHITE); all.addActionListener(e->ejecutarTodo()); buttons.add(all);
        JButton map=boton("▤ Mapa de memoria", PANEL2, TEXT); map.addActionListener(e->{kernel.imprimirMemoria(); actualizarVista();}); buttons.add(map);
        p.add(fields,BorderLayout.NORTH); p.add(buttons,BorderLayout.CENTER); return p;
    }

    private JPanel crearEstadoPanel() {
        JPanel p = panel("Sincronización y recursos", "Semáforo binario y recursos exclusivos R1 / R2"); p.setLayout(new BorderLayout(0,10));
        JPanel status = new JPanel(new GridLayout(3,2,8,7)); status.setOpaque(false);
        status.add(label("Semáforo")); semaforoLabel.setForeground(CYAN); semaforoLabel.setFont(new Font("SansSerif",Font.BOLD,12)); status.add(semaforoLabel);
        status.add(label("Recurso 1")); r1Label.setForeground(TEXT); status.add(r1Label);
        status.add(label("Recurso 2")); r2Label.setForeground(TEXT); status.add(r2Label);
        JPanel actions = new JPanel(new GridLayout(2,1,0,7)); actions.setOpaque(false);
        JPanel wait = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0)); wait.setOpaque(false); wait.add(label("PID")); wait.add(pidInput);
        JButton ws=boton("wait() / signal()", new Color(27,89,100), Color.WHITE); ws.addActionListener(e->operarSemaforo()); wait.add(ws);
        JPanel dead = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0)); dead.setOpaque(false); dead.add(label("A")); dead.add(pidAInput); dead.add(label("B")); dead.add(pidBInput);
        JButton dl=boton("Forzar deadlock", new Color(119,49,73), Color.WHITE); dl.addActionListener(e->forzarDeadlock()); dead.add(dl);
        actions.add(wait); actions.add(dead); p.add(status,BorderLayout.CENTER); p.add(actions,BorderLayout.SOUTH); return p;
    }

    private JPanel panel(String title, String subtitle) {
        JPanel p = new JPanel(); p.setBackground(PANEL); p.setBorder(new EmptyBorder(14,15,14,15)); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title); t.setForeground(TEXT); t.setFont(new Font("SansSerif",Font.BOLD,15));
        JLabel s = new JLabel(subtitle); s.setForeground(MUTED); s.setFont(new Font("SansSerif",Font.PLAIN,11)); s.setBorder(new EmptyBorder(4,0,10,0));
        p.add(t); p.add(s); return p;
    }
    private JLabel label(String text) { JLabel l=new JLabel(text); l.setForeground(MUTED); l.setFont(new Font("SansSerif",Font.PLAIN,12)); return l; }
    private JButton boton(String text, Color bg, Color fg) {
        JButton b=new JButton(text);
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setForeground(fg); b.setBackground(bg); b.setOpaque(true); b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.brighter(), 1, true),
                new EmptyBorder(12,12,12,12)));
        b.setFont(new Font("SansSerif",Font.BOLD,12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(150, 48));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(bg.brighter()); }
            public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    private void estilizarCampos() {
        JTextField[] campos = {memoriaInput, rafagaInput, pidInput, pidAInput, pidBInput, abortInput};
        for (JTextField campo : campos) {
            campo.setBackground(new Color(10, 16, 31));
            campo.setForeground(TEXT);
            campo.setCaretColor(CYAN);
            campo.setSelectionColor(new Color(48, 88, 150));
            campo.setSelectedTextColor(Color.WHITE);
            campo.setFont(new Font("SansSerif", Font.BOLD, 14));
            campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(67, 91, 135), 1, true),
                    new EmptyBorder(7, 8, 7, 8)));
        }
    }
    private void estilizarTabla() {
        tabla.setBackground(PANEL); tabla.setForeground(TEXT); tabla.setGridColor(new Color(47,58,85)); tabla.setRowHeight(29);
        tabla.setFont(new Font("SansSerif",Font.PLAIN,12)); tabla.setSelectionBackground(new Color(44,66,112)); tabla.setSelectionForeground(Color.WHITE);
        tabla.getTableHeader().setBackground(PANEL2); tabla.getTableHeader().setForeground(MUTED); tabla.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,11));
        tabla.setFillsViewportHeight(true);
        tabla.setIntercellSpacing(new Dimension(1, 1));
        tabla.setShowGrid(true);
        tabla.getTableHeader().setOpaque(true);
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 36));
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }
    private void estilizarConsola() { consola.setEditable(false); consola.setBackground(new Color(9,13,25)); consola.setForeground(new Color(157,231,205)); consola.setCaretColor(TEXT); consola.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12)); consola.setBorder(new EmptyBorder(9,10,9,10)); }

    private void redirigirSalida() {
        if (redirigiendo) return; redirigiendo=true;
        PrintStream original = System.out;
        OutputStream out = new OutputStream() {
            private final java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            @Override public synchronized void write(int b) {
                if (b == '\n') {
                    String line = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
                    buffer.reset();
                    if (line.endsWith("\r")) line = line.substring(0, line.length() - 1);
                    final String safeLine = line;
                    SwingUtilities.invokeLater(() -> appendLog(safeLine));
                } else buffer.write(b);
            }
            @Override public synchronized void flush() {
                if (buffer.size() > 0) {
                    String line = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
                    buffer.reset();
                    final String safeLine = line;
                    SwingUtilities.invokeLater(() -> appendLog(safeLine));
                }
            }
        };
        System.setOut(new PrintStream(out,true,StandardCharsets.UTF_8));
    }
    private void appendLog(String s) { consola.append(s+"\n"); consola.setCaretPosition(consola.getDocument().getLength()); }
    private void log(String s) { appendLog("[KernelLab] "+s); }

    private void crearProceso() {
        try { int m=Integer.parseInt(memoriaInput.getText().trim()); int r=Integer.parseInt(rafagaInput.getText().trim()); PCB p=kernel.crearProceso(m,r); log("Creado "+p.getNombre()+" correctamente."); actualizarVista(); }
        catch(Exception ex) { mostrarError(ex); }
    }
    private void ejecutarTurno() { try { kernel.ejecutarTurno(); actualizarVista(); } catch(Exception ex){mostrarError(ex);} }
    private void ejecutarTodo() { try { kernel.ejecutarTodo(); actualizarVista(); } catch(Exception ex){mostrarError(ex);} }
    private void operarSemaforo() {
        String[] ops={"wait() — solicitar sección crítica","signal() — liberar sección crítica"};
        int op=JOptionPane.showOptionDialog(this,"Selecciona una operación para el PID indicado.","Semáforo binario",JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,ops,ops[0]);
        try { int pid=Integer.parseInt(pidInput.getText().trim()); if(op==0) kernel.entrarSeccionCritica(pid); else if(op==1) kernel.salirSeccionCritica(pid); actualizarVista(); }
        catch(Exception ex){mostrarError(ex);}
    }
    private void forzarDeadlock() {
        try { int a=Integer.parseInt(pidAInput.getText().trim()); int b=Integer.parseInt(pidBInput.getText().trim()); kernel.forzarDeadlock(a,b); actualizarVista(); }
        catch(Exception ex){mostrarError(ex);}
    }
    private void abortarProceso() {
        String s=JOptionPane.showInputDialog(this,"PID del proceso que se abortará:",abortInput.getText()); if(s==null)return;
        try { int pid=Integer.parseInt(s.trim()); kernel.abortarProceso(pid); abortInput.setText(s); actualizarVista(); }
        catch(Exception ex){mostrarError(ex);}
    }
    private void reiniciar() {
        int r=JOptionPane.showConfirmDialog(this,"¿Reiniciar todos los procesos, la RAM y los recursos?","Confirmar reinicio",JOptionPane.YES_NO_OPTION);
        if(r==JOptionPane.YES_OPTION){kernel=new Kernel();model.setRowCount(0);consola.setText("");actualizarVista();log("Kernel reiniciado. RAM y recursos en estado inicial.");}
    }
    private void mostrarError(Exception ex) { log("[ERROR] "+ex.getMessage()); JOptionPane.showMessageDialog(this,ex.getMessage(),"No se pudo completar la operación",JOptionPane.WARNING_MESSAGE); }

    private void actualizarVista() {
        model.setRowCount(0);
        List<PCB> procesos=kernel.getProcesos();
        for(PCB p:procesos) model.addRow(new Object[]{p.getPid(),p.getNombre(),p.isAbortado()?"ABORTADO":p.getEstado(),p.getTamanoMemoria()+" MB",p.getEstado()==Estado.TERMINADO?"—":p.getBaseMemoria(),p.getRafagaTotal(),p.getRafagaRestante()});
        int used=kernel.getMemoriaUsada(); ramBar.setValue(used); ramBar.setStringPainted(true); ramBar.setString(used+"% utilizado"); ramBar.setForeground(used>=85?RED:CYAN); ramBar.setBackground(new Color(39, 49, 74)); ramBar.setPreferredSize(new Dimension(100, 28));
        ramLabel.setText(used+" / 100 MB"); procesosLabel.setText(String.valueOf(procesos.size())); relojLabel.setText("t = "+kernel.getReloj()); contextosLabel.setText(String.valueOf(kernel.getCambiosContexto()));
        PCB owner=kernel.getPropietarioSemaforo(); semaforoLabel.setText(owner==null?"LIBRE":"OCUPADO · "+owner.getNombre()); semaforoLabel.setForeground(owner==null?CYAN:ORANGE);
        PCB r1=kernel.getPropietarioRecurso(1), r2=kernel.getPropietarioRecurso(2); r1Label.setText(r1==null?"Disponible":"En uso por "+r1.getNombre()); r2Label.setText(r2==null?"Disponible":"En uso por "+r2.getNombre());
        for(Component c:((JPanel)getContentPane().getComponent(0)).getComponents()) if(c instanceof JProgressBar && "sideRam".equals(c.getName())) { JProgressBar b=(JProgressBar)c; b.setValue(used); b.setString(used+" / 100 MB"); }
    }
}
