package com.recetas.backend.config;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Key;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.cert.Certificate;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.recetas.backend.dto.UsuarioCredencial;
import com.recetas.backend.repository.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
/**
 * Servidor OAuth 2.0 para RecetON, Authorization Code + PKCE y tokens de acceso JWT.
 * Los clientes son públicos (SPA), por lo que no se incorpora un client_secret en Angular.
 */
@Configuration
public class SeguridadOAuth2Config {

    /**
     * Carga la llave RSA desde un almacén PKCS#12 externo al código fuente.
     * La misma pareja se reutiliza entre reinicios para conservar la validación
     * de los JWT previamente emitidos.
     */
    @Bean
    KeyPair recetonParLlavesRsa(
            @Value("${receton.jwt.keystore-path:secrets/receton-jwt.p12}") String rutaKeystore,
            @Value("${receton.jwt.keystore-password:}") String passwordKeystore,
            @Value("${receton.jwt.key-alias:receton-jwt}") String alias)
            throws GeneralSecurityException, IOException {
        if (passwordKeystore == null || passwordKeystore.isBlank()) {
            throw new IllegalStateException(
                    "Falta RECETON_JWT_KEYSTORE_PASSWORD. Inicia el backend con scripts/iniciar-backend-seguro.ps1.");
        }
        if (rutaKeystore == null || rutaKeystore.isBlank()) {
            throw new IllegalStateException("No se configuró la ruta del almacén PKCS#12 de RecetON.");
        }
        if (alias == null || alias.isBlank()) {
            throw new IllegalStateException("No se configuró el alias de la llave RSA de RecetON.");
        }

        Path archivoKeystore = Path.of(rutaKeystore).toAbsolutePath().normalize();
        if (!Files.isRegularFile(archivoKeystore)) {
            throw new IllegalStateException("No se encontró el almacén de llaves JWT en: "
                    + archivoKeystore + ". Ejecuta primero scripts/crear-keystore-jwt.ps1.");
        }

        char[] password = passwordKeystore.toCharArray();
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            try (InputStream entrada = Files.newInputStream(archivoKeystore)) {
                keystore.load(entrada, password);
            }

            Key llave = keystore.getKey(alias, password);
            Certificate certificado = keystore.getCertificate(alias);
            if (!(llave instanceof RSAPrivateKey llavePrivada)) {
                throw new GeneralSecurityException(
                        "El alias '" + alias + "' no contiene una llave privada RSA.");
            }
            if (certificado == null || !(certificado.getPublicKey() instanceof RSAPublicKey llavePublica)) {
                throw new GeneralSecurityException(
                        "El alias '" + alias + "' no contiene un certificado con llave pública RSA.");
            }
            if (!llavePrivada.getModulus().equals(llavePublica.getModulus())) {
                throw new GeneralSecurityException(
                        "La llave privada y el certificado del alias '" + alias + "' no forman una pareja RSA.");
            }
            return new KeyPair(llavePublica, llavePrivada);
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    @Bean
    JWKSource<SecurityContext> recetonJwkSource(KeyPair recetonParLlavesRsa)
            throws GeneralSecurityException {
        RSAPublicKey llavePublica = (RSAPublicKey) recetonParLlavesRsa.getPublic();
        RSAKey rsaKey = new RSAKey.Builder(llavePublica)
                .privateKey((RSAPrivateKey) recetonParLlavesRsa.getPrivate())
                // Identificador estable para que el JWKS no cambie en cada reinicio.
                .keyID(UUID.nameUUIDFromBytes(llavePublica.getEncoded()).toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (selector, contexto) -> selector.select(jwkSet);
    }

    @Bean
    JwtDecoder jwtDecoder(KeyPair recetonParLlavesRsa,
            @Value("${receton.oauth2.issuer:http://api.receton.test:8080}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(
                (RSAPublicKey) recetonParLlavesRsa.getPublic()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    @Bean
    RegisteredClientRepository registeredClientRepository(
            @Value("${receton.oauth2.redirect-uris:http://receton.test:4200/oauth-callback,http://localhost:4200/oauth-callback,http://127.0.0.1:4200/oauth-callback,https://oauth.pstmn.io/v1/callback}")
            String redirectUrisConfigurados) {
        TokenSettings tokens = TokenSettings.builder()
                // JWT de acceso corto. Este despliegue de demostración no emite refresh tokens:
                // al expirar, el usuario vuelve a autorizarse y no se prolonga una sesión antigua.
                .accessTokenTimeToLive(Duration.ofMinutes(20))
                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                .build();

        // Lee la lista declarada en application.properties / RECETON_OAUTH2_REDIRECT_URIS.
        // Esto permite registrar el callback HTTPS de ngrok sin dejarlo fijo en el código.
        Set<String> redirectUris = new LinkedHashSet<>();
        if (redirectUrisConfigurados != null) {
            Arrays.stream(redirectUrisConfigurados.split(","))
                    .map(String::trim)
                    .filter(uri -> !uri.isEmpty())
                    .forEach(redirectUris::add);
        }
        if (redirectUris.isEmpty()) {
            throw new IllegalStateException(
                    "No hay URI de retorno OAuth configuradas. Revisa RECETON_OAUTH2_REDIRECT_URIS.");
        }

        RegisteredClient.Builder clienteAngular = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("receton-angular")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("api.read")
                .scope("api.write")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(true)
                        .build())
                .tokenSettings(tokens);

        redirectUris.forEach(clienteAngular::redirectUri);
        return new InMemoryRegisteredClientRepository(clienteAngular.build());
    }

    @Bean
    OAuth2AuthorizationService authorizationService(RegisteredClientRepository registeredClients) {
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    OAuth2AuthorizationConsentService authorizationConsentService(RegisteredClientRepository registeredClients) {
        return new InMemoryOAuth2AuthorizationConsentService();
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings(
            @Value("${receton.oauth2.issuer:http://api.receton.test:8080}") String issuer) {
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }

    @Bean
    UserDetailsService recetonUserDetailsService(UsuarioRepositorio usuarios) {
        return nombreUsuario -> {
            UsuarioCredencial credencial = usuarios.buscarCredenciales(nombreUsuario)
                    .filter(usuario -> Boolean.TRUE.equals(usuario.activo()))
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario o contraseña incorrectos."));

            List<String> roles = usuarios.listarRoles(credencial.id());
            if (roles.isEmpty()) roles = List.of("Usuario");
            String[] autoridades = roles.stream()
                    .filter(rol -> rol != null && !rol.isBlank())
                    .map(SeguridadOAuth2Config::convertirAutoridad)
                    .toArray(String[]::new);
            if (autoridades.length == 0) autoridades = new String[] { "ROLE_USUARIO" };

            return User.withUsername(credencial.nombreUsuario())
                    .password(credencial.contrasenaHash())
                    .authorities(autoridades)
                    .disabled(!Boolean.TRUE.equals(credencial.activo()))
                    .build();
        };
    }

    @Bean
    AuthenticationProvider recetonAuthenticationProvider(UserDetailsService recetonUserDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(recetonUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(UsuarioRepositorio usuarios) {
        return contexto -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(contexto.getTokenType())) return;

            UsuarioCredencial usuario = usuarios.buscarCredenciales(contexto.getPrincipal().getName())
                    .filter(credencial -> Boolean.TRUE.equals(credencial.activo()))
                    .orElse(null);
            if (usuario == null) return;

            List<String> roles = usuarios.listarRoles(usuario.id());
            if (roles.isEmpty()) roles = List.of("Usuario");
            contexto.getClaims()
                    .subject(usuario.nombreUsuario())
                    .claim("id_usuario", usuario.id())
                    .claim("nombre_usuario", usuario.nombreUsuario())
                    .claim("nombre_completo", usuario.nombreCompleto())
                    .claim("roles", roles);
        };
    }

    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
            org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .headers(this::configurarCabecerasSeguras)
                .with(authorizationServerConfigurer, Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain applicationSecurityFilterChain(HttpSecurity http,
            org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource,
            AuthenticationFailureHandler recetonLoginFailureHandler,
            AuthenticationSuccessHandler recetonLoginSuccessHandler) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
                .headers(this::configurarCabecerasSeguras)
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        PathPatternRequestMatcher.withDefaults().matcher("/api/**"),
                        PathPatternRequestMatcher.withDefaults().matcher("/oauth2/token"),
                        PathPatternRequestMatcher.withDefaults().matcher("/oauth2/revoke"),
                        PathPatternRequestMatcher.withDefaults().matcher("/oauth2/introspect")))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/saludo", "/adios", "/api/autenticacion/iniciar-sesion", "/error").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/**").hasAuthority("SCOPE_api.read")
                        .requestMatchers(org.springframework.http.HttpMethod.HEAD, "/api/**").hasAuthority("SCOPE_api.read")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/**").hasAuthority("SCOPE_api.write")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/**").hasAuthority("SCOPE_api.write")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/**").hasAuthority("SCOPE_api.write")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/**").hasAuthority("SCOPE_api.write")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        PathPatternRequestMatcher.withDefaults().matcher("/api/**")))
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .failureHandler(recetonLoginFailureHandler)
                        .successHandler(recetonLoginSuccessHandler)
                        .permitAll())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        return http.build();
    }

    private void configurarCabecerasSeguras(
            org.springframework.security.config.annotation.web.configurers.HeadersConfigurer<HttpSecurity> headers) {
        headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(referrer -> referrer.policy(
                        ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31_536_000))
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'self'; base-uri 'self'; form-action 'self'; "
                        + "frame-ancestors 'none'; object-src 'none'; script-src 'self'; "
                        + "style-src 'self' 'unsafe-inline'; img-src 'self' data:; "
                        + "font-src 'self' data:; connect-src 'self'"))
                .addHeaderWriter(new StaticHeadersWriter(
                        "Permissions-Policy", "camera=(), microphone=(), geolocation=()"));
    }

    private static String convertirAutoridad(String rol) {
        String limpio = rol.trim().toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('Á', 'A').replace('É', 'E').replace('Í', 'I')
                .replace('Ó', 'O').replace('Ú', 'U');
        return limpio.startsWith("ROLE_") ? limpio : "ROLE_" + limpio;
    }
}
