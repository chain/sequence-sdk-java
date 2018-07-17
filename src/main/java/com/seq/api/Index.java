package com.seq.api;

import com.seq.exception.*;
import com.seq.http.Client;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.net.MalformedURLException;
import java.util.*;

/**
 * Indexes are used to pre-compute queries that could potentially be slow.
 */
public class Index<T> {
  /**
   * Unique identifier of the index.
   */
  @Expose
  public String id;

  /**
   * Type of index, "action" or "token".
   */
  @Expose
  public String type;

  /**
   * Token/Action object fields to group by.
   */
  @SerializedName("group_by")
  @Expose
  public List<String> groupBy;

  /**
   * The query filter used to select matching items.
   */
  @Expose
  public String filter;

  public static class Action {
    public static class Builder extends Index.Builder<com.seq.api.Action> {
      public Builder() {
        super("action");
      }
    }
  }

  public static class Token {
    public static class Builder extends Index.Builder<com.seq.api.Token> {
      public Builder() {
        super("token");
      }
    }
  }

  /**
   * Configuration object for creating indexes.
   */
  abstract public static class Builder<T> {
    @Expose
    private String id;

    @Expose
    private String type;

    @SerializedName("group_by")
    @Expose
    private List<String> groupBy;

    @Expose
    private String filter;

    private Builder(String type) {
      this.type = type;
    };

    /**
     * Specifies the id for the new index.
     * @param id unique identifier. Will be auto-generated if not provided.
     * @return the updated builder
     */
    public Builder<T> setId(String id) {
      this.id = id;
      return this;
    }

    /**
     * Sets the filter expression for the index.
     * @param filter a filter expression
     * @return updated builder
     */
    public Builder<T> setFilter(String filter) {
      this.filter = filter;
      return this;
    }

    /**
     * Specifies object fields to group by.
     * @param groupBy a list of fields
     * @return updated builder
     */
    public Builder<T> setGroupBy(List<String> groupBy) {
      this.groupBy = groupBy;
      return this;
    }

    /**
     * Adds a field on which objects group by.
     * @param field name of a field
     * @return updated builder
     */
    public Builder<T> addGroupByField(String field) {
      if (this.groupBy == null) {
        this.groupBy = new ArrayList<>();
      }
      this.groupBy.add(field);
      return this;
    }

    /**
     * Creates an index.
     * @param client ledger API connection object
     * @return a key object
     * @throws ChainException
     */
    public Index<T> create(Client client) throws ChainException {
      Index<T> index = client.request("create-index", this, Index.class);
      return index;
    }
  }

  /**
   * Deletes an index.
   *
   * @param id the feed id
   * @param client ledger API connection object
   * @throws ChainException
   */
  public static Index delete(String id, Client client) throws ChainException {
    Map<String, Object> req = new HashMap<>();
    req.put("id", id);
    client.request("delete-index", req, Index.class);
    return null;
  }

  public static class Page extends BasePage<Index> {}

  public static class ItemIterable extends BaseItemIterable<Index> {
    public ItemIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * A builder class for listing indexes in the ledger.
   */
  public static class ListBuilder extends BaseQueryBuilder<ListBuilder> {
    /**
     * Executes the query, returning a page of indexes that match the query.
     * @param client ledger API connection object
     * @return a page of indexes
     * @throws ChainException
     */
    public Page getPage(Client client) throws ChainException {
      return client.request("list-indexes", this.next, Page.class);
    }

    /**
     * Executes the query, returning a page of indexes that match the query
     * beginning with provided cursor.
     * @param client ledger API connection object
     * @param cursor string representing encoded query object
     * @return a page of indexes
     * @throws ChainException
     */
    public Page getPage(Client client, String cursor) throws ChainException {
      Query next = new Query();
      next.cursor = cursor;
      return client.request("list-indexes", next, Page.class);
    }

    /**
     * Executes the query, returning an iterable over indexes that match the query.
     * @param client ledger API connection object
     * @return an iterable over indexes
     * @throws ChainException
     */
    public ItemIterable getIterable(Client client) throws ChainException {
      return new ItemIterable(client, "list-indexes", this.next);
    }
  }
}
