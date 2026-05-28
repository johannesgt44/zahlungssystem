package com.acme.zahlung;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

/**
 * RabbitMQ-Consumer fuer Zahlungsauftraege.
 */
public final class ZahlungsServiceWorker {
    private ZahlungsServiceWorker() {
    }

    public static void main(final String[] args) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String queueName = ZahlungsQueueConfig.queueName();
            Connection connection = ZahlungsQueueConfig.connectionFactory().newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicQos(1);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> closeQuietly(channel, connection)));

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    Zahlungsauftrag auftrag = objectMapper.readValue(delivery.getBody(), Zahlungsauftrag.class);
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

            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
            System.out.println("Worker wartet auf eine eingehende Queue '" + queueName + "'");
        } catch (Exception exception) {
            System.err.println("Beim Starten des Zahlungsservices ist folgender Fehler aufgetreten: "
                    + exception.getMessage());
        }
    }

    private static void closeQuietly(Channel channel, Connection connection) {
        try {
            channel.close();
            connection.close();
        } catch (Exception exception) {
            System.err.println("Fehler beim Beenden der Verbindung zu RabbitMQ.");
        }
    }
}
