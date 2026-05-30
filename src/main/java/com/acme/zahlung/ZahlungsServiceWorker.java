package com.acme.zahlung;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.util.concurrent.CountDownLatch;

/**
 * RabbitMQ-Consumer fuer Zahlungsauftraege.
 */
public final class ZahlungsServiceWorker {
    private static final VerarbeiteteZahlungen VERARBEITETE_ZAHLUNGEN = new VerarbeiteteZahlungen();

    private ZahlungsServiceWorker() {
    }

    static void main() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String queueName = ZahlungsQueueConfig.queueName();

            try (
                    Connection connection = ZahlungsQueueConfig.connectionFactory().newConnection();
                    Channel channel = connection.createChannel()
            ) {
                channel.queueDeclare(queueName, true, false, false, null);
                channel.basicQos(1);

                DeliverCallback deliverCallback = (_, delivery) -> {
                    try {
                        Zahlungsauftrag auftrag = objectMapper.readValue(delivery.getBody(), Zahlungsauftrag.class);
                        VERARBEITETE_ZAHLUNGEN.registriereOderWirf(auftrag);
                        System.out.println("Zahlung verarbeitet: zahlungsId=" + auftrag.zahlungsId()
                                + " rechnungsId=" + auftrag.rechnungsId()
                                + " betrag=" + auftrag.betrag()
                                + " " + auftrag.waehrung());
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } catch (Exception exception) {
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                        System.err.println("Zahlung konnte nicht verarbeitet werden: " + exception.getMessage());
                    }
                };

                channel.basicConsume(queueName, false, deliverCallback, _ -> { });
                System.out.println("Worker wartet auf eine eingehende Queue '" + queueName + "'");
                new CountDownLatch(1).await();
            }
        } catch (Exception exception) {
            System.err.println("Beim Starten des Zahlungsservices ist folgender Fehler aufgetreten: "
                    + exception.getMessage());
        }
    }
}
