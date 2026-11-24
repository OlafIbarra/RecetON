export interface DireccionEnvio {
  idDireccion: number;
  idUsuario: number;
  calle: string;
  numero: string;
  colonia: string;
  ciudad: string;
  estado: string;
  codigoPostal: string;
  esPrincipal: boolean;
}

export interface SolicitudDireccion {
  calle: string;
  numero: string;
  colonia: string;
  ciudad: string;
  estado: string;
  codigoPostal: string;
  esPrincipal: boolean;
}

export interface MetodoPagoGuardado {
  idMetodoPago: number;
  idUsuario: number;
  tipo: string;
  ultimos4Digitos: string | null;
  esPrincipal: boolean;
}

export interface SolicitudMetodoPago {
  tipo: string;
  ultimos4Digitos: string | null;
  esPrincipal: boolean;
}

export interface SolicitudCrearPedido {
  idDireccion: number;
  idMetodoPago: number;
}

export interface PedidoResumen {
  idPedido: number;
  idUsuario: number;
  usuario: string;
  fechaPedido: string;
  estado: string;
  total: number;
  cantidadArticulos: number;
}

export interface PedidoItem {
  idProducto: number;
  nombreProducto: string | null;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface PedidoDetalle {
  idPedido: number;
  idUsuario: number;
  usuario: string;
  fechaPedido: string;
  estado: string;
  total: number;
  direccionEnvio: DireccionEnvio;
  metodoPago: MetodoPagoGuardado;
  items: PedidoItem[];
}

export interface SolicitudEstadoPedido {
  estado: string;
}
