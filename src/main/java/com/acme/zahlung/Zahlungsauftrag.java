package com.acme.zahlung;

import java.util.Map;
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
        return ausVariablen(Map.of(
                "rechnungsId", jobInformation.getStringVariable("rechnungsId"),
                "lieferantenName", jobInformation.getStringVariable("lieferantenName"),
                "rechnungsNummer", jobInformation.getStringVariable("rechnungsNummer"),
                "gesamtbetragBrutto", jobInformation.getStringVariable("gesamtbetragBrutto"),
                "waehrung", jobInformation.getStringVariable("waehrung")
        ));
    }

    static Zahlungsauftrag ausVariablen(Map<String, ?> variablen) {
        String rechnungsId = stringVariable(variablen, "rechnungsId");
        String lieferantenName = stringVariable(variablen, "lieferantenName");
        String rechnungsNummer = stringVariable(variablen, "rechnungsNummer");
        String betrag = stringVariable(variablen, "gesamtbetragBrutto");
        String waehrung = stringVariable(variablen, "waehrung");

        return new Zahlungsauftrag(
                UUID.randomUUID().toString(),
                rechnungsId,
                lieferantenName,
                rechnungsNummer,
                betrag,
                waehrung
        );
    }

    private static String stringVariable(Map<String, ?> variablen, String name) {
        Object value = variablen.get(name);
        return value == null ? "" : value.toString().trim();
    }
}
