package com.assetiq.auth_service

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import spock.lang.Shared

trait KratosFixture {

    @Shared
    static DockerComposeContainer environment =
            new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
                    .withExposedService("kratos_1", 4434)
                    .withOptions("--compatibility")
                    .waitingFor("kratos_1", Wait.forListeningPorts(4434))

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        environment.start()
        registry.add("kratos.url", () -> "http://" + environment.getServiceHost("kratos_1", 4434) + ":" + environment.getServicePort("kratos_1", 4434))
    }


}