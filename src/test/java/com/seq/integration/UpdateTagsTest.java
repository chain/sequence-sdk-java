package com.seq.integration;

import com.seq.TestUtils;
import com.seq.api.*;
import com.seq.exception.*;
import com.seq.http.*;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class UpdateTagsTest {
  @Test
  public void accountTags() throws Exception {
    Client client = TestUtils.generateClient();
    Key key = new Key.Builder().create(client);

    Account account1 =
      new Account.Builder()
        .addKeyId(key.id)
        .setQuorum(1)
        .addTag("x", "zero")
        .create(client);
    Account account2 =
      new Account.Builder()
        .addKeyId(key.id)
        .setQuorum(1)
        .addTag("y", "zero")
        .create(client);

    Map<String, Object> update1, update2;

    // Account tag update

    update1 = new HashMap<>();
    update1.put("x", "one");

    new Account.TagUpdateBuilder().forId(account1.id).setTags(update1).update(client);

    Account.ItemIterable accounts =
        new Account.ListBuilder()
            .setFilter("id=$1")
            .addFilterParameter(account1.id)
            .getIterable(client);

    for (Account account : accounts) {
      assertEquals(account.tags.get("x"), "one");
    }

    // Account tag update that raises an error

    try {
      update1 = new HashMap<>();
      update1.put("x", "two");

      new Account.TagUpdateBuilder()
          // ID intentionally omitted
          .setTags(update1)
          .update(client);
    } catch (APIException e) {
      assertEquals(e.seqCode, "SEQ008");
    }
  }

  @Test
  public void flavorTags() throws Exception {
    Client client = TestUtils.generateClient();
    Key key = new Key.Builder().create(client);

    Flavor flavor1 = new Flavor.Builder()
      .addKeyId(key.id)
      .setQuorum(1)
      .addTag("x", "zero")
      .create(client);
    Flavor flavor2 = new Flavor.Builder()
      .addKeyId(key.id)
      .setQuorum(1)
      .addTag("y", "zero")
      .create(client);

    Map<String, Object> update1, update2;

    // Flavor tag update

    update1 = new HashMap<>();
    update1.put("x", "one");

    new Flavor.TagUpdateBuilder().forId(flavor1.id).setTags(update1).update(client);

    Flavor.ItemIterable flavors =
        new Flavor.ListBuilder()
            .setFilter("id=$1")
            .addFilterParameter(flavor1.id)
            .getIterable(client);

    for (Flavor flavor : flavors) {
      assertEquals(flavor.tags.get("x"), "one");
    }

    // Flavor tag update that raises an error

    try {
      update1 = new HashMap<>();
      update1.put("x", "two");

      new Flavor.TagUpdateBuilder()
          // ID intentionally omitted
          .setTags(update1)
          .update(client);
    } catch (APIException e) {
      assertEquals(e.seqCode, "SEQ008");
    }
  }
}
