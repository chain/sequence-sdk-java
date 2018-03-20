package com.seq.integration;

import com.seq.TestUtils;
import com.seq.api.*;
import com.seq.exception.APIException;
import com.seq.http.Client;
import com.seq.common.Utils;

import org.junit.Test;

import java.util.*;
import java.text.*;

import static org.junit.Assert.*;

public class CreateTest {
  static Client client;
  static Key key;

  @Test
  public void run() throws Exception {
    testKeyCreate();
    testAccountCreate();
    testFlavorCreate();
  }

  public void testKeyCreate() throws Exception {
    client = TestUtils.generateClient();
    String id = "CreateTest-testKeyCreate-id";
    key = new Key.Builder().setId(id).create(client);
    assertNotNull(key.id);
    assertEquals(id, key.id);

    try {
      new Key.Builder().setId(id).create(client);
    } catch (APIException e) {
      return;
    }
    throw new Exception("expecting APIException");
  }

  public void testAccountCreate() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String alice = "CreateTest-testAccountCreate-alice";
    String test = "CreateTest-testAccountCreate-test";
    Map<String, Object> tags = new HashMap<>();
    tags.put("name", alice);
    Account account =
      new Account.Builder()
        .setId(alice)
        .addKeyId(key.id)
        .setQuorum(1)
        .setTags(tags)
        .addTag("test", test)
        .create(client);
    assertNotNull(account.id);
    assertEquals(alice, account.id);
    assertEquals(1, account.quorum);
    assertEquals(alice, account.tags.get("name"));
    assertEquals(test, account.tags.get("test"));
    assertEquals(key.id, account.keyIds.get(0));

    try {
      new Account.Builder()
        .setId(alice)
        .addKeyId(key.id)
        .setQuorum(1)
        .addTag("name", alice)
        .create(client);
    } catch (APIException e) {
      return;
    }
    throw new Exception("expecting APIException");
  }

  public void testFlavorCreate() throws Exception {
    client = TestUtils.generateClient();
    key = new Key.Builder().create(client);
    String flavor = "CreateTest-testFlavorCreate-flavor";
    String test = "CreateTest-testFlavorCreate-test";
    Map<String, Object> tags = new HashMap<>();
    tags.put("name", flavor);
    Flavor testFlavor =
      new Flavor.Builder()
        .setId(flavor)
        .addKeyId(key.id)
        .setQuorum(1)
        .setTags(tags)
        .addTag("test", test)
        .create(client);
    assertEquals(flavor, testFlavor.id);
    assertEquals(1, testFlavor.quorum);
    assertEquals(flavor, testFlavor.tags.get("name"));
    assertEquals(test, testFlavor.tags.get("test"));
    assertEquals(key.id, testFlavor.keyIds.get(0));

    try {
      new Flavor.Builder()
        .setId(flavor)
        .addKeyId(key.id)
        .setQuorum(1)
        .addTag("name", flavor)
        .create(client);
    } catch (APIException e) {
      return;
    }
    throw new Exception("expecting APIException");
  }
}
