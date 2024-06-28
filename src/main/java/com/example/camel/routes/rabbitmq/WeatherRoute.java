package com.example.camel.routes.rabbitmq;

import com.example.camel.dto.WeatherDto;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.support.DefaultMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.example.camel.routes.rabbitmq.RabbitmqConfiguration.*;
import static org.apache.camel.LoggingLevel.INFO;

@Component
@ConditionalOnProperty(name = "com.example.camel.routes.rabbitmq.enabled", havingValue = "true")
public class WeatherRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        /**
         *  queue=weather&routingKey=weather - exchange name & routing key
         *  autoDelete - do not delete queue on app shutdown
         *  configuration for connection is stored in ConnectionFactory and auto-injected
         *  to specify more than 1 connection use connectionFactory=connectionFactoryName2
         *
         **/

/* Create this message in weather queue
{"city": "Moscow",
"temp": "21",
"unit": "C"}
*/
        fromF(RABBIT_URI, QUEUE_WEATHER, QUEUE_WEATHER)
                .routeId("WeatherRoute")
                .log(INFO, "Headers: ${headers}")
                .log(INFO, "Original message from RabbitMQ: ${body}")
                .unmarshal().json(JsonLibrary.Jackson, WeatherDto.class)
                .process(this::enrichWeatherDto)
                .log(INFO, "Enriched message from RabbitMQ: ${body}")
                .marshal().json(JsonLibrary.Jackson, WeatherDto.class)
                .toF(RABBIT_URI, QUEUE_WEATHER_EVENT, ROUTING_KEY_EVENT)
                .to("file://./?fileName=weather-event.txt&fileExist=Append");
    }

    private void enrichWeatherDto(Exchange exchange) {
        log.error("Enriching...");
        WeatherDto weatherDto = exchange.getMessage().getBody(WeatherDto.class);
        weatherDto.setReceivedTime(new Date().toString());

        Message message = new DefaultMessage(exchange);
        message.setBody(weatherDto);
        exchange.setMessage(message);
    }
}
