package com.seq.integration;

import com.seq.TestUtils;
import com.seq.api.*;
import com.seq.http.Client;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ActionTest {
  static Client client;
  static Key key;
  static Key key2;
  static Key key3;

  @Test
  public void run() throws Exception {
    testSnapshotWithTags();
  }

  public void testSnapshotWithTags() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String alice = "TransactionTest-testSnapshotWithTags-issue-alice";
    String bob = "TransactionTest-testSnapshotWithTags-issue-bob";
    String flavor = "TransactionTest-testSnapshotWithTags-issue-flavor";
    String test = "TransactionTest-testSnapshotWithTags-issue-test";

    new Account.Builder()
      .setId(alice)
      .addKeyId(key.id)
      .addTag("test-alice", test)
      .create(client);
    new Account.Builder()
      .setId(bob)
      .addKeyId(key.id)
      .addTag("test-bob", test)
      .create(client);
    new Flavor.Builder()
      .setId(flavor)
      .addKeyId(key.id)
      .addTag("test-flavor", test)
      .create(client);

    Transaction resp =
        new Transaction.Builder()
            .addAction(
                new Transaction.Builder.Action.Issue()
                    .setFlavorId(flavor)
                    .setAmount(100)
                    .setDestinationAccountId(alice))
            .addAction(
                new Transaction.Builder.Action.Transfer()
                    .setFlavorId(flavor)
                    .setAmount(50)
                    .setSourceAccountId(alice)
                    .setDestinationAccountId(bob)
                    .addActionTagsField("test-action", test)
                    .addTokenTagsField("test-token", test))
            .transact(client);

    Action.Page actions =
        new Action.ListBuilder()
            .setFilter("tags.test-action=$1")
            .addFilterParameter(test)
            .getPage(client);
    Action action = actions.items.get(0);
    assertEquals(test, action.snapshot.actionTags.get("test-action"));
    assertEquals(test, action.snapshot.flavorTags.get("test-flavor"));
    assertEquals(test, action.snapshot.tokenTags.get("test-token"));
    assertEquals(test, action.snapshot.sourceAccountTags.get("test-alice"));
    assertEquals(test, action.snapshot.destinationAccountTags.get("test-bob"));
  }
}
