package com.seq;

import com.seq.http.Client;

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
    String credential = System.getenv("SEQCRED");

    if (ledgerName == null || ledgerName.isEmpty()) {
      ledgerName = "test";
    }

    Client client =
        new Client.Builder()
            .setLedgerName(ledgerName)
            .setCredential(credential)
            .build();
    return client;
  }
}
