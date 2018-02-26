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
    testAssetQuery();
    testTransactionQuery();
    testActionQuery();
    testBalanceQuery();
    testTokenQuery();
    testContractQuery();
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
    new Account.Builder().setId(alice).addKey(key).setQuorum(1).create(client);

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

    Account.PageIterable piter =
        new Account.QueryBuilder()
            .setFilter("alias=$1")
            .addFilterParameter(alice)
            .getPageIterable(client);

    for (Account.Page p : piter) {
      assertEquals(alice, p.items.get(0).id);
    }
  }

  public void testAssetQuery() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String asset = UUID.randomUUID().toString();
    new Asset.Builder().setAlias(asset).addKey(key).setQuorum(1).create(client);
    Asset.Page items =
        new Asset.QueryBuilder().setFilter("alias=$1").addFilterParameter(asset).getPage(client);
    assertEquals(1, items.items.size());
    assertEquals(asset, items.items.get(0).alias);
  }

  public void testFlavorQuery() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String flavor = UUID.randomUUID().toString();
    new Flavor.Builder().setId(flavor).addKey(key).setQuorum(1).create(client);
    Flavor.Page items =
        new Flavor.QueryBuilder().setFilter("id=$1").addFilterParameter(flavor).getPage(client);
    assertEquals(1, items.items.size());
    assertEquals(flavor, items.items.get(0).id);
  }

  public void testTransactionQuery() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String alice = UUID.randomUUID().toString();
    String asset = UUID.randomUUID().toString();
    String test = UUID.randomUUID().toString();
    long amount = 100;

    new Account.Builder().setId(alice).addKey(key).setQuorum(1).create(client);
    new Asset.Builder().setAlias(asset).addKey(key).setQuorum(1).create(client);

    Map<String, Object> refData = new HashMap<>();
    refData.put("asset", asset);
    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Issue()
                .setAssetAlias(asset)
                .setAmount(amount)
                .setDestinationAccountId(alice)
                .addReferenceDataField("test", test))
        .setReferenceData(refData)
        .addReferenceDataField("test", test)
        .transact(client);

    Transaction.Page txs =
        new Transaction.QueryBuilder()
            .setFilter("reference_data.test=$1")
            .addFilterParameter(test)
            .setStartTime(System.currentTimeMillis())
            .getPage(client);
    assertEquals(0, txs.items.size());

    txs =
        new Transaction.QueryBuilder()
            .setFilter("reference_data.test=$1")
            .addFilterParameter(test)
            .setEndTime(System.currentTimeMillis() - 100000000000L)
            .getPage(client);
    assertEquals(0, txs.items.size());

    txs =
        new Transaction.QueryBuilder()
            .setFilter("contracts(reference_data.test=$1)")
            .addFilterParameter(test)
            .getPage(client);
    Transaction tx = txs.items.get(0);
    assertEquals(1, txs.items.size());
    assertEquals(asset, tx.referenceData.get("asset"));
    assertEquals(test, tx.referenceData.get("test"));

    txs =
        new Transaction.QueryBuilder()
            .setFilter("reference_data.test=$1")
            .addFilterParameter(test)
            .getPage(client);
    tx = txs.items.get(0);
    assertEquals(1, txs.items.size());
    assertEquals(asset, tx.referenceData.get("asset"));
    assertEquals(test, tx.referenceData.get("test"));
  }

  public void testActionQuery() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String asset = UUID.randomUUID().toString();
    String alice = UUID.randomUUID().toString();
    String test = UUID.randomUUID().toString();
    String firstAccountId = "";
    String oldTime =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .format(new Date(System.currentTimeMillis() - 100000000000L))
            + "Z";
    long amount = 100;

    new Asset.Builder()
        .addKey(key)
        .setAlias(asset)
        .addTag("name", asset)
        .setQuorum(1)
        .create(client);

    Transaction.Builder txBuilder = new Transaction.Builder();

    for (int i = 0; i < 10; i++) {
      Account account =
          new Account.Builder().setId(alice + i).addKey(key).setQuorum(1).create(client);
      txBuilder.addAction(
          new Transaction.Builder.Action.Issue()
              .setAssetAlias(asset)
              .setAmount(amount)
              .setDestinationAccountId(account.id)
              .addReferenceDataField("test", test));

      if (i == 0) {
        firstAccountId = account.id;
      }

      if (i == 8 || i == 9) {
        txBuilder.addAction(
            new Transaction.Builder.Action.Transfer()
                .setAssetAlias(asset)
                .setAmount(5)
                .setSourceAccountId(account.id)
                .setDestinationAccountId(firstAccountId)
                .addReferenceDataField("test", test));
      }
    }

    txBuilder.transact(client);

    Action.Page items =
        new Action.ListBuilder()
            .setFilter("reference_data.test=$1")
            .addFilterParameter(test)
            .getPage(client);
    assertEquals(12, items.items.size());

    items =
        new Action.ListBuilder()
            .setFilter("reference_data.test=$1 AND timestamp<$2")
            .addFilterParameter(test)
            .addFilterParameter(oldTime)
            .getPage(client);
    assertEquals(0, items.items.size());

    ActionSum.Page sumPage =
        new Action.SumBuilder()
            .setFilter("type=$1 AND reference_data.test=$2 AND timestamp<$3")
            .addFilterParameter("issue")
            .addFilterParameter(test)
            .addFilterParameter(oldTime)
            .addGroupByField("type")
            .addGroupByField("reference_data.test")
            .getPage(client);
    assertEquals(0, sumPage.items.size());

    sumPage =
        new Action.SumBuilder()
            .setFilter("type=$1 AND reference_data.test=$2")
            .addFilterParameter("issue")
            .addFilterParameter(test)
            .addGroupByField("type")
            .addGroupByField("reference_data.test")
            .getPage(client);
    ActionSum as = sumPage.items.get(0);
    assertEquals(1, sumPage.items.size());
    assertEquals(1000, as.amount);
    assertNotNull(as.referenceData);
    Map<String, String> nestedField = (Map<String, String>) (as.referenceData);
    assertNotNull(nestedField.get("test"));

    sumPage =
        new Action.SumBuilder()
            .setFilter("type=$1 AND reference_data.test=$2")
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
    key = new Key.Builder().create(client);
    String flavor = UUID.randomUUID().toString();
    String alice = UUID.randomUUID().toString();
    String test = UUID.randomUUID().toString();
    long amount = 100;

    new Flavor.Builder()
        .addKey(key)
        .setId(flavor)
        .addTag("name", flavor)
        .setQuorum(1)
        .create(client);

    Transaction.Builder txBuilder = new Transaction.Builder();

    for (int i = 0; i < 10; i++) {
      Account account =
          new Account.Builder().setId(alice + i).addKey(key).setQuorum(1).create(client);
      txBuilder.addAction(
          new Transaction.Builder.Action.Issue()
              .setFlavorId(flavor)
              .setAmount(amount)
              .setDestinationAccountId(account.id)
              .addTokenTagsField("test", test));
    }

    txBuilder.transact(client);

    Token.Page items =
        new Token.ListBuilder()
            .setFilter("tags.test=$1")
            .addFilterParameter(test)
            .getPage(client);

    assertEquals(10, items.items.size());

    Token token = items.items.get(0);
    assertEquals(100, token.amount);
  }

  public void testBalanceQuery() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String asset = UUID.randomUUID().toString();
    String alice = UUID.randomUUID().toString();
    String test = UUID.randomUUID().toString();
    long amount = 100;

    new Asset.Builder()
        .addKey(key)
        .setAlias(asset)
        .addTag("name", asset)
        .setQuorum(1)
        .create(client);

    Transaction.Builder txBuilder = new Transaction.Builder();

    for (int i = 0; i < 10; i++) {
      Account account =
          new Account.Builder().setId(alice + i).addKey(key).setQuorum(1).create(client);
      txBuilder.addAction(
          new Transaction.Builder.Action.Issue()
              .setAssetAlias(asset)
              .setAmount(amount)
              .setDestinationAccountId(account.id)
              .addReferenceDataField("test", test));
    }

    txBuilder.transact(client);

    Balance.Page items =
        new Balance.QueryBuilder()
            .setFilter("reference_data.test=$1")
            .addFilterParameter(test)
            .getPage(client);
    Balance bal = items.items.get(0);
    assertNotNull(bal.sumBy);
    assertNotNull(bal.sumBy.get("account_alias"));
    assertNotNull(bal.sumBy.get("account_id"));
    assertNotNull(bal.sumBy.get("asset_alias"));
    assertNotNull(bal.sumBy.get("asset_id"));
    assertEquals(10, items.items.size());
    assertEquals(100, bal.amount);
  }

  public void testContractQuery() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String asset = UUID.randomUUID().toString();
    String alice = UUID.randomUUID().toString();
    String test = UUID.randomUUID().toString();
    long amount = 100;

    new Asset.Builder()
        .addKey(key)
        .setAlias(asset)
        .addTag("name", asset)
        .setQuorum(1)
        .create(client);

    Transaction.Builder txBuilder = new Transaction.Builder();

    for (int i = 0; i < 10; i++) {
      Account account =
          new Account.Builder()
              .setId(alice + i)
              .addKey(key)
              .setQuorum(1)
              .addTag("test", test)
              .create(client);
      txBuilder.addAction(
          new Transaction.Builder.Action.Issue()
              .setAssetAlias(asset)
              .setAmount(amount)
              .setDestinationAccountId(account.id)
              .addReferenceDataField("test", test));
    }

    txBuilder.transact(client);

    Contract.Page items =
        new Contract.QueryBuilder()
            .setFilter("reference_data.test=$1")
            .setFilterParameters(Arrays.asList((Object) test))
            .getPage(client);
    Contract contract = items.items.get(0);
    assertNotNull(contract.id);
    assertNotNull(contract.type);
    assertNotNull(contract.transactionId);
    assertNotNull(contract.assetId);
    assertNotNull(contract.assetAlias);
    assertNotNull(contract.accountId);
    assertNotNull(contract.assetTags);
    assertNotNull(contract.accountTags);
    assertNotNull(contract.referenceData);
    assertEquals(100, contract.amount);
    assertEquals(10, items.items.size());
  }

  // Because BaseQueryBuilder#getPage is used in the execute
  // method and for pagination, testing pagination for one
  // api object is sufficient for exercising the code path.
  public void testPagination() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String tag = UUID.randomUUID().toString();
    for (int i = 0; i < PAGE_SIZE + 1; i++) {
      new Account.Builder().addKey(key).setQuorum(1).addTag("tag", tag).create(client);
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

    // Test page iterator
    Account.PageIterable pages =
        new Account.QueryBuilder()
            .setFilter("tags.tag=$1")
            .addFilterParameter(tag)
            .getPageIterable(client);

    counter = 0;
    Boolean checkedFirstPage = false;
    for (Account.Page p : pages) {
      assertNotNull(p.items.get(0).id);
      if (!checkedFirstPage) {
        assertEquals(PAGE_SIZE, p.items.size());
        checkedFirstPage = true;
      }
      counter = counter + p.items.size();
    }
    assertEquals(PAGE_SIZE + 1, counter);
  }
}
