package com.acme.zahlung;

import com.rabbitmq.client.ConnectionFactory;

final class ZahlungsQueueConfig {
    static final String DEFAULT_NAME = "payment.orders";

    private ZahlungsQueueConfig() {
    }

    static String queueName() {
        return environmentValue("PAYMENT_QUEUE", DEFAULT_NAME);
    }

    static ConnectionFactory connectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(environmentValue("RABBITMQ_HOST", "localhost"));
        factory.setPort(Integer.parseInt(environmentValue("RABBITMQ_PORT", "5672")));
        factory.setUsername(environmentValue("RABBITMQ_USERNAME", "guest"));
        factory.setPassword(environmentValue("RABBITMQ_PASSWORD", "guest"));
        return factory;
    }

    private static String environmentValue(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
