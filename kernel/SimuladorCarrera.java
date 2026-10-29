package kernel;

import java.util.ArrayList;
import java.util.List;

/**
 * Demostración de condición de carrera sobre una "Variable Global Compartida".
 * Cada tarea repite k veces la secuencia  LEER -> INCREMENTAR -> ESCRIBIR
 * (equivalente a global++ a nivel de instrucciones). Las tareas se intercalan
 * una micro-operación por turno, lo que provoca "actualizaciones perdidas"
 * cuando NO hay exclusión mutua. Con el semáforo binario el resultado es exacto.
 */
public class SimuladorCarrera {

    private enum Op { WAIT, LEER, INCREMENTAR, ESCRIBIR, SIGNAL }

    private static class Tarea {
        final PCB pcb;
        final Op[] programa;
        int pc = 0;
        int iteraciones;
        int registro = 0;
        boolean bloqueada = false;
        Tarea(PCB pcb, Op[] programa, int iteraciones) {
            this.pcb = pcb;
            this.programa = programa;
            this.iteraciones = iteraciones;
        }
        boolean terminada() { return iteraciones == 0; }
    }

    /** Ejecuta la demo en ambos modos y compara resultados. */
    public static void demostrar(int numTareas, int incrementos) {
        if (numTareas < 2 || numTareas > 5)
            throw new IllegalArgumentException("El número de tareas debe estar entre 2 y 5.");
        if (incrementos < 1 || incrementos > 5)
            throw new IllegalArgumentException("Los incrementos por tarea deben estar entre 1 y 5.");

        int esperado = numTareas * incrementos;
        System.out.println("=== CONDICIÓN DE CARRERA: " + numTareas + " tareas x " + incrementos
            + " incrementos. Valor esperado = " + esperado + " ===");

        System.out.println("\n--- A) SIN semáforo ---");
        int sin = ejecutar(numTareas, incrementos, false);
        System.out.println("  Resultado = " + sin + " (esperado " + esperado + ") -> "
            + (sin != esperado ? "CONDICIÓN DE CARRERA: se perdieron " + (esperado - sin) + " actualizaciones"
                               : "no se manifestó en esta intercalación"));

        System.out.println("\n--- B) CON semáforo binario (wait/signal) ---");
        int con = ejecutar(numTareas, incrementos, true);
        System.out.println("  Resultado = " + con + " (esperado " + esperado + ") -> "
            + (con == esperado ? "CORRECTO: exclusión mutua garantizada" : "ERROR inesperado"));
    }

    private static int ejecutar(int numTareas, int incrementos, boolean usarSemaforo) {
        int global = 0;
        SemaforoBinario sem = new SemaforoBinario();
        Op[] prog = usarSemaforo
            ? new Op[] { Op.WAIT, Op.LEER, Op.INCREMENTAR, Op.ESCRIBIR, Op.SIGNAL }
            : new Op[] { Op.LEER, Op.INCREMENTAR, Op.ESCRIBIR };

        List<Tarea> tareas = new ArrayList<>();
        for (int i = 1; i <= numTareas; i++) {
            tareas.add(new Tarea(new PCB(i, "T" + i, 0, incrementos), prog, incrementos));
        }

        boolean quedan = true;
        while (quedan) {
            boolean avanzo = false;
            quedan = false;
            for (Tarea t : tareas) {
                if (t.terminada()) continue;
                quedan = true;
                if (t.bloqueada) continue;
                avanzo = true;
                Op op = t.programa[t.pc];
                String msg;
                switch (op) {
                    case WAIT:
                        if (sem.wait(t.pcb)) {
                            msg = "wait()  -> entra a la sección crítica";
                        } else {
                            t.bloqueada = true;
                            msg = "wait()  -> OCUPADO, pasa a la cola de espera (BLOQUEADO)";
                        }
                        break;
                    case LEER:
                        t.registro = global;
                        msg = "LEER    global=" + global + " -> registro local";
                        break;
                    case INCREMENTAR:
                        t.registro++;
                        msg = "INC     registro local = " + t.registro;
                        break;
                    case ESCRIBIR:
                        global = t.registro;
                        msg = "ESCRIBIR global = " + global;
                        break;
                    default: // SIGNAL
                        PCB despertado = sem.signal(t.pcb);
                        msg = "signal() -> sale de la sección crítica";
                        if (despertado != null) {
                            for (Tarea o : tareas) if (o.pcb == despertado) o.bloqueada = false;
                            msg += "; despierta a " + despertado.getNombre();
                        }
                        break;
                }
                System.out.printf("  %-3s %s%n", t.pcb.getNombre(), msg);
                t.pc++;
                if (t.pc == t.programa.length) { t.pc = 0; t.iteraciones--; }
            }
            if (quedan && !avanzo) {   // salvaguarda: nunca debería ocurrir
                System.out.println("  [!] Ninguna tarea puede avanzar.");
                break;
            }
        }
        return global;
    }
}
