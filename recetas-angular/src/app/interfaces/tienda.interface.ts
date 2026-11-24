export interface Producto {
  idProducto: number;
  idReceta: number | null;
  nombreProducto: string;
  descripcion: string | null;
  precio: number;
  stock: number;
  nombreReceta: string | null;
  imagen: string | null;
}

export interface SolicitudProducto {
  idReceta: number | null;
  nombreProducto: string;
  descripcion: string;
  precio: number;
  stock: number;
}

export interface ItemCarrito {
  idProducto: number;
  nombreProducto: string;
  descripcion: string | null;
  precio: number;
  stock: number;
  cantidad: number;
  subtotal: number;
  idReceta: number | null;
  nombreReceta: string | null;
  imagen: string | null;
}

export interface Carrito {
  idCarrito: number | null;
  idUsuario: number;
  items: ItemCarrito[];
  subtotal: number;
}

export interface SolicitudItemCarrito {
  idProducto: number;
  cantidad: number;
}
