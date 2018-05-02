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

public class QueryTest {
  static Client client;
  static Key key;

  @Test
  public void testKeyQuery() throws Exception {
    client = TestUtils.generateClient();
    DevUtils.reset(client);
    for (int i = 0; i < 3; i++) {
      new Key.Builder().setId(UUID.randomUUID().toString()).create(client);
    }

    Key.Page page = new Key.ListBuilder().getPage(client);

    assertEquals(3, page.items.size());
  }

  @Test
  public void testAccountQuery() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String alice = UUID.randomUUID().toString();
    new Account.Builder()
      .setId(alice)
      .addKeyId(key.id)
      .setQuorum(1)
      .create(client);

    Account.Page page =
      new Account.ListBuilder()
        .setFilter("id=$1")
        .addFilterParameter(alice)
        .getPage(client);

    assertEquals(1, page.items.size());
    assertEquals(alice, page.items.get(0).id);

    Account.ItemIterable items =
      new Account.ListBuilder()
        .setFilter("id=$1")
        .addFilterParameter(alice)
        .getIterable(client);

    int counter = 0;
    for (Account a : items) {
      assertEquals(alice, a.id);
      counter++;
    }
    assertEquals(1, counter);
  }

  @Test
  public void testFlavorQuery() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String flavorId = UUID.randomUUID().toString();
    new Flavor.Builder()
      .setId(flavorId)
      .addKeyId(key.id)
      .setQuorum(1)
      .create(client);

    Flavor.Page page =
        new Flavor.ListBuilder()
        .setFilter("id=$1")
        .addFilterParameter(flavorId)
        .getPage(client);

    assertEquals(1, page.items.size());
    assertEquals(flavorId, page.items.get(0).id);
  }

  @Test
  public void testTransactionQuery() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String alice = UUID.randomUUID().toString();
    String flavorId = UUID.randomUUID().toString();
    String test = UUID.randomUUID().toString();
    long amount = 100;
    String before =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .format(new Date(System.currentTimeMillis() - 100000000000L));
    new Account.Builder()
      .setId(alice)
      .addKeyId(key.id)
      .setQuorum(1)
      .create(client);
    new Flavor.Builder()
      .setId(flavorId)
      .addKeyId(key.id)
      .setQuorum(1)
      .addTag("test", test)
      .create(client);

    new Transaction.Builder()
      .addAction(
        new Transaction.Builder.Action.Issue()
          .setFlavorId(flavorId)
          .setAmount(amount)
          .setDestinationAccountId(alice)
          .addActionTagsField("test", test))
      .transact(client);

    String later =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .format(new Date(System.currentTimeMillis() + 100000000000L));
    Transaction.Page page =
      new Transaction.ListBuilder()
        .setFilter("actions(tags.test=$1) AND timestamp > $2")
        .addFilterParameter(test)
        .addFilterParameter(later)
        .getPage(client);
    assertEquals(0, page.items.size());

    page =
      new Transaction.ListBuilder()
        .setFilter("actions(tags.test=$1) AND timestamp < $2")
        .addFilterParameter(test)
        .addFilterParameter(before)
        .getPage(client);
    assertEquals(0, page.items.size());

    page =
      new Transaction.ListBuilder()
        .setFilter("actions(tags.test=$1)")
        .addFilterParameter(test)
        .getPage(client);
    assertEquals(1, page.items.size());
    assertEquals(test, page.items.get(0).actions.get(0).tags.get("test"));

    page =
      new Transaction.ListBuilder()
        .setFilter("actions(snapshot.flavorTags.test=$1)")
        .addFilterParameter(test)
        .getPage(client);
    assertEquals(1, page.items.size());
  }

  @Test
  public void testActionQuery() throws Exception {
    client = TestUtils.generateClient();
    DevUtils.reset(client);
    key = new Key.Builder().create(client);
    String flavorId = UUID.randomUUID().toString();
    String alice = UUID.randomUUID().toString();
    String tagData = UUID.randomUUID().toString();
    String firstAccountId = "";
    String oldTime =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .format(new Date(System.currentTimeMillis() - 100000000000L))
            + "Z";
    long amount = 100;

    new Flavor.Builder()
      .addKeyId(key.id)
      .setId(flavorId)
      .addTag("name", flavorId)
      .setQuorum(1)
      .create(client);

    Transaction.Builder txBuilder = new Transaction.Builder();

    for (int i = 0; i < 10; i++) {
      Account account =
        new Account.Builder()
          .setId(alice + i)
          .addKeyId(key.id)
          .addTag("test", tagData)
          .setQuorum(1)
          .create(client);
      txBuilder.addAction(
        new Transaction.Builder.Action.Issue()
          .setFlavorId(flavorId)
          .setAmount(amount)
          .setDestinationAccountId(account.id)
          .addActionTagsField("test", tagData)
          .addTokenTagsField("test", tagData));

      if (i == 0) {
        firstAccountId = account.id;
      }

      if (i == 8 || i == 9) {
        txBuilder.addAction(
          new Transaction.Builder.Action.Transfer()
            .setFlavorId(flavorId)
            .setAmount(5)
            .setSourceAccountId(account.id)
            .setDestinationAccountId(firstAccountId)
            .addActionTagsField("test", tagData)
            .addTokenTagsField("test", tagData));
      }
    }

    txBuilder.transact(client);

    Action.ItemIterable actions = new Action.ListBuilder()
      .setFilter("tags.test=$1")
      .addFilterParameter(tagData)
      .getIterable(client);
    int i = 0;
    for (Action action : actions) {
      i++;
    }
    assertEquals(12, i);

    Action.Page page =
        new Action.ListBuilder()
            .setFilter("tags.test=$1")
            .addFilterParameter(tagData)
            .getPage(client);
    assertEquals(12, page.items.size());

    page =
        new Action.ListBuilder()
            .setFilter("tags.test=$1")
            .addFilterParameter(tagData)
            .setPageSize(10)
            .getPage(client);
    assertEquals(10, page.items.size());

    page = new Action.ListBuilder().getPage(client, page.cursor);
    assertEquals(2, page.items.size());

    page =
        new Action.ListBuilder()
            .setFilter("tags.test=$1 AND timestamp<$2")
            .addFilterParameter(tagData)
            .addFilterParameter(oldTime)
            .getPage(client);
    assertEquals(0, page.items.size());

    ActionSum.ItemIterable actionSums = new Action.SumBuilder()
        .addGroupByField("type")
        .getIterable(client);
    int issued = 0;
    int transferred = 0;
    for (ActionSum sum : actionSums) {
      if (sum.type.equals("issue")) {
          issued++;
      } else if (sum.type.equals("transfer")) {
          transferred++;
      }
    }
    assertEquals(1, issued);
    assertEquals(1, transferred);

    ActionSum.Page sumPage =
        new Action.SumBuilder()
            .setFilter("type=$1 AND tags.test=$2 AND timestamp<$3")
            .addFilterParameter("issue")
            .addFilterParameter(tagData)
            .addFilterParameter(oldTime)
            .addGroupByField("type")
            .addGroupByField("tags.test")
            .getPage(client);
    assertEquals(0, sumPage.items.size());

    sumPage =
        new Action.SumBuilder()
            .addGroupByField("type")
            .getPage(client);
    assertEquals(2, sumPage.items.size());

    sumPage =
        new Action.SumBuilder()
            .addGroupByField("type")
            .setPageSize(1)
            .getPage(client);
    assertEquals(1, sumPage.items.size());

    sumPage = new Action.SumBuilder().getPage(client, sumPage.cursor);
    assertEquals(1, sumPage.items.size());

    sumPage =
        new Action.SumBuilder()
            .setFilter("type=$1 AND tags.test=$2")
            .addFilterParameter("issue")
            .addFilterParameter(tagData)
            .addGroupByField("type")
            .addGroupByField("tags.test")
            .getPage(client);
    ActionSum as = sumPage.items.get(0);
    assertEquals(1, sumPage.items.size());
    assertEquals(1000, as.amount);
    assertNotNull(as.tags);
    Map<String, Object> nestedField = (Map<String, Object>) (as.tags);
    assertNotNull(nestedField.get("test"));

    sumPage =
        new Action.SumBuilder()
            .setFilter("type=$1 AND tags.test=$2")
            .addFilterParameter("transfer")
            .addGroupByField("type")
            .addFilterParameter(tagData)
            .getPage(client);
    as = sumPage.items.get(0);
    assertEquals(1, sumPage.items.size());
    assertEquals(10, as.amount);

    sumPage =
      new Action.SumBuilder()
        .setFilter("type=$1 AND flavorId=$2")
        .addFilterParameter("transfer")
        .addFilterParameter(flavorId)
        .addGroupByField("type")
        .addGroupByField("flavorId")
        .addGroupByField("tags")
        .addGroupByField("snapshot")
        .getPage(client);
    as = sumPage.items.get(0);
    assertEquals(1, sumPage.items.size());
    assertEquals(10, as.amount);
    assertEquals(flavorId, as.flavorId);
    Map<String, Object> tagsField = (Map<String, Object>) (as.tags);
    assertEquals(tagData, tagsField.get("test"));
    assertEquals(tagData, as.snapshot.actionTags.get("test"));
    assertEquals(flavorId, as.snapshot.flavorTags.get("name"));
    assertEquals(tagData, as.snapshot.tokenTags.get("test"));
    assertEquals(tagData, as.snapshot.sourceAccountTags.get("test"));
    assertEquals(tagData, as.snapshot.destinationAccountTags.get("test"));
  }

  @Test
  public void testTokenQuery() throws Exception {
    client = TestUtils.generateClient();
    DevUtils.reset(client);
    key = new Key.Builder().create(client);
    String flavor = UUID.randomUUID().toString();
    String alice = UUID.randomUUID().toString();
    String test = UUID.randomUUID().toString();
    long amount = 100;

    new Flavor.Builder()
      .addKeyId(key.id)
      .setId(flavor)
      .addTag("name", flavor)
      .setQuorum(1)
      .create(client);

    Transaction.Builder txBuilder = new Transaction.Builder();

    for (int i = 0; i < 10; i++) {
      Account account =
        new Account.Builder()
          .setId(alice + i)
          .addKeyId(key.id)
          .setQuorum(1)
          .create(client);
      txBuilder.addAction(
          new Transaction.Builder.Action.Issue()
              .setFlavorId(flavor)
              .setAmount(amount)
              .setDestinationAccountId(account.id)
              .addTokenTagsField("test", test));
    }

    txBuilder.transact(client);

    Token.ItemIterable tokens = new Token.ListBuilder()
        .setFilter("tags.test=$1")
        .addFilterParameter(test)
        .getIterable(client);
    int i = 0;
    for (Token token : tokens) {
        i++;
    }
    assertEquals(10, i);

    Token.Page page =
        new Token.ListBuilder()
            .setFilter("tags.test=$1")
            .addFilterParameter(test)
            .getPage(client);
    assertEquals(10, page.items.size());

    Token token = page.items.get(0);
    assertEquals(100, token.amount);

    page =
        new Token.ListBuilder()
            .setFilter("tags.test=$1")
            .addFilterParameter(test)
            .setPageSize(7)
            .getPage(client);
    assertEquals(7, page.items.size());

    page = new Token.ListBuilder().getPage(client, page.cursor);
    assertEquals(3, page.items.size());

    TokenSum.Page sumPage =
        new Token.SumBuilder()
            .addGroupByField("accountId")
            .getPage(client);
    assertEquals(10, sumPage.items.size());

    sumPage =
        new Token.SumBuilder()
            .addGroupByField("accountId")
            .setPageSize(7)
            .getPage(client);
    assertEquals(7, sumPage.items.size());

    sumPage = new Token.SumBuilder().getPage(client, sumPage.cursor);
    assertEquals(3, sumPage.items.size());

    TokenSum.ItemIterable tokenSums = new Token.SumBuilder()
      .addGroupByField("accountId")
      .getIterable(client);
    for (TokenSum sum : tokenSums) {
      assertEquals(100, sum.amount);
    }
  }

  // Because BaseQueryBuilder#getPage is used in the execute
  // method and for pagination, testing pagination for one
  // api object is sufficient for exercising the code path.
  @Test
  public void testPagination() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String tag = UUID.randomUUID().toString();
    for (int i = 0; i < 101; i++) {
      new Account.Builder()
        .addKeyId(key.id)
        .setQuorum(1)
        .addTag("tag", tag)
        .create(client);
    }

    Account.ItemIterable items =
      new Account.ListBuilder()
        .setFilter("tags.tag=$1")
        .addFilterParameter(tag)
        .getIterable(client);

    int counter = 0;
    for (Account a : items) {
      assertNotNull(a.id);
      counter++;
    }
    assertEquals(101, counter);
  }
}
