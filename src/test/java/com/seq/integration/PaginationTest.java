package com.seq.integration;

import com.seq.TestUtils;
import com.seq.api.*;
import com.seq.http.Client;

import org.junit.Test;

import java.util.*;
import java.text.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PaginationTest {
  static Client client;
  static Key key;
  final static int PAGE_SIZE = 5;

  @Test
  public void testKeyPageCursor() throws Exception {
    client = TestUtils.generateClient();
    DevUtils.reset(client);
    for (int i = 0; i < PAGE_SIZE + 1; i++) {
      new Key.Builder().setId(UUID.randomUUID().toString()).create(client);
    }

    Key.Page page = new Key.ListBuilder()
      .setPageSize(PAGE_SIZE)
      .getPage(client);
    assertEquals(5, page.items.size());
    assertEquals(false, page.lastPage);

    Key.Page page2 = new Key.ListBuilder()
      .getPage(client, page.cursor);
    assertEquals(1, page2.items.size());
    assertEquals(true, page2.lastPage);
  }

  @Test
  public void testAccountPageCursor() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);

    String testFilter = UUID.randomUUID().toString();

    ArrayList<String> ids = new ArrayList<String>();
    for (int i = 0; i < PAGE_SIZE + 1; i++) {
      new Account.Builder()
        .setId(UUID.randomUUID().toString())
        .addKeyId(key.id)
        .addTag("filter", testFilter)
        .create(client);
    }
    Account.Page page = new Account.ListBuilder()
      .setFilter("tags.filter = $1")
      .setFilterParameters(Arrays.asList(testFilter))
      .setPageSize(PAGE_SIZE)
      .getPage(client);
    assertEquals(5, page.items.size());
    assertEquals(false, page.lastPage);

    Account.Page page2 = new Account.ListBuilder()
      .getPage(client, page.cursor);
    assertEquals(1, page2.items.size());
    assertEquals(true, page2.lastPage);
  }

  @Test
  public void testFlavorPageCursor() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);

    String testFilter = UUID.randomUUID().toString();

    ArrayList<String> ids = new ArrayList<String>();
    for (int i = 0; i < PAGE_SIZE + 1; i++) {
      new Flavor.Builder()
        .setId(UUID.randomUUID().toString())
        .addKeyId(key.id)
        .addTag("filter", testFilter)
        .create(client);
    }
    Flavor.Page page = new Flavor.ListBuilder()
      .setFilter("tags.filter = $1")
      .setFilterParameters(Arrays.asList(testFilter))
      .setPageSize(PAGE_SIZE)
      .getPage(client);
    assertEquals(5, page.items.size());
    assertEquals(false, page.lastPage);

    Flavor.Page page2 = new Flavor.ListBuilder()
      .getPage(client, page.cursor);
    assertEquals(1, page2.items.size());
    assertEquals(true, page2.lastPage);
  }

  @Test
  public void testTransactionPageCursor() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);

    Flavor flavor = new Flavor.Builder().addKeyId(key.id).create(client);
    Account account = new Account.Builder().addKeyId(key.id).create(client);

    String testFilter = UUID.randomUUID().toString();

    ArrayList<String> ids = new ArrayList<String>();
    for (int i = 0; i < PAGE_SIZE + 1; i++) {
      new Transaction.Builder()
          .addAction(
              new Transaction.Builder.Action.Issue()
                  .setFlavorId(flavor.id)
                  .setAmount(1)
                  .setDestinationAccountId(account.id)
                  .addActionTagsField("filter", testFilter))
          .transact(client);
    }
    Transaction.Page page = new Transaction.ListBuilder()
      .setFilter("actions(tags.filter = $1)")
      .setFilterParameters(Arrays.asList(testFilter))
      .setPageSize(PAGE_SIZE)
      .getPage(client);
    assertEquals(5, page.items.size());
    assertEquals(false, page.lastPage);

    Transaction.Page page2 = new Transaction.ListBuilder()
      .getPage(client, page.cursor);
    assertEquals(1, page2.items.size());
    assertEquals(true, page2.lastPage);
  }

  @Test
  public void testFeedPageCursor() throws Exception {
    client = TestUtils.generateClient();
    String uuid = UUID.randomUUID().toString();

    Feed f1 = new Feed.Transaction.Builder()
      .setId("zzzzz" + uuid)
      .create(client);
    Feed f2 = new Feed.Action.Builder()
      .setId("zzzzy" + uuid)
      .create(client);
    Feed f3 = new Feed.Transaction.Builder()
      .setId("zzzzx" + uuid)
      .create(client);

    Feed.Page feeds = new Feed.ListBuilder()
      .setPageSize(1)
      .getPage(client);

    assertEquals(feeds.items.get(0).id, f3.id);

    feeds = new Feed.ListBuilder()
      .getPage(client, feeds.cursor);

    assertEquals(feeds.items.get(0).id, f2.id);
    assertEquals(feeds.lastPage, false);
  }
}
