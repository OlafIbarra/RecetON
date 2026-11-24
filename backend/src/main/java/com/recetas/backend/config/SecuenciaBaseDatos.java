package com.recetas.backend.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * El esquema usa INT PRIMARY KEY sin autoincremento. Las secuencias generan
 * IDs para altas nuevas y se sincronizan con los registros actuales.
 * No crean ni alteran tablas o columnas.
 */
@Configuration
public class SecuenciaBaseDatos {

    @Bean
    ApplicationRunner prepararSecuencias(JdbcTemplate jdbc) {
        return argumentos -> {
            crearSecuencia(jdbc, "seq_usuarios",
                    "SELECT setval('seq_usuarios', GREATEST(COALESCE((SELECT MAX(id_usuario) FROM usuarios), 0) + 1, 1), false)");
            crearSecuencia(jdbc, "seq_recetas",
                    "SELECT setval('seq_recetas', GREATEST(COALESCE((SELECT MAX(id_receta) FROM recetas), 0) + 1, 1), false)");
            crearSecuencia(jdbc, "seq_categorias",
                    "SELECT setval('seq_categorias', GREATEST(COALESCE((SELECT MAX(id_categoria) FROM categorias), 0) + 1, 1), false)");
            crearSecuencia(jdbc, "seq_imagenes_receta",
                    "SELECT setval('seq_imagenes_receta', GREATEST(COALESCE((SELECT MAX(id_imagen) FROM imagenes_receta), 0) + 1, 1), false)");
            crearSecuencia(jdbc, "seq_ingredientes",
                    "SELECT setval('seq_ingredientes', GREATEST(COALESCE((SELECT MAX(id_ingrediente) FROM ingredientes), 0) + 1, 1), false)");
            crearSecuencia(jdbc, "seq_unidades_medida",
                    "SELECT setval('seq_unidades_medida', GREATEST(COALESCE((SELECT MAX(id_unidad) FROM unidades_medida), 0) + 1, 1), false)");
            crearSecuencia(jdbc, "seq_pasos_receta",
                    "SELECT setval('seq_pasos_receta', GREATEST(COALESCE((SELECT MAX(id_paso) FROM pasos_receta), 0) + 1, 1), false)");
            crearSecuencia(jdbc, "seq_equipos_cocina",
                    "SELECT setval('seq_equipos_cocina', GREATEST(COALESCE((SELECT MAX(id_equipo) FROM equipos_cocina), 0) + 1, 1), false)");
            crearSecuencia(jdbc, "seq_valoraciones",
                    "SELECT setval('seq_valoraciones', GREATEST(COALESCE((SELECT MAX(id_valoracion) FROM valoraciones), 0) + 1, 1), false)");
            crearSecuencia(jdbc, "seq_productos",
                    "SELECT setval('seq_productos', GREATEST(COALESCE((SELECT MAX(id_producto) FROM productos), 0) + 1, 1), false)");
            crearSecuencia(jdbc, "seq_carritos",
                    "SELECT setval('seq_carritos', GREATEST(COALESCE((SELECT MAX(id_carrito) FROM carritos), 0) + 1, 1), false)");
            crearSecuencia(jdbc, "seq_direcciones",
                    "SELECT setval('seq_direcciones', GREATEST(COALESCE((SELECT MAX(id_direccion) FROM direcciones), 0) + 1, 1), false)");
            crearSecuencia(jdbc, "seq_metodos_pago",
                    "SELECT setval('seq_metodos_pago', GREATEST(COALESCE((SELECT MAX(id_metodo_pago) FROM metodos_pago), 0) + 1, 1), false)");
            crearSecuencia(jdbc, "seq_pedidos",
                    "SELECT setval('seq_pedidos', GREATEST(COALESCE((SELECT MAX(id_pedido) FROM pedidos), 0) + 1, 1), false)");
        };
    }

    private void crearSecuencia(JdbcTemplate jdbc, String nombre, String sincronizacion) {
        jdbc.execute("CREATE SEQUENCE IF NOT EXISTS " + nombre + " START WITH 1");
        jdbc.queryForObject(sincronizacion, Long.class);
    }
}
