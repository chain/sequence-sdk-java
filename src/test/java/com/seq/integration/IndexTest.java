package com.seq.integration;

import com.seq.TestUtils;
import com.seq.api.*;
import com.seq.exception.APIException;
import com.seq.http.Client;

import org.junit.Test;

import java.util.*;
import java.text.*;

import static org.junit.Assert.*;

public class IndexTest {
  static Client client;
  static Key key;

  @Test
  public void testActionIndexCreate() throws Exception {
    client = TestUtils.generateClient();
    DevUtils.reset(client);
    String test = "IndexTest-testActionIndexCreate-test";
    Index<Action> index =
      new Index.Action.Builder()
        .setId(test)
        .setFilter("tags.type=$1")
        .create(client);
    assertNotNull(index.id);
    assertEquals(test, index.id);

    try {
      new Index.Action.Builder()
        .setId(test)
        .setFilter("tags.type=$1")
        .create(client);
    } catch (APIException e) {
      return;
    }
    throw new Exception("expecting APIException");
  }

  @Test
  public void testTokenIndexCreate() throws Exception {
    client = TestUtils.generateClient();
    DevUtils.reset(client);
    String test = "IndexTest-testTokenIndexCreate-test";
    ArrayList<String> groupBy = new ArrayList<String>();
    groupBy.add("flavorId");
    groupBy.add("accountId");

    Index<Token> index =
      new Index.Token.Builder()
        .setId(test)
        .setFilter("tags.type=$1")
      	.setGroupBy(groupBy)
        .create(client);
    assertNotNull(index.id);
    assertEquals(test, index.id);

    try {
      new Index.Token.Builder()
        .setId(test)
        .setFilter("tags.type=$1")
        .addGroupByField("flavorId")
        .create(client);
    } catch (APIException e) {
      return;
    }
    throw new Exception("expecting APIException");
  }

  @Test
  public void testIndexPageCursor() throws Exception {
    client = TestUtils.generateClient();
    DevUtils.reset(client);
    String testAction = "IndexTest-testTokenIndexList-testAction";
    String testToken = "IndexTest-testTokenIndexList-testToken";
    Index<Action> actionIndex =
      new Index.Action.Builder()
        .setId(testAction)
        .setFilter("tags.type=$1")
        .create(client);

    Index<Token> tokenIndex =
      new Index.Token.Builder()
        .setId(testToken)
        .setFilter("tags.type=$1")
      	.addGroupByField("flavorId")
        .create(client);

    Index.Page indexes = new Index.ListBuilder()
      .setPageSize(1)
      .getPage(client);

    assertEquals(indexes.items.get(0).id, tokenIndex.id);

    indexes = new Index.ListBuilder()
      .getPage(client, indexes.cursor);

    assertEquals(indexes.items.get(0).id, actionIndex.id);
    assertEquals(indexes.lastPage, false);
  }

  @Test
  public void testIndexDeletion() throws Exception {
    client = TestUtils.generateClient();
    DevUtils.reset(client);
    String test = "IndexTest-testTokenIndexDelete-test";
    ArrayList<String> groupBy = new ArrayList<String>();
    groupBy.add("accountId");

    Index<Token> index =
      new Index.Token.Builder()
        .setId(test)
        .setFilter("tags.type=$1")
        .setGroupBy(groupBy)
        .addGroupByField("flavorId")
        .create(client);

    Index.ItemIterable items =
      new Index.ListBuilder()
        .getIterable(client);

    int counter = 0;
    for (Index a : items) {
      counter++;
    }
    assertEquals(1, counter);

    Index.delete(index.id, client);

    items = new Index.ListBuilder()
        .getIterable(client);

    counter = 0;
    for (Index a : items) {
      counter++;
    }
    assertEquals(0, counter);
  }
}
