package com.example.camel.intro.components;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.camel.LoggingLevel.ERROR;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.support.DefaultMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * .to("seda:complexProcess");
 *
 * <p>from("seda:complexProcess?multipleConsumers=true") .to("direct:complexProcess");
 */
@Component
@ConditionalOnProperty(name = "com.example.camel.intro.seda.enabled", havingValue = "true")
public class SedaRoute extends RouteBuilder {

    static AtomicInteger counterProduced = new AtomicInteger();
    static AtomicInteger counterConsumed = new AtomicInteger();

    @Override
    public void configure() {
        AtomicInteger counter = counterProduced;
        from("timer:ping?period=100")
                .routeId("Timer")
                .process(
                        exchange -> {
                            System.out.println("New message #" + counter.incrementAndGet());
                            Message message = new DefaultMessage(exchange);
                            message.setBody(new Date());
                            exchange.setMessage(message);
                        })
                .to("seda:weightLifter?multipleConsumers=true");

        from("seda:weightLifter?multipleConsumers=true")
                .routeId("Seda-WeightLifter")
                .to("direct:complexProcess");

        from("direct:complexProcess")
                .routeId("Direct-ComplexProcess")
                                .log(ERROR, "${body}")
                .process(
                        exchange -> {
                            if (new Random().nextInt(20) < 5) {
                                throw new Exception("Fake exception");
                            }
                        })
                .process(
                        exchange -> {
                            SECONDS.sleep(2);
                            counterConsumed.incrementAndGet();
                            System.out.println("Messages processed: " + counterConsumed.get());
                        })
                .end();
    }

    public static void main(String[] args) {
        int total = 200;

        System.out.println("> " + total % 100);
    }
}