package com.se114p12.backend.configs;

import com.se114p12.backend.constants.AppConstant;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
  private SecurityScheme createAPIKeyScheme() {
    return new SecurityScheme().type(SecurityScheme.Type.HTTP).bearerFormat("JWT").scheme("bearer");
  }

  private Info createInfo() {
    return new Info()
        .title("SE114.P12")
        .version("1.0")
        .description("API cho dự án nhập môn ứng dụng di dộng");
  }

  @Bean
  public OpenAPI myOpenAPI() {
    Server remoteServer = new Server();
    remoteServer.setUrl(AppConstant.DOMAIN);
    Server localServer = new Server();
    localServer.setUrl("http://localhost:8080");

    return new OpenAPI()
        .info(createInfo())
        .addServersItem(localServer)
        .addServersItem(remoteServer)
        .addSecurityItem(new SecurityRequirement().addList("BearerAuthentication"))
        .components(
            new Components().addSecuritySchemes("BearerAuthentication", createAPIKeyScheme()));
  }
}
