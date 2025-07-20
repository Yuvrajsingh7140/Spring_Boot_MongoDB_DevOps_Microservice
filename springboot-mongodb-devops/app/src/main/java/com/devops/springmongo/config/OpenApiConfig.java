package com.devops.springmongo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot MongoDB Microservice API")
                        .version("1.0.0")
                        .description("DevOps automation project with Spring Boot and MongoDB")
                        .contact(new Contact()
                                .name("DevOps Team")
                                .email("devops@company.com")
                                .url("https://github.com/company/springboot-mongodb-devops"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}