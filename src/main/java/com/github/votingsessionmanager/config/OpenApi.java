package com.github.votingsessionmanager.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Voting Session Manager API",
                version = "v1",
                description = "This is a REST API to manage voting sessions.",
                contact = @Contact(
                        name = "Yan Pereira",
                        email = "yansantos0220@gmail.com",
                        url = "https://github.com/Yanzord"
                )
        ),
        servers = @Server(
                url = "http://localhost:8080/",
                description = "Demo"
        )
)
public class OpenApi {
}
