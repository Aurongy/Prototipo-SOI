package kernel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestor de los dos recursos exclusivos (Recurso 1 y Recurso 2).
 * Detecta interbloqueo mediante un grafo de espera ("wait-for"):
 * arista P -> Q si P espera un recurso que retiene Q. Un ciclo = deadlock.
 */
public class GestorRecursos {
    private final Recurso[] recursos = { new Recurso(1), new Recurso(2) };

    public Recurso get(int id) {
        if (id < 1 || id > recursos.length) {
            throw new IllegalArgumentException("Recurso inválido '" + id + "': use 1 o 2.");
        }
        return recursos[id - 1];
    }

    /** Recurso por el que el proceso está esperando (null si ninguno). */
    public Recurso esperaPor(PCB p) {
        for (Recurso r : recursos) if (r.getColaEspera().contains(p)) return r;
        return null;
    }

    /** @return true si se concedió; false si el proceso quedó BLOQUEADO en espera. */
    public boolean solicitar(PCB p, int id) {
        Recurso r = get(id);
        if (r.getPropietario() == p) {
            throw new IllegalStateException(p.getNombre() + " ya posee el Recurso " + id + ".");
        }
        if (esperaPor(p) != null) {
            throw new IllegalStateException(p.getNombre() + " ya está bloqueado esperando un recurso.");
        }
        if (r.getPropietario() == null) {
            r.setPropietario(p);
            return true;
        }
        r.getColaEspera().add(p);
        p.setEstado(Estado.BLOQUEADO);
        return false;
    }

    /** Libera el recurso; si hay esperando, se le asigna al primero. @return proceso despertado o null. */
    public PCB liberar(PCB p, int id) {
        Recurso r = get(id);
        if (r.getPropietario() != p) {
            throw new IllegalStateException(p.getNombre() + " no posee el Recurso " + id + ".");
        }
        r.setPropietario(null);
        PCB siguiente = r.getColaEspera().poll();
        if (siguiente != null) {
            r.setPropietario(siguiente);
            siguiente.setEstado(Estado.LISTO);
        }
        return siguiente;
    }

    /** Libera todo lo que retiene el proceso y lo saca de las colas. @return procesos despertados. */
    public List<PCB> liberarTodo(PCB p) {
        List<PCB> despertados = new ArrayList<>();
        for (Recurso r : recursos) {
            r.getColaEspera().remove(p);
            if (r.getPropietario() == p) {
                PCB d = liberar(p, r.getId());
                if (d != null) despertados.add(d);
            }
        }
        return despertados;
    }

    /**
     * Detecta una espera circular.
     * Cada proceso espera como máximo un recurso y cada recurso tiene un solo
     * dueño, por lo que cada nodo tiene a lo sumo una arista saliente: basta
     * seguir la cadena y ver si regresa a un nodo ya visitado.
     * @return lista de procesos en el ciclo (vacía si no hay deadlock)
     */
    public List<PCB> detectarDeadlock() {
        Map<PCB, PCB> espera = new LinkedHashMap<>();
        for (Recurso r : recursos) {
            if (r.getPropietario() != null) {
                for (PCB q : r.getColaEspera()) espera.put(q, r.getPropietario());
            }
        }
        for (PCB inicio : espera.keySet()) {
            List<PCB> camino = new ArrayList<>();
            PCB actual = inicio;
            while (actual != null && !camino.contains(actual)) {
                camino.add(actual);
                actual = espera.get(actual);
            }
            if (actual != null) {
                return new ArrayList<>(camino.subList(camino.indexOf(actual), camino.size()));
            }
        }
        return new ArrayList<>();
    }

    public void imprimir() {
        for (Recurso r : recursos) {
            System.out.println("  Recurso " + r.getId() + ": propietario="
                + (r.getPropietario() == null ? "(libre)" : r.getPropietario().getNombre())
                + " | en espera=" + r.getColaEspera());
        }
    }
}
