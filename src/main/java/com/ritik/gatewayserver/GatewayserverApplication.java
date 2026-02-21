package com.ritik.gatewayserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.time.LocalDateTime;

@SpringBootApplication
public class GatewayserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayserverApplication.class, args);
    }

    @Bean
    public RouteLocator bankRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(predicateSpec ->
                        predicateSpec.path("/bank/accounts/**")
                                .filters(filter -> filter
                                        .rewritePath("/bank/accounts/(?<segment>.*)", "/${segment}")
                                        .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                        .circuitBreaker(config -> config.setName("accountsCircuitBraker")
                                                .setFallbackUri("forward:/contactSupport")))
                                .uri("lb://ACCOUNTS")

                )
                .route(predicateSpec ->
                        predicateSpec.path("/bank/loans/**")
                                .filters(filter -> filter
                                        .rewritePath("/bank/loans/(?<segment>.*)", "/${segment}")
                                        .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                        .retry(retryConfig -> retryConfig.setRetries(3)
                                                .setMethods(HttpMethod.GET)
                                                .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true)))
                                .uri("lb://LOANS")

                )
                .route(predicateSpec ->
                        predicateSpec.path("/bank/cards/**")
                                .filters(filter -> filter
                                        .rewritePath("/bank/cards/(?<segment>.*)", "/${segment}")
                                        .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
                                .uri("lb://CARDS")

                )
                .build();
    }
}
