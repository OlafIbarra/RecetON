import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  DireccionEnvio,
  MetodoPagoGuardado,
  PedidoDetalle,
  PedidoResumen,
  SolicitudCrearPedido,
  SolicitudDireccion,
  SolicitudEstadoPedido,
  SolicitudMetodoPago,
} from '../interfaces/pedidos.interface';

@Injectable({ providedIn: 'root' })
export class PedidosApiServicio {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  listarDirecciones(idUsuario: number): Observable<DireccionEnvio[]> {
    return this.http.get<DireccionEnvio[]>(`${this.baseUrl}/api/usuarios/${idUsuario}/direcciones`);
  }
  crearDireccion(idUsuario: number, solicitud: SolicitudDireccion): Observable<DireccionEnvio> {
    return this.http.post<DireccionEnvio>(`${this.baseUrl}/api/usuarios/${idUsuario}/direcciones`, solicitud);
  }
  actualizarDireccion(idUsuario: number, idDireccion: number, solicitud: SolicitudDireccion): Observable<DireccionEnvio> {
    return this.http.put<DireccionEnvio>(`${this.baseUrl}/api/usuarios/${idUsuario}/direcciones/${idDireccion}`, solicitud);
  }
  eliminarDireccion(idUsuario: number, idDireccion: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/usuarios/${idUsuario}/direcciones/${idDireccion}`);
  }
  listarMetodosPago(idUsuario: number): Observable<MetodoPagoGuardado[]> {
    return this.http.get<MetodoPagoGuardado[]>(`${this.baseUrl}/api/usuarios/${idUsuario}/metodos-pago`);
  }
  crearMetodoPago(idUsuario: number, solicitud: SolicitudMetodoPago): Observable<MetodoPagoGuardado> {
    return this.http.post<MetodoPagoGuardado>(`${this.baseUrl}/api/usuarios/${idUsuario}/metodos-pago`, solicitud);
  }
  actualizarMetodoPago(idUsuario: number, idMetodoPago: number, solicitud: SolicitudMetodoPago): Observable<MetodoPagoGuardado> {
    return this.http.put<MetodoPagoGuardado>(`${this.baseUrl}/api/usuarios/${idUsuario}/metodos-pago/${idMetodoPago}`, solicitud);
  }
  eliminarMetodoPago(idUsuario: number, idMetodoPago: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/usuarios/${idUsuario}/metodos-pago/${idMetodoPago}`);
  }
  crearPedido(idUsuario: number, solicitud: SolicitudCrearPedido): Observable<PedidoDetalle> {
    return this.http.post<PedidoDetalle>(`${this.baseUrl}/api/usuarios/${idUsuario}/pedidos`, solicitud);
  }
  listarPedidosUsuario(idUsuario: number): Observable<PedidoResumen[]> {
    return this.http.get<PedidoResumen[]>(`${this.baseUrl}/api/usuarios/${idUsuario}/pedidos`);
  }
  obtenerPedidoUsuario(idUsuario: number, idPedido: number): Observable<PedidoDetalle> {
    return this.http.get<PedidoDetalle>(`${this.baseUrl}/api/usuarios/${idUsuario}/pedidos/${idPedido}`);
  }
  listarPedidosAdministracion(): Observable<PedidoResumen[]> {
    return this.http.get<PedidoResumen[]>(`${this.baseUrl}/api/administracion/pedidos`);
  }
  obtenerPedidoAdministracion(idPedido: number): Observable<PedidoDetalle> {
    return this.http.get<PedidoDetalle>(`${this.baseUrl}/api/administracion/pedidos/${idPedido}`);
  }
  actualizarEstadoPedido(idPedido: number, solicitud: SolicitudEstadoPedido): Observable<PedidoDetalle> {
    return this.http.put<PedidoDetalle>(`${this.baseUrl}/api/administracion/pedidos/${idPedido}/estado`, solicitud);
  }
}
