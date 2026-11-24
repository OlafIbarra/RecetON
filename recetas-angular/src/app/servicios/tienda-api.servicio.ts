import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Carrito, Producto, SolicitudItemCarrito, SolicitudProducto } from '../interfaces/tienda.interface';

@Injectable({ providedIn: 'root' })
export class TiendaApiServicio {
  private readonly http: HttpClient = inject(HttpClient);
  private readonly baseUrl: string = environment.apiUrl;

  obtenerProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.baseUrl}/api/productos`);
  }

  obtenerProducto(idProducto: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.baseUrl}/api/productos/${idProducto}`);
  }

  crearProducto(solicitud: SolicitudProducto): Observable<Producto> {
    return this.http.post<Producto>(`${this.baseUrl}/api/productos`, solicitud);
  }

  actualizarProducto(idProducto: number, solicitud: SolicitudProducto): Observable<Producto> {
    return this.http.put<Producto>(`${this.baseUrl}/api/productos/${idProducto}`, solicitud);
  }

  eliminarProducto(idProducto: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/productos/${idProducto}`);
  }

  obtenerCarrito(idUsuario: number): Observable<Carrito> {
    return this.http.get<Carrito>(`${this.baseUrl}/api/usuarios/${idUsuario}/carrito`);
  }

  agregarAlCarrito(idUsuario: number, solicitud: SolicitudItemCarrito): Observable<Carrito> {
    return this.http.post<Carrito>(`${this.baseUrl}/api/usuarios/${idUsuario}/carrito/items`, solicitud);
  }

  actualizarCantidadCarrito(idUsuario: number, idProducto: number, cantidad: number): Observable<Carrito> {
    return this.http.put<Carrito>(`${this.baseUrl}/api/usuarios/${idUsuario}/carrito/items/${idProducto}`, { cantidad });
  }

  quitarDelCarrito(idUsuario: number, idProducto: number): Observable<Carrito> {
    return this.http.delete<Carrito>(`${this.baseUrl}/api/usuarios/${idUsuario}/carrito/items/${idProducto}`);
  }

  vaciarCarrito(idUsuario: number): Observable<Carrito> {
    return this.http.delete<Carrito>(`${this.baseUrl}/api/usuarios/${idUsuario}/carrito`);
  }
}
