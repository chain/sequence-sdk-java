package com.seq.integration;

import com.seq.TestUtils;
import com.seq.api.*;
import com.seq.exception.ChainException;
import com.seq.http.Client;
import org.junit.Test;
import com.seq.exception.APIException;

import java.util.*;
import java.util.concurrent.*;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FeedTest {
  static Client client;

  @Test
  public void run() throws Exception {
    testActionFeedCreation();
    testTransactionFeedCreation();
    testActionFeedConsumption();
    testTransactionFeedConsumption();
    testFeedDeletion();
  }

  public void testActionFeedCreation() throws Exception {
    client = TestUtils.generateClient();
    String id = UUID.randomUUID().toString();

    Feed<Action> created = new Feed.Action.Builder()
      .setId(id)
      .setFilter("snapshot.actionTags.type=$1")
      .addFilterParameter("test")
      .create(client);

    Feed<Action> got = Feed.Action.get(id, client);

    assertEquals(created.id, got.id);
    assertEquals(created.filter, got.filter);
    assertEquals(created.filterParams, got.filterParams);
    assertEquals(created.type, "action");
    assertEquals(created.type, got.type);
  }

  public void testTransactionFeedCreation() throws Exception {
    client = TestUtils.generateClient();
    String id = UUID.randomUUID().toString();

    Feed<Transaction> created = new Feed.Transaction.Builder()
      .create(client);

    assertEquals(created.type, "transaction");
  }

  public void testActionFeedConsumption() throws Exception {
    client = TestUtils.generateClient();
    String uuid = UUID.randomUUID().toString();
    final List<Action> actions = new ArrayList<>();

    Key key = new Key.Builder().create(client);
    new Account.Builder().setId(uuid).addKeyId(key.id).create(client);
    new Flavor.Builder().setId(uuid).addKeyId(key.id).create(client);

    final Feed<Action> feed = new Feed.Action.Builder()
      .setId(uuid)
      .setFilter("snapshot.actionTags.test=$1")
      .addFilterParameter(uuid)
      .create(client);

    final CountDownLatch latch = new CountDownLatch(1);
    new Thread(new Runnable() {
      public void run() {
        for (Action action : feed) {
          actions.add(action);
          if (actions.size() >= 2) {
            break;
          }
        }
        latch.countDown();
      }
    }).start();

    Transaction tx = new Transaction.Builder()
      .addAction(
        new Transaction.Builder.Action.Issue()
          .setFlavorId(uuid)
          .setAmount(1)
          .setDestinationAccountId(uuid)
          .addActionTagsField("test", uuid))
      .addAction(
        new Transaction.Builder.Action.Issue()
          .setFlavorId(uuid)
          .setAmount(1)
          .setDestinationAccountId(uuid)
          .addActionTagsField("test", uuid))
      .transact(client);

    latch.await(2, TimeUnit.SECONDS);
    assertEquals(actions.get(0).id, tx.actions.get(0).id);
    assertEquals(actions.get(1).id, tx.actions.get(1).id);
  }

  public void testTransactionFeedConsumption() throws Exception {
    client = TestUtils.generateClient();
    String uuid = UUID.randomUUID().toString();
    final List<Transaction> txs = new ArrayList<>();

    Key key = new Key.Builder().create(client);
    new Account.Builder().setId(uuid).addKeyId(key.id).create(client);
    new Flavor.Builder().setId(uuid).addKeyId(key.id).create(client);

    final Feed<Transaction> feed = new Feed.Transaction.Builder()
      .setId(uuid)
      .setFilter("actions(snapshot.actionTags.test=$1)")
      .addFilterParameter(uuid)
      .create(client);

    final CountDownLatch latch = new CountDownLatch(1);
    new Thread(new Runnable() {
      public void run() {
        for (Transaction tx : feed) {
          txs.add(tx);
          try { feed.ack(); } catch (ChainException e) {}
          if (txs.size() >= 2) {
            break;
          }
        }
        latch.countDown();
      }
    }).start();

    Transaction tx1 = new Transaction.Builder()
      .addAction(
        new Transaction.Builder.Action.Issue()
          .setFlavorId(uuid)
          .setAmount(1)
          .setDestinationAccountId(uuid)
          .addActionTagsField("test", uuid))
      .transact(client);
    Transaction tx2 = new Transaction.Builder()
      .addAction(
        new Transaction.Builder.Action.Issue()
          .setFlavorId(uuid)
          .setAmount(1)
          .setDestinationAccountId(uuid)
          .addActionTagsField("test", uuid))
      .transact(client);

    latch.await(2, TimeUnit.SECONDS);

    assertEquals(txs.get(0).id, tx1.id);
    assertEquals(txs.get(1).id, tx2.id);
  }

  public void testFeedDeletion() throws Exception {
    client = TestUtils.generateClient();
    String uuid = UUID.randomUUID().toString();

    Feed<Action> feed = new Feed.Action.Builder().setId(uuid).create(client);
    feed.delete();

    try {
      Feed<Action> missing = Feed.Action.get(uuid, client);
    } catch (APIException e) {
      return;
    }
    throw new Exception("expecting APIException");
  }
}
