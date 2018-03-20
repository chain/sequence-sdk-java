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

  public static class PageIterable extends BasePageIterable<Page> {
    public PageIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * A builder class for querying keys in the ledger.
   */
  public static class QueryBuilder extends BaseQueryBuilder<QueryBuilder> {
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

    /**
     * Executes the query, returning an iterable over pages of keys that match
     * the query.
     * @param client ledger API connection object
     * @return an iterable over pages of keys
     * @throws ChainException
     */
    public PageIterable getPageIterable(Client client) throws ChainException {
      return new PageIterable(client, "list-keys", this.next);
    }

    /**
     * Specifies a list of ids of keys to be queried.
     * @param ids a list of key ids
     * @return updated builder
     * @deprecated this method is not useful
     */
    @Deprecated
    public QueryBuilder setIds(List<String> ids) {
      this.next.ids = new ArrayList<>(ids);
      return this;
    }

    /**
     * Adds an id to the list of id of keys to be queried.
     * @param id a key id
     * @return updated builder
     * @deprecated this method is not useful
     */
    @Deprecated
    public QueryBuilder addId(String id) {
      this.next.ids.add(id);
      return this;
    }
  }

  /**
   * A composite key identifier, containing an ID.
   * @deprecated this class is no longer needed
   */
  @Deprecated
  public static class Handle {
    public String id;

    /**
     * Creates a new Handle from a Key.
     * @param k a key
     * @return a new handle based on the provided key
     */
    public static Handle fromKey(Key k) {
      Handle h = new Handle();

      if (k.id != null && !k.id.equals("")) {
        h.id = k.id;
      }

      return h;
    }
  }
}
