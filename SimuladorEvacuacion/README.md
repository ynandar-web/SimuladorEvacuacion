# Simulador de Evacuación

Proyecto Java con **dos modos de ejecución**:

| Modo | Clase principal | Descripción |
|------|----------------|-------------|
| Consola (original) | `evacuation.main.Main` | Salida por terminal, exporta `.txt` |
| GUI Swing (nueva) | `evacuation.gui.MainGUI` | Interfaz gráfica completa |

> **Ninguna clase del modelo, servicio ni utilidad fue modificada.**
> La GUI es una capa adicional en `evacuation.gui.*`.

---

## Estructura del proyecto

```
src/main/java/evacuation/
├── main/
│   └── Main.java                      ← Entrada consola (original)
│
├── model/                             ← Sin cambios
│   ├── building/  EdificioNodos.java, NodoFactory.java
│   ├── graph/     Nodo.java, Arista.java, Grafo.java
│   ├── person/    Persona.java
│   └── zone/      Zona.java
│
├── service/                           ← Sin cambios
│   ├── graph/     BFSService.java, DijkstraService.java, RutaService.java
│   ├── evacuation/ EvacuacionService.java, SimulacionService.java
│   └── report/    ReporteService.java
│
├── util/                              ← Sin cambios
│   ├── structure/ ArbolRiesgo.java, TablaHash.java,
│   │              ColaPasillo.java, PilaRuta.java, ListaOcupantes.java
│   └── export/    ExportadorTxt.java
│
└── gui/                               ← NUEVO — solo capa visual
    ├── MainGUI.java                   ← Entrada GUI
    ├── model/
    │   └── EstadoSimulacion.java      ← Modelo MVC de la GUI
    ├── canvas/
    │   └── GrafoCanvas.java           ← Visualización Java2D del grafo
    └── panel/
        ├── VentanaPrincipal.java      ← JFrame orquestador
        ├── PanelControl.java          ← Botones / configuración
        ├── PanelPersonas.java         ← Estado de ocupantes
        ├── PanelZonas.java            ← Árbol de riesgo visual
        └── PanelLog.java              ← Log de simulación
```

---

## Compilar y ejecutar

```bash
# Compilar
javac -d out $(find src -name "*.java")

# Modo consola (original)
java -cp out evacuation.main.Main

# Modo GUI
java -cp out evacuation.gui.MainGUI
```

---

## Funcionalidades de la GUI

- **Grafo del edificio** dibujado con Java2D (nodos, aristas con peso, bloqueos)
- **Selector de algoritmo** BFS / Dijkstra antes de iniciar
- **Configuración de bloqueos** (humo / incendio) con checkboxes
- **Control de simulación**: Iniciar · Turno a turno · Auto-play · Reset
- **Barra de progreso** de evacuados en tiempo real
- **Panel de ocupantes** con estado individual por persona
- **Panel de zonas** con barra de riesgo visual (ArbolRiesgo inOrden)
- **Log de simulación** con colores por tipo de mensaje

---

## Principios de diseño respetados

| Principio | Dónde aplica |
|-----------|-------------|
| SRP | Cada panel tiene una sola responsabilidad visual |
| OCP | Se añadió GUI sin tocar clases existentes |
| LSP | `GrafoCanvas` acepta cualquier subtipo de `Nodo` |
| DIP | `EstadoSimulacion` recibe `RutaService` por inyección |
| Observer | `VentanaPrincipal` se registra en `EstadoSimulacion` |
