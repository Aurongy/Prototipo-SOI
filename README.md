# KernelLab

### Simulador de funciones del kernel de un sistema operativo

![Java](https://img.shields.io/badge/Java-Swing-orange)
![Platform](https://img.shields.io/badge/Plataforma-Windows-blue)
![Project](https://img.shields.io/badge/Tipo-Proyecto%20acad%C3%A9mico-informational)
![Status](https://img.shields.io/badge/Estado-En%20validaci%C3%B3n-yellow)

**KernelLab** es un simulador académico desarrollado en Java que
representa mecanismos fundamentales de un sistema operativo:
administración de procesos, planificación de CPU, gestión de memoria,
sincronización y tratamiento de interbloqueos (*deadlocks*). Incluye una
interfaz gráfica de escritorio y una interfaz de consola.

> El programa simula estos mecanismos con datos y recursos virtuales. No
> modifica la memoria física, los procesos ni los recursos reales del
> sistema operativo anfitrión.

------------------------------------------------------------------------

## Tabla de contenidos

-   [Características](#características)
-   [Requisitos del proyecto](#requisitos-del-proyecto)
-   [Requisitos del equipo para
    ejecutar](#requisitos-del-equipo-para-ejecutar)
-   [Tecnologías](#tecnologías)
-   [Estructura del repositorio](#estructura-del-repositorio)
-   [Instalación](#instalación)
-   [Ejecución](#ejecución)
-   [Validación y pruebas](#validación-y-pruebas)
-   [Equipo](#equipo)

## Características

-   Administración de procesos mediante PCB (*Process Control Block*).
-   Planificación de CPU Round Robin con quantum fijo de 3 unidades.
-   Cola de procesos listos y simulación de cambios de contexto.
-   Administración de una RAM virtual de 100 MB.
-   Asignación contigua dinámica de memoria.
-   Semáforo binario con operaciones `wait()` y `signal()`.
-   Cola de procesos bloqueados por espera del semáforo.
-   Simulación de recursos exclusivos y detección/resolución de
    deadlock.
-   Interfaz gráfica Java Swing y modo de consola.

## Requisitos del proyecto

Los siguientes puntos corresponden a los requisitos funcionales
establecidos para el simulador.

### A. Gestión y planificación de procesos

Cada proceso debe representarse mediante un PCB que almacene:

  Campo             Descripción
  ----------------- ---------------------------------------
  PID               Identificador único del proceso.
  Estado            Estado actual del proceso.
  Memoria           Tamaño de memoria asignado.
  Ráfaga total      Tiempo de CPU requerido inicialmente.
  Ráfaga restante   Tiempo de CPU pendiente.

**Planificación Round Robin (RR):** - Quantum fijo: `q = 3`. - Los
procesos listos se administran mediante una cola. - Si un proceso
consume su quantum y no termina, vuelve al final de la cola. - Si
termina su ráfaga, finaliza y libera la memoria asignada.

### B. Gestión de memoria

-   RAM simulada: **100 MB**.
-   Asignación contigua dinámica.
-   Validación del espacio disponible al crear un proceso.
-   Rechazo de solicitudes que no puedan satisfacerse por falta de
    espacio contiguo.
-   Liberación de memoria al finalizar o abortar un proceso.

### C. Concurrencia y sincronización

-   Simulación de una variable compartida y una sección crítica.
-   Semáforo binario con operaciones explícitas `wait()` y `signal()`.
-   Si el semáforo está ocupado, el proceso solicitante pasa a una cola
    de espera.
-   Al liberar el semáforo, se despierta al proceso que esperaba y se
    transfiere el acceso según la implementación del simulador.

### D. Interbloqueo (Deadlock)

-   Dos recursos exclusivos: R1 y R2.
-   Escenario requerido:
    -   El proceso A retiene R1 y solicita R2.
    -   El proceso B retiene R2 y solicita R1.
-   Detección de la espera circular.
-   Resolución mediante el aborto de uno de los procesos y liberación de
    sus recursos.

## Requisitos del equipo para ejecutar

### Sistema operativo

-   **Plataforma objetivo documentada:** Microsoft Windows de 64 bits.
-   Se recomienda Windows 10 o Windows 11.
-   El simulador utiliza Java y Swing; no requiere acceso a hardware
    especial.

> La versión exacta de Windows utilizada durante las pruebas del equipo
> debe confirmarse y puede añadirse aquí. No se ha establecido
> compatibilidad verificada con Linux o macOS.

### Hardware

Los valores siguientes son una **referencia práctica recomendada para
ejecutar VS Code/IDE y Java**, no requisitos medidos por el simulador:

  -----------------------------------------------------------------------
  Componente           Mínimo práctico sugerido               Recomendado
  ------------------- ------------------------- -------------------------
  Procesador                          2 núcleos      4 núcleos o superior

  Memoria RAM del                          4 GB           8 GB o superior
  equipo                                        

  Espacio libre en         500 MB para código y    1 GB o más, sin contar
  disco                             compilación                   IDE/JDK

  Pantalla                           1366 × 768               1920 × 1080
  -----------------------------------------------------------------------

**Importante:** los 100 MB mencionados en el apartado de memoria son la
RAM virtual que administra KernelLab dentro de la simulación. No
representan la memoria RAM física necesaria en la computadora.

### Software

-   JDK (Java Development Kit) instalado.
-   Visual Studio Code con Extension Pack for Java, o un IDE compatible
    con Java.
-   Git para clonar y actualizar el repositorio.
-   PowerShell o una terminal compatible (para los comandos de
    compilación de Windows).

**Versión de Java:** el entorno de desarrollo utilizado en las pruebas
mostradas emplea un runtime Java 25. Para reproducir exactamente ese
entorno, se recomienda instalar un JDK 25. La compatibilidad con otras
versiones debe verificarse compilando y ejecutando el proyecto.

## Tecnologías

  Tecnología           Uso
  -------------------- ----------------------------------
  Java                 Lenguaje de programación.
  Java Swing           Interfaz gráfica de escritorio.
  Git                  Control de versiones.
  GitHub               Repositorio colaborativo.
  Visual Studio Code   Entorno de desarrollo utilizado.

## Estructura del repositorio

``` text
Prototipo-SOI/
├── src/
│   └── kernel/
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

`src/kernel/` contiene la versión actual del código fuente Java.

## Instalación

### 1. Clonar el repositorio

``` bash
git clone https://github.com/Aurongy/Prototipo-SOI.git
cd Prototipo-SOI
```

### 2. Abrir el proyecto

Abre la carpeta `Prototipo-SOI` en Visual Studio Code o en un IDE
compatible con Java. Verifica que el IDE detecte el JDK instalado.

## Ejecución

### Desde Visual Studio Code

**Interfaz gráfica:** 1. Abre `src/kernel/MainGrafico.java`. 2.
Selecciona **Run Java**. 3. Se iniciará la aplicación KernelLab.

**Interfaz de consola:** 1. Abre `src/kernel/Main.java`. 2. Selecciona
**Run Java**.

### Desde PowerShell

Desde la raíz del repositorio, con el JDK configurado en `PATH`:

``` powershell
New-Item -ItemType Directory -Force out
javac -d out src/kernel/*.java
```

Iniciar la interfaz gráfica:

``` powershell
java -cp out kernel.MainGrafico
```

Iniciar la interfaz de consola:

``` powershell
java -cp out kernel.Main
```

## Validación y pruebas

### Pruebas ejecutadas y observadas

  -----------------------------------------------------------------------
  Módulo                  Escenario               Resultado
  ----------------------- ----------------------- -----------------------
  Memoria                 Crear procesos y        Correcto en la prueba
                          asignar memoria         realizada

  Memoria                 Solicitar 81 MB cuando  Solicitud rechazada
                          solo había 80 MB libres 
                          contiguos               

  Round Robin             Dos procesos con ráfaga Ambos finalizaron;
                          7 y quantum 3           reloj final `t = 14`

  Semáforo                P1 ejecuta `wait()` y   P2 queda bloqueado
                          P2 solicita el semáforo 
                          ocupado                 

  Semáforo                P1 ejecuta `signal()`   P2 despierta y recibe
                                                  el acceso
  -----------------------------------------------------------------------

### Pendiente de completar

-   Validar la sección crítica y la variable compartida.
-   Ejecutar y documentar el escenario de condición de carrera.
-   Provocar el deadlock entre R1 y R2.
-   Verificar la detección del ciclo y el aborto de un proceso.
-   Realizar pruebas integrales y de estrés.
-   Completar el informe académico y sus evidencias.

Los resultados pendientes no deben considerarse aprobados hasta ejecutar
y registrar sus pruebas.

## Equipo

Proyecto colaborativo de **Sistemas Operativos I**.

-   Repositorio:
    [Aurongy/Prototipo-SOI](https://github.com/Aurongy/Prototipo-SOI)
-   Rama principal: `master`

Añadir aquí los nombres de todos los integrantes y sus responsabilidades
acordadas por el equipo.

## Licencia

Proyecto académico. No se ha especificado una licencia de distribución.
