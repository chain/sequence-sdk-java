package com.seq.api;

import com.seq.exception.*;
import com.seq.http.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

import java.util.*;

public class Feed<T> implements Iterable<T> {
  /**
   * Unique identifier of the feed.
   */
  @Expose
  public String id;

  /**
   * Type of feed, "action" or "transaction".
   */
  @Expose
  public String type;

  /**
   * The query filter used in /stream-feed-items.
   */
  @Expose
  public String filter;

  /**
   * A list of parameters to be interpolated into the filter expression.
   */
  @SerializedName("filter_params")
  @Expose
  public List<Object> filterParams;

  /**
   * Indicates the last transaction consumed by this feed.
   */
  @Expose
  public String cursor;

  private Client _client;

  private T latestItem;
  private String latestCursor;

  class IterablePage<S> {
    @Expose
    public List<S> items;

    @Expose
    public List<String> cursors;

    public IterablePage() {
      this.items = new ArrayList<>();
      this.cursors = new ArrayList<>();
    }
  }

  class ActionPage extends IterablePage<com.seq.api.Action> {}
  class TransactionPage extends IterablePage<com.seq.api.Transaction> {}

  public Iterator<T> iterator() {
    return new Iterator<T>() {
      private int pos = 0;
      private List<T> items = new ArrayList<>();
      private List<String> cursors = new ArrayList<>();

      private IterablePage getPage() throws ChainException {
        Map<String, Object> req = new HashMap<>();
        req.put("id", id);

        if (type.equals("action")) {
          return _client.request("stream-feed-items", req, ActionPage.class);
        } else {
          return _client.request("stream-feed-items", req, TransactionPage.class);
        }
      }

      /**
       * Returns the next item in the results items.
       * @return api object of type T
       */
      public T next() {
        latestItem = items.get(pos);
        latestCursor = cursors.get(pos);
        pos++;
        return latestItem;
      }

      /**
       * Returns true if there is another item in the results items.
       * @return boolean
       */
      public boolean hasNext() {
        if (pos < items.size()) {
          return true;
        } else {
          boolean fetched = false;
          while (!fetched) {
            try {
              IterablePage page = getPage();
              fetched = true;

              this.pos = 0;
              this.items = page.items;
              this.cursors = page.cursors;
            } catch (ChainException e) {
              // Error fetching a new connection. Try again.
            }
          }
        }

        return true;
      }

      /**
       * This method is unsupported.
       * @throws UnsupportedOperationException
       */
      public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
      }
    };
  }

  public void ack() throws ChainException {
    Map<String, Object> req = new HashMap<>();
    req.put("id", id);
    req.put("cursor", latestCursor);
    req.put("previous_cursor", cursor);
    _client.request("ack-feed", req, Feed.class);
    cursor = latestCursor;
  }

  public void delete() throws ChainException {
    Map<String, Object> req = new HashMap<>();
    req.put("id", id);
    _client.request("delete-feed", req, Feed.class);
  }

  public static class Action {
    public static class Builder extends Feed.Builder<com.seq.api.Action> {
      public Builder() {
        super("action");
      }
    }

    /**
     * Retrieves an individual action feed.
     *
     * @param id the feed id
     * @param client ledger API connection object
     * @return a feed object
     * @throws ChainException
     */
    public static Feed<com.seq.api.Action> get(String id, Client client) throws ChainException {
      return Feed.<com.seq.api.Action>get(id, "action", client);
    }
  }

  public static class Transaction {
    public static class Builder extends Feed.Builder<com.seq.api.Transaction> {
      public Builder() {
        super("transaction");
      }
    }

    /**
     * Retrieves an individual transaction feed.
     *
     * @param id the feed id
     * @param client ledger API connection object
     * @return a feed object
     * @throws ChainException
     */
    public static Feed<com.seq.api.Transaction> get(String id, Client client) throws ChainException {
      return Feed.<com.seq.api.Transaction>get(id, "transaction", client);
    }
  }

  abstract public static class Builder<T> {
    @Expose
    private String id;

    @Expose
    protected String type;

    @Expose
    private String filter;

    @SerializedName("filter_params")
    @Expose
    private List<Object> filterParams;

    private Builder(String type) {
      this.type = type;
      this.filterParams = new ArrayList<>();
    };

    /**
     * Creates a new feed for the ledger.
     * @param client ledger API connection object
     * @return a feed
     * @throws ChainException
     */
    public Feed<T> create(Client client) throws ChainException {
      Feed<T> feed = client.request("create-feed", this, Feed.class);
      feed._client = client;
      return feed;
    }

    /**
     * Specifies the id for the new feed.
     * @param id unique identifier. Will be auto-generated if not provided.
     * @return updated builder
     */
    public Builder<T> setId(String id) {
      this.id = id;
      return this;
    }

    /**
     * Sets the filter expression for the feed.
     * @param filter a filter expression
     * @return updated builder
     */
    public Builder<T> setFilter(String filter) {
      this.filter = filter;
      return this;
    }

    /**
     * Adds a filter parameter that will be interpolated into the filter expression.
     * @param param a filter parameter
     * @return updated builder
     */
    public Builder<T> addFilterParameter(Object param) {
      this.filterParams.add(param);
      return this;
    }

    /**
     * Specifies the parameters that will be interpolated into the filter expression.
     * @param params list of filter parameters
     */
    public Builder<T> setFilterParameters(List<?> params) {
      this.filterParams = new ArrayList<>(params);
      return this;
    }
  }

  /**
   * Retrieves an individual feed.
   *
   * @param id the feed id
   * @param client ledger API connection object
   * @return a feed object
   * @throws ChainException
   */
  private static <T> Feed<T> get(String id, String type, Client client) throws ChainException {
    Map<String, Object> req = new HashMap<>();
    req.put("id", id);
    Feed<T> feed = client.request("get-feed", req, Feed.class);
    feed._client = client;
    if (!feed.type.equals(type)) {
        throw new ChainException("Feed " + id + " is a " + feed.type + " feed, not "+ type);
    }
    return feed;
  }

  public static class Page extends BasePage<Feed> {}

  public static class ItemIterable extends BaseItemIterable<Feed> {
    public ItemIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * A builder class for listing feeds in the ledger.
   */
  public static class ListBuilder extends BaseQueryBuilder<ListBuilder> {
    /**
     * Executes the query, returning a page of feeds that match the query.
     * @param client ledger API connection object
     * @return a page of feeds
     * @throws ChainException
     */
    public Page getPage(Client client) throws ChainException {
      return client.request("list-feeds", this.next, Page.class);
    }

    /**
     * Executes the query, returning a page of feeds that match the query
     * beginning with provided cursor.
     * @param client ledger API connection object
     * @param cursor string representing encoded query object
     * @return a page of feeds
     * @throws ChainException
     */
    public Page getPage(Client client, String cursor) throws ChainException {
      Query next = new Query();
      next.cursor = cursor;
      return client.request("list-feeds", next, Page.class);
    }

    /**
     * Executes the query, returning an iterable over feeds that match the query.
     * @param client ledger API connection object
     * @return an iterable over feeds
     * @throws ChainException
     */
    public ItemIterable getIterable(Client client) throws ChainException {
      return new ItemIterable(client, "list-feeds", this.next);
    }
  }
}
