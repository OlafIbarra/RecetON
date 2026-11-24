import { Categoria } from '../interfaces/categoria.interface';
import { Receta } from '../interfaces/receta.interface';

/**
 * Datos de ejemplo usados SOLO cuando no se puede contactar al backend
 * de Spring Boot (por ejemplo, mientras desarrollas nada más el frontend).
 * En cuanto el backend real responda, estos datos dejan de usarse solos,
 * sin tocar ningún componente.
 */
export const CATEGORIAS_RESPALDO: Categoria[] = [
  { id: 1, nombre: 'Snacks', imagen: 'https://picsum.photos/seed/snacks/100/100' },
  { id: 2, nombre: 'Ensaladas', imagen: 'https://picsum.photos/seed/ensaladas/100/100' },
  { id: 3, nombre: 'Postres', imagen: 'https://picsum.photos/seed/postres/100/100' },
  { id: 4, nombre: 'Bebidas', imagen: 'https://picsum.photos/seed/bebidas/100/100' },
  { id: 5, nombre: 'Desayunos', imagen: 'https://picsum.photos/seed/desayunos/100/100' },
  { id: 6, nombre: 'Sopas', imagen: 'https://picsum.photos/seed/sopas/100/100' },
];

export const RECETAS_RESPALDO: Receta[] = [
  { id: 1, nombre: 'Tazón de Quinoa y Aguacate', imagen: 'https://picsum.photos/seed/quinoa1/300/200', precio: '$12.99', calificacion: 4.8, tiempo: '20 min', etiqueta: 'Nuevo', colorEtiqueta: 'squash' },
  { id: 2, nombre: 'Ensalada César con Pollo', imagen: 'https://picsum.photos/seed/cesar1/300/200', precio: '$10.99', calificacion: 4.6, tiempo: '15 min' },
  { id: 3, nombre: 'Tarta de Arándanos Silvestres', imagen: 'https://picsum.photos/seed/tarta1/300/200', precio: '$8.50', calificacion: 4.9, tiempo: '45 min', etiqueta: 'Popular', colorEtiqueta: 'sage' },
  { id: 4, nombre: 'Waffles Integrales de Fresa', imagen: 'https://picsum.photos/seed/waffles1/300/200', precio: '$9.99', calificacion: 4.7, tiempo: '25 min' },
  { id: 5, nombre: 'Bowl de Salmón y Sésamo', imagen: 'https://picsum.photos/seed/bowlsalmon1/300/200', precio: '$14.50', calificacion: 4.8, tiempo: '30 min' },
  { id: 6, nombre: 'Zumo Orgánico Prensado', imagen: 'https://picsum.photos/seed/zumo1/300/200', precio: '$6.00', calificacion: 4.5, tiempo: '5 min' },
];
