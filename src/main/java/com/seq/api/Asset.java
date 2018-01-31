package com.seq.api;

import com.seq.exception.*;
import com.seq.http.*;
import com.google.gson.annotations.SerializedName;

import java.util.*;

/**
 * A type or class of value that can be tracked on a ledger.
 */
public class Asset {
  /**
   * Unique identifier of the asset, derived from its keys.
   */
  public String id;

  /**
   * Unique, user-specified identifier.
   */
  public String alias;

  /**
   * The set of keys used to sign transactions that issue the asset.
   */
  public List<Key.Handle> keys;

  /**
   * The number of keys required to sign transactions that issue the asset.
   */
  public int quorum;

  /**
   * User-specified key-value data describing the asset.
   */
  public Map<String, Object> tags;

  /**
   * A single page of assets returned from a query.
   */
  public static class Page extends BasePage<Asset> {}

  /**
   * Iterable interface for consuming individual assets from a query.
   */
  public static class ItemIterable extends BaseItemIterable<Asset> {
    public ItemIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * Iterable interface for consuming pages of assets from a query.
   */
  public static class PageIterable extends BasePageIterable<Page> {
    public PageIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * A builder class for querying assets in the ledger.
   */
  public static class QueryBuilder extends BaseQueryBuilder<QueryBuilder> {
    /**
     * Executes the query, returning a page of assets that match the query.
     * @param client ledger API connection object
     * @return a page of assets
     * @throws ChainException
     */
    public Page getPage(Client client) throws ChainException {
      return client.request("list-assets", this.next, Page.class);
    }

    /**
     * Executes the query, returning an iterable over assets that match the query
     * @param client ledger API connection object
     * @return an iterable over assets
     * @throws ChainException
     */
    public ItemIterable getIterable(Client client) throws ChainException {
      return new ItemIterable(client, "list-assets", this.next);
    }

    /**
     * Executes the query, returning an iterable over pages of assets that match the query
     * @param client ledger API connection object
     * @return an iterable over pages of assets
     * @throws ChainException
     */
    public PageIterable getPageIterable(Client client) throws ChainException {
      return new PageIterable(client, "list-assets", this.next);
    }
  }

  /**
   * A builder for defining assets in the ledger.
   */
  public static class Builder {
    private String alias;
    private Map<String, Object> tags;

    @SerializedName("keys")
    private List<Key.Handle> keys;

    private Integer quorum;

    public Builder() {
      this.keys = new ArrayList<>();
    }

    /**
     * Creates a new asset in the ledger.
     * @param client ledger API connection object
     * @return an asset
     * @throws ChainException
     */
    public Asset create(Client client) throws ChainException {
      return client.request("create-asset", this, Asset.class);
    }

    /**
     * Specifies the alias for the new asset.
     * @param alias a unique, user-specified identifier
     * @return updated builder
     */
    public Builder setAlias(String alias) {
      this.alias = alias;
      return this;
    }

    /**
     * Adds a key-value pair to the asset's tags.
     * @param key key of the tag
     * @param value value of the tag
     * @return updated builder
     */
    public Builder addTag(String key, Object value) {
      if (this.tags == null) {
        this.tags = new HashMap<>();
      }
      this.tags.put(key, value);
      return this;
    }

    /**
     * Specifies key-value data that describes the asset.
     * @param tags map of tag keys to tag values
     * @return updated builder
     */
    public Builder setTags(Map<String, Object> tags) {
      this.tags = tags;
      return this;
    }

    /**
     * Specifies the number of keys required to sign transactions that issue the
     * asset. Defaults to the number of keys provided.
     * @param quorum a number less than or equal to the number of keys
     * @return updated builder
     */
    public Builder setQuorum(int quorum) {
      this.quorum = new Integer(quorum);
      return this;
    }

    /**
     * Adds a key that can be used to sign transactions that issue the asset.
     * @param k a key
     * @return updated builder
     */
    public Builder addKey(Key k) {
      keys.add(Key.Handle.fromKey(k));
      return this;
    }

    /**
     * Adds a key that can be used to sign transactions that issue the asset.
     * @param id the key's ID
     * @return updated builder
     */
    public Builder addKeyById(String id) {
      Key.Handle h = new Key.Handle();
      h.id = id;
      this.keys.add(h);
      return this;
    }

    /**
     * Adds a key that can be used to sign transactions that issue the asset.
     * @param alias the key's alias
     * @return updated builder
     */
    public Builder addKeyByAlias(String alias) {
      Key.Handle h = new Key.Handle();
      h.alias = alias;
      this.keys.add(h);
      return this;
    }
  }

  /**
   * A builder for updating an asset's tags.
   */
  public static class TagUpdateBuilder {
    private String alias;
    private String id;
    private Map<String, Object> tags;

    /**
     * Specifies the asset to be updated.
     * @param id the asset's ID
     * @return updated builder
     */
    public TagUpdateBuilder forId(String id) {
      this.id = id;
      return this;
    }

    /**
     * Specifies the asset to be updated.
     * @param alias the asset's alias
     * @return updated builder
     */
    public TagUpdateBuilder forAlias(String alias) {
      this.alias = alias;
      return this;
    }

    /**
     * Specifies a new set of tags.
     * @param tags map of tag keys to tag values
     * @return updated builder
     */
    public TagUpdateBuilder setTags(Map<String, Object> tags) {
      this.tags = tags;
      return this;
    }

    /**
     * Updates the asset's tags.
     * @param client ledger API connection object
     * @throws ChainException
     */
    public void update(Client client) throws ChainException {
      client.request("update-asset-tags", this, SuccessMessage.class);
    }
  }
}
