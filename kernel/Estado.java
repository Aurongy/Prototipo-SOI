package kernel;

/**
 * Estados posibles de un proceso (modelo de 5 estados).
 */
public enum Estado {
    NUEVO,       // Recién creado, aún sin entrar a la cola de listos
    LISTO,       // En la cola de listos esperando CPU
    EJECUCION,   // Usando la CPU
    BLOQUEADO,   // Esperando semáforo o recurso
    TERMINADO    // Finalizó (o fue abortado)
}
