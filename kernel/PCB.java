package kernel;

/**
 * Bloque de Control de Proceso (PCB).
 * Contiene la información que el kernel necesita para administrar un proceso:
 * PID, estado, tamaño en memoria, ráfaga total y ráfaga restante.
 * Se agregan campos auxiliares para estadísticas (llegada/finalización).
 */
public class PCB {
    private final int pid;
    private final String nombre;
    private Estado estado;
    private final int tamanoMemoria;   // MB
    private final int rafagaTotal;     // unidades de CPU
    private int rafagaRestante;

    // Campos auxiliares
    private int baseMemoria = -1;      // dirección base en la RAM (MB)
    private int tiempoLlegada = 0;
    private int tiempoFinalizacion = -1;
    private boolean abortado = false;

    public PCB(int pid, String nombre, int tamanoMemoria, int rafagaTotal) {
        this.pid = pid;
        this.nombre = nombre;
        this.tamanoMemoria = tamanoMemoria;
        this.rafagaTotal = rafagaTotal;
        this.rafagaRestante = rafagaTotal;
        this.estado = Estado.NUEVO;
    }

    public int getPid() { return pid; }
    public String getNombre() { return nombre; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public int getTamanoMemoria() { return tamanoMemoria; }
    public int getRafagaTotal() { return rafagaTotal; }
    public int getRafagaRestante() { return rafagaRestante; }
    public void setRafagaRestante(int r) { this.rafagaRestante = r; }
    public int getBaseMemoria() { return baseMemoria; }
    public void setBaseMemoria(int b) { this.baseMemoria = b; }
    public int getTiempoLlegada() { return tiempoLlegada; }
    public void setTiempoLlegada(int t) { this.tiempoLlegada = t; }
    public int getTiempoFinalizacion() { return tiempoFinalizacion; }
    public void setTiempoFinalizacion(int t) { this.tiempoFinalizacion = t; }
    public boolean isAbortado() { return abortado; }
    public void setAbortado(boolean a) { this.abortado = a; }

    /** Tiempo de espera = finalización - llegada - ráfaga total. */
    public int getTiempoEspera() {
        return tiempoFinalizacion - tiempoLlegada - rafagaTotal;
    }

    /** Tiempo de retorno = finalización - llegada. */
    public int getTiempoRetorno() {
        return tiempoFinalizacion - tiempoLlegada;
    }

    @Override
    public String toString() { return nombre; }
}
