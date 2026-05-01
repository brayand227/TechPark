package com.techpark.techpark_uq.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementación propia de Conjunto usando Hash Table
 * Para almacenar atracciones favoritas sin duplicados
 * @param <T> Tipo de dato de los elementos
 */
public class Conjunto<T> implements Iterable<T> {
    
    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA = 0.75;
    
    private ListaEnlazada<T>[] buckets;
    private int tamanio;
    
    @SuppressWarnings("unchecked")
    public Conjunto() {
        this.buckets = new ListaEnlazada[CAPACIDAD_INICIAL];
        this.tamanio = 0;
    }
    
    // ============= FUNCIÓN HASH =============
    
    private int obtenerIndice(T elemento) {
        int hashCode = elemento != null ? elemento.hashCode() : 0;
        return Math.abs(hashCode) % buckets.length;
    }
    
    // ============= OPERACIONES BÁSICAS =============
    
    /**
     * Agregar un elemento al conjunto (si no existe)
     * @return true si se agregó, false si ya existía
     */
    public boolean agregar(T elemento) {
        if (contiene(elemento)) {
            return false;
        }
        
        // Redimensionar si es necesario
        if ((double) tamanio / buckets.length >= FACTOR_CARGA) {
            redimensionar();
        }
        
        int indice = obtenerIndice(elemento);
        if (buckets[indice] == null) {
            buckets[indice] = new ListaEnlazada<>();
        }
        
        buckets[indice].agregar(elemento);
        tamanio++;
        return true;
    }
    
    /**
     * Eliminar un elemento del conjunto
     * @return true si se eliminó, false si no existía
     */
    public boolean eliminar(T elemento) {
        int indice = obtenerIndice(elemento);
        ListaEnlazada<T> bucket = buckets[indice];
        
        if (bucket == null) {
            return false;
        }
        
        boolean eliminado = bucket.eliminar(elemento);
        if (eliminado) {
            tamanio--;
        }
        return eliminado;
    }
    
    /**
     * Verificar si un elemento existe en el conjunto
     */
    public boolean contiene(T elemento) {
        int indice = obtenerIndice(elemento);
        ListaEnlazada<T> bucket = buckets[indice];
        
        if (bucket == null) {
            return false;
        }
        
        return bucket.contiene(elemento);
    }
    
    /**
     * Obtener el tamaño del conjunto
     */
    public int getTamanio() {
        return tamanio;
    }
    
    /**
     * Verificar si el conjunto está vacío
     */
    public boolean estaVacio() {
        return tamanio == 0;
    }
    
    /**
     * Vaciar el conjunto
     */
    public void vaciar() {
        for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] != null) {
                buckets[i].vaciar();
            }
        }
        tamanio = 0;
    }
    
    // ============= OPERACIONES DE CONJUNTOS =============
    
    /**
     * Unión de dos conjuntos
     */
    public Conjunto<T> union(Conjunto<T> otro) {
        Conjunto<T> resultado = new Conjunto<>();
        
        // Agregar todos los elementos de este conjunto
        for (T elemento : this) {
            resultado.agregar(elemento);
        }
        
        // Agregar todos los elementos del otro conjunto
        for (T elemento : otro) {
            resultado.agregar(elemento);
        }
        
        return resultado;
    }
    
    /**
     * Intersección de dos conjuntos
     */
    public Conjunto<T> interseccion(Conjunto<T> otro) {
        Conjunto<T> resultado = new Conjunto<>();
        
        for (T elemento : this) {
            if (otro.contiene(elemento)) {
                resultado.agregar(elemento);
            }
        }
        
        return resultado;
    }
    
    /**
     * Diferencia de conjuntos (this - otro)
     */
    public Conjunto<T> diferencia(Conjunto<T> otro) {
        Conjunto<T> resultado = new Conjunto<>();
        
        for (T elemento : this) {
            if (!otro.contiene(elemento)) {
                resultado.agregar(elemento);
            }
        }
        
        return resultado;
    }
    
    /**
     * Verificar si es subconjunto de otro conjunto
     */
    public boolean esSubconjunto(Conjunto<T> otro) {
        for (T elemento : this) {
            if (!otro.contiene(elemento)) {
                return false;
            }
        }
        return true;
    }
    
    // ============= MÉTODOS PRIVADOS =============
    
    @SuppressWarnings("unchecked")
    private void redimensionar() {
        int nuevaCapacidad = buckets.length * 2;
        ListaEnlazada<T>[] nuevosBuckets = new ListaEnlazada[nuevaCapacidad];
        
        // Rehashear todos los elementos
        for (ListaEnlazada<T> bucket : buckets) {
            if (bucket != null) {
                for (T elemento : bucket) {
                    int nuevoIndice = elemento != null ? Math.abs(elemento.hashCode()) % nuevaCapacidad : 0;
                    if (nuevosBuckets[nuevoIndice] == null) {
                        nuevosBuckets[nuevoIndice] = new ListaEnlazada<>();
                    }
                    nuevosBuckets[nuevoIndice].agregar(elemento);
                }
            }
        }
        
        buckets = nuevosBuckets;
    }
    
    /**
     * Convertir a lista
     */
    public ListaEnlazada<T> toList() {
        ListaEnlazada<T> lista = new ListaEnlazada<>();
        for (T elemento : this) {
            lista.agregar(elemento);
        }
        return lista;
    }
    
    // ============= ITERADOR =============
    
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int bucketActual = 0;
            private Iterator<T> iteradorActual = null;
            
            private void avanzarAlSiguienteBucket() {
                while (bucketActual < buckets.length && 
                       (buckets[bucketActual] == null || iteradorActual == null || !iteradorActual.hasNext())) {
                    if (bucketActual < buckets.length && buckets[bucketActual] != null) {
                        iteradorActual = buckets[bucketActual].iterator();
                        if (iteradorActual.hasNext()) {
                            return;
                        }
                    }
                    bucketActual++;
                }
            }
            
            @Override
            public boolean hasNext() {
                if (iteradorActual == null) {
                    bucketActual = 0;
                    avanzarAlSiguienteBucket();
                }
                return iteradorActual != null && iteradorActual.hasNext();
            }
            
            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T elemento = iteradorActual.next();
                avanzarAlSiguienteBucket();
                return elemento;
            }
        };
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Conjunto{");
        boolean primero = true;
        for (T elemento : this) {
            if (!primero) {
                sb.append(", ");
            }
            sb.append(elemento);
            primero = false;
        }
        sb.append(", tamaño=").append(tamanio).append("}");
        return sb.toString();
    }
}