package evacuation.model.building;

import evacuation.model.graph.Nodo;

/**
 * Fábrica centralizada para crear nodos del edificio.
 * Principio OCP: agregar nuevos tipos no requiere modificar clientes.
 */
public class NodoFactory {

    private NodoFactory() { /* utilidad estática */ }

    public static Nodo habitacion(String id, String nombre, int piso, int capacidad) {
        return new Habitacion(id, nombre, piso, capacidad);
    }

    public static Nodo escalera(String id, String nombre, int pisoBase, int pisoCima) {
        return new Escalera(id, nombre, pisoBase, pisoCima);
    }

    public static Nodo pasillo(String id, String nombre, int piso, int anchoMetros) {
        return new Pasillo(id, nombre, piso, anchoMetros);
    }

    public static Nodo salida(String id, String codigoEmergencia, int piso) {
        return new Salida(id, codigoEmergencia, piso, codigoEmergencia);
    }
}
