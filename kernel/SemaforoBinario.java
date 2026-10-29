package kernel;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Semáforo binario (valor 1 = libre, 0 = ocupado) con cola de espera propia.
 * Protege la sección crítica (variable global compartida).
 *  - wait(): si está ocupado, el proceso pasa a BLOQUEADO y a la cola de espera.
 *  - signal(): si hay procesos esperando, se despierta al primero (FIFO) y
 *              se le transfiere la propiedad; si no, el semáforo queda libre.
 */
public class SemaforoBinario {
    private int valor = 1;
    private PCB propietario = null;
    private final Queue<PCB> colaEspera = new LinkedList<>();

    /** @return true si el proceso entró a la sección crítica; false si quedó bloqueado. */
    public boolean wait(PCB p) {
        if (propietario == p) {
            throw new IllegalStateException(p.getNombre() + " ya se encuentra dentro de la sección crítica.");
        }
        if (colaEspera.contains(p)) {
            throw new IllegalStateException(p.getNombre() + " ya está en la cola de espera del semáforo.");
        }
        if (valor == 1) {
            valor = 0;
            propietario = p;
            return true;
        }
        colaEspera.add(p);
        p.setEstado(Estado.BLOQUEADO);
        return false;
    }

    /** @return proceso despertado (que ahora es propietario) o null si no había esperando. */
    public PCB signal(PCB p) {
        if (propietario != p) {
            throw new IllegalStateException(p.getNombre() + " no está dentro de la sección crítica; no puede hacer signal().");
        }
        PCB siguiente = colaEspera.poll();
        if (siguiente == null) {
            valor = 1;
            propietario = null;
            return null;
        }
        propietario = siguiente;      // valor permanece en 0: la sección sigue ocupada
        siguiente.setEstado(Estado.LISTO);
        return siguiente;
    }

    public boolean retirarDeCola(PCB p) { return colaEspera.remove(p); }
    public PCB getPropietario() { return propietario; }
    public int getValor() { return valor; }

    public String describir() {
        StringBuilder sb = new StringBuilder();
        sb.append("Semáforo binario: valor=").append(valor)
          .append(" | dentro de la sección crítica=")
          .append(propietario == null ? "(nadie)" : propietario.getNombre())
          .append(" | cola de espera=").append(colaEspera);
        return sb.toString();
    }
}
