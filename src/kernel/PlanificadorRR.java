package kernel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/**
 * Planificador Round Robin con quantum fijo q = 3.
 * Mantiene la cola de listos (FIFO). Cuando un proceso agota su quantum sin
 * terminar, se simula un cambio de contexto y regresa al final de la cola.
 */
public class PlanificadorRR {

    public static final int QUANTUM = 3;

    private final Deque<PCB> colaListos = new ArrayDeque<>();
    private final Consumer<PCB> alTerminar;   // callback hacia el kernel (liberar RAM, etc.)
    private int reloj = 0;
    private int cambiosContexto = 0;
    private final StringBuilder gantt = new StringBuilder();
    private final List<int[]> segmentos = new ArrayList<>();   // {pid, inicio, fin} para el Gantt gráfico

    public PlanificadorRR(Consumer<PCB> alTerminar) {
        this.alTerminar = alTerminar;
    }

    public void agregar(PCB p) {
        p.setEstado(Estado.LISTO);
        if (!colaListos.contains(p)) colaListos.addLast(p);
    }

    public boolean remover(PCB p) { return colaListos.remove(p); }

    public boolean hayListos() { return !colaListos.isEmpty(); }
    public int getReloj() { return reloj; }
    public int getCambiosContexto() { return cambiosContexto; }
    public String getGantt() { return gantt.toString(); }
    public List<int[]> getSegmentos() { return new ArrayList<>(segmentos); }

    public List<PCB> verCola() { return new ArrayList<>(colaListos); }

    /**
     * Ejecuta un turno: toma el primer proceso de la cola y lo corre durante
     * min(quantum, ráfaga restante) unidades de tiempo.
     * @return false si la cola estaba vacía
     */
    public boolean ejecutarTurno() {
        PCB p = colaListos.pollFirst();
        if (p == null) return false;

        p.setEstado(Estado.EJECUCION);
        int uso = Math.min(QUANTUM, p.getRafagaRestante());
        int inicio = reloj;
        p.setRafagaRestante(p.getRafagaRestante() - uso);
        reloj += uso;
        segmentos.add(new int[] { p.getPid(), inicio, reloj });
        gantt.append("| ").append(p.getNombre()).append(" (").append(inicio)
             .append("-").append(reloj).append(") ");

        System.out.printf("  t=%3d..%3d | %-4s ejecuta %d u. | ráfaga restante = %d%n",
            inicio, reloj, p.getNombre(), uso, p.getRafagaRestante());

        if (p.getRafagaRestante() == 0) {
            p.setEstado(Estado.TERMINADO);
            p.setTiempoFinalizacion(reloj);
            alTerminar.accept(p);
        } else {
            // Quantum agotado: cambio de contexto simulado
            cambiosContexto++;
            p.setEstado(Estado.LISTO);
            colaListos.addLast(p);
            System.out.printf("             -> Quantum agotado: CAMBIO DE CONTEXTO, %s regresa al final de la cola%n",
                p.getNombre());
        }
        return true;
    }
}
