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
  final static int PAGE_SIZE = 100;

  @Test
  public void run() throws Exception {
    testKeyQuery();
    testAccountQuery();
    testFlavorQuery();
    testTransactionQuery();
    testActionQuery();
    testTokenQuery();
    testPagination();
  }

  public void testKeyQuery() throws Exception {
    UUID idOne = UUID.randomUUID();
    UUID idTwo = UUID.randomUUID();
    UUID idThree = UUID.randomUUID();

    client = TestUtils.generateClient();
    new Key.Builder().setId(idOne.toString()).create(client);
    new Key.Builder().setId(idTwo.toString()).create(client);
    new Key.Builder().setId(idThree.toString()).create(client);
    for (int i = 0; i < 3; i++) {
      new Key.Builder().setId(UUID.randomUUID().toString()).create(client);
    }
    Key.Page items =
        new Key.QueryBuilder()
            .setIds(Arrays.asList(idOne.toString(), idTwo.toString()))
            .addId(idThree.toString())
            .getPage(client);
    assertEquals(3, items.items.size());
  }

  public void testAccountQuery() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String alice = UUID.randomUUID().toString();
    new Account.Builder()
      .setId(alice)
      .addKeyId(key.id)
      .setQuorum(1)
      .create(client);

    Account.Page items =
        new Account.QueryBuilder().setFilter("id=$1").addFilterParameter(alice).getPage(client);

    assertEquals(1, items.items.size());
    assertEquals(alice, items.items.get(0).id);

    Account.ItemIterable iter =
        new Account.QueryBuilder()
            .setFilter("id=$1")
            .addFilterParameter(alice)
            .getIterable(client);

    for (Account a : iter) {
      assertEquals(alice, a.id);
    }
  }

  public void testFlavorQuery() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String flavorId = UUID.randomUUID().toString();
    new Flavor.Builder()
      .setId(flavorId)
      .addKeyId(key.id)
      .setQuorum(1)
      .create(client);

    Flavor.Page items =
        new Flavor.QueryBuilder()
        .setFilter("id=$1")
        .addFilterParameter(flavorId)
        .getPage(client);

    assertEquals(1, items.items.size());
    assertEquals(flavorId, items.items.get(0).id);
  }

  public void testTransactionQuery() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String alice = UUID.randomUUID().toString();
    String flavorId = UUID.randomUUID().toString();
    String test = UUID.randomUUID().toString();
    long amount = 100;

    new Account.Builder()
      .setId(alice)
      .addKeyId(key.id)
      .setQuorum(1)
      .create(client);
    new Flavor.Builder()
      .setId(flavorId)
      .addKeyId(key.id)
      .setQuorum(1)
      .create(client);

    new Transaction.Builder()
      .addAction(
        new Transaction.Builder.Action.Issue()
          .setFlavorId(flavorId)
          .setAmount(amount)
          .setDestinationAccountId(alice)
          .addActionTagsField("test", test))
      .transact(client);

    Transaction.Page txs =
        new Transaction.QueryBuilder()
            .setFilter("actions(tags.test=$1)")
            .addFilterParameter(test)
            .setStartTime(System.currentTimeMillis())
            .getPage(client);
    assertEquals(0, txs.items.size());

    txs =
        new Transaction.QueryBuilder()
            .setFilter("actions(tags.test=$1)")
            .addFilterParameter(test)
            .setEndTime(System.currentTimeMillis() - 100000000000L)
            .getPage(client);
    assertEquals(0, txs.items.size());

    txs =
        new Transaction.QueryBuilder()
            .setFilter("actions(tags.test=$1)")
            .addFilterParameter(test)
            .getPage(client);
    Transaction tx = txs.items.get(0);
    assertEquals(1, txs.items.size());
    assertEquals(test, tx.actions.get(0).tags.get("test"));
  }

  public void testActionQuery() throws Exception {
    client = TestUtils.generateClient();
    DevUtils.reset(client);
    key = new Key.Builder().create(client);
    String flavorId = UUID.randomUUID().toString();
    String alice = UUID.randomUUID().toString();
    String test = UUID.randomUUID().toString();
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
          .setQuorum(1)
          .create(client);
      txBuilder.addAction(
        new Transaction.Builder.Action.Issue()
          .setFlavorId(flavorId)
          .setAmount(amount)
          .setDestinationAccountId(account.id)
          .addActionTagsField("test", test));

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
            .addActionTagsField("test", test));
      }
    }

    txBuilder.transact(client);

    Action.ItemIterable actions = new Action.ListBuilder()
      .setFilter("tags.test=$1")
      .addFilterParameter(test)
      .getIterable(client);
    int i = 0;
    for (Action action : actions) {
      i++;
    }
    assertEquals(12, i);

    Action.Page page =
        new Action.ListBuilder()
            .setFilter("tags.test=$1")
            .addFilterParameter(test)
            .getPage(client);
    assertEquals(12, page.items.size());

    page =
        new Action.ListBuilder()
            .setFilter("tags.test=$1")
            .addFilterParameter(test)
            .setPageSize(10)
            .getPage(client);
    assertEquals(10, page.items.size());

    page = new Action.ListBuilder().getPage(client, page.cursor);
    assertEquals(2, page.items.size());

    page =
        new Action.ListBuilder()
            .setFilter("tags.test=$1 AND timestamp<$2")
            .addFilterParameter(test)
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
            .addFilterParameter(test)
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
            .addFilterParameter(test)
            .addGroupByField("type")
            .addGroupByField("tags.test")
            .getPage(client);
    ActionSum as = sumPage.items.get(0);
    assertEquals(1, sumPage.items.size());
    assertEquals(1000, as.amount);
    assertNotNull(as.tags);
    Map<String, String> nestedField = (Map<String, String>) (as.tags);
    assertNotNull(nestedField.get("test"));

    sumPage =
        new Action.SumBuilder()
            .setFilter("type=$1 AND tags.test=$2")
            .addFilterParameter("transfer")
            .addGroupByField("type")
            .addFilterParameter(test)
            .getPage(client);
    as = sumPage.items.get(0);
    assertEquals(1, sumPage.items.size());
    assertEquals(10, as.amount);
  }

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
            .addGroupByField("account_id")
            .getPage(client);
    assertEquals(10, sumPage.items.size());

    sumPage =
        new Token.SumBuilder()
            .addGroupByField("account_id")
            .setPageSize(7)
            .getPage(client);
    assertEquals(7, sumPage.items.size());

    sumPage = new Token.SumBuilder().getPage(client, sumPage.cursor);
    assertEquals(3, sumPage.items.size());

    TokenSum.ItemIterable tokenSums = new Token.SumBuilder()
      .addGroupByField("account_id")
      .getIterable(client);
    for (TokenSum sum : tokenSums) {
      assertEquals(100, sum.amount);
    }
  }

  // Because BaseQueryBuilder#getPage is used in the execute
  // method and for pagination, testing pagination for one
  // api object is sufficient for exercising the code path.
  public void testPagination() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String tag = UUID.randomUUID().toString();
    for (int i = 0; i < PAGE_SIZE + 1; i++) {
      new Account.Builder()
        .addKeyId(key.id)
        .setQuorum(1)
        .addTag("tag", tag)
        .create(client);
    }

    int counter;

    // Test item iterator
    Account.ItemIterable items =
        new Account.QueryBuilder()
            .setFilter("tags.tag=$1")
            .addFilterParameter(tag)
            .getIterable(client);

    counter = 0;
    for (Account a : items) {
      assertNotNull(a.id);
      counter++;
    }
    assertEquals(PAGE_SIZE + 1, counter);
  }
}
