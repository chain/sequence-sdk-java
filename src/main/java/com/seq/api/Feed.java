package com.seq.api;

import com.seq.exception.*;
import com.seq.http.*;
import com.google.gson.annotations.SerializedName;

import java.util.*;

public class Feed<T> {
  /**
   * Unique identifier of the feed.
   */
  public String id;

  /**
   * Type of feed, "action" or "transaction".
   */
  public String type;

  /**
   * The query filter used in /stream-feed-items.
   */
  public String filter;

  /**
   * A list of parameters to be interpolated into the filter expression.
   */
  @SerializedName("filter_params")
  public List<Object> filterParams;

  /**
   * Indicates the last transaction consumed by this feed.
   */
  public String cursor;

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
    private String id;
    protected String type;
    private String filter;

    @SerializedName("filter_params")
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
      return client.request("create-feed", this, Feed.class);
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
    if (feed.type != type) {
        throw new ChainException("Feed " + id + " is a " + feed.type + " feed, not "+ type);
    }
    return feed;
  }
}
