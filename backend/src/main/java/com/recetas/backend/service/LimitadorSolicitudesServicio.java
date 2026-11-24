package com.recetas.backend.service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

/**
 * Contadores concurrentes en memoria para limitar intentos de autenticación.
 * No modifica la base de datos ni almacena contraseñas, tokens o cuerpos HTTP.
 */
@Service
public class LimitadorSolicitudesServicio {

    private final ConcurrentHashMap<String, Ventana> ventanas = new ConcurrentHashMap<>();
    private final AtomicLong operaciones = new AtomicLong();

    /** Consulta si una clave ya alcanzó su límite, sin consumir un intento. */
    public Resultado comprobar(String clave, int maximo, Duration periodo) {
        validarParametros(clave, maximo, periodo);
        long ahora = System.nanoTime();
        AtomicReference<Resultado> resultado = new AtomicReference<>(Resultado.ok());

        // No se crea una entrada para cada nombre de usuario enviado por un
        // visitante: solo existen claves después de un fallo real de login.
        ventanas.computeIfPresent(clave, (llave, ventana) -> {
            if (ahora - ventana.inicioNanos >= ventana.duracionNanos) {
                return null;
            }
            if (ventana.solicitudes >= maximo) {
                resultado.set(Resultado.bloqueado(segundosRestantes(ventana, ahora)));
            }
            return ventana;
        });
        limpiarPeriodicamente(ahora);
        return resultado.get();
    }

    /** Consume una solicitud (por ejemplo, una llamada al endpoint OAuth). */
    public Resultado consumir(String clave, int maximo, Duration periodo) {
        validarParametros(clave, maximo, periodo);
        long ahora = System.nanoTime();
        long duracion = periodo.toNanos();
        AtomicReference<Resultado> resultado = new AtomicReference<>(Resultado.ok());

        ventanas.compute(clave, (llave, actual) -> {
            Ventana ventana = normalizar(actual, ahora, duracion);
            if (ventana.solicitudes >= maximo) {
                resultado.set(Resultado.bloqueado(segundosRestantes(ventana, ahora)));
            } else {
                ventana.solicitudes++;
                resultado.set(Resultado.ok());
            }
            return ventana;
        });
        limpiarPeriodicamente(ahora);
        return resultado.get();
    }

    /** Registra únicamente un fallo real de autenticación, no errores CSRF. */
    public Resultado registrarFallo(String clave, int maximo, Duration periodo) {
        validarParametros(clave, maximo, periodo);
        long ahora = System.nanoTime();
        long duracion = periodo.toNanos();
        AtomicReference<Resultado> resultado = new AtomicReference<>(Resultado.ok());

        ventanas.compute(clave, (llave, actual) -> {
            Ventana ventana = normalizar(actual, ahora, duracion);
            if (ventana.solicitudes >= maximo) {
                resultado.set(Resultado.bloqueado(segundosRestantes(ventana, ahora)));
            } else {
                ventana.solicitudes++;
                resultado.set(ventana.solicitudes >= maximo
                        ? Resultado.bloqueado(segundosRestantes(ventana, ahora))
                        : Resultado.ok());
            }
            return ventana;
        });
        limpiarPeriodicamente(ahora);
        return resultado.get();
    }

    /** Elimina el contador al autenticarse correctamente. */
    public void limpiar(String clave) {
        if (clave != null && !clave.isBlank()) {
            ventanas.remove(clave);
        }
    }

    private Ventana normalizar(Ventana actual, long ahora, long duracion) {
        if (actual == null || ahora - actual.inicioNanos >= actual.duracionNanos) {
            return new Ventana(ahora, 0, duracion);
        }
        return actual;
    }

    private long segundosRestantes(Ventana ventana, long ahora) {
        long restante = Math.max(1L, ventana.duracionNanos - (ahora - ventana.inicioNanos));
        return Math.max(1L, (restante + 999_999_999L) / 1_000_000_000L);
    }

    private void limpiarPeriodicamente(long ahora) {
        if ((operaciones.incrementAndGet() & 127L) != 0L) {
            return;
        }
        ventanas.entrySet().removeIf(entrada ->
                ahora - entrada.getValue().inicioNanos >= entrada.getValue().duracionNanos);
    }

    private void validarParametros(String clave, int maximo, Duration periodo) {
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("La clave del limitador no puede estar vacía.");
        }
        if (maximo < 1) {
            throw new IllegalArgumentException("El máximo de solicitudes debe ser mayor que cero.");
        }
        if (periodo == null || periodo.isZero() || periodo.isNegative()) {
            throw new IllegalArgumentException("El periodo del limitador debe ser positivo.");
        }
    }

    private static final class Ventana {
        private final long inicioNanos;
        private final long duracionNanos;
        private int solicitudes;

        private Ventana(long inicioNanos, int solicitudes, long duracionNanos) {
            this.inicioNanos = inicioNanos;
            this.solicitudes = solicitudes;
            this.duracionNanos = duracionNanos;
        }
    }

    public record Resultado(boolean permitido, long segundosEspera) {
        static Resultado ok() {
            return new Resultado(true, 0L);
        }

        static Resultado bloqueado(long segundosEspera) {
            return new Resultado(false, segundosEspera);
        }
    }
}
