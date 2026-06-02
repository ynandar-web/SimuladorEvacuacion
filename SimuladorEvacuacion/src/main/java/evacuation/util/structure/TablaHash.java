package evacuation.util.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tabla hash genérica con encadenamiento para resolver colisiones.
 * Clave: idPersona → Persona; operaciones en O(1) promedio.
 */
public class TablaHash<K, V> {

    private static final int    CAPACIDAD_INICIAL  = 16;
    private static final double FACTOR_CARGA_MAX   = 0.75;

    @SuppressWarnings("unchecked")
    private Lista<Entry<K, V>>[] tabla = new Lista[CAPACIDAD_INICIAL];
    private int                  tamanio = 0;
    private int                  capacidad = CAPACIDAD_INICIAL;

    /** Inserta o actualiza un par clave-valor. O(1) promedio. */
    public void poner(K clave, V valor) {
        if ((double) tamanio / capacidad >= FACTOR_CARGA_MAX) rehash();

        int idx    = indice(clave);
        if (tabla[idx] == null) tabla[idx] = new Lista<>();

        for (Entry<K, V> e : tabla[idx]) {
            if (e.clave.equals(clave)) { e.valor = valor; return; }
        }
        tabla[idx].agregar(new Entry<>(clave, valor));
        tamanio++;
    }

    /** Retorna el valor asociado a la clave o null. O(1) promedio. */
    public V obtener(K clave) {
        int idx = indice(clave);
        if (tabla[idx] == null) return null;
        for (Entry<K, V> e : tabla[idx]) {
            if (e.clave.equals(clave)) return e.valor;
        }
        return null;
    }

    /** Elimina la entrada con esa clave. O(1) promedio. */
    public boolean eliminar(K clave) {
        int idx = indice(clave);
        if (tabla[idx] == null) return false;
        Entry<K, V> objetivo = null;
        for (Entry<K, V> e : tabla[idx]) {
            if (e.clave.equals(clave)) { objetivo = e; break; }
        }
        if (objetivo != null) { tabla[idx].eliminar(objetivo); tamanio--; return true; }
        return false;
    }

    public boolean contiene(K clave) { return obtener(clave) != null; }
    public int getTamanio()          { return tamanio; }

    /** Devuelve todos los valores almacenados. */
    public Collection<V> valores() {
        List<V> lista = new ArrayList<>();
        for (Lista<Entry<K, V>> bucket : tabla) {
            if (bucket == null) continue;
            for (Entry<K, V> e : bucket) lista.add(e.valor);
        }
        return lista;
    }

    // ── Privados ───────────────────────────────────────────────────

    private int indice(K clave) {
        return Math.abs(clave.hashCode() % capacidad);
    }

    @SuppressWarnings("unchecked")
    private void rehash() {
        capacidad *= 2;
        Lista<Entry<K, V>>[] nuevaTabla = new Lista[capacidad];
        for (Lista<Entry<K, V>> bucket : tabla) {
            if (bucket == null) continue;
            for (Entry<K, V> e : bucket) {
                int idx = Math.abs(e.clave.hashCode() % capacidad);
                if (nuevaTabla[idx] == null) nuevaTabla[idx] = new Lista<>();
                nuevaTabla[idx].agregar(e);
            }
        }
        tabla = nuevaTabla;
    }

    // ── Tipos internos ─────────────────────────────────────────────

    private static class Entry<K, V> {
        K clave; V valor;
        Entry(K clave, V valor) { this.clave = clave; this.valor = valor; }
    }

    /** Mini lista enlazada para los buckets. */
    private static class Lista<E> implements Iterable<E> {
        private Nodo<E> cabeza;

        void agregar(E e) {
            Nodo<E> n = new Nodo<>(e); n.sig = cabeza; cabeza = n;
        }
        void eliminar(E e) {
            if (cabeza == null) return;
            if (cabeza.dato.equals(e)) { cabeza = cabeza.sig; return; }
            Nodo<E> a = cabeza;
            while (a.sig != null) {
                if (a.sig.dato.equals(e)) { a.sig = a.sig.sig; return; }
                a = a.sig;
            }
        }
        @Override
        public java.util.Iterator<E> iterator() {
            return new java.util.Iterator<>() {
                Nodo<E> c = cabeza;
                public boolean hasNext() { return c != null; }
                public E next() { E d = c.dato; c = c.sig; return d; }
            };
        }
        private static class Nodo<E> { E dato; Nodo<E> sig; Nodo(E d) { dato = d; } }
    }
}
