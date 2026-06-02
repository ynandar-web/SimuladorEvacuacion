package evacuation.util.structure;

import java.util.EmptyStackException;

/**
 * Pila LIFO genérica implementada con lista enlazada.
 * Almacena pasos de evacuación; desapilando se obtiene el camino hacia atrás.
 */
public class PilaRuta<T> {

    private Nodo<T> tope;
    private int     tamanio;

    /** Apila un elemento en el tope. O(1). */
    public void apilar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        nuevo.siguiente = tope;
        tope    = nuevo;
        tamanio++;
    }

    /** Elimina y retorna el elemento del tope. O(1). */
    public T desapilar() {
        if (estaVacia()) throw new EmptyStackException();
        T valor = tope.dato;
        tope    = tope.siguiente;
        tamanio--;
        return valor;
    }

    /** Consulta el tope sin eliminar. O(1). */
    public T verTope() {
        if (estaVacia()) throw new EmptyStackException();
        return tope.dato;
    }

    public boolean estaVacia() { return tamanio == 0; }
    public int getTamanio()    { return tamanio; }

    /** Imprime la pila mostrando el tope primero. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Pila(tope→base)[");
        Nodo<T> actual = tope;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) sb.append(" | ");
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
