package com.example.camel.routes.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Properties;

import static com.example.camel.routes.wiretap.Wiretap.AUDIT;
import static com.example.camel.routes.wiretap.Wiretap.RECEIVER;

@Configuration
@ConditionalOnExpression("${com.example.camel.routes.rabbitmq.enabled:true} " + "|| ${com.example.camel.routes.rabbitmq.rabbitmq-throttler.enabled:true} ")
@Slf4j
public class RabbitmqConfiguration {

    /**
     * https://camel.apache.org/components/4.4.x/spring-rabbitmq-component.html
     */
    public static final String QUEUE_WEATHER = "weather";
    public static final String ROUTING_KEY = "weather";
    public static final String QUEUE_WEATHER_EVENT = "weather-event";
    public static final String ROUTING_KEY_EVENT = "weather-event";
    public static String EXCHANGE_WEATHER_DATA = "weather.data";
    public static final String RABBIT_URI = 
            "spring-rabbitmq:" +
                    EXCHANGE_WEATHER_DATA +
                    "?" +
                    "queues=%s&" +
                    "routingKey=%s&" + 
                    "arg.queue.autoDelete=false&" + 
                    "autoDeclare=true&" +
                    "concurrentConsumers=1&" +
                    "arg.queue.durable=true&" + 
                    "connectionFactory=#rabbitConnectionFactory&" +
                    "arg.queue.usePublisherConnection=true";

    public static String RMQ_HOST = "rmq.host";
    public static String RMQ_PORT = "rmq.port";

    /**
     * Create queue for weather-event to receive from toF() producer after enrichment
     * spring-rabbitmq in Camel 4.4. will autoDeclare queue only for consumer fromF()
     * https://camel.apache.org/components/4.4.x/spring-rabbitmq-component.html#_endpoint_query_option_autoDeclare
     * https://stackoverflow.com/questions/16370911/how-to-get-spring-rabbitmq-to-create-a-new-queue/33878724#33878724
     */


    @Bean
    DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_WEATHER_DATA);
    }
    
    @Bean
    public Queue weatherEventQueue() {
        return new Queue(QUEUE_WEATHER_EVENT, true, false, false);
    }
    
    @Bean
    public Queue receiverEventQueue() {
        return new Queue(RECEIVER, true, false, false);
    }
    
    @Bean
    public Queue auditTransactionsEventQueue() {
        return new Queue(AUDIT, true, false, false);
    }

    @Bean
    public Binding weatherEventBinding(Exchange exchange) {
        return BindingBuilder.bind(weatherEventQueue()).to((DirectExchange) exchange).with(ROUTING_KEY_EVENT);
    }
    
    @Bean
    public Binding receiverBinding(Exchange exchange) {
        return BindingBuilder.bind(receiverEventQueue()).to((DirectExchange) exchange).with(RECEIVER);
    }
    
    @Bean
    public Binding auditTransactionsBinding(Exchange exchange) {
        return BindingBuilder.bind(auditTransactionsEventQueue()).to((DirectExchange) exchange).with(AUDIT);
    }
    
    
    
    /**
     * camel-spring-rabbitmq lib will either create all channels under one connection or one channel per connection 
     * using Scope=PROTOTYPE will create new connection per route
     *
     * bean name rabbitConnectionFactory() is auto-injected. Other name must be declared at endpoint as connectionFactory param
     */
    
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ConnectionFactory rabbitConnectionFactory() {
        log.error("Setting Factory");
        Properties properties = System.getProperties();
        String host = properties.getProperty(RMQ_HOST, "localhost");
        String port = properties.getProperty(RMQ_PORT, "5672");
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setAddresses(host + ":" + port);
        factory.setUsername("admin");
        factory.setPassword("admin");
        return factory;
    }
}
