package com.example.camel.intro.components;

import static org.apache.camel.LoggingLevel.ERROR;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name = "com.example.camel.intro.enabled", havingValue = "true")
public class HelloRoute extends RouteBuilder {

    @Override
    public void configure() {

        from("direct:greeting")
                .id("greeting")
                .log(ERROR, "It's ${body}")
                .choice()
                    .when()
                        .simple("${body} contains '^-^'")
                        .log(ERROR, "Cats rules the world!")
                    .otherwise()
                        .choice()
                            .when()
                                .simple("${body} contains 'moto'")
                                .log(ERROR, "hello moto!")
                .end()
                .to("direct:finishGreeting");

        from("direct:finishGreeting").log(ERROR, "Bye ${body}");
    }
}