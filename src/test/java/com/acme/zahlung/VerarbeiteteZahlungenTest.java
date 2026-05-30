package com.acme.zahlung;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class VerarbeiteteZahlungenTest {
    @Test
    void ersteRegistrierungIstErfolgreich() {
        VerarbeiteteZahlungen verarbeiteteZahlungen = new VerarbeiteteZahlungen();
        Zahlungsauftrag auftrag = zahlungsauftrag("RE-2026-0001", "RE-2026-0001");

        assertDoesNotThrow(() -> verarbeiteteZahlungen.registriereOderWirf(auftrag));
    }

    @Test
    void zweiteRegistrierungDerselbenRechnungWirftException() {
        VerarbeiteteZahlungen verarbeiteteZahlungen = new VerarbeiteteZahlungen();
        Zahlungsauftrag auftrag = zahlungsauftrag("RE-2026-0001", "RE-2026-0001");

        verarbeiteteZahlungen.registriereOderWirf(auftrag);
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> verarbeiteteZahlungen.registriereOderWirf(auftrag)
        );

        assertEquals("Zahlung fuer Rechnung RE-2026-0001 wurde bereits verarbeitet.", exception.getMessage());
    }

    @Test
    void unterschiedlicheRechnungenSindErlaubt() {
        VerarbeiteteZahlungen verarbeiteteZahlungen = new VerarbeiteteZahlungen();

        verarbeiteteZahlungen.registriereOderWirf(zahlungsauftrag("RE-2026-0001", "RE-2026-0001"));

        assertDoesNotThrow(() -> verarbeiteteZahlungen.registriereOderWirf(
                zahlungsauftrag("RE-2026-0002", "RE-2026-0002")
        ));
    }

    @Test
    void rechnungsIdWirdAlsFallbackGenutzt() {
        VerarbeiteteZahlungen verarbeiteteZahlungen = new VerarbeiteteZahlungen();
        Zahlungsauftrag auftrag = zahlungsauftrag("RE-2026-0001", "");

        verarbeiteteZahlungen.registriereOderWirf(auftrag);
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> verarbeiteteZahlungen.registriereOderWirf(auftrag)
        );

        assertEquals("Zahlung fuer Rechnung RE-2026-0001 wurde bereits verarbeitet.", exception.getMessage());
    }

    @Test
    void fehlenderRechnungsschluesselWirftException() {
        VerarbeiteteZahlungen verarbeiteteZahlungen = new VerarbeiteteZahlungen();
        Zahlungsauftrag auftrag = zahlungsauftrag("", "");

        assertThrows(IllegalArgumentException.class, () -> verarbeiteteZahlungen.registriereOderWirf(auftrag));
    }

    private static Zahlungsauftrag zahlungsauftrag(String rechnungsId, String rechnungsNummer) {
        return new Zahlungsauftrag(
                "zahlung-1",
                rechnungsId,
                "Lieferant 1 GmbH",
                rechnungsNummer,
                "125.00",
                "EUR"
        );
    }
}
