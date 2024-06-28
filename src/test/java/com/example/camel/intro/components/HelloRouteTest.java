package com.example.camel.intro.components;

import com.example.camel.CamelApplication;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = CamelApplication.class, properties = {"com.example.camel.intro.enabled=true"})
class HelloRouteTest {
    @Autowired
    private ProducerTemplate template;

    @Test
    void testMocksAreValid() {
        System.out.println("Sending 1");
        template.sendBody("direct:greeting", "^-^");
        System.out.println("Sending 2");
        template.sendBody("direct:greeting", "moto!");
    }
}