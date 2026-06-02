package evacuation.util.structure;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Lista enlazada genérica para recorrer ocupantes por piso/zona.
 * Evita el uso de arrays; permite filtrado sin java.util.stream.
 */
public class ListaOcupantes<T> implements Iterable<T> {

    private Nodo<T> cabeza;
    private int     tamanio;

    /** Agrega al final de la lista. O(n). */
    public void agregar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            Nodo<T> actual = cabeza;
            while (actual.siguiente != null) actual = actual.siguiente;
            actual.siguiente = nuevo;
        }
        tamanio++;
    }

    /** Elimina la primera ocurrencia igual a elemento. O(n). */
    public boolean eliminar(T elemento) {
        if (cabeza == null) return false;
        if (cabeza.dato.equals(elemento)) {
            cabeza = cabeza.siguiente;
            tamanio--;
            return true;
        }
        Nodo<T> actual = cabeza;
        while (actual.siguiente != null) {
            if (actual.siguiente.dato.equals(elemento)) {
                actual.siguiente = actual.siguiente.siguiente;
                tamanio--;
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    /** Devuelve la primera coincidencia con el predicado o null. O(n). */
    public T buscar(Predicate<T> predicado) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (predicado.test(actual.dato)) return actual.dato;
            actual = actual.siguiente;
        }
        return null;
    }

    public boolean estaVacia() { return tamanio == 0; }
    public int getTamanio()    { return tamanio; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            Nodo<T> actual = cabeza;
            @Override public boolean hasNext() { return actual != null; }
            @Override public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Lista[");
        Nodo<T> actual = cabeza;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) sb.append(" ↔ ");
            actual = actual.siguiente;
        }
        return sb.append("]").toString();
    }

    // ── Nodo interno ───────────────────────────────────────────────
    private static class Nodo<T> {
        T      dato;
        Nodo<T> siguiente;
        Nodo(T dato) { this.dato = dato; }
    }
}
