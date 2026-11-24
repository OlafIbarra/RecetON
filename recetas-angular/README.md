# Form Recetas — Frontend Angular

Proyecto reestructurado para cumplir con los 10 puntos que pidió el profesor.
Todo el código (carpetas, archivos, variables, funciones, comentarios) está
en español, salvo las palabras reservadas de TypeScript/Angular
(`import`, `export`, `class`, `interface`, etc.), que son obligatorias del lenguaje.

## Cómo correrlo

```bash
npm install
npm start
```

Abre `http://localhost:4200`. El flujo es:

1. **Inicio de sesión** (`/inicio-sesion`) — usuario de prueba: `admin` / `admin123`
   (ver `servicios/autenticacion.servicio.ts`).
2. **Índice** (`/inicio`) — aquí eliges a cuál de las 4 pantallas entrar, y
   también se ve la "receta sorpresa" que trae la API externa.
3. Cada pantalla es una **página independiente**, con su propia URL:
   - `/reloj` — diseño para reloj / smartwatch
   - `/telefono` — diseño para teléfono
   - `/tableta` — diseño para tableta
   - `/tv` — diseño para pantalla de TV

   Desde cualquiera de ellas puedes volver al índice con "Cambiar pantalla"
   (en la barra lateral de TV, o en el menú de "Perfil" de teléfono/tableta;
   en el reloj hay un botón "Índice").

Si además quieres que jale datos reales, levanta también tu backend de
Spring Boot en el puerto 8080 (`http://localhost:8080/api/recipes` y
`/api/categories` deben responder JSON). Si el backend no está corriendo,
el login sigue funcionando en modo local y el panel te avisa con un mensaje
que no pudo conectarse, en vez de romperse.

## Los 10 puntos, uno por uno

**1. Diseño modular** → cada una de las 4 pantallas es una página propia
en `src/app/paginas/` (`pagina-reloj`, `pagina-telefono`, `pagina-tableta`,
`pagina-tv`), armada con piezas reutilizables de `src/app/componentes/`
(icono, barra-busqueda, pestanas-navegacion, banner-principal,
lista-categorias, tarjeta-receta, cuadricula-recetas, barra-lateral,
barra-inferior, tarjeta-reloj, tarjeta-pantalla, receta-sorpresa).

**2. Tipado estricto** → todas las variables, parámetros y retornos de
función tienen su tipo explícito (`string`, `number`, `boolean`,
interfaces propias). No hay ningún `any` en el proyecto.

**3. Login funcional** → `paginas/inicio-sesion/` con formulario reactivo
tipado (`FormGroup<FormularioInicioSesion>`), validaciones y redirección
al índice si el login es correcto.

**4. Interfaces de TypeScript** → carpeta `src/app/interfaces/`:
`usuario.interface.ts`, `receta.interface.ts`, `categoria.interface.ts`,
`pestana.interface.ts`, `icono.interface.ts`, `enlace-menu.interface.ts`,
`opcion-pantalla.interface.ts`, `panel.interface.ts`,
`receta-externa.interface.ts`. Todo el JSON que entra o sale de la app pasa
por una de estas interfaces.

**5. Validación del login mediante interfaces + consumo de API** →
`servicios/autenticacion.servicio.ts` intenta `POST /api/auth/login`
contra tu backend usando `CredencialesLogin` y `RespuestaLogin`; si el
backend no tiene ese endpoint todavía, cae en una validación local de
respaldo (mismo contrato de interfaces) para que puedas seguir probando.

**6. UX/UI responsiva, una página por pantalla, sin marcos de dispositivo**
→ 4 páginas independientes (`/reloj`, `/telefono`, `/tableta`, `/tv`), cada
una calcada del diseño de Figma que le corresponde. Dentro de cada una, el
layout sigue siendo fluido (CSS Grid/Flexbox, `clamp()`) por si la ventana
cambia de tamaño — pero no hay ningún contenedor fijo simulando el borde
de un teléfono, tablet, reloj o TV: cada página ES la pantalla completa.

**7. Persistencia de sesión y logout** → `autenticacion.servicio.ts`
guarda al usuario en `localStorage` al iniciar sesión y lo borra en
`cerrarSesion()`. `guardias/autenticacion.guardia.ts` protege `/inicio`,
`/reloj`, `/telefono`, `/tableta`, `/tv` y `/receta/:id`, mandando a
`/inicio-sesion` si no hay sesión. El botón de salir vive en
`app-barra-lateral` (TV), en el menú de `app-barra-inferior` (teléfono y
tableta) y como botón directo en `pagina-reloj`.

**8. Manual de usuario y prototipo Figma** → carpeta `documentacion/`
con una plantilla lista para llenar (`manual-usuario/README.md`) y un
espacio para tu archivo de Figma (`prototipo-figma/README.md`).

## Estructura de carpetas

```
src/app/
├── interfaces/                contratos de los datos (JSON) que viajan en la app
├── servicios/                 lógica de negocio + llamadas HTTP
│   ├── autenticacion.servicio.ts
│   ├── receta-api.servicio.ts        (backend propio: Spring Boot)
│   └── receta-externa.servicio.ts    (API pública externa: TheMealDB)
├── guardias/
│   └── autenticacion.guardia.ts      protege todas las rutas privadas
├── componentes/                piezas reutilizables
│   ├── icono/                      <app-icono>
│   ├── barra-busqueda/             <app-barra-busqueda>
│   ├── pestanas-navegacion/        <app-pestanas-navegacion>   (teléfono y tableta)
│   ├── banner-principal/           <app-banner-principal>      (tamaño por @Input)
│   ├── lista-categorias/           <app-lista-categorias>
│   ├── tarjeta-receta/             <app-tarjeta-receta>
│   ├── cuadricula-recetas/         <app-cuadricula-recetas>    (columnas por @Input)
│   ├── barra-lateral/              <app-barra-lateral>         (solo TV)
│   ├── barra-inferior/             <app-barra-inferior>        (teléfono y tableta)
│   ├── tarjeta-reloj/              <app-tarjeta-reloj>         (solo reloj)
│   ├── tarjeta-pantalla/           <app-tarjeta-pantalla>      (botones del índice)
│   └── receta-sorpresa/            <app-receta-sorpresa>       (muestra la API externa)
├── paginas/                    una carpeta por pantalla, cada una con su propia ruta
│   ├── inicio-sesion/                /inicio-sesion
│   ├── pagina-indice/                /inicio      (elige una de las 4 pantallas)
│   ├── pagina-reloj/                 /reloj
│   ├── pagina-telefono/              /telefono
│   ├── pagina-tableta/               /tableta
│   ├── pagina-tv/                    /tv
│   └── detalle-receta/               /receta/:id
├── app.routes.ts               mapea URL -> página, con la guardia en las privadas
├── app.config.ts               registra el router y HttpClient
└── app.component.ts/html       solo contiene <router-outlet>

documentacion/
├── manual-usuario/          plantilla del manual (punto 8)
└── prototipo-figma/         donde vas a poner el link/capturas de Figma
```

## Notas honestas (para que no te tomen por sorpresa)

- El login real contra tu backend solo funcionará cuando agregues un
  endpoint `POST /api/auth/login` en Spring Boot. Mientras tanto, usa
  `admin / admin123` (modo local, ver arriba).
- `receta-api.servicio.ts` asume que tu backend de Spring Boot sigue
  llamándose `Recipe`/`Category` con campos en inglés (`name`, `image`,
  etc.), porque así se construyó en la sesión anterior. Si algún día
  también traduces esos campos a español en el backend, avísame y
  ajustamos el servicio para que ya no tenga que "traducir" nada.
- La "receta sorpresa" del índice depende de que tu computadora tenga
  internet normal hacia `themealdb.com` (es una API pública externa, no
  tu backend). Si no hay internet, el índice muestra un mensaje de error
  en esa tarjeta nada más — el resto de la app sigue funcionando igual.
