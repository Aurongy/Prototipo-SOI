package kernel;

/**
 * Error de desbordamiento (Out of Memory) lanzado cuando no existe
 * espacio contiguo suficiente en la RAM simulada para un proceso.
 */
public class DesbordamientoMemoriaException extends Exception {
    private static final long serialVersionUID = 1L;

    public DesbordamientoMemoriaException(String mensaje) {
        super(mensaje);
    }
}
