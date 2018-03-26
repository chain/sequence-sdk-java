package com.seq.api;

import com.seq.exception.*;
import com.seq.http.Client;

import java.net.MalformedURLException;
import java.util.*;

/**
 * Keys are used to sign transactions.
 */
public class Key {
  /**
   * Unique identifier of the key.
   */
  public String id;

  /**
   * Configuration object for creating keys.
   */
  public static class Builder {
    private String id;

    /**
     * Specifies the id for the new key.
     * @param id unique identifier. Will be auto-generated if not provided.
     * @return the updated builder
     */
    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    /**
     * Creates a key.
     * @param client ledger API connection object
     * @return a key object
     * @throws ChainException
     */
    public Key create(Client client) throws ChainException {
      return client.request("create-key", this, Key.class);
    }
  }

  public static class Page extends BasePage<Key> {}

  public static class ItemIterable extends BaseItemIterable<Key> {
    public ItemIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * A builder class for listing keys in the ledger.
   */
  public static class ListBuilder extends BaseQueryBuilder<ListBuilder> {
    /**
     * Executes the query, returning a page of keys that match the query.
     * @param client ledger API connection object
     * @return a page of keys
     * @throws ChainException
     */
    public Page getPage(Client client) throws ChainException {
      return client.request("list-keys", this.next, Page.class);
    }

    /**
     * Executes the query, returning a page of keys that match the query
     * beginning with provided cursor.
     * @param client ledger API connection object
     * @param cursor string representing encoded query object
     * @return a page of keys
     * @throws ChainException
     */
    public Page getPage(Client client, String cursor) throws ChainException {
      Query next = new Query();
      next.cursor = cursor;
      return client.request("list-keys", next, Page.class);
    }

    /**
     * Executes the query, returning an iterable over keys that match the query.
     * @param client ledger API connection object
     * @return an iterable over keys
     * @throws ChainException
     */
    public ItemIterable getIterable(Client client) throws ChainException {
      return new ItemIterable(client, "list-keys", this.next);
    }
  }
}
