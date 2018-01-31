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
        new Account.Builder().addKey(key).setQuorum(1).addTag("x", "zero").create(client);
    Account account2 =
        new Account.Builder().addKey(key).setQuorum(1).addTag("y", "zero").create(client);

    Map<String, Object> update1, update2;

    // Account tag update

    update1 = new HashMap<>();
    update1.put("x", "one");

    new Account.TagUpdateBuilder().forId(account1.id).setTags(update1).update(client);

    Account.ItemIterable accounts =
        new Account.QueryBuilder()
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
      assertTrue(e.toString().contains("CH051"));
    }
  }

  @Test
  public void assetTags() throws Exception {
    Client client = TestUtils.generateClient();
    Key key = new Key.Builder().create(client);

    Asset asset1 = new Asset.Builder().addKey(key).setQuorum(1).addTag("x", "zero").create(client);
    Asset asset2 = new Asset.Builder().addKey(key).setQuorum(1).addTag("y", "zero").create(client);

    Map<String, Object> update1, update2;

    // Asset tag update

    update1 = new HashMap<>();
    update1.put("x", "one");

    new Asset.TagUpdateBuilder().forId(asset1.id).setTags(update1).update(client);

    Asset.ItemIterable assets =
        new Asset.QueryBuilder()
            .setFilter("id=$1")
            .addFilterParameter(asset1.id)
            .getIterable(client);

    for (Asset asset : assets) {
      assertEquals(asset.tags.get("x"), "one");
    }

    // Asset tag update that raises an error

    try {
      update1 = new HashMap<>();
      update1.put("x", "two");

      new Asset.TagUpdateBuilder()
          // ID intentionally omitted
          .setTags(update1)
          .update(client);
    } catch (APIException e) {
      assertTrue(e.toString().contains("CH051"));
    }
  }
}
