/**
 * To run:
 *
 * cd $CHAIN/sdk/java
 * mvn package
 * cd examples
 * $CHAIN/bin/jrun FiveMinuteGuide.java
 */

import java.nio.file.*;
import java.util.*;

import com.seq.api.*;
import com.seq.http.*;

class FiveMinuteGuide {
  public static void main(String[] args) throws Exception {
    Client ledger =
        new Client.Builder()
            .setLedgerName("CHANGEME")
            .setCredential("CHANGEME")
            .build();

    String uuid = UUID.randomUUID().toString();
    String key = "key" + uuid;
    String usd = "usd" + uuid;
    String alice = "alice" + uuid;
    String bob = "bob" + uuid;

    new Key.Builder().setId(key).create(ledger);
    new Asset.Builder().setAlias(usd).addKeyById(key).create(ledger);
    new Account.Builder().setId(alice).addKeyById(key).create(ledger);
    new Account.Builder().setId(bob).addKeyById(key).create(ledger);

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Issue()
                .setAssetAlias(usd)
                .setAmount(100)
                .setDestinationAccountId(alice))
        .transact(ledger);

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Transfer()
                .setAssetAlias(usd)
                .setAmount(50)
                .setSourceAccountId(alice)
                .setDestinationAccountId(bob))
        .transact(ledger);

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Transfer()
                .setAssetAlias(usd)
                .setAmount(50)
                .setSourceAccountId(alice)
                .setDestinationAccountId(bob))
        .transact(ledger);

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Retire()
                .setAssetAlias(usd)
                .setAmount(10)
                .setSourceAccountId(bob))
        .transact(ledger);

    ActionSum.ItemIterable sums =
        new Action.SumBuilder()
            .setFilter("type = $1 AND asset_tags.type = $2 AND timestamp >= $3 AND timestamp =< $4")
            .addFilterParameter("issue")
            .addFilterParameter("currency")
            .addFilterParameter(t1)
            .addFilterParameter(t2)
            .setGroupBy(
                new ArrayList<String>() {
                  {
                    add("asset_tags.type");
                  }
                })
            .getIterable(ledger);
    for (ActionSum sum : sums) {
      System.out.printf("currency: %s\n", sum.assetAlias);
      System.out.printf("amount issued: %d\n", sum.amount);
    }
  }
}
