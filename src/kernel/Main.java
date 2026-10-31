package kernel;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.Scanner;

/**
 * Interfaz de línea de comandos (menú de texto) del Simulador del Kernel.
 * Todas las entradas se validan: texto, vacío, negativos, desbordes de int,
 * PIDs inexistentes, operaciones en estados inválidos, etc.
 */
public class Main {

    private static final Scanner SC = new Scanner(System.in);
    private static Kernel kernel = new Kernel();

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException ignorada) { }

        System.out.println("==============================================");
        System.out.println("   SIMULADOR DE FUNCIONES DEL KERNEL (SO)");
        System.out.println("   RAM: " + AdministradorMemoria.TOTAL_MB + " MB | Round Robin q=" + PlanificadorRR.QUANTUM);
        System.out.println("==============================================");

        while (true) {
            mostrarMenu();
            Integer op = leerEntero("Opción");
            if (op == null) continue;
            try {
                if (!procesar(op)) break;
            } catch (DesbordamientoMemoriaException e) {
                System.out.println("  [RECHAZADO] " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("  [ERROR INESPERADO] " + e);
            }
        }
        System.out.println("Simulador finalizado.");
    }

    private static void mostrarMenu() {
        System.out.println("\n----------------- MENÚ -----------------");
        System.out.println(" PROCESOS Y MEMORIA");
        System.out.println("  1. Crear proceso            2. Tabla de PCB");
        System.out.println("  3. Mapa de memoria          4. Generar procesos aleatorios (estrés)");
        System.out.println(" PLANIFICACIÓN ROUND ROBIN");
        System.out.println("  5. Ejecutar un turno        6. Ejecutar hasta terminar");
        System.out.println("  7. Estadísticas de tiempos  8. Calculadora teórica de espera RR");
        System.out.println(" SINCRONIZACIÓN (SEMÁFORO BINARIO)");
        System.out.println("  9. wait()  (entrar a sección crítica)");
        System.out.println(" 10. signal() (salir de sección crítica)");
        System.out.println(" 11. Demostración de condición de carrera");
        System.out.println(" INTERBLOQUEO");
        System.out.println(" 12. Solicitar recurso       13. Liberar recurso");
        System.out.println(" 14. Forzar deadlock         15. Detectar deadlock");
        System.out.println(" 16. Abortar proceso");
        System.out.println(" OTROS");
        System.out.println(" 17. Estado de semáforo y recursos");
        System.out.println(" 18. Casos de prueba automáticos (bitácora)");
        System.out.println(" 19. Reiniciar kernel         0. Salir");
    }

    /** @return false para terminar el programa. */
    private static boolean procesar(int op) throws DesbordamientoMemoriaException {
        switch (op) {
            case 0:
                return false;
            case 1: {
                Integer tam = leerEntero("Tamaño en memoria (MB)");
                if (tam == null) break;
                Integer raf = leerEntero("Ráfaga de CPU total (1-" + Kernel.RAFAGA_MAX + ")");
                if (raf == null) break;
                kernel.crearProceso(tam, raf);
                break;
            }
            case 2: kernel.imprimirTablaPCB(); break;
            case 3: kernel.imprimirMemoria(); break;
            case 4: estres(); break;
            case 5: kernel.ejecutarTurno(); break;
            case 6: kernel.ejecutarTodo(); break;
            case 7: kernel.imprimirEstadisticas(); break;
            case 8: calculadoraTeorica(); break;
            case 9: {
                Integer pid = leerEntero("PID del proceso");
                if (pid != null) kernel.entrarSeccionCritica(pid);
                break;
            }
            case 10: {
                Integer pid = leerEntero("PID del proceso");
                if (pid != null) kernel.salirSeccionCritica(pid);
                break;
            }
            case 11: {
                Integer n = leerEntero("Número de tareas (2-5)");
                if (n == null) break;
                Integer k = leerEntero("Incrementos por tarea (1-5)");
                if (k == null) break;
                SimuladorCarrera.demostrar(n, k);
                break;
            }
            case 12: {
                Integer pid = leerEntero("PID del proceso");
                if (pid == null) break;
                Integer r = leerEntero("Recurso (1 o 2)");
                if (r != null) kernel.solicitarRecurso(pid, r);
                break;
            }
            case 13: {
                Integer pid = leerEntero("PID del proceso");
                if (pid == null) break;
                Integer r = leerEntero("Recurso (1 o 2)");
                if (r != null) kernel.liberarRecurso(pid, r);
                break;
            }
            case 14: {
                Integer a = leerEntero("PID del Proceso A");
                if (a == null) break;
                Integer b = leerEntero("PID del Proceso B");
                if (b != null) kernel.forzarDeadlock(a, b);
                break;
            }
            case 15: kernel.detectarDeadlock(); break;
            case 16: {
                Integer pid = leerEntero("PID del proceso a abortar");
                if (pid != null) kernel.abortarProceso(pid);
                break;
            }
            case 17: kernel.imprimirSincronizacion(); break;
            case 18: menuCasos(); break;
            case 19:
                kernel = new Kernel();
                System.out.println("  Kernel reiniciado (RAM, procesos, semáforo y recursos en estado inicial).");
                break;
            default:
                System.out.println("  [ERROR] Opción '" + op + "' no existe en el menú.");
        }
        return true;
    }

    // ------------------------------------------------------------------

    private static void estres() throws DesbordamientoMemoriaException {
        Integer n = leerEntero("Cantidad de procesos aleatorios (1-50)");
        if (n == null) return;
        if (n < 1 || n > 50) {
            System.out.println("  [ERROR] La cantidad debe estar entre 1 y 50.");
            return;
        }
        Random rnd = new Random();
        for (int i = 0; i < n; i++) {
            int tam = rnd.nextInt(70) - 5;      // incluye valores inválidos (<=0)
            int raf = rnd.nextInt(20) - 2;      // incluye valores inválidos (<=0)
            System.out.println("> Aleatorio: tam=" + tam + " MB, ráfaga=" + raf);
            try {
                kernel.crearProceso(tam, raf);
            } catch (DesbordamientoMemoriaException e) {
                System.out.println("  [RECHAZADO] " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("  [PARÁMETRO INVÁLIDO] " + e.getMessage());
            }
        }
    }

    private static void calculadoraTeorica() {
        System.out.print("Ráfagas separadas por coma o espacio (ej. 7,4,5): ");
        String linea = leerLinea().trim();
        if (linea.isEmpty()) {
            System.out.println("  [ERROR] Entrada vacía.");
            return;
        }
        String[] partes = linea.split("[,\\s]+");
        if (partes.length > 20) {
            System.out.println("  [ERROR] Máximo 20 procesos.");
            return;
        }
        int[] r = new int[partes.length];
        try {
            for (int i = 0; i < partes.length; i++) {
                r[i] = Integer.parseInt(partes[i]);
                if (r[i] < 1 || r[i] > Kernel.RAFAGA_MAX) {
                    System.out.println("  [ERROR] Cada ráfaga debe estar entre 1 y " + Kernel.RAFAGA_MAX + ".");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("  [ERROR] Hay valores que no son enteros válidos.");
            return;
        }
        TeoriaRR.calcular(r, PlanificadorRR.QUANTUM);
    }

    private static void menuCasos() {
        System.out.println("  1. Rechazo por falta de RAM   2. Round Robin");
        System.out.println("  3. Semáforo / carrera         4. Deadlock");
        System.out.println("  5. Ejecutar todos");
        Integer c = leerEntero("Caso");
        if (c == null) return;
        switch (c) {
            case 1: CasosPrueba.rechazoMemoria(); break;
            case 2: CasosPrueba.roundRobin(); break;
            case 3: CasosPrueba.semaforo(); break;
            case 4: CasosPrueba.deadlock(); break;
            case 5: CasosPrueba.todos(); break;
            default: System.out.println("  [ERROR] Caso inexistente.");
        }
    }

    // ------------------------------------------------------------------
    // Lectura robusta de entrada
    // ------------------------------------------------------------------

    private static String leerLinea() {
        if (!SC.hasNextLine()) {
            System.out.println("\nFin de la entrada. Saliendo.");
            System.exit(0);
        }
        return SC.nextLine();
    }

    /** Lee un entero; si la entrada es inválida informa y devuelve null. */
    private static Integer leerEntero(String mensaje) {
        System.out.print(mensaje + ": ");
        String s = leerLinea().trim();
        if (s.isEmpty()) {
            System.out.println("  [ERROR] Entrada vacía; se esperaba un número entero.");
            return null;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            String mostrado = s.length() > 25 ? s.substring(0, 25) + "..." : s;
            System.out.println("  [ERROR] '" + mostrado + "' no es un entero válido (o excede el rango de int).");
            return null;
        }
    }
}
