package com.recetas.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Página de inicio de sesión propia de RecetON para el formulario que utiliza
 * Spring Security dentro del flujo Authorization Code + PKCE.
 * El procesamiento del POST /login sigue a cargo de Spring Security.
 */
@Controller
public class LoginOAuthController {

    private final String frontendUrl;

    public LoginOAuthController(
            @Value("${receton.frontend.url:http://receton.test:4200}") String frontendUrl) {
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String mostrarLogin(
            @RequestParam(name = "error", required = false) String error,
            @RequestAttribute("_csrf") CsrfToken csrfToken) {

        String avisoError = error == null ? "" : """
                <div class="alerta-error" role="alert">
                  <span class="alerta-icono" aria-hidden="true">!</span>
                  <span>El usuario o la contraseña no son correctos. Revisa tus datos e inténtalo de nuevo.</span>
                </div>
                """;

        String html = """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <meta name="color-scheme" content="light">
                  <meta name="theme-color" content="#103b2b">
                  <title>Iniciar sesión · RecetON</title>
                  <style>
                    :root {
                      color-scheme: light;
                      --bosque: #103b2b;
                      --bosque-profundo: #09271d;
                      --menta: #d9f2a6;
                      --turquesa: #52b89a;
                      --naranja: #f07845;
                      --tinta: #1b3529;
                      --suave: #6b7c72;
                      --borde: #dce7dd;
                    }
                    * { box-sizing: border-box; }
                    html, body { min-height: 100%; }
                    body {
                      margin: 0;
                      min-height: 100vh;
                      font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                      color: var(--tinta);
                      background:
                        radial-gradient(circle at 12% 10%, rgba(217,242,166,.20), transparent 28rem),
                        radial-gradient(circle at 92% 90%, rgba(82,184,154,.19), transparent 30rem),
                        linear-gradient(135deg, #08271d, #164c37 58%, #0c3325);
                      display: grid;
                      place-items: center;
                      padding: clamp(18px, 4vw, 48px);
                      overflow-x: hidden;
                    }
                    .decoracion { position: fixed; border: 1px solid rgba(255,255,255,.10); border-radius: 50%; pointer-events: none; }
                    .decoracion.uno { width: 520px; height: 520px; right: -220px; top: -210px; }
                    .decoracion.dos { width: 390px; height: 390px; left: -190px; bottom: -190px; }
                    .layout {
                      position: relative; z-index: 1; width: min(100%, 1000px); min-height: 570px;
                      display: grid; grid-template-columns: 1fr 440px; overflow: hidden;
                      border: 1px solid rgba(255,255,255,.20); border-radius: 30px;
                      background: rgba(255,255,255,.07); box-shadow: 0 32px 90px rgba(0,0,0,.30);
                      backdrop-filter: blur(14px);
                    }
                    .marca-panel { padding: clamp(30px, 5vw, 58px); color: #f8fff9; display: flex; flex-direction: column; justify-content: space-between; gap: 36px; }
                    .marca { display: block; width: min(100%, 270px); }
                    .marca-logo { display: block; width: 100%; height: auto; object-fit: contain; }
                    .hero { max-width: 420px; }
                    .etiqueta { color: var(--menta); font-weight: 800; text-transform: uppercase; letter-spacing: .16em; font-size: .72rem; }
                    h1 { margin: 15px 0 18px; max-width: 420px; font-family: Georgia, "Times New Roman", serif; font-weight: 600; font-size: clamp(2.3rem, 4vw, 3.65rem); line-height: 1.03; letter-spacing: -.045em; }
                    .hero p { max-width: 370px; margin: 0; color: rgba(245,255,248,.78); font-size: 1rem; line-height: 1.8; }
                    .puntos { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 28px; }
                    .punto { display: inline-flex; align-items: center; gap: 8px; padding: 9px 12px; border: 1px solid rgba(255,255,255,.17); border-radius: 999px; color: #f5fff6; font-size: .78rem; background: rgba(255,255,255,.05); }
                    .punto i { width: 7px; height: 7px; border-radius: 50%; background: var(--menta); display: inline-block; }
                    .nota-panel { color: rgba(245,255,248,.68); font-size: .76rem; line-height: 1.6; }
                    .form-panel { display: flex; align-items: center; justify-content: center; padding: clamp(22px, 4vw, 38px); background: #fbfdf9; }
                    .form-card { width: 100%; max-width: 350px; }
                    .icono-receton { width: 68px; height: 68px; margin-bottom: 24px; }
                    .icono-receton img { display: block; width: 100%; height: 100%; object-fit: contain; border-radius: 17px; }
                    .form-card h2 { margin: 0 0 8px; font-size: 1.8rem; letter-spacing: -.04em; color: #193a2b; }
                    .intro { margin: 0 0 27px; color: var(--suave); font-size: .91rem; line-height: 1.65; }
                    .campo { display: block; margin: 0 0 18px; }
                    .campo span { display: block; margin-bottom: 8px; font-size: .83rem; font-weight: 750; color: #294b39; }
                    .campo input { display: block; width: 100%; min-height: 49px; padding: 12px 14px; border: 1px solid #d5e1d7; border-radius: 13px; color: var(--tinta); background: #fff; font: inherit; font-size: .95rem; transition: border-color .18s, box-shadow .18s; }
                    .campo input::placeholder { color: #9ba99f; }
                    .campo input:focus { outline: none; border-color: var(--turquesa); box-shadow: 0 0 0 4px rgba(82,184,154,.15); }
                    .alerta-error { display: flex; align-items: flex-start; gap: 10px; padding: 12px; margin: 0 0 18px; border-radius: 12px; border: 1px solid #f2c6c6; background: #fff1f0; color: #8e2630; font-size: .82rem; line-height: 1.5; }
                    .alerta-icono { flex: 0 0 20px; width: 20px; height: 20px; display: grid; place-items: center; border-radius: 50%; background: #8e2630; color: #fff; font-size: .75rem; font-weight: 900; }
                    .boton { width: 100%; min-height: 51px; display: inline-flex; align-items: center; justify-content: center; gap: 10px; margin-top: 4px; padding: 13px 16px; border: 0; border-radius: 13px; background: var(--bosque); color: white; font: inherit; font-size: .94rem; font-weight: 800; cursor: pointer; box-shadow: 0 10px 20px rgba(16,59,43,.16); transition: transform .16s, background .16s, box-shadow .16s; }
                    .boton:hover { background: #1b6044; transform: translateY(-1px); box-shadow: 0 13px 24px rgba(16,59,43,.20); }
                    .boton:focus-visible { outline: 3px solid #8bd4ba; outline-offset: 3px; }
                    .boton svg { width: 18px; height: 18px; }
                    .boton path { fill: none; stroke: currentColor; stroke-width: 2; stroke-linecap: round; stroke-linejoin: round; }
                    .seguridad { display: flex; gap: 9px; align-items: flex-start; margin: 20px 0 0; color: #78877d; font-size: .73rem; line-height: 1.55; }
                    .seguridad svg { width: 17px; height: 17px; flex: 0 0 17px; color: #3a825d; margin-top: 1px; }
                    .seguridad path { fill: none; stroke: currentColor; stroke-width: 1.8; stroke-linecap: round; stroke-linejoin: round; }
                    .pie { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px; margin-top: 24px; padding-top: 18px; border-top: 1px solid var(--borde); color: #829087; font-size: .72rem; }
                    .pie a { color: #236948; font-weight: 750; text-decoration: none; }
                    .pie a:hover { text-decoration: underline; }
                    @media (max-width: 760px) {
                      body { padding: 12px; }
                      .layout { grid-template-columns: 1fr; width: min(100%, 500px); min-height: auto; border-radius: 24px; }
                      .marca-panel { padding: 24px 24px 18px; gap: 26px; }
                      .hero h1 { font-size: clamp(2rem, 9vw, 3rem); }
                      .hero p { font-size: .9rem; }
                      .puntos { display: none; }
                      .nota-panel { display: none; }
                      .form-panel { padding: 26px 24px 28px; }
                      .icono-receton { margin-bottom: 17px; }
                    }
                    @media (prefers-reduced-motion: reduce) { *, *::before, *::after { transition: none !important; } }
                  </style>
                </head>
                <body>
                  <div class="decoracion uno" aria-hidden="true"></div>
                  <div class="decoracion dos" aria-hidden="true"></div>
                  <main class="layout">
                    <section class="marca-panel" aria-label="Bienvenida a RecetON">
                      <div class="marca">
                        <img class="marca-logo" src="/assets/brand/receton-logo-transparente.png" alt="RecetON">
                      </div>
                      <div class="hero">
                        <span class="etiqueta">Tu cocina, tu comunidad</span>
                        <h1>Las buenas recetas empiezan aquí.</h1>
                        <p>Entra a tu espacio para descubrir ideas, guardar tus favoritas y disfrutar cada paso de cocinar.</p>
                        <div class="puntos" aria-label="Beneficios de RecetON">
                          <span class="punto"><i></i> Recetas para inspirarte</span>
                          <span class="punto"><i></i> Tus favoritos contigo</span>
                          <span class="punto"><i></i> Tu comunidad</span>
                        </div>
                      </div>
                      <div class="nota-panel">RecetON · Cocina con confianza, comparte con gusto.</div>
                    </section>
                    <section class="form-panel">
                      <div class="form-card">
                        <div class="icono-receton" aria-hidden="true">
                          <img src="/assets/icons/icon-192x192.png" alt="">
                        </div>
                        <h2>Bienvenido de nuevo</h2>
                        <p class="intro">Inicia sesión con tu cuenta de RecetON para continuar.</p>
                        __ERROR__
                        <form method="post" action="/login" autocomplete="on">
                          <input type="hidden" name="__CSRF_PARAM__" value="__CSRF_TOKEN__">
                          <label class="campo" for="username">
                            <span>Nombre de usuario</span>
                            <input id="username" name="username" type="text" placeholder="Tu nombre de usuario" autocomplete="username" autocapitalize="none" spellcheck="false" required autofocus>
                          </label>
                          <label class="campo" for="password">
                            <span>Contraseña</span>
                            <input id="password" name="password" type="password" placeholder="Escribe tu contraseña" autocomplete="current-password" required>
                          </label>
                          <button class="boton" type="submit">
                            Entrar a RecetON
                            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h14M13 6l6 6-6 6"/></svg>
                          </button>
                        </form>
                        <p class="seguridad">
                          <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3 19 6v5c0 4.8-2.9 8-7 10-4.1-2-7-5.2-7-10V6l7-3Z"/><path d="m9 12 2 2 4-4"/></svg>
                          Tu contraseña se valida en el servidor de RecetON. El acceso continúa mediante OAuth 2.0 y PKCE.
                        </p>
                        <footer class="pie"><span>© RecetON</span><a href="__PRIVACY_URL__">Privacidad y derechos ARCO</a></footer>
                      </div>
                    </section>
                  </main>
                </body>
                </html>
                """;

        String urlPrivacidad = frontendUrl + "/privacidad";
        return html
                .replace("__CSRF_PARAM__", escapeHtml(csrfToken.getParameterName()))
                .replace("__CSRF_TOKEN__", escapeHtml(csrfToken.getToken()))
                .replace("__ERROR__", avisoError)
                .replace("__PRIVACY_URL__", escapeHtml(urlPrivacidad));
    }

    private static String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
