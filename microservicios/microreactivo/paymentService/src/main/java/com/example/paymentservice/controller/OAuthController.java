package com.example.paymentservice.controller;

import com.example.paymentservice.service.OAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/api/payments/oauth")
public class OAuthController {

    private final OAuthService oAuthService;

    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    /**
     * GET /api/payments/oauth/connect/{motelId}
     * El dueño del motel llama esto → se le redirige a MercadoPago
     */
    @GetMapping("/connect/{motelId}")
    public Mono<Void> connect(
            @PathVariable Long motelId,
            ServerWebExchange exchange) {

        String url = oAuthService.getAuthorizationUrl(motelId);
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create(url));
        return exchange.getResponse().setComplete();
    }

    /**
     * GET /api/payments/oauth/callback?code=XXX&state=motelId
     * MercadoPago redirige aquí después de que el motel autoriza
     */
    @GetMapping("/callback")
    public Mono<String> callback(
            @RequestParam String code,
            @RequestParam Long state) { // state = motelId

        return oAuthService.handleOAuthCallback(code, state)
                .map(account -> "Cuenta MercadoPago vinculada exitosamente para motel: " + state);
    }

    /**
     * DELETE /api/payments/oauth/{motelId}
     * Desvincula la cuenta del motel
     */
    @DeleteMapping("/{motelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> disconnect(@PathVariable Long motelId) {
        return oAuthService.disconnect(motelId);
    }
}