package kernel;

import java.util.LinkedList;
import java.util.Queue;

/** Recurso exclusivo (un solo propietario a la vez) con su cola de espera. */
public class Recurso {
    private final int id;
    private PCB propietario = null;
    private final Queue<PCB> colaEspera = new LinkedList<>();

    public Recurso(int id) { this.id = id; }

    public int getId() { return id; }
    public PCB getPropietario() { return propietario; }
    public void setPropietario(PCB p) { this.propietario = p; }
    public Queue<PCB> getColaEspera() { return colaEspera; }
}
