# Zahlungssystem

Separater Microservice fuer Zahlungsauftraege. Camunda kann einen Zahlungsauftrag erzeugen lassen; der Worker veroeffentlicht ihn als JSON in RabbitMQ. Der Zahlungsservice konsumiert anschliessend die Queue `payment.orders`.

## Komponenten

- `zahlungsauftrag-worker`: Camunda Job Worker fuer den Job Type `zahlungsauftrag-erzeugen`
- `zahlung-service`: RabbitMQ-Consumer fuer Zahlungsauftraege

## Demo starten

RabbitMQ starten:

```powershell
docker compose up -d
```

Terminal 1: Zahlung-Service starten.

```powershell
.\gradlew.bat runZahlungService
```

Terminal 2: Camunda Job Worker starten.

```powershell
.\gradlew.bat runZahlungsauftragCamundaWorker
```

## Camunda

Im Camunda Modeler muss beim Service Task `Zahlungsauftrag erzeugen` dieser Job Type eingetragen werden:

```text
zahlungsauftrag-erzeugen
```

Notwendige Prozessvariablen:

```json
{
  "rechnungsId": "RE-2026-0001",
  "rechnungsNummer": "RE-2026-0001",
  "lieferantenName": "Lieferant 1 GmbH",
  "gesamtbetragBrutto": "125.00",
  "waehrung": "EUR"
}
```

Nach erfolgreichem Publish schreibt der Worker diese Prozessvariablen:

```json
{
  "zahlungsId": "<uuid>",
  "zahlungsStatus": "ZAHLUNGSAUFTRAG_GESENDET"
}
```

Der Zahlungsservice verhindert doppelte Verarbeitung pro laufendem Prozess anhand der `rechnungsNummer`.
Falls diese leer ist, wird `rechnungsId` als technischer Fallback genutzt.
Die Liste ist in-memory: Nach einem Neustart ist sie leer und bei mehreren Service-Instanzen nicht instanzuebergreifend geteilt.

## Verfuegbare Gradle-Tasks

- `.\gradlew.bat runZahlungService` startet den RabbitMQ-Consumer
- `.\gradlew.bat runZahlungsauftragCamundaWorker` startet den Camunda Worker

## Konfiguration

Die Defaults passen fuer lokale Entwicklung:

- `RABBITMQ_HOST=localhost`
- `RABBITMQ_PORT=5672`
- `RABBITMQ_USERNAME=guest`
- `RABBITMQ_PASSWORD=guest`
- `PAYMENT_QUEUE=payment.orders`

Der Camunda Worker liest `CamundaClientCredentials.properties` aus dem Projektverzeichnis.
