package kernel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Núcleo del simulador: integra Planificador RR, Administrador de Memoria,
 * Semáforo (sección crítica) y Gestor de Recursos (deadlock).
 * Toda validación de parámetros ocurre aquí para garantizar robustez.
 */
public class Kernel {

    public static final int RAFAGA_MAX = 1000;

    private final AdministradorMemoria memoria = new AdministradorMemoria();
    private final SemaforoBinario semaforo = new SemaforoBinario();
    private final GestorRecursos recursos = new GestorRecursos();
    private final Map<Integer, PCB> procesos = new LinkedHashMap<>();
    private final PlanificadorRR planificador;
    private int siguientePid = 1;
    private int rechazados = 0;

    public Kernel() {
        this.planificador = new PlanificadorRR(this::alTerminarProceso);
    }


    // Accesores de solo lectura para la interfaz gráfica Swing.
    public List<PCB> getProcesos() { return new ArrayList<>(procesos.values()); }
    public int getMemoriaUsada() { return memoria.memoriaUsada(); }
    public int getMemoriaLibre() { return memoria.memoriaLibre(); }
    public int getMayorHueco() { return memoria.mayorHueco(); }
    public List<PCB> getColaListos() { return planificador.verCola(); }
    public int getReloj() { return planificador.getReloj(); }
    public int getCambiosContexto() { return planificador.getCambiosContexto(); }
    public int getRechazados() { return rechazados; }
    public PCB getPropietarioSemaforo() { return semaforo.getPropietario(); }
    public int getValorSemaforo() { return semaforo.getValor(); }
    public PCB getPropietarioRecurso(int id) { return recursos.get(id).getPropietario(); }

    // ------------------------------------------------------------------
    // Gestión de procesos y memoria
    // ------------------------------------------------------------------

    /** Crea un proceso: valida parámetros, reserva RAM y lo encola como LISTO. */
    public PCB crearProceso(int tamMB, int rafaga) throws DesbordamientoMemoriaException {
        if (tamMB <= 0) {
            throw new IllegalArgumentException("El tamaño en memoria debe ser mayor que 0 MB (recibido: " + tamMB + ").");
        }
        if (rafaga <= 0 || rafaga > RAFAGA_MAX) {
            throw new IllegalArgumentException("La ráfaga de CPU debe estar entre 1 y " + RAFAGA_MAX + " (recibido: " + rafaga + ").");
        }
        int pid = siguientePid;
        int base;
        try {
            base = memoria.asignar(pid, tamMB);
        } catch (DesbordamientoMemoriaException e) {
            rechazados++;
            throw e;   // el proceso es rechazado inmediatamente y no consume PID
        }
        siguientePid++;
        PCB p = new PCB(pid, "P" + pid, tamMB, rafaga);
        p.setBaseMemoria(base);
        p.setTiempoLlegada(planificador.getReloj());
        procesos.put(pid, p);
        planificador.agregar(p);
        System.out.printf("  [OK] Proceso %s creado: %d MB en base=%d, ráfaga=%d -> LISTO%n",
            p.getNombre(), tamMB, base, rafaga);
        return p;
    }

    public PCB obtener(int pid) {
        PCB p = procesos.get(pid);
        if (p == null) throw new IllegalArgumentException("No existe un proceso con PID " + pid + ".");
        return p;
    }

    /** Callback del planificador al finalizar un proceso. */
    private void alTerminarProceso(PCB p) {
        System.out.printf("  [FIN] %s TERMINADO en t=%d. Se libera su memoria (%d MB).%n",
            p.getNombre(), p.getTiempoFinalizacion(), p.getTamanoMemoria());
        memoria.liberar(p.getPid());
        if (semaforo.getPropietario() == p) {
            System.out.println("        (retenía la sección crítica: se libera automáticamente)");
            PCB n = semaforo.signal(p);
            if (n != null) planificador.agregar(n);
        }
        for (PCB d : recursos.liberarTodo(p)) planificador.agregar(d);
    }

    private void validarActivo(PCB p) {
        if (p.getEstado() == Estado.TERMINADO)
            throw new IllegalStateException(p.getNombre() + " ya está TERMINADO.");
        if (p.getEstado() == Estado.BLOQUEADO)
            throw new IllegalStateException(p.getNombre() + " está BLOQUEADO y no puede ejecutar esa operación.");
    }

    // ------------------------------------------------------------------
    // Planificación Round Robin
    // ------------------------------------------------------------------

    public void ejecutarTurno() {
        if (!planificador.hayListos()) {
            System.out.println("  No hay procesos en la cola de listos." + avisoBloqueados());
            return;
        }
        planificador.ejecutarTurno();
    }

    public void ejecutarTodo() {
        if (!planificador.hayListos()) {
            System.out.println("  No hay procesos en la cola de listos." + avisoBloqueados());
            return;
        }
        System.out.println("  --- Round Robin (q = " + PlanificadorRR.QUANTUM + ") ---");
        while (planificador.hayListos()) planificador.ejecutarTurno();
        System.out.println("  Diagrama de Gantt: " + planificador.getGantt() + "|");
        System.out.println("  Cambios de contexto: " + planificador.getCambiosContexto()
            + " | Reloj final: t=" + planificador.getReloj() + avisoBloqueados());
    }

    private String avisoBloqueados() {
        int n = 0;
        for (PCB p : procesos.values()) if (p.getEstado() == Estado.BLOQUEADO) n++;
        return n == 0 ? "" : "\n  AVISO: hay " + n + " proceso(s) BLOQUEADO(s) (semáforo/recursos); no avanzan hasta ser liberados.";
    }

    // ------------------------------------------------------------------
    // Semáforo / sección crítica
    // ------------------------------------------------------------------

    /** wait(): el proceso intenta entrar a la sección crítica. */
    public void entrarSeccionCritica(int pid) {
        PCB p = obtener(pid);
        validarActivo(p);
        if (semaforo.wait(p)) {
            System.out.println("  [wait] " + p.getNombre() + " ENTRÓ a la sección crítica (semáforo=0).");
        } else {
            planificador.remover(p);
            System.out.println("  [wait] Sección crítica OCUPADA por " + semaforo.getPropietario().getNombre()
                + ": " + p.getNombre() + " -> BLOQUEADO (cola de espera del semáforo).");
        }
    }

    /** signal(): el proceso sale de la sección crítica. */
    public void salirSeccionCritica(int pid) {
        PCB p = obtener(pid);
        PCB despertado = semaforo.signal(p);
        if (despertado == null) {
            System.out.println("  [signal] " + p.getNombre() + " salió. Semáforo liberado (valor=1).");
        } else {
            planificador.agregar(despertado);
            System.out.println("  [signal] " + p.getNombre() + " salió. Se despierta a " + despertado.getNombre()
                + " (entra a la sección crítica y pasa a LISTO).");
        }
    }

    // ------------------------------------------------------------------
    // Recursos e interbloqueo
    // ------------------------------------------------------------------

    public void solicitarRecurso(int pid, int idRecurso) {
        PCB p = obtener(pid);
        validarActivo(p);
        if (recursos.solicitar(p, idRecurso)) {
            System.out.printf("  [recurso] %s obtuvo el Recurso %d.%n", p.getNombre(), idRecurso);
        } else {
            planificador.remover(p);
            System.out.printf("  [recurso] Recurso %d ocupado por %s: %s -> BLOQUEADO en espera.%n",
                idRecurso, recursos.get(idRecurso).getPropietario().getNombre(), p.getNombre());
        }
    }

    public void liberarRecurso(int pid, int idRecurso) {
        PCB p = obtener(pid);
        PCB d = recursos.liberar(p, idRecurso);
        System.out.printf("  [recurso] %s liberó el Recurso %d.%n", p.getNombre(), idRecurso);
        if (d != null) {
            planificador.agregar(d);
            System.out.printf("            %s estaba esperando: obtiene el Recurso %d y pasa a LISTO.%n",
                d.getNombre(), idRecurso);
        }
    }

    /** Fuerza la espera circular: A retiene R1 y pide R2; B retiene R2 y pide R1. */
    public void forzarDeadlock(int pidA, int pidB) {
        if (pidA == pidB) throw new IllegalArgumentException("Debe elegir dos procesos distintos.");
        PCB a = obtener(pidA);
        PCB b = obtener(pidB);
        validarActivo(a);
        validarActivo(b);
        if (recursos.get(1).getPropietario() != null || recursos.get(2).getPropietario() != null
            || !recursos.get(1).getColaEspera().isEmpty() || !recursos.get(2).getColaEspera().isEmpty()) {
            throw new IllegalStateException("Para forzar el deadlock ambos recursos deben estar libres y sin cola.");
        }
        System.out.println("  --- Forzando espera circular entre " + a.getNombre() + " y " + b.getNombre() + " ---");
        solicitarRecurso(pidA, 1);   // A retiene R1
        solicitarRecurso(pidB, 2);   // B retiene R2
        solicitarRecurso(pidA, 2);   // A pide R2 -> bloqueado
        solicitarRecurso(pidB, 1);   // B pide R1 -> bloqueado
        detectarDeadlock();
    }

    /** @return true si se detectó interbloqueo. */
    public boolean detectarDeadlock() {
        List<PCB> ciclo = recursos.detectarDeadlock();
        if (ciclo.isEmpty()) {
            System.out.println("  [deadlock] No se detecta interbloqueo.");
            return false;
        }
        StringBuilder sb = new StringBuilder();
        for (PCB p : ciclo) sb.append(p.getNombre()).append(" -> ");
        sb.append(ciclo.get(0).getNombre());
        System.out.println("  *************************************************");
        System.out.println("  *** ¡¡ALERTA: DEADLOCK DETECTADO!!             ***");
        System.out.println("  *** Espera circular: " + sb);
        for (PCB p : ciclo) {
            Recurso r = recursos.esperaPor(p);
            System.out.println("  ***   " + p.getNombre() + " espera Recurso " + r.getId()
                + " (retenido por " + r.getPropietario().getNombre() + ")");
        }
        System.out.println("  *** Use la opción 'Abortar proceso' para romperlo.");
        System.out.println("  *************************************************");
        return true;
    }

    /** Aborta forzosamente un proceso: libera RAM, semáforo y recursos. */
    public void abortarProceso(int pid) {
        PCB p = obtener(pid);
        if (p.getEstado() == Estado.TERMINADO) {
            throw new IllegalStateException(p.getNombre() + " ya está TERMINADO.");
        }
        planificador.remover(p);
        semaforo.retirarDeCola(p);
        if (semaforo.getPropietario() == p) {
            PCB n = semaforo.signal(p);
            if (n != null) planificador.agregar(n);
        }
        List<PCB> despertados = recursos.liberarTodo(p);
        memoria.liberar(p.getPid());
        p.setEstado(Estado.TERMINADO);
        p.setAbortado(true);
        System.out.println("  [ABORT] " + p.getNombre() + " abortado forzosamente. Memoria, semáforo y recursos liberados.");
        for (PCB d : despertados) {
            planificador.agregar(d);
            System.out.println("          " + d.getNombre() + " obtiene el recurso liberado y pasa a LISTO.");
        }
    }

    // ------------------------------------------------------------------
    // Reportes
    // ------------------------------------------------------------------

    public void imprimirTablaPCB() {
        System.out.println("=== TABLA DE PCB (reloj t=" + planificador.getReloj() + ") ===");
        if (procesos.isEmpty()) {
            System.out.println("  (sin procesos)  | Rechazados por falta de RAM: " + rechazados);
            return;
        }
        System.out.printf("  %-4s %-10s %-9s %-8s %-7s %-14s %-16s%n",
            "PID", "Estado", "Mem(MB)", "Base", "Llegada", "Ráfaga total", "Ráfaga restante");
        for (PCB p : procesos.values()) {
            System.out.printf("  %-4d %-10s %-9d %-8s %-7d %-14d %-16d%s%n",
                p.getPid(), p.getEstado(), p.getTamanoMemoria(),
                p.getEstado() == Estado.TERMINADO ? "-" : String.valueOf(p.getBaseMemoria()),
                p.getTiempoLlegada(), p.getRafagaTotal(), p.getRafagaRestante(),
                p.isAbortado() ? "  (ABORTADO)" : "");
        }
        System.out.println("  Cola de listos: " + planificador.verCola() + " | Rechazados por falta de RAM: " + rechazados);
    }

    public void imprimirMemoria() { memoria.imprimirMapa(); }

    public void imprimirSincronizacion() {
        System.out.println("=== SEMÁFORO Y RECURSOS ===");
        System.out.println("  " + semaforo.describir());
        recursos.imprimir();
    }

    public void imprimirEstadisticas() {
        System.out.println("=== ESTADÍSTICAS DE PLANIFICACIÓN (procesos terminados normalmente) ===");
        List<PCB> fin = new ArrayList<>();
        for (PCB p : procesos.values()) if (p.getEstado() == Estado.TERMINADO && !p.isAbortado()) fin.add(p);
        if (fin.isEmpty()) {
            System.out.println("  Aún no hay procesos terminados.");
            return;
        }
        System.out.printf("  %-5s %-8s %-8s %-12s %-10s %-8s%n", "Proc", "Llegada", "Ráfaga", "Finalización", "Retorno", "Espera");
        double sumE = 0, sumR = 0;
        for (PCB p : fin) {
            System.out.printf("  %-5s %-8d %-8d %-12d %-10d %-8d%n", p.getNombre(), p.getTiempoLlegada(),
                p.getRafagaTotal(), p.getTiempoFinalizacion(), p.getTiempoRetorno(), p.getTiempoEspera());
            sumE += p.getTiempoEspera();
            sumR += p.getTiempoRetorno();
        }
        System.out.printf("  Promedio de espera = %.2f | Promedio de retorno = %.2f%n", sumE / fin.size(), sumR / fin.size());
    }
}
