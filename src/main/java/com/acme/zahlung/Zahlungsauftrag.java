package com.acme.zahlung;

import java.util.UUID;

/**
 * JSON-Vertrag fuer Zahlungsauftraege, die ueber RabbitMQ verarbeitet werden.
 */
public record Zahlungsauftrag(
        String zahlungsId,
        String rechnungsId,
        String lieferantenName,
        String rechnungsNummer,
        String betrag,
        String waehrung
) {
    static Zahlungsauftrag ausCamundaVariablen(JobInformation jobInformation) {
        return new Zahlungsauftrag(
                UUID.randomUUID().toString(),
                jobInformation.getStringVariable("rechnungsId"),
                jobInformation.getStringVariable("lieferantenName"),
                jobInformation.getStringVariable("rechnungsNummer"),
                jobInformation.getStringVariable("gesamtbetragBrutto"),
                jobInformation.getStringVariable("waehrung")
        );
    }
}
