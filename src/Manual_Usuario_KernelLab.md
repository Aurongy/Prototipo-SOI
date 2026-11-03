# Manual de usuario — KernelLab
## Simulador de funciones del Kernel de un Sistema Operativo

**Versión:** Prototipo académico  
**Tecnología:** Java, Swing  
**Proyecto:** `Prototipo-SOI`

---

## 1. Introducción

KernelLab es un simulador educativo que permite observar cómo un sistema operativo administra procesos, memoria, planificación de CPU, sincronización y recursos. El programa es una simulación: no modifica el kernel real ni administra el hardware del equipo.

Este manual explica cómo iniciar el prototipo, utilizar la interfaz gráfica y comprender los resultados principales.

## 2. Requisitos

- Windows, Linux o macOS.
- Java JDK instalado.
- Los archivos fuente del proyecto, ubicados en `src/kernel/`.
- Para compilar desde una terminal, el comando `java` y el compilador `javac` deben estar disponibles en el PATH.

Compruebe la instalación desde una terminal:

```bash
java -version
javac -version
```

Se recomienda usar una versión de Java compatible con el código fuente instalado.

## 3. Iniciar el simulador

### 3.1 Desde IntelliJ IDEA

1. Descomprima `Prototipo-SOI-master.zip` en una carpeta.
2. Abra IntelliJ IDEA y seleccione **Open**.
3. Seleccione la carpeta `Prototipo-SOI-master`.
4. Configure un JDK para el proyecto si IntelliJ lo solicita.
5. En el panel del proyecto, abra `src/kernel/MainGrafico.java`.
6. Ejecute el método `main` con el botón **Run**.

Se abrirá la ventana **KernelLab | Simulador de Sistema Operativo**.

### 3.2 Desde una terminal

Desde la carpeta raíz del proyecto, compile los archivos Java:

```bash
javac -encoding UTF-8 -d out src/kernel/*.java
```

Luego inicie la interfaz gráfica:

```bash
java -cp out kernel.MainGrafico
```

Para utilizar la versión de consola en lugar de la ventana gráfica, ejecute:

```bash
java -cp out kernel.Main
```

Si los comandos no reconocen `javac` o `java`, revise la instalación del JDK y la variable PATH.

## 4. Descripción de la interfaz gráfica

La ventana presenta paneles de control y un área de salida. Los nombres y la distribución exacta pueden variar ligeramente según la resolución de pantalla.

<img width="1917" height="1016" alt="image" src="https://github.com/user-attachments/assets/c9757439-f6f7-4d59-b0ff-c08fb144a6e0" />

### 4.1 Indicadores generales

- **RAM física:** muestra la memoria utilizada respecto del total simulado de **100 MB**.
- **Procesos:** indica cuántos procesos aparecen registrados.
- **Reloj:** muestra el tiempo simulado de CPU.
- **Cambios de contexto:** cuenta los cambios entre turnos de ejecución.
- **Semáforo:** indica si la sección crítica está libre u ocupada y, cuando corresponde, qué proceso la posee.
- **Recursos:** muestra el estado de los recursos identificados como R1 y R2.
- **Tabla de procesos:** muestra PID, nombre, estado, RAM, dirección base, ráfaga total de CPU y ráfaga restante.
- **Salida del sistema:** registra mensajes de creación, ejecución, finalización y errores. El botón **Limpiar bitácora** borra los mensajes visibles de esta área; no reinicia por sí mismo los procesos.

### 4.2 Estados de los procesos

| Estado | Significado |
|---|---|
| `NUEVO` | El proceso acaba de ser creado. |
| `LISTO` | Está en la cola y puede recibir CPU. |
| `EJECUCION` / `EJECUTANDO` | Está usando la CPU en el turno actual. |
| `BLOQUEADO` | Espera un semáforo o recurso. |
| `TERMINADO` | Completó su ráfaga de CPU. |
| `ABORTADO` | Fue cancelado por el usuario. |

<img width="1557" height="602" alt="image" src="https://github.com/user-attachments/assets/10100f6d-2a09-437f-9cce-a9c0eadbf77a" />

## 5. Operaciones principales

### 5.1 Crear un proceso

1. Localice la sección de creación de procesos.
2. Introduzca el tamaño de memoria en MB.
3. Introduzca la ráfaga total de CPU.
4. Pulse **Crear proceso** (o el botón equivalente de creación).
5. Compruebe la tabla y la bitácora.

El sistema asigna automáticamente un PID y una dirección base de memoria. El proceso se incorpora a la cola de listos si la creación se completa correctamente.

**Reglas de entrada:**
- El tamaño de memoria debe ser mayor que cero.
- La ráfaga de CPU debe ser un entero entre 1 y 1000.
- La memoria total simulada es de 100 MB. Si no hay espacio suficiente, el proceso se rechaza y se informa el motivo.

**Ejemplo de prueba:** cree un proceso de 20 MB con una ráfaga de CPU de 7 unidades.

### 5.2 Ejecutar un turno

1. Cree dos o más procesos con diferentes ráfagas.
2. Pulse **Ejecutar turno**.
3. Observe cómo disminuye la ráfaga restante del proceso seleccionado.
4. Repita la operación para ver cómo avanzan los procesos de la cola.

El planificador utiliza **Round Robin** con un quantum fijo de **3 unidades de CPU**. Un proceso que no termina durante su turno vuelve al final de la cola de listos.

### 5.3 Ejecutar todo

Pulse **Ejecutar todo** para ejecutar los turnos pendientes hasta que ya no haya procesos listos para ejecutar. Observe el reloj simulado, los estados y los mensajes de finalización.

Cuando un proceso termina, el simulador libera la memoria que tenía asignada y puede liberar los recursos que poseía.

### 5.4 Consultar el mapa de memoria

Pulse **Mapa de memoria**. La información mostrada en la salida del sistema representa las regiones de memoria asignadas y disponibles.

Si la creación de un proceso falla por falta de memoria, pruebe a ejecutar procesos hasta que terminen y liberen memoria, y después vuelva a intentarlo.

### 5.5 Utilizar `wait()` y `signal()`

El semáforo binario simula la protección de una sección crítica compartida.

- **`wait()`**: intenta entrar en la sección crítica. Si está libre, el proceso obtiene el semáforo; si está ocupado, el proceso queda bloqueado y espera su turno.
- **`signal()`**: permite salir de la sección crítica. Si hay procesos esperando, se habilita el siguiente; de lo contrario, el semáforo queda libre.

Para probarlo:

1. Cree al menos dos procesos.
2. Introduzca el PID del proceso que realizará la operación, si la interfaz lo solicita.
3. Ejecute `wait()` con un proceso y compruebe que el semáforo cambia a ocupado.
4. Intente ejecutar `wait()` con otro proceso y observe la espera o el mensaje que aparece.
5. Ejecute `signal()` con el propietario actual para liberar la sección crítica o transferirla al siguiente proceso en espera.

Un proceso que no posee el semáforo no debe ejecutar `signal()` sobre él.

### 5.6 Solicitar y liberar recursos

El simulador incluye recursos exclusivos identificados como **R1** y **R2**.

1. Seleccione el PID del proceso que solicita el recurso.
2. Indique el recurso solicitado.
3. Confirme la operación con el control correspondiente.
4. Revise el estado del recurso y la bitácora.
5. Para liberar un recurso, seleccione el proceso propietario y utilice la opción de liberación.

Un recurso ocupado puede hacer que otro proceso tenga que esperar. Siga los mensajes de la bitácora para conocer el resultado de cada operación.

<img width="1462" height="156" alt="image" src="https://github.com/user-attachments/assets/afd06e47-0839-4523-8016-5b06405ced3d" />

### 5.7 Demostrar o detectar un interbloqueo (deadlock)

El interbloqueo ocurre cuando varios procesos quedan esperando recursos retenidos entre sí y ninguno puede continuar.

- **Forzar deadlock:** ejecuta una situación demostrativa para estudiar el problema.
- **Detectar deadlock:** revisa el estado de los recursos y las esperas para informar si se identifica un interbloqueo.

Utilice esta función como experimento académico. Después de la demostración, revise la bitácora y, si el estado no permite continuar con otras pruebas, reinicie el kernel.

### 5.8 Abortar un proceso

1. Introduzca el PID del proceso que desea abortar.
2. Use el botón de aborto correspondiente.
3. Confirme la operación si se solicita.
4. Revise la tabla y la bitácora.

Verifique siempre el PID antes de confirmar. El sistema puede rechazar operaciones sobre procesos inexistentes o que estén en un estado no válido para la acción.

### 5.9 Reiniciar el kernel



Utilice la opción **Reiniciar** solo cuando desee comenzar las pruebas desde cero. Confirme el diálogo si aparece.

El reinicio vuelve a crear el estado inicial del simulador: limpia los procesos, la memoria y los recursos simulados, y restablece los contadores. La acción no borra ni modifica archivos del proyecto.

## 6. Uso de la versión de consola

Si inicia `kernel.Main`, el simulador muestra un menú de texto. Introduzca el número de la opción y pulse Enter. Las opciones disponibles son:

| Opción | Función |
|---:|---|
| 1 | Crear proceso |
| 2 | Mostrar tabla de PCB |
| 3 | Mostrar mapa de memoria |
| 4 | Generar procesos aleatorios para una prueba de estrés |
| 5 | Ejecutar un turno de CPU |
| 6 | Ejecutar hasta terminar los procesos listos |
| 7 | Mostrar estadísticas de tiempos |
| 8 | Calcular teóricamente los tiempos de Round Robin |
| 9 | Ejecutar `wait()` |
| 10 | Ejecutar `signal()` |
| 11 | Demostrar condición de carrera |
| 12 | Solicitar un recurso |
| 13 | Liberar un recurso |
| 14 | Forzar un deadlock |
| 15 | Detectar deadlock |
| 16 | Abortar un proceso |
| 17 | Mostrar estado del semáforo y los recursos |
| 18 | Ejecutar casos de prueba automáticos |
| 19 | Reiniciar el kernel |
| 0 | Salir |

Siga las instrucciones que aparecen en pantalla para introducir los valores solicitados. Si un dato es inválido, el programa mostrará un mensaje y podrá solicitar nuevamente la entrada.

## 7. Ejemplo de recorrido recomendado

Para familiarizarse con el simulador, realice esta secuencia:

1. Inicie la interfaz gráfica.
2. Cree P1 con 20 MB y ráfaga 7.
3. Cree P2 con 15 MB y ráfaga 4.
4. Cree P3 con 10 MB y ráfaga 5.
5. Consulte el mapa de memoria.
6. Ejecute un turno y observe la ráfaga restante y el reloj.
7. Ejecute otros turnos y compare el orden de ejecución.
8. Ejecute todo para completar los procesos.
9. Compruebe que se libera la memoria de los procesos terminados.
10. Pruebe el semáforo y los recursos con procesos nuevos.
11. Revise la bitácora para documentar los resultados.

Los resultados exactos de los tiempos dependen del orden en que se creen y ejecuten los procesos y de las operaciones de sincronización realizadas.

## 8. Errores comunes y soluciones

| Situación | Qué hacer |
|---|---|
| No se abre la ventana | Confirme que ejecutó `kernel.MainGrafico` y que el JDK está configurado. |
| `java` o `javac` no se reconoce | Instale/configure el JDK y revise el PATH. |
| No se puede crear un proceso | Compruebe que el tamaño sea positivo y que exista memoria libre suficiente. |
| La ráfaga es inválida | Use un entero entre 1 y 1000. |
| PID inexistente | Consulte la tabla de procesos e introduzca un PID válido. |
| No se puede liberar el semáforo | Verifique que el proceso indicado sea el propietario de la sección crítica. |
| Un proceso queda bloqueado | Revise el semáforo, los recursos y la bitácora; puede estar esperando a otro proceso. |
| El simulador queda en un estado difícil de interpretar | Guarde las observaciones necesarias y reinicie el kernel para comenzar una prueba nueva. |

## 9. Recomendaciones

- Comience con procesos pequeños para comprender la planificación.
- Cree procesos con ráfagas diferentes para observar Round Robin.
- Tome nota del reloj, cambios de contexto y uso de RAM.
- Pruebe las operaciones de semáforo y recursos con cuidado, verificando siempre el PID propietario.
- Use el reinicio entre experimentos independientes.
- Para un informe académico, incluya capturas de la creación de procesos, tabla PCB, mapa de memoria, ejecución por turnos y pruebas de sincronización/interbloqueo.

## 10. Glosario

- **Kernel:** núcleo del sistema operativo que coordina recursos y procesos.
- **PCB (Process Control Block):** estructura que guarda la información de un proceso.
- **PID:** identificador único del proceso.
- **Ráfaga de CPU:** cantidad de unidades de procesamiento que requiere el proceso.
- **Quantum:** máximo de tiempo de CPU que recibe un proceso en un turno de Round Robin.
- **Cambio de contexto:** cambio de la CPU de un proceso a otro.
- **RAM:** memoria asignada a los procesos del simulador.
- **Sección crítica:** parte del trabajo que requiere acceso exclusivo a un recurso compartido.
- **Semáforo binario:** mecanismo de sincronización con estado libre/ocupado.
- **Condición de carrera:** resultado dependiente del orden de ejecución de operaciones concurrentes.
- **Deadlock o interbloqueo:** situación en la que procesos esperan recursos y no pueden avanzar.
