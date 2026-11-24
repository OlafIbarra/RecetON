import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';

@Component({
  selector: 'app-oauth-callback',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <main class="oauth-callback">
      <section class="oauth-callback__tarjeta" aria-live="polite">
        <h1>{{ mensajeError ? 'No se pudo iniciar sesión' : 'Validando acceso seguro…' }}</h1>
        <p *ngIf="!mensajeError">RecetON está validando la respuesta OAuth 2.0 y preparando tu sesión.</p>
        <p class="oauth-callback__error" *ngIf="mensajeError">{{ mensajeError }}</p>
        <a *ngIf="mensajeError" routerLink="/inicio-sesion">Volver al inicio de sesión</a>
      </section>
    </main>
  `,
  styles: [`
    :host { display:block; min-height:100vh; }
    .oauth-callback { min-height:100vh; display:grid; place-items:center; padding:24px; background:#f3f7f1; color:#173b2d; }
    .oauth-callback__tarjeta { width:min(100%,560px); box-sizing:border-box; background:#fff; border-radius:22px; padding:32px; box-shadow:0 18px 54px rgba(16,58,42,.12); }
    h1 { margin:0 0 14px; font-size:clamp(1.35rem,3vw,2rem); }
    p { line-height:1.65; }
    .oauth-callback__error { color:#9a2433; }
    a { color:#176747; font-weight:700; }
  `],
})
export class OAuthCallbackComponent implements OnInit {
  private readonly ruta = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly autenticacion = inject(AutenticacionServicio);

  mensajeError: string | null = null;

  async ngOnInit(): Promise<void> {
    const parametros = this.ruta.snapshot.queryParamMap;
    const errorOAuth = parametros.get('error');
    if (errorOAuth) {
      this.mensajeError = errorOAuth === 'access_denied'
        ? 'Se canceló la autorización. Puedes intentarlo nuevamente.'
        : `El servidor OAuth 2.0 devolvió un error: ${errorOAuth}.`;
      this.autenticacion.limpiarSesionLocal();
      return;
    }

    const codigo = parametros.get('code');
    const state = parametros.get('state');
    if (!codigo || !state) {
      this.mensajeError = 'La respuesta de autorización está incompleta. Vuelve a iniciar sesión.';
      this.autenticacion.limpiarSesionLocal();
      return;
    }

    try {
      await this.autenticacion.completarInicioSesion(codigo, state);
      await this.router.navigate(['/inicio']);
    } catch (error: unknown) {
      this.mensajeError = error instanceof Error
        ? error.message
        : 'No se pudo finalizar la autenticación. Vuelve a intentarlo.';
      this.autenticacion.limpiarSesionLocal();
    }
  }
}
