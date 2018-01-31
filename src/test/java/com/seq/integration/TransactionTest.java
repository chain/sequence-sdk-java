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

public class TransactionTest {
  static Client client;
  static Key key;
  static Key key2;
  static Key key3;

  @Test
  public void run() throws Exception {
    testBasicTransaction();
    testMultiSigTransaction();
    testContract();
  }

  public void testBasicTransaction() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String alice = "TransactionTest-testBasicTransaction-alice";
    String bob = "TransactionTest-testBasicTransaction-bob";
    String asset = "TransactionTest-testBasicTransaction-asset";
    String test = "TransactionTest-testBasicTransaction-test";

    new Account.Builder().setId(alice).addKey(key).setQuorum(1).create(client);
    new Account.Builder().setId(bob).addKey(key).setQuorum(1).create(client);
    new Asset.Builder().setAlias(asset).addKey(key).setQuorum(1).create(client);

    Transaction resp =
        new Transaction.Builder()
            .addAction(
                new Transaction.Builder.Action.Issue()
                    .setAssetAlias(asset)
                    .setAmount(100)
                    .setDestinationAccountId(alice)
                    .addReferenceDataField("test", test))
            .addReferenceDataField("test", test)
            .transact(client);
    Transaction.Page txs =
        new Transaction.QueryBuilder()
            .setFilter("id=$1")
            .addFilterParameter(resp.id)
            .getPage(client);
    Transaction tx = txs.items.get(0);
    assertEquals(1, txs.items.size());
    // TODO(jackson): bring back asserts about the
    // annotated transaction when the SDK knows about
    // actions & entries

    resp =
        new Transaction.Builder()
            .addAction(
                new Transaction.Builder.Action.Transfer()
                    .setSourceAccountId(alice)
                    .setAssetAlias(asset)
                    .setAmount(10)
                    .setDestinationAccountId(bob)
                    .addReferenceDataField("test", test))
            .addReferenceDataField("test", test)
            .transact(client);
    txs =
        new Transaction.QueryBuilder()
            .setFilter("id=$1")
            .addFilterParameter(resp.id)
            .getPage(client);
    tx = txs.items.get(0);
    assertEquals(1, txs.items.size());
    // TODO(jackson): bring back asserts about the
    // annotated transaction when the SDK knows about
    // actions & entries

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Retire()
                .setSourceAccountAlias(bob)
                .setAssetAlias(asset)
                .setAmount(5)
                .addReferenceDataField("test", test))
        .addReferenceDataField("test", test)
        .transact(client);
    txs =
        new Transaction.QueryBuilder()
            .setFilter("reference_data.test=$1")
            .addFilterParameter(test)
            .getPage(client);
    assertEquals(3, txs.items.size());
  }

  public void testMultiSigTransaction() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    key2 = new Key.Builder().create(client);
    key3 = new Key.Builder().create(client);
    String alice = "TransactionTest-testMultiSigTransaction-alice";
    String bob = "TransactionTest-testMultiSigTransaction-bob";
    String asset = "TransactionTest-testMultiSigTransaction-asset";

    new Account.Builder()
        .setId(alice)
        .addKey(key)
        .addKey(key2)
        .addKey(key3)
        .setQuorum(2)
        .create(client);
    new Account.Builder()
        .setId(bob)
        .addKey(key)
        .addKey(key2)
        .addKey(key3)
        .setQuorum(1)
        .create(client);
    new Asset.Builder()
        .setAlias(asset)
        .addKey(key)
        .addKey(key2)
        .addKey(key3)
        .setQuorum(1)
        .create(client);

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Issue()
                .setAssetAlias(asset)
                .setAmount(100)
                .setDestinationAccountId(alice))
        .transact(client);

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Transfer()
                .setSourceAccountId(alice)
                .setAssetAlias(asset)
                .setAmount(10)
                .setDestinationAccountId(bob))
        .transact(client);

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Retire()
                .setSourceAccountId(bob)
                .setAssetAlias(asset)
                .setAmount(5))
        .transact(client);
  }

  public void testContract() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String alice = "TransactionTest-testContract-alice";
    String bob = "TransactionTest-testContract-bob";
    String asset = "TransactionTest-testContract-asset";

    new Account.Builder().setId(alice).addKey(key).setQuorum(1).create(client);
    new Account.Builder().setId(bob).addKey(key).setQuorum(1).create(client);
    new Asset.Builder().setAlias(asset).addKey(key).setQuorum(1).create(client);

    Transaction resp =
        new Transaction.Builder()
            .addAction(
                new Transaction.Builder.Action.Issue()
                    .setAssetAlias(asset)
                    .setAmount(100)
                    .setDestinationAccountId(alice))
            .transact(client);

    Transaction.Page txs =
        new Transaction.QueryBuilder()
            .setFilter("id=$1")
            .addFilterParameter(resp.id)
            .getPage(client);
    Transaction tx = txs.items.get(0);

    Contract contract = tx.contracts.get(0);

    resp =
        new Transaction.Builder()
            .addAction(
                new Transaction.Builder.Action.Transfer()
                    .setSourceContractId(contract.id)
                    .setDestinationAccountId(bob)
                    .setAssetAlias(contract.assetAlias)
                    .setAmount(contract.amount))
            .transact(client);

    Contract.Page items =
        new Contract.QueryBuilder()
            .setFilter("transaction_id=$1")
            .addFilterParameter(resp.id)
            .getPage(client);
    Contract unspent = items.items.get(0);

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Retire()
                .setSourceContractId(unspent.id)
                .setAssetAlias(asset)
                .setAmount(unspent.amount))
        .transact(client);
  }
}
