package kernel;

import java.util.ArrayList;
import java.util.List;

/**
 * Administrador de Memoria: RAM física simulada de 100 MB.
 * Estrategia: Asignación Contigua Dinámica (particiones variables) con
 * política First-Fit. Al liberar, los huecos adyacentes se fusionan.
 * Si no existe un hueco contiguo suficiente se lanza Out of Memory.
 */
public class AdministradorMemoria {

    public static final int TOTAL_MB = 100;

    /** Partición de memoria. pid = -1 significa hueco libre. */
    private static class Bloque {
        int base;
        int tam;
        int pid;
        Bloque(int base, int tam, int pid) {
            this.base = base;
            this.tam = tam;
            this.pid = pid;
        }
    }

    private final List<Bloque> bloques = new ArrayList<>();

    public AdministradorMemoria() {
        bloques.add(new Bloque(0, TOTAL_MB, -1)); // toda la RAM libre
    }

    /**
     * Reserva 'tam' MB para el proceso 'pid' (First-Fit).
     * @return dirección base asignada
     * @throws DesbordamientoMemoriaException si no hay hueco suficiente
     */
    public int asignar(int pid, int tam) throws DesbordamientoMemoriaException {
        if (tam <= 0) {
            throw new IllegalArgumentException("El tamaño debe ser mayor que 0 MB.");
        }
        for (int i = 0; i < bloques.size(); i++) {
            Bloque b = bloques.get(i);
            if (b.pid == -1 && b.tam >= tam) {
                int base = b.base;
                if (b.tam == tam) {
                    b.pid = pid;                       // ajuste exacto
                } else {
                    // Se divide el hueco: [proceso][hueco restante]
                    bloques.add(i + 1, new Bloque(b.base + tam, b.tam - tam, -1));
                    b.tam = tam;
                    b.pid = pid;
                }
                return base;
            }
        }
        throw new DesbordamientoMemoriaException(
            "OUT OF MEMORY: se solicitaron " + tam + " MB, libres en total "
            + memoriaLibre() + " MB (mayor hueco contiguo " + mayorHueco() + " MB).");
    }

    /** Libera todas las particiones del proceso y fusiona huecos vecinos. */
    public void liberar(int pid) {
        for (Bloque b : bloques) {
            if (b.pid == pid) b.pid = -1;
        }
        int i = 0;
        while (i < bloques.size() - 1) {
            Bloque a = bloques.get(i);
            Bloque s = bloques.get(i + 1);
            if (a.pid == -1 && s.pid == -1) {
                a.tam += s.tam;          // fusión de huecos adyacentes
                bloques.remove(i + 1);
            } else {
                i++;
            }
        }
    }

    public int memoriaUsada() {
        int u = 0;
        for (Bloque b : bloques) if (b.pid != -1) u += b.tam;
        return u;
    }

    public int memoriaLibre() { return TOTAL_MB - memoriaUsada(); }

    public int mayorHueco() {
        int m = 0;
        for (Bloque b : bloques) if (b.pid == -1 && b.tam > m) m = b.tam;
        return m;
    }

    public void imprimirMapa() {
        System.out.println("=== MAPA DE MEMORIA (RAM " + TOTAL_MB + " MB, asignación contigua dinámica, First-Fit) ===");
        for (Bloque b : bloques) {
            System.out.printf("  [%3d - %3d]  %3d MB  %s%n",
                b.base, b.base + b.tam - 1, b.tam, b.pid == -1 ? "LIBRE" : "P" + b.pid);
        }
        // Barra visual: cada celda representa 2 MB
        StringBuilder sb = new StringBuilder("  [");
        for (int mb = 0; mb < TOTAL_MB; mb += 2) {
            char c = '.';
            for (Bloque b : bloques) {
                if (mb >= b.base && mb < b.base + b.tam) {
                    if (b.pid != -1) c = Character.forDigit(b.pid % 10, 10);
                    break;
                }
            }
            sb.append(c);
        }
        sb.append("]  (cada celda = 2 MB; '.' = libre; dígito = último dígito del PID)");
        System.out.println(sb);
        System.out.printf("  Usada: %d MB | Libre: %d MB | Mayor hueco: %d MB%n",
            memoriaUsada(), memoriaLibre(), mayorHueco());
    }
}
