package com.seq.integration;

import com.seq.TestUtils;
import com.seq.api.*;
import com.seq.http.*;

import org.junit.*;
import static org.junit.Assert.*;

public class StatsTest {
  @Test
  public void basicUsage() throws Exception {
    Client c = TestUtils.generateClient();
    Stats initial = Stats.get(c);

    Key k = new Key.Builder().create(c);

    Account acc = new Account.Builder().addKeyId(k.id).setQuorum(1).create(c);
    Flavor flavor = new Flavor.Builder().addKeyId(k.id).setQuorum(1).create(c);

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Issue()
                .setFlavorId(flavor.id)
                .setAmount(1)
                .setDestinationAccountId(acc.id))
        .transact(c);

    Stats got = Stats.get(c);

    assertEquals(got.flavorCount, initial.flavorCount + 1);
    assertEquals(got.accountCount, initial.accountCount + 1);
    assertEquals(got.txCount, initial.txCount + 1);
    assertEquals(got.ledgerType, "dev");
  }
}
