package kernel;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Calculadora teórica de Round Robin (todos los procesos llegan en t=0).
 * Útil para el apartado "Análisis Teórico" del reporte.
 *   Retorno_i = Finalización_i          (llegada = 0)
 *   Espera_i  = Finalización_i - Ráfaga_i
 */
public class TeoriaRR {

    public static void calcular(int[] rafagas, int q) {
        int n = rafagas.length;
        int[] restante = rafagas.clone();
        int[] fin = new int[n];
        Deque<Integer> cola = new ArrayDeque<>();
        for (int i = 0; i < n; i++) cola.addLast(i);

        StringBuilder gantt = new StringBuilder();
        int t = 0;
        while (!cola.isEmpty()) {
            int i = cola.pollFirst();
            int uso = Math.min(q, restante[i]);
            gantt.append("| P").append(i + 1).append(" (").append(t).append("-").append(t + uso).append(") ");
            t += uso;
            restante[i] -= uso;
            if (restante[i] > 0) cola.addLast(i); else fin[i] = t;
        }
        System.out.println("=== CÁLCULO TEÓRICO RR (q = " + q + ", llegada = 0 para todos) ===");
        System.out.println("  Gantt: " + gantt + "|");
        System.out.printf("  %-5s %-8s %-12s %-10s %-8s%n", "Proc", "Ráfaga", "Finalización", "Retorno", "Espera");
        double sE = 0, sR = 0;
        for (int i = 0; i < n; i++) {
            int espera = fin[i] - rafagas[i];
            System.out.printf("  P%-4d %-8d %-12d %-10d %-8d   (espera = %d - %d)%n",
                i + 1, rafagas[i], fin[i], fin[i], espera, fin[i], rafagas[i]);
            sE += espera;
            sR += fin[i];
        }
        System.out.printf("  Espera promedio = %.2f | Retorno promedio = %.2f%n", sE / n, sR / n);
    }
}
