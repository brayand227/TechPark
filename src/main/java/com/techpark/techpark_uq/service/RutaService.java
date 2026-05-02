package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.estructuras.Grafo;
import com.techpark.techpark_uq.estructuras.ListaEnlazada;
import com.techpark.techpark_uq.exception.BusinessException;
import com.techpark.techpark_uq.model.dto.PasoRutaDTO;
import com.techpark.techpark_uq.model.dto.RutaDTO;
import com.techpark.techpark_uq.model.dto.RutaMultipleDTO;
import com.techpark.techpark_uq.model.dto.SolicitudRutaDTO;
import com.techpark.techpark_uq.model.entity.Atraccion;
import com.techpark.techpark_uq.repository.AtraccionRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de Rutas y Mapas
 * Utiliza nuestro Grafo propio con algoritmo de Dijkstra
 * Encuentra caminos más cortos entre atracciones
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RutaService {

    private final AtraccionRepository atraccionRepository;

    // Grafo que representa el mapa del parque (nodos = atracciones, aristas =
    // caminos)
    private final Grafo<Long> grafoParque = new Grafo<>();

    // Mapa para acceso rápido a coordenadas
    private final Map<Long, Atraccion> cacheAtracciones = new ConcurrentHashMap<>();

    // Velocidad de caminata promedio en metros por minuto
    private static final double VELOCIDAD_CAMINATA = 80.0; // 80 m/min ≈ 4.8 km/h

    // Velocidad en zona congestionada
    private static final double VELOCIDAD_CONGESTION = 50.0; // 50 m/min ≈ 3 km/h

    // Umbral de congestión (personas en cola)
    private static final int UMBRAL_CONGESTION = 100;

    // ============= INICIALIZACIÓN DEL MAPA =============

    /**
     * Inicializa el grafo del parque con todas las atracciones y caminos
     * Este método se ejecuta automáticamente al iniciar la aplicación
     */
    @PostConstruct
    public void inicializarMapa() {
        log.info("🗺️ Inicializando mapa del parque...");

        List<Atraccion> atracciones = atraccionRepository.findAll();

        if (atracciones.isEmpty()) {
            log.warn("No hay atracciones en la base de datos. El mapa se inicializará cuando se agreguen.");
            return;
        }

        // Agregar vértices (atracciones) al grafo
        for (Atraccion atraccion : atracciones) {
            cacheAtracciones.put(atraccion.getId(), atraccion);

            double x = atraccion.getPosicionX() != null ? atraccion.getPosicionX() : 0;
            double y = atraccion.getPosicionY() != null ? atraccion.getPosicionY() : 0;

            grafoParque.agregarVertice(atraccion.getId(), atraccion.getNombre(), x, y);
            log.debug("Vértice agregado: {} en ({}, {})", atraccion.getNombre(), x, y);
        }

        // Conectar atracciones con aristas (caminos)
        conectarAtraccionesPorZona();
        conectarAtraccionesCercanas();

        log.info("✅ Mapa del parque inicializado. Vértices: {}, Aristas: {}",
                grafoParque.getCantidadVertices(), grafoParque.getCantidadAristas());
    }

    /**
     * Conecta atracciones dentro de la misma zona
     */
    private void conectarAtraccionesPorZona() {
        Map<Long, List<Atraccion>> atraccionesPorZona = new HashMap<>();

        for (Atraccion atraccion : cacheAtracciones.values()) {
            if (atraccion.getZona() != null) {
                atraccionesPorZona.computeIfAbsent(atraccion.getZona().getId(), k -> new ArrayList<>())
                        .add(atraccion);
            }
        }

        for (List<Atraccion> atraccionesZona : atraccionesPorZona.values()) {
            for (int i = 0; i < atraccionesZona.size(); i++) {
                for (int j = i + 1; j < atraccionesZona.size(); j++) {
                    Atraccion a1 = atraccionesZona.get(i);
                    Atraccion a2 = atraccionesZona.get(j);

                    double distancia = calcularDistanciaEuclidiana(a1, a2);
                    // Conectar si están a menos de 150 metros (misma zona)
                    if (distancia <= 150) {
                        String nombreCamino = String.format("Camino interno - %s", a1.getZona().getNombre());
                        grafoParque.agregarArista(a1.getId(), a2.getId(), distancia, nombreCamino);
                        log.debug("Conectadas en zona: {} → {} (distancia: {:.1f}m)",
                                a1.getNombre(), a2.getNombre(), distancia);
                    }
                }
            }
        }
    }

    /**
     * Conecta atracciones cercanas entre diferentes zonas
     */
    private void conectarAtraccionesCercanas() {
        List<Atraccion> atracciones = new ArrayList<>(cacheAtracciones.values());

        for (int i = 0; i < atracciones.size(); i++) {
            for (int j = i + 1; j < atracciones.size(); j++) {
                Atraccion a1 = atracciones.get(i);
                Atraccion a2 = atracciones.get(j);

                // Si ya están en la misma zona, ya las conectamos antes
                if (a1.getZona() != null && a2.getZona() != null &&
                        a1.getZona().getId().equals(a2.getZona().getId())) {
                    continue;
                }

                double distancia = calcularDistanciaEuclidiana(a1, a2);
                // Conectar si están a menos de 100 metros (zonas cercanas)
                if (distancia <= 100) {
                    String nombreCamino = String.format("Conexión entre zonas (%s - %s)",
                            a1.getZona() != null ? a1.getZona().getNombre() : "Sin zona",
                            a2.getZona() != null ? a2.getZona().getNombre() : "Sin zona");
                    grafoParque.agregarArista(a1.getId(), a2.getId(), distancia, nombreCamino);
                    log.debug("Conectadas entre zonas: {} → {} (distancia: {:.1f}m)",
                            a1.getNombre(), a2.getNombre(), distancia);
                }
            }
        }
    }

    /**
     * Calcular distancia euclidiana entre dos atracciones
     */
    private double calcularDistanciaEuclidiana(Atraccion a1, Atraccion a2) {
        double x1 = a1.getPosicionX() != null ? a1.getPosicionX() : 0;
        double y1 = a1.getPosicionY() != null ? a1.getPosicionY() : 0;
        double x2 = a2.getPosicionX() != null ? a2.getPosicionX() : 0;
        double y2 = a2.getPosicionY() != null ? a2.getPosicionY() : 0;

        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    // ============= MÉTODOS PRINCIPALES =============

    /**
     * Encontrar la ruta más corta entre dos atracciones usando Dijkstra
     */
    public RutaDTO encontrarRutaMasCorta(SolicitudRutaDTO solicitud) {
        log.info("🔍 Buscando ruta de atracción {} a {}", solicitud.getOrigenId(), solicitud.getDestinoId());

        // Validar que las atracciones existen
        Atraccion origen = cacheAtracciones.get(solicitud.getOrigenId());
        Atraccion destino = cacheAtracciones.get(solicitud.getDestinoId());

        if (origen == null) {
            throw new BusinessException("Atracción origen no encontrada", "ORIGEN_NO_ENCONTRADO");
        }
        if (destino == null) {
            throw new BusinessException("Atracción destino no encontrada", "DESTINO_NO_ENCONTRADO");
        }

        // Ejecutar Dijkstra
        var resultado = grafoParque.dijkstra(solicitud.getOrigenId(), solicitud.getDestinoId());

        if (resultado.getCamino() == null || resultado.getCamino().getTamanio() == 0) {
            throw new BusinessException("No se encontró una ruta entre las atracciones", "RUTA_NO_ENCONTRADA");
        }

        // Construir la respuesta con los pasos detallados
        return construirRespuestaRuta(origen, destino, resultado);
    }

    /**
     * Encontrar una ruta que pase por múltiples atracciones (tour personalizado)
     */
    public RutaMultipleDTO encontrarRutaMultiple(List<Long> atraccionesIds) {
        log.info("🎢 Planificando ruta para {} atracciones", atraccionesIds.size());

        if (atraccionesIds == null || atraccionesIds.size() < 2) {
            throw new BusinessException("Se necesitan al menos 2 atracciones para planificar una ruta",
                    "NUMERO_ATRACCIONES_INSUFICIENTE");
        }

        List<RutaDTO> rutas = new ArrayList<>();
        double distanciaTotal = 0;
        int tiempoTotal = 0;

        for (int i = 0; i < atraccionesIds.size() - 1; i++) {
            SolicitudRutaDTO solicitud = new SolicitudRutaDTO();
            solicitud.setOrigenId(atraccionesIds.get(i));
            solicitud.setDestinoId(atraccionesIds.get(i + 1));

            try {
                RutaDTO ruta = encontrarRutaMasCorta(solicitud);
                rutas.add(ruta);
                distanciaTotal += ruta.getDistanciaTotal();
                tiempoTotal += ruta.getTiempoEstimadoTotal();
            } catch (BusinessException e) {
                log.warn("No se pudo encontrar ruta entre {} y {}", atraccionesIds.get(i), atraccionesIds.get(i + 1));
                throw new BusinessException("No es posible conectar todas las atracciones", "RUTA_INCOMPLETA");
            }
        }

        // Generar sugerencia basada en la distancia total
        String sugerencia = generarSugerenciaRuta(distanciaTotal, tiempoTotal);

        return RutaMultipleDTO.builder()
                .atraccionesIds(atraccionesIds)
                .rutas(rutas)
                .distanciaTotal(distanciaTotal)
                .tiempoTotal(tiempoTotal)
                .sugerencia(sugerencia)
                .build();
    }

    /**
     * Encontrar las atracciones más cercanas a una ubicación dada
     */
    public List<Map<String, Object>> encontrarAtraccionesCercanas(Long atraccionReferencia, int limite) {
        log.info("📍 Buscando {} atracciones cercanas a {}", limite, atraccionReferencia);

        Atraccion referencia = cacheAtracciones.get(atraccionReferencia);
        if (referencia == null) {
            throw new BusinessException("Atracción de referencia no encontrada", "REFERENCIA_NO_ENCONTRADA");
        }

        var masCercanos = grafoParque.encontrarMasCercanos(atraccionReferencia, limite);

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (var entry : masCercanos) {
            Long atraccionId = entry.getValue();
            Atraccion atraccion = cacheAtracciones.get(atraccionId);

            if (atraccion != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", atraccion.getId());
                item.put("nombre", atraccion.getNombre());
                item.put("tipo", atraccion.getTipo().toString());
                item.put("distancia", entry.getKey());
                item.put("tiempoEstimado", Math.round(entry.getKey() / VELOCIDAD_CAMINATA));
                resultado.add(item);
            }
        }

        return resultado;
    }

    /**
     * Obtener estado del mapa (conectividad, clusters, etc.)
     */
    public Map<String, Object> obtenerEstadoMapa() {
        Map<String, Object> estado = new HashMap<>();

        estado.put("vertices", grafoParque.getCantidadVertices());
        estado.put("aristas", grafoParque.getCantidadAristas());
        estado.put("esConexo", grafoParque.esConexo());

        // Detectar clusters de atracciones populares
        var clusters = grafoParque.detectarClusters(50.0); // Clusters con distancia <= 50m
        estado.put("clusters", clusters.size());

        List<Map<String, Object>> clustersInfo = new ArrayList<>();
        for (var cluster : clusters) {
            Map<String, Object> clusterInfo = new HashMap<>();
            List<String> nombres = new ArrayList<>();
            for (Long id : cluster) {
                Atraccion a = cacheAtracciones.get(id);
                if (a != null) {
                    nombres.add(a.getNombre());
                }
            }
            clusterInfo.put("tamanio", cluster.getTamanio());
            clusterInfo.put("atracciones", nombres);
            clustersInfo.add(clusterInfo);
        }
        estado.put("detalleClusters", clustersInfo);

        estado.put("grafoTexto", grafoParque.toString());

        return estado;
    }

    /**
     * Obtener el mapa visual en formato JSON para el frontend
     */
    public Map<String, Object> obtenerMapaVisual() {
        Map<String, Object> mapa = new HashMap<>();

        List<Map<String, Object>> nodos = new ArrayList<>();
        List<Map<String, Object>> aristas = new ArrayList<>();

        // Agregar nodos
        for (Long id : grafoParque.obtenerVertices()) {
            Atraccion atraccion = cacheAtracciones.get(id);
            if (atraccion != null) {
                Map<String, Object> nodo = new HashMap<>();
                nodo.put("id", id);
                nodo.put("nombre", atraccion.getNombre());
                nodo.put("tipo", atraccion.getTipo().toString());
                nodo.put("estado", atraccion.getEstado().toString());
                nodo.put("x", atraccion.getPosicionX() != null ? atraccion.getPosicionX() : 0);
                nodo.put("y", atraccion.getPosicionY() != null ? atraccion.getPosicionY() : 0);
                nodo.put("visitantes", atraccion.getContadorVisitantes());
                nodo.put("tiempoEspera", atraccion.getTiempoEsperaEstimado());
                nodos.add(nodo);
            }
        }

        // Agregar aristas (caminos)
        Set<String> aristasAgregadas = new HashSet<>();
        for (Long id : grafoParque.obtenerVertices()) {
            var vecinos = grafoParque.obtenerVecinos(id);
            for (Grafo.Arista<Long> arista : vecinos) {
                String key = Math.min(id, arista.getDestino()) + "-" + Math.max(id, arista.getDestino());
                if (!aristasAgregadas.contains(key)) {
                    Map<String, Object> aristaMap = new HashMap<>();
                    aristaMap.put("origen", id);
                    aristaMap.put("destino", arista.getDestino());
                    aristaMap.put("distancia", arista.getPeso());
                    aristaMap.put("tiempo", Math.round(arista.getPeso() / VELOCIDAD_CAMINATA));
                    aristaMap.put("nombre", arista.getNombreCamino());
                    aristas.add(aristaMap);
                    aristasAgregadas.add(key);
                }
            }
        }

        mapa.put("nodos", nodos);
        mapa.put("aristas", aristas);
        mapa.put("totalNodos", nodos.size());
        mapa.put("totalAristas", aristas.size());

        return mapa;
    }

    // ============= MÉTODOS PRIVADOS =============

    /**
     * Construye la respuesta de ruta con todos los detalles
     */
    private RutaDTO construirRespuestaRuta(Atraccion origen, Atraccion destino,
            Grafo.ResultadoDijkstra<Long> resultado) {

        List<PasoRutaDTO> pasos = new ArrayList<>();
        int orden = 1;

        // Convertir el camino de IDs a pasos detallados
        ListaEnlazada<Long> camino = resultado.getCamino();
        List<Long> listaCamino = new ArrayList<>();
        for (Long id : camino) {
            listaCamino.add(id);
        }

        for (int i = 0; i < listaCamino.size(); i++) {
            Long idActual = listaCamino.get(i);
            Atraccion actual = cacheAtracciones.get(idActual);

            if (actual == null)
                continue;

            double distanciaDesdeAnterior = 0;
            Integer tiempoEstimado = 0;
            String instruccion = "";

            if (i > 0) {
                Long idAnterior = listaCamino.get(i - 1);
                distanciaDesdeAnterior = grafoParque.obtenerPeso(idAnterior, idActual);

                // Calcular tiempo basado en congestión
                int visitantesEnCola = actual.getContadorVisitantes() != null ? actual.getContadorVisitantes() : 0;
                double velocidad = visitantesEnCola > UMBRAL_CONGESTION ? VELOCIDAD_CONGESTION : VELOCIDAD_CAMINATA;
                tiempoEstimado = (int) Math.round(distanciaDesdeAnterior / velocidad);

                instruccion = generarInstruccion(i, listaCamino, cacheAtracciones, grafoParque);
            }

            PasoRutaDTO paso = PasoRutaDTO.builder()
                    .atraccionId(actual.getId())
                    .atraccionNombre(actual.getNombre())
                    .tipoAtraccion(actual.getTipo().toString())
                    .distanciaDesdeAnterior(distanciaDesdeAnterior)
                    .tiempoEstimado(tiempoEstimado)
                    .instruccion(instruccion)
                    .orden(orden++)
                    .build();

            pasos.add(paso);
        }

        // Calcular tiempo total considerando congestión
        double distanciaTotal = resultado.getDistanciaTotal() != null ? resultado.getDistanciaTotal() : 0;
        int tiempoTotal = calcularTiempoTotalConCongestion(camino, cacheAtracciones);

        String mensaje = generarMensajeRuta(distanciaTotal, tiempoTotal, origen.getNombre(), destino.getNombre());

        return RutaDTO.builder()
                .origenId(origen.getId())
                .origenNombre(origen.getNombre())
                .destinoId(destino.getId())
                .destinoNombre(destino.getNombre())
                .pasos(pasos)
                .distanciaTotal(distanciaTotal)
                .tiempoEstimadoTotal(tiempoTotal)
                .mensaje(mensaje)
                .build();
    }

    /**
     * Genera instrucciones paso a paso para el visitante
     */
    private String generarInstruccion(int index, List<Long> camino,
            Map<Long, Atraccion> cache,
            Grafo<Long> grafo) {
        if (index == 0) {
            return "📍 Comienza aquí";
        }

        if (index == camino.size() - 1) {
            return "🏁 ¡Llegaste a tu destino!";
        }

        Long anterior = camino.get(index - 1);
        Long actual = camino.get(index);
        Long siguiente = index + 1 < camino.size() ? camino.get(index + 1) : null;

        Atraccion atracActual = cache.get(actual);

        if (siguiente == null) {
            return String.format("🎯 Dirígete a %s", atracActual.getNombre());
        }

        // Determinar si es giro o seguir recto
        Double angulo = calcularAngulo(anterior, actual, siguiente, cache, grafo);

        if (angulo == null) {
            return String.format("Continúa hacia %s", atracActual.getNombre());
        } else if (angulo < 45) {
            return String.format("Sigue derecho hacia %s", atracActual.getNombre());
        } else if (angulo < 135) {
            return String.format("Gira a la derecha hacia %s", atracActual.getNombre());
        } else {
            return String.format("Gira a la izquierda hacia %s", atracActual.getNombre());
        }
    }

    /**
     * Calcula el ángulo entre tres puntos para determinar giros
     */
    private Double calcularAngulo(Long anteriorId, Long actualId, Long siguienteId,
            Map<Long, Atraccion> cache, Grafo<Long> grafo) {
        Atraccion anterior = cache.get(anteriorId);
        Atraccion actual = cache.get(actualId);
        Atraccion siguiente = cache.get(siguienteId);

        if (anterior == null || actual == null || siguiente == null) {
            return null;
        }

        double x1 = anterior.getPosicionX() != null ? anterior.getPosicionX() : 0;
        double y1 = anterior.getPosicionY() != null ? anterior.getPosicionY() : 0;
        double x2 = actual.getPosicionX() != null ? actual.getPosicionX() : 0;
        double y2 = actual.getPosicionY() != null ? actual.getPosicionY() : 0;
        double x3 = siguiente.getPosicionX() != null ? siguiente.getPosicionX() : 0;
        double y3 = siguiente.getPosicionY() != null ? siguiente.getPosicionY() : 0;

        // Vector 1: de anterior a actual
        double v1x = x2 - x1;
        double v1y = y2 - y1;

        // Vector 2: de actual a siguiente
        double v2x = x3 - x2;
        double v2y = y3 - y2;

        // Producto punto
        double dot = v1x * v2x + v1y * v2y;

        // Magnitudes
        double mag1 = Math.sqrt(v1x * v1x + v1y * v1y);
        double mag2 = Math.sqrt(v2x * v2x + v2y * v2y);

        if (mag1 == 0 || mag2 == 0)
            return null;

        // Ángulo en radianes, convertir a grados
        double angulo = Math.acos(dot / (mag1 * mag2)) * 180.0 / Math.PI;

        // Determinar dirección del giro usando producto cruz
        double cross = v1x * v2y - v1y * v2x;
        if (cross < 0) {
            angulo = 360 - angulo;
        }

        return angulo;
    }

    /**
     * Calcula el tiempo total considerando congestión en cada atracción
     */
    private int calcularTiempoTotalConCongestion(ListaEnlazada<Long> camino,
            Map<Long, Atraccion> cache) {
        double tiempoTotal = 0;
        Long anterior = null;

        for (Long id : camino) {
            if (anterior != null) {
                double distancia = grafoParque.obtenerPeso(anterior, id);
                Atraccion destino = cache.get(id);
                int visitantes = destino != null && destino.getContadorVisitantes() != null
                        ? destino.getContadorVisitantes()
                        : 0;

                double velocidad = visitantes > UMBRAL_CONGESTION ? VELOCIDAD_CONGESTION : VELOCIDAD_CAMINATA;
                tiempoTotal += distancia / velocidad;
            }
            anterior = id;
        }

        return (int) Math.round(tiempoTotal);
    }

    /**
     * Genera mensaje amigable para la ruta
     */
    private String generarMensajeRuta(double distancia, int tiempo, String origen, String destino) {
        if (tiempo < 5) {
            return String.format("🎉 ¡Excelente! %s está muy cerca de %s. Solo %d minutos de caminata.",
                    destino, origen, tiempo);
        } else if (tiempo < 15) {
            return String.format("🚶‍♂️ %s está a %d minutos de %s. ¡Disfruta el paseo!",
                    destino, tiempo, origen);
        } else {
            return String.format("🗺️ Para llegar de %s a %s caminarás aproximadamente %d minutos (%.0f metros). " +
                    "Te recomendamos tomar un descanso a mitad del camino.",
                    origen, destino, tiempo, distancia);
        }
    }

    /**
     * Genera sugerencia basada en la distancia total de la ruta múltiple
     */
    private String generarSugerenciaRuta(double distanciaTotal, int tiempoTotal) {
        if (tiempoTotal < 30) {
            return "✅ Ruta corta y cómoda. ¡Perfecta para una visita rápida!";
        } else if (tiempoTotal < 60) {
            return "👍 Ruta de duración media. Te recomendamos llevar agua y tomar descansos.";
        } else {
            return "⚠️ Ruta larga. Sugerimos dividirla en dos visitas o usar silla de ruedas si es necesario.";
        }
    }

    /**
     * Refrescar el mapa (útil cuando se agregan nuevas atracciones)
     */
    public void refrescarMapa() {
        log.info("🔄 Refrescando mapa del parque...");
        cacheAtracciones.clear();

        List<Atraccion> atracciones = atraccionRepository.findAll();
        for (Atraccion atraccion : atracciones) {
            cacheAtracciones.put(atraccion.getId(), atraccion);
        }

        inicializarMapa();
    }
}