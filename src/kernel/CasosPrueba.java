package kernel;

/**
 * Casos de prueba automáticos que reproducen los escenarios exigidos para la
 * Bitácora del reporte. Cada caso usa un Kernel nuevo e independiente.
 */
public class CasosPrueba {

    private static void titulo(String t) {
        System.out.println("\n########## " + t + " ##########");
    }

    /** Caso 1: rechazo de procesos por falta de RAM (Out of Memory). */
    public static void rechazoMemoria() {
        titulo("CASO 1: RECHAZO DE PROCESOS POR FALTA DE RAM");
        Kernel k = new Kernel();
        int[][] casos = { {40, 5}, {40, 5}, {30, 5}, {20, 4}, {1, 2}, {0, 3}, {-5, 3} };
        for (int[] c : casos) {
            System.out.println("> crearProceso(tam=" + c[0] + " MB, ráfaga=" + c[1] + ")");
            try {
                k.crearProceso(c[0], c[1]);
            } catch (DesbordamientoMemoriaException e) {
                System.out.println("  [RECHAZADO] " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("  [PARÁMETRO INVÁLIDO] " + e.getMessage());
            }
        }
        k.imprimirMemoria();
        k.imprimirTablaPCB();
    }

    /** Caso 2: sucesión de ráfagas en Round Robin y comparación con el cálculo teórico. */
    public static void roundRobin() {
        titulo("CASO 2: SUCESIÓN DE RÁFAGAS EN ROUND ROBIN (q=3)");
        Kernel k = new Kernel();
        int[] rafagas = { 7, 4, 5 };
        try {
            for (int r : rafagas) k.crearProceso(10, r);
        } catch (DesbordamientoMemoriaException e) {
            System.out.println(e.getMessage());
        }
        k.ejecutarTodo();
        k.imprimirEstadisticas();
        k.imprimirMemoria();
        System.out.println();
        TeoriaRR.calcular(rafagas, PlanificadorRR.QUANTUM);
    }

    /** Caso 3: efectividad del semáforo ante condiciones de carrera. */
    public static void semaforo() {
        titulo("CASO 3: SEMÁFORO BINARIO Y CONDICIONES DE CARRERA");
        SimuladorCarrera.demostrar(2, 2);
        System.out.println("\n--- Uso manual del semáforo con procesos del kernel ---");
        Kernel k = new Kernel();
        try {
            k.crearProceso(10, 6);
            k.crearProceso(10, 6);
        } catch (DesbordamientoMemoriaException e) {
            System.out.println(e.getMessage());
        }
        k.entrarSeccionCritica(1);
        k.entrarSeccionCritica(2);   // debe bloquearse
        k.imprimirSincronizacion();
        k.imprimirTablaPCB();
        k.salirSeccionCritica(1);    // despierta a P2
        k.imprimirSincronizacion();
        k.salirSeccionCritica(2);
        k.imprimirSincronizacion();
    }

    /** Caso 4: detección y eliminación de un deadlock. */
    public static void deadlock() {
        titulo("CASO 4: DETECCIÓN Y ELIMINACIÓN DE DEADLOCK");
        Kernel k = new Kernel();
        try {
            k.crearProceso(10, 6);
            k.crearProceso(10, 5);
        } catch (DesbordamientoMemoriaException e) {
            System.out.println(e.getMessage());
        }
        k.forzarDeadlock(1, 2);
        k.imprimirTablaPCB();
        k.imprimirSincronizacion();
        System.out.println("\n> Intento de ejecutar RR con ambos procesos bloqueados:");
        k.ejecutarTodo();
        System.out.println("\n> Se aborta P2 para romper el ciclo:");
        k.abortarProceso(2);
        k.detectarDeadlock();
        k.imprimirTablaPCB();
        k.imprimirSincronizacion();
        System.out.println("\n> P1 continúa y finaliza:");
        k.ejecutarTodo();
        k.imprimirMemoria();
    }

    public static void todos() {
        rechazoMemoria();
        roundRobin();
        semaforo();
        deadlock();
    }
}
