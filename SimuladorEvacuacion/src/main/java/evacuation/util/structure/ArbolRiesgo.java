package evacuation.util.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * Árbol binario de búsqueda genérico con clave comparable.
 * Clave: nivelRiesgo (Integer) → Zona.
 * InOrden produce zonas de menor a mayor riesgo.
 */
public class ArbolRiesgo<K extends Comparable<K>, V> {

    private Nodo<K, V> raiz;
    private int        tamanio;

    /** Inserta o actualiza. O(log n) promedio. */
    public void insertar(K clave, V valor) {
        raiz = insertarRec(raiz, clave, valor);
    }

    /** Busca por clave. O(log n) promedio. */
    public V buscar(K clave) {
        Nodo<K, V> n = buscarRec(raiz, clave);
        return n == null ? null : n.valor;
    }

    /** Elimina por clave (método de Hibbard). O(log n) promedio. */
    public void eliminar(K clave) {
        raiz = eliminarRec(raiz, clave);
    }

    /** Recorrido inOrden → lista ascendente por clave. */
    public List<V> inOrden() {
        List<V> resultado = new ArrayList<>();
        inOrdenRec(raiz, resultado);
        return resultado;
    }

    public int getTamanio() { return tamanio; }
    public boolean estaVacio() { return raiz == null; }

    // ── Recursivos ─────────────────────────────────────────────────

    private Nodo<K, V> insertarRec(Nodo<K, V> nodo, K clave, V valor) {
        if (nodo == null) { tamanio++; return new Nodo<>(clave, valor); }
        int cmp = clave.compareTo(nodo.clave);
        if      (cmp < 0) nodo.izq = insertarRec(nodo.izq, clave, valor);
        else if (cmp > 0) nodo.der = insertarRec(nodo.der, clave, valor);
        else              nodo.valor = valor;   // actualiza
        return nodo;
    }

    private Nodo<K, V> buscarRec(Nodo<K, V> nodo, K clave) {
        if (nodo == null) return null;
        int cmp = clave.compareTo(nodo.clave);
        if      (cmp < 0) return buscarRec(nodo.izq, clave);
        else if (cmp > 0) return buscarRec(nodo.der, clave);
        else              return nodo;
    }

    private Nodo<K, V> eliminarRec(Nodo<K, V> nodo, K clave) {
        if (nodo == null) return null;
        int cmp = clave.compareTo(nodo.clave);
        if (cmp < 0) { nodo.izq = eliminarRec(nodo.izq, clave); }
        else if (cmp > 0) { nodo.der = eliminarRec(nodo.der, clave); }
        else {
            tamanio--;
            if (nodo.izq == null) return nodo.der;
            if (nodo.der == null) return nodo.izq;
            // Sucesor inOrden (mínimo del subárbol derecho)
            Nodo<K, V> sucesor = minimo(nodo.der);
            nodo.clave = sucesor.clave;
            nodo.valor = sucesor.valor;
            tamanio++;   // se descontará al eliminar sucesor
            nodo.der = eliminarRec(nodo.der, sucesor.clave);
        }
        return nodo;
    }

    private void inOrdenRec(Nodo<K, V> nodo, List<V> acc) {
        if (nodo == null) return;
        inOrdenRec(nodo.izq, acc);
        acc.add(nodo.valor);
        inOrdenRec(nodo.der, acc);
    }

    private Nodo<K, V> minimo(Nodo<K, V> nodo) {
        while (nodo.izq != null) nodo = nodo.izq;
        return nodo;
    }

    // ── Nodo interno ───────────────────────────────────────────────
    private static class Nodo<K, V> {
        K clave; V valor;
        Nodo<K, V> izq, der;
        Nodo(K clave, V valor) { this.clave = clave; this.valor = valor; }
    }
}
