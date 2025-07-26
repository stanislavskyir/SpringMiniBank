package dev.stanislavskyi.gateway.routes;


import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class Routes {
//    @Bean
//    public RouteLocator userServiceRoute(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("user_service", r -> r
//                        .path("/api/v1/users/**")
//                        .uri("http://localhost:8080"))
//                .build();
//    }

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return GatewayRouterFunctions.route("user_service")
                .route(RequestPredicates.path("/api/v1/users/register"), HandlerFunctions.http("http://localhost:8080"))
                .build();
    }
}
