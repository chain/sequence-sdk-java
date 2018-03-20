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
import java.text.*;

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

    String oldTime =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .format(new Date(System.currentTimeMillis() - 100000000000L));

    new Key.Builder().setId(key).create(ledger);
    new Flavor.Builder().setId(usd).addKeyId(key).create(ledger);
    new Account.Builder().setId(alice).addKeyId(key).create(ledger);
    new Account.Builder().setId(bob).addKeyId(key).create(ledger);

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Issue()
                .setFlavorId(usd)
                .setAmount(100)
                .setDestinationAccountId(alice))
        .transact(ledger);

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Transfer()
                .setFlavorId(usd)
                .setAmount(50)
                .setSourceAccountId(alice)
                .setDestinationAccountId(bob))
        .transact(ledger);

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Transfer()
                .setFlavorId(usd)
                .setAmount(50)
                .setSourceAccountId(alice)
                .setDestinationAccountId(bob))
        .transact(ledger);

    new Transaction.Builder()
        .addAction(
            new Transaction.Builder.Action.Retire()
                .setFlavorId(usd)
                .setAmount(10)
                .setSourceAccountId(bob))
        .transact(ledger);

    String newTime =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .format(new Date(System.currentTimeMillis() - 100000000000L));

    ActionSum.ItemIterable sums =
      new Action.SumBuilder()
          .setFilter("type = $1 AND flavorTags.type = $2 AND timestamp >= $3 AND timestamp =< $4")
          .addFilterParameter("issue")
          .addFilterParameter("currency")
          .addFilterParameter(oldTime)
          .addFilterParameter(newTime)
          .setGroupBy(
              new ArrayList<String>() {
                {
                  add("flavorTags.type");
                }
              })
          .getIterable(ledger);
    for (ActionSum sum : sums) {
      System.out.printf("currency: %d\n", sum.flavorId);
      System.out.printf("amount issued: %d\n", sum.amount);
    }
  }
}
