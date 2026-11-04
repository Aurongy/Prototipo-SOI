# KernelLab --- Simulador de funciones del Kernel de un Sistema Operativo

> Proyecto académico de **Sistemas Operativos I**\
> Implementación en Java de una simulación modular de funciones
> fundamentales del kernel.

## Descripción

**KernelLab** es un simulador educativo que representa, mediante una
interfaz gráfica de escritorio y una interfaz de consola, algunos
mecanismos básicos de un sistema operativo: administración de procesos,
planificación de CPU, gestión de memoria, sincronización mediante
semáforos y manejo de interbloqueos (*deadlocks*).

El proyecto trabaja con recursos simulados. No administra la memoria
física del equipo ni ejecuta procesos del sistema operativo anfitrión;
reproduce su comportamiento para facilitar el aprendizaje y la
demostración de los conceptos.

## Objetivos

-   Representar procesos mediante su Bloque de Control de Procesos
    (PCB).
-   Simular la planificación de CPU con Round Robin y quantum fijo de 3.
-   Administrar un espacio de RAM simulado de 100 MB.
-   Demostrar exclusión mutua y sincronización con un semáforo binario.
-   Provocar, detectar y resolver un escenario de interbloqueo con dos
    recursos exclusivos.

## Requisitos técnicos del proyecto

### A. Gestión y planificación de procesos

-   Cada proceso se representa mediante un **PCB** que contiene:
    -   PID (identificador del proceso).
    -   Estado actual.
    -   Tamaño asignado en memoria.
    -   Ráfaga de CPU total.
    -   Ráfaga de CPU restante.
-   Se utiliza **Round Robin (RR)** con quantum fijo `q = 3`.
-   Los procesos listos se organizan en una cola.
-   Si un proceso agota el quantum y todavía no termina, se simula un
    cambio de contexto y vuelve al final de la cola.

### B. Gestión de memoria

-   Se simula una RAM limitada a **100 MB**.
-   Al crear un proceso se valida si existe espacio disponible.
-   El proyecto utiliza asignación contigua dinámica para administrar
    los bloques de memoria.
-   Si no existe espacio suficiente, se rechaza la creación del proceso
    y se informa el desbordamiento de memoria (*Out of Memory*).
-   Al finalizar o abortar un proceso, su memoria se libera.

### C. Concurrencia y sincronización

-   Se simula una sección crítica asociada a una variable global
    compartida.
-   Se implementa un semáforo binario con operaciones explícitas:
    -   `wait()`: solicita acceso a la sección crítica.
    -   `signal()`: libera la sección crítica.
-   Si un proceso intenta entrar cuando el semáforo está ocupado, se
    bloquea y se incorpora a una cola de espera específica.
-   Cuando se libera el semáforo, el simulador puede despertar y
    transferir el acceso al proceso que esperaba.

### D. Interbloqueo (Deadlock)

-   Se simulan dos recursos exclusivos: **Recurso 1 (R1)** y **Recurso 2
    (R2)**.
-   El escenario de interbloqueo requerido es:
    -   El proceso A retiene R1 y solicita R2.
    -   El proceso B retiene R2 y solicita R1.
-   El simulador debe detectar la espera circular y alertar sobre el
    deadlock.
-   El usuario dispone de una opción para abortar uno de los procesos,
    liberar sus recursos y romper el ciclo de interbloqueo.

## Tecnologías

-   **Lenguaje:** Java.
-   **Interfaz gráfica:** Java Swing.
-   **Entorno recomendado:** Visual Studio Code con soporte para Java o
    un IDE compatible.
-   **Control de versiones:** Git y GitHub.

## Estructura del repositorio

``` text
Prototipo-SOI/
├── kernel/                 # Código Java de la versión inicial del repositorio
├── src/
│   └── kernel/             # Código fuente Java de la versión actual
│       ├── AdministradorMemoria.java
│       ├── CasosPrueba.java
│       ├── DesbordamientoMemoriaException.java
│       ├── Estado.java
│       ├── GestorRecursos.java
│       ├── Graficos.java
│       ├── Kernel.java
│       ├── Main.java
│       ├── MainGrafico.java
│       ├── PCB.java
│       ├── PlanificadorRR.java
│       ├── Recurso.java
│       ├── SemaforoBinario.java
│       ├── SimuladorCarrera.java
│       ├── Tema.java
│       ├── TeoriaRR.java
│       ├── VentanaKernel.java
│       └── Widgets.java
└── README.md
```

> Para trabajar con la versión actual, utiliza los archivos de
> `src/kernel/`. La carpeta `kernel/` corresponde a la estructura
> anterior que permanece en el repositorio.

## Requisitos para ejecutar

-   Tener instalado un **JDK** compatible con el código fuente.
-   Tener Git si se desea clonar el repositorio.
-   Utilizar un IDE compatible con proyectos Java o compilar desde la
    terminal.

## Descargar el proyecto

Clona el repositorio:

``` bash
git clone https://github.com/Aurongy/Prototipo-SOI.git
cd Prototipo-SOI
```

## Ejecución desde Visual Studio Code

1.  Abre la carpeta del repositorio en Visual Studio Code.
2.  Comprueba que la extensión de Java esté instalada y que el proyecto
    haya sido reconocido.
3.  Abre `src/kernel/MainGrafico.java`.
4.  Ejecuta la clase para iniciar la interfaz gráfica de KernelLab.

Para iniciar la versión de consola, abre y ejecuta
`src/kernel/Main.java`.

## Compilación y ejecución desde terminal

Desde la carpeta raíz del repositorio, en una terminal de Windows
PowerShell con el JDK configurado en el `PATH`:

``` powershell
New-Item -ItemType Directory -Force out
javac -d out src/kernel/*.java
```

Para iniciar la interfaz gráfica:

``` powershell
java -cp out kernel.MainGrafico
```

Para iniciar la versión de consola:

``` powershell
java -cp out kernel.Main
```

## Pruebas y validaciones realizadas

Las siguientes pruebas se han ejecutado en la interfaz gráfica durante
la validación del proyecto:

  -----------------------------------------------------------------------
  Componente              Prueba realizada        Resultado observado
  ----------------------- ----------------------- -----------------------
  Memoria                 Crear procesos y        Correcto
                          comprobar el uso de RAM 
                          simulada                

  Memoria                 Solicitar más memoria   Solicitud rechazada
                          que el espacio contiguo 
                          disponible              

  Round Robin             Ejecutar dos procesos   Ambos finalizaron;
                          con ráfagas de 7 y      reloj final `t = 14`
                          quantum 3               

  Semáforo                P1 ejecuta `wait()` y   P2 queda bloqueado
                          P2 intenta entrar       

  Semáforo                P1 ejecuta `signal()`   P2 despierta y recibe
                                                  el acceso a la sección
                                                  crítica
  -----------------------------------------------------------------------

**Pendiente de documentar con evidencia:** prueba completa de deadlock
(detección y aborto de un proceso), pruebas adicionales de
concurrencia/condición de carrera y ejecución desde un entorno limpio
después de clonar el repositorio.

## Equipo de desarrollo

Proyecto desarrollado colaborativamente por los integrantes del equipo
de Sistemas Operativos I.

-   Repositorio:
    [Aurongy/Prototipo-SOI](https://github.com/Aurongy/Prototipo-SOI)
-   Rama principal: `master`

## Estado del proyecto

Este repositorio contiene el código fuente del simulador. La
documentación académica, las capturas de pruebas y los resultados de la
demostración pueden incorporarse a medida que se completen y validen.

## Licencia

Proyecto desarrollado con fines académicos. No se ha definido una
licencia de distribución.
