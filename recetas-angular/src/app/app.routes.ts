import { Routes } from '@angular/router';
import { InicioSesionComponent } from './paginas/inicio-sesion/inicio-sesion.component';
import { OAuthCallbackComponent } from './paginas/oauth-callback/oauth-callback.component';
import { PaginaIndiceComponent } from './paginas/pagina-indice/pagina-indice.component';
import { PaginaRelojComponent } from './paginas/pagina-reloj/pagina-reloj.component';
import { PaginaTelefonoComponent } from './paginas/pagina-telefono/pagina-telefono.component';
import { PaginaTabletaComponent } from './paginas/pagina-tableta/pagina-tableta.component';
import { PaginaTvComponent } from './paginas/pagina-tv/pagina-tv.component';
import { DetalleRecetaComponent } from './paginas/detalle-receta/detalle-receta.component';
import { PaginaFavoritosComponent } from './paginas/pagina-favoritos/pagina-favoritos.component';
import { PaginaProductosComponent } from './paginas/pagina-productos/pagina-productos.component';
import { PaginaCarritoComponent } from './paginas/pagina-carrito/pagina-carrito.component';
import { PaginaAdministracionComponent } from './paginas/pagina-administracion/pagina-administracion.component';
import { PaginaPrivacidadArcoComponent } from './paginas/pagina-privacidad-arco/pagina-privacidad-arco.component';
import { PaginaMiCuentaComponent } from './paginas/pagina-mi-cuenta/pagina-mi-cuenta.component';
import { PaginaAdministracionArcoComponent } from './paginas/pagina-administracion-arco/pagina-administracion-arco.component';
import { PaginaFinalizarCompraComponent } from './paginas/pagina-finalizar-compra/pagina-finalizar-compra.component';
import { PaginaPedidosComponent } from './paginas/pagina-pedidos/pagina-pedidos.component';
import { PaginaAdministracionPedidosComponent } from './paginas/pagina-administracion-pedidos/pagina-administracion-pedidos.component';
import { administradorGuardia } from './guardias/administrador.guardia';
import { autenticacionGuardia } from './guardias/autenticacion.guardia';

/**
 * Flujo: inicio de sesión -> índice -> una de las 4 pantallas.
 * Todas las rutas privadas están protegidas por la guardia de autenticación.
 */
export const routes: Routes = [
  { path: '', redirectTo: 'inicio-sesion', pathMatch: 'full' },
  { path: 'inicio-sesion', component: InicioSesionComponent },
  { path: 'oauth-callback', component: OAuthCallbackComponent },
  { path: 'privacidad', component: PaginaPrivacidadArcoComponent },
  { path: 'mi-cuenta', component: PaginaMiCuentaComponent, canActivate: [autenticacionGuardia] },
  { path: 'inicio', component: PaginaIndiceComponent, canActivate: [autenticacionGuardia] },
  { path: 'reloj', component: PaginaRelojComponent, canActivate: [autenticacionGuardia] },
  { path: 'telefono', component: PaginaTelefonoComponent, canActivate: [autenticacionGuardia] },
  { path: 'tableta', component: PaginaTabletaComponent, canActivate: [autenticacionGuardia] },
  { path: 'tv', component: PaginaTvComponent, canActivate: [autenticacionGuardia] },
  { path: 'receta/:id', component: DetalleRecetaComponent, canActivate: [autenticacionGuardia] },
  { path: 'favoritos', component: PaginaFavoritosComponent, canActivate: [autenticacionGuardia] },
  { path: 'productos', component: PaginaProductosComponent, canActivate: [autenticacionGuardia] },
  { path: 'carrito', component: PaginaCarritoComponent, canActivate: [autenticacionGuardia] },
  { path: 'finalizar-compra', component: PaginaFinalizarCompraComponent, canActivate: [autenticacionGuardia] },
  { path: 'mis-pedidos', component: PaginaPedidosComponent, canActivate: [autenticacionGuardia] },
  { path: 'administracion/solicitudes-arco', component: PaginaAdministracionArcoComponent, canActivate: [autenticacionGuardia, administradorGuardia] },
  { path: 'administracion/pedidos', component: PaginaAdministracionPedidosComponent, canActivate: [autenticacionGuardia, administradorGuardia] },
  { path: 'administracion', component: PaginaAdministracionComponent, canActivate: [autenticacionGuardia, administradorGuardia] },
  { path: '**', redirectTo: 'inicio-sesion' },
];
