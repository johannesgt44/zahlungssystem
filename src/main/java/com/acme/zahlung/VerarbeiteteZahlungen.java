package com.acme.zahlung;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class VerarbeiteteZahlungen {
    private final Set<String> rechnungsschluessel = ConcurrentHashMap.newKeySet();

    void registriereOderWirf(Zahlungsauftrag auftrag) {
        String schluessel = rechnungsschluessel(auftrag);
        if (!rechnungsschluessel.add(schluessel)) {
            throw new IllegalStateException("Zahlung fuer Rechnung " + schluessel + " wurde bereits verarbeitet.");
        }
    }

    private static String rechnungsschluessel(Zahlungsauftrag auftrag) {
        String rechnungsNummer = trim(auftrag.rechnungsNummer());
        if (!rechnungsNummer.isBlank()) {
            return rechnungsNummer;
        }

        String rechnungsId = trim(auftrag.rechnungsId());
        if (!rechnungsId.isBlank()) {
            return rechnungsId;
        }

        throw new IllegalArgumentException("Zahlung kann ohne Rechnungsschluessel nicht dedupliziert werden.");
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
