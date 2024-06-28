package com.example.camel.routes.wiretap;

import com.example.camel.dto.TransactionDto;
import com.example.camel.routes.rabbitmq.RabbitmqConfiguration;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.support.DefaultMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Date;

import static org.apache.camel.LoggingLevel.INFO;

@Component
@ConditionalOnProperty(name = "com.example.camel.routes.wiretap.enabled", havingValue = "true")
public class Wiretap extends RouteBuilder {

    public static final String SENDER = "sender";
    public static final String RECEIVER = "receiver";
    public static final String AUDIT_TRANSACTION_ROUTE = "direct:audit-transaction";
    public static final String AUDIT = "audit-transactions";

/* TEST MESSAGE TO SEND FROM SENDER QUEUE
{"transactionId":1,
"senderAccountId":"100",
"receiverAccountId":"200",
"amount":"5000",
"currency":"RUR"}
 */

    @Override
    public void configure() throws Exception {
        fromF(RabbitmqConfiguration.RABBIT_URI, SENDER, SENDER)
                .unmarshal()
                .json(JsonLibrary.Jackson, TransactionDto.class)
                .wireTap(AUDIT_TRANSACTION_ROUTE)
                .process(this::enrichTransactionDto)
                .marshal()
                .json(JsonLibrary.Jackson, TransactionDto.class)
                .toF(RabbitmqConfiguration.RABBIT_URI, RECEIVER, RECEIVER)
                .log(INFO, "Money transferred: ${body}");

        from(AUDIT_TRANSACTION_ROUTE)
                .process(this::enrichTransactionDto)
                .marshal()
                .json(JsonLibrary.Jackson, TransactionDto.class)
                .toF(RabbitmqConfiguration.RABBIT_URI, AUDIT, AUDIT);
    }

    private void enrichTransactionDto(Exchange exchange) {
        TransactionDto dto = exchange.getMessage().getBody(TransactionDto.class);
        dto.setTransactionDate(new Date().toString());

        Message message = new DefaultMessage(exchange);
        message.setBody(dto);
        exchange.setMessage(message);
    }
}