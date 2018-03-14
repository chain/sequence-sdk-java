package com.seq.integration;

import com.seq.TestUtils;
import com.seq.api.*;
import com.seq.http.Client;
import org.junit.Test;

import java.util.*;

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
}
