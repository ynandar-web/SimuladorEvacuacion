package evacuation.util.structure;

import java.util.NoSuchElementException;

/**
 * Cola FIFO genérica implementada con lista enlazada.
 * Modela el cuello de botella en escaleras y pasillos estrechos.
 * No usa java.util.Queue internamente: implementación manual requerida.
 */
public class ColaPasillo<T> {

    private Nodo<T> frente;
    private Nodo<T> final_;
    private int     tamanio;

    /** Agrega un elemento al final de la cola. O(1). */
    public void encolar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            final_.siguiente = nuevo;
        }
        final_  = nuevo;
        tamanio++;
    }

    /** Elimina y retorna el elemento del frente. O(1). */
    public T desencolar() {
        if (estaVacia()) throw new NoSuchElementException("Cola vacía.");
        T valor = frente.dato;
        frente  = frente.siguiente;
        if (frente == null) final_ = null;
        tamanio--;
        return valor;
    }

    /** Consulta el frente sin eliminar. O(1). */
    public T frente() {
        if (estaVacia()) throw new NoSuchElementException("Cola vacía.");
        return frente.dato;
    }

    public boolean estaVacia() { return tamanio == 0; }
    public int getTamanio()    { return tamanio; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Cola[");
        Nodo<T> actual = frente;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) sb.append(" → ");
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
