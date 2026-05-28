package com.acme.zahlung;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.worker.JobClient;
import io.camunda.client.api.worker.JobWorker;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Camunda Job Worker fuer den Service Task "Zahlungsauftrag erzeugen".
 *
 * In Camunda muss beim Service Task dieser Job Type eingetragen werden:
 * zahlungsauftrag-erzeugen
 */
public final class ZahlungsauftragCamundaWorker {
    private static final String JOB_TYPE = "zahlungsauftrag-erzeugen";
    private static final String WORKER_NAME = "zahlungsauftrag-worker";
    private static final String STATUS_GESENDET = "ZAHLUNGSAUFTRAG_GESENDET";

    private ZahlungsauftragCamundaWorker() {
    }

    public static void main(String[] args) throws InterruptedException {
        Properties camundaCredentials = ladeCamundaCredentials();

        try (
                CamundaClient camundaClient = CamundaClient.newCloudClientBuilder()
                        .withClusterId(camundaCredentials.getProperty("camunda.client.cloud.cluster-id"))
                        .withClientId(camundaCredentials.getProperty("camunda.client.auth.client-id"))
                        .withClientSecret(camundaCredentials.getProperty("camunda.client.auth.client-secret"))
                        .withRegion(camundaCredentials.getProperty("camunda.client.cloud.region"))
                        .build();
                JobWorker worker = new ZahlungsauftragWorker().open(camundaClient)
        ) {
            System.out.printf("Job Worker gestartet und wartet auf Jobs vom Typ: %s%n", JOB_TYPE);
            new CountDownLatch(1).await();
        }
    }

    private static final class ZahlungsauftragWorker extends BaseCamundaWorker {
        private ZahlungsauftragWorker() {
            super(WORKER_NAME);
        }

        @Override
        String getType() {
            return JOB_TYPE;
        }

        @Override
        void executeWorker(JobClient jobClient, JobInformation jobInformation) {
            System.out.println("Camunda Job empfangen: zahlungsauftrag-erzeugen");
            Zahlungsauftrag zahlungsauftrag = Zahlungsauftrag.ausCamundaVariablen(jobInformation);

            try (ZahlungsauftragPublisher publisher = new ZahlungsauftragPublisher()) {
                publisher.publish(zahlungsauftrag);
                complete(jobClient, jobInformation, Map.of(
                        "zahlungsId", zahlungsauftrag.zahlungsId(),
                        "zahlungsStatus", STATUS_GESENDET
                ));
                System.out.printf("Zahlungsauftrag gesendet: %s%n", zahlungsauftrag.zahlungsId());
            } catch (Exception exception) {
                fail(jobClient, jobInformation, exception.getMessage());
            }
        }
    }

    private static Properties ladeCamundaCredentials() {
        Path credentialsPath = Path.of(
                System.getProperty("user.dir"),
                "CamundaClientCredentials.properties"
        );

        Properties properties = new Properties();
        try (var inputStream = Files.newInputStream(credentialsPath)) {
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Camunda-Credentials-Datei konnte nicht gelesen werden: "
                    + credentialsPath, exception);
        }

        pruefeProperty(properties, "camunda.client.cloud.cluster-id");
        pruefeProperty(properties, "camunda.client.cloud.region");
        pruefeProperty(properties, "camunda.client.auth.client-id");
        pruefeProperty(properties, "camunda.client.auth.client-secret");
        return properties;
    }

    private static void pruefeProperty(Properties properties, String name) {
        String value = properties.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Camunda-Credentials-Datei enthaelt keinen Wert fuer: " + name);
        }
    }
}
