package com.example.camel.intro.components;

import com.example.camel.CamelApplication;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Date;

@SpringBootTest(classes = CamelApplication.class, properties = {"com.example.camel.intro.file.enabled=true"})
@CamelSpringBootTest
@MockEndpoints()
@Disabled("Fix to run in CI build. " + "Issue 1: Temp file not created")
public class FileHandlerRouteTest {

    @Autowired
    private ProducerTemplate template;

    @Test
    @DirtiesContext
    public void testCamelFileRoute() {
        System.out.println("Sending request to append to existing file...");
        template.sendBody("direct:appendToFile", "Test time " + new Date() + "\n");
        System.out.println("Sent request to append to existing file...");
    }
}