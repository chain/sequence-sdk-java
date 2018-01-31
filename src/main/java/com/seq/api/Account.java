package com.seq.api;

import com.seq.exception.*;
import com.seq.http.*;
import com.google.gson.annotations.SerializedName;

import java.util.*;

/**
 * A container for asset balances on a ledger.
 */
public class Account {
  /**
   * Unique identifier of the account.
   */
  public String id;

  /**
   * Unique, user-specified identifier.
   * @deprecated use {@link #id} instead
   */
  @Deprecated
  public String alias;

  /**
   * The set of keys used for signing transactions that spend from the account.
   */
  public List<Key.Handle> keys;

  /**
   * The number of keys required to sign transactions that spend from the account.
   */
  public int quorum;

  /**
   * User-specified key-value data describing the account.
   */
  public Map<String, Object> tags;

  /**
   * A single page of accounts returned from a query.
   */
  public static class Page extends BasePage<Account> {}

  /**
   * Iterable interface for consuming individual accounts from a query.
   */
  public static class ItemIterable extends BaseItemIterable<Account> {
    public ItemIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * Iterable interface for consuming pages of accounts from a query.
   */
  public static class PageIterable extends BasePageIterable<Page> {
    public PageIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * A builder class for querying accounts in the ledger.
   */
  public static class QueryBuilder extends BaseQueryBuilder<QueryBuilder> {
    /**
     * Executes the query, returning a page of accounts that match the query.
     * @param client ledger API connection object
     * @return a page of accounts
     * @throws ChainException
     */
    public Page getPage(Client client) throws ChainException {
      return client.request("list-accounts", this.next, Page.class);
    }

    /**
     * Executes the query, returning an iterable over accounts that match the query.
     * @param client ledger API connection object
     * @return an iterable over accounts
     * @throws ChainException
     */
    public ItemIterable getIterable(Client client) throws ChainException {
      return new ItemIterable(client, "list-accounts", this.next);
    }

    /**
     * Executes the query, returning an iterable over pages of accounts that match the query.
     * @param client ledger API connection object
     * @return an iterable over pages of accounts
     * @throws ChainException
     */
    public PageIterable getPageIterable(Client client) throws ChainException {
      return new PageIterable(client, "list-accounts", this.next);
    }
  }

  /**
   * A builder for creating accounts in the ledger.
   */
  public static class Builder {
    private String id;
    private String alias;
    private Integer quorum;

    @SerializedName("keys")
    private List<Key.Handle> keys;

    private Map<String, Object> tags;

    public Builder() {
      this.keys = new ArrayList<>();
    }

    /**
     * Creates a new account in the ledger.
     * @param client ledger API connection object
     * @return an account
     * @throws ChainException
     */
    public Account create(Client client) throws ChainException {
      return client.request("create-account", this, Account.class);
    }

    /**
     * Specifies the id for the new account.
     * @param id unique identifier. Will be auto-generated if not provided.
     * @return updated builder
     */
    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    /**
     * Specifies the alias for the new account.
     * @param alias a unique, user-specified identifier
     * @return updated builder
     * @deprecated use {@link #setId(String)} instead.
     */
    @Deprecated
    public Builder setAlias(String alias) {
      this.alias = alias;
      return this;
    }

    /**
     * Specifies the number of keys required to sign transactions that spend
     * from the account. Defaults to the number of keys provided.
     * @param quorum a number less than or equal to the number of keys
     * @return updated builder
     */
    public Builder setQuorum(int quorum) {
      this.quorum = new Integer(quorum);
      return this;
    }

    /**
     * Adds a key that can be used to sign transactions that spend from the account.
     * @param k a key
     * @return updated builder
     */
    public Builder addKey(Key k) {
      keys.add(Key.Handle.fromKey(k));
      return this;
    }

    /**
     * Adds a key that can be used to sign transactions that spend from the account.
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
     * Adds a key that can be used to sign transactions that spend from the account.
     * @param alias the key's alias
     * @return updated builder
     * @deprecated use {@link #addKeyById(String)} instead
     */
    @Deprecated
    public Builder addKeyByAlias(String alias) {
      Key.Handle h = new Key.Handle();
      h.alias = alias;
      this.keys.add(h);
      return this;
    }

    /**
     * Adds a key-value pair to the account's tags.
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
     * Specifies key-value data that describes the account.
     * @param tags map of tag keys to tag values
     * @return updated builder
     */
    public Builder setTags(Map<String, Object> tags) {
      this.tags = tags;
      return this;
    }
  }

  /**
   * A builder for updating an account's tags.
   */
  public static class TagUpdateBuilder {
    private String alias;
    private String id;
    private Map<String, Object> tags;

    /**
     * Specifies the account to be updated.
     * @param id the account's ID
     * @return updated builder
     */
    public TagUpdateBuilder forId(String id) {
      this.id = id;
      return this;
    }

    /**
     * Specifies the account to be updated.
     * @param alias the account's alias
     * @return updated builder
     * @deprecated use {@link #forId(String)} instead
     */
    @Deprecated
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
     * Updates the account's tags.
     * @param client ledger API connection object
     * @throws ChainException
     */
    public void update(Client client) throws ChainException {
      client.request("update-account-tags", this, SuccessMessage.class);
    }
  }
}
