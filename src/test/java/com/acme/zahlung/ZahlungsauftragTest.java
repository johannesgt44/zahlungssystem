package com.acme.zahlung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ZahlungsauftragTest {
    @Test
    void gueltigeVariablenErzeugenZahlungsauftrag() {
        Map<String, Object> variablen = gueltigeVariablen();

        Zahlungsauftrag zahlungsauftrag = Zahlungsauftrag.ausVariablen(variablen);

        assertFalse(zahlungsauftrag.zahlungsId().isBlank());
        assertEquals("RE-2026-0001", zahlungsauftrag.rechnungsId());
        assertEquals("Lieferant 1 GmbH", zahlungsauftrag.lieferantenName());
        assertEquals("RE-2026-0001", zahlungsauftrag.rechnungsNummer());
        assertEquals("125.00", zahlungsauftrag.betrag());
        assertEquals("EUR", zahlungsauftrag.waehrung());
    }

    @Test
    void mappingFuehrtKeineFachlicheValidierungAus() {
        Map<String, Object> variablen = gueltigeVariablen();
        variablen.put("gesamtbetragBrutto", "-1.00");
        variablen.put("waehrung", "EURO");

        Zahlungsauftrag zahlungsauftrag = Zahlungsauftrag.ausVariablen(variablen);

        assertEquals("-1.00", zahlungsauftrag.betrag());
        assertEquals("EURO", zahlungsauftrag.waehrung());
    }

    private static Map<String, Object> gueltigeVariablen() {
        Map<String, Object> variablen = new HashMap<>();
        variablen.put("rechnungsId", "RE-2026-0001");
        variablen.put("lieferantenName", "Lieferant 1 GmbH");
        variablen.put("rechnungsNummer", "RE-2026-0001");
        variablen.put("gesamtbetragBrutto", "125.00");
        variablen.put("waehrung", "EUR");
        return variablen;
    }
}
