package com.seq;

import com.seq.http.Client;
import com.seq.http.session.TestRefresher;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
/**
 * TestUtils provides a simplified api for testing.
 */
public class TestUtils {

  public static Client generateClient() throws Exception {
    String ledgerName = System.getenv("LEDGER_NAME");
    String teamName = System.getenv("TEAM_NAME");
    String macaroon = System.getenv("MACAROON");
    String dischargeMacaroon = System.getenv("DISCHARGE_MACAROON");

    if (ledgerName == null || ledgerName.isEmpty()) {
      ledgerName = "test";
    }

    if (teamName == null || teamName.isEmpty()) {
      teamName = "team";
    }

    Client client =
        new Client.Builder()
            .setLedgerName(ledgerName)
            .setCredential(macaroon)
            .setTrustedCerts(new ByteArrayInputStream(System.getenv("ROOT_CA_CERTS").getBytes()))
            .build();
    client.refresher = new TestRefresher(teamName, dischargeMacaroon);
    return client;
  }
}
