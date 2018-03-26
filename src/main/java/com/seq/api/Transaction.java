package com.seq.api;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.seq.exception.*;
import com.seq.http.*;

/**
 * A transaction is an atomic update to the state of the ledger. Transactions
 * can issue new flavor units, transfer flavor units from one account to
 * another, and/or retire flavor units from an account.
 */
public class Transaction {
  /**
   * A unique ID.
   */
  public String id;

  /**
   * Time of transaction.
   */
  public Date timestamp;

  /**
   * Sequence number of the transaction.
   */
  @SerializedName("sequence_number")
  public long sequenceNumber;

  /**
   * User-specified key-value data embedded into the transaction.
   * @deprecated use {@link Action#tags} instead
   */
  @SerializedName("reference_data")
  @Deprecated
  public Map<String, Object> referenceData;

  /**
   * List of actions taken by the transaction.
   */
  public List<Action> actions;

  /**
   * List of contracts created by the transaction.
   */
  public List<Contract> contracts;

  /**
   * A single page of transactions returned from a query.
   */
  public static class Page extends BasePage<Transaction> {}

  /**
   * Iterable interface for consuming individual transactions from a query.
   */
  public static class ItemIterable extends BaseItemIterable<Transaction> {
    public ItemIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * Iterable interface for consuming pages of transactions from a query.
   */
  @Deprecated
  public static class PageIterable extends BasePageIterable<Page> {
    public PageIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * A builder class for querying transactions in the ledger.
   * @deprecated Use {@link Transaction.ListBuilder} instead
   */
  @Deprecated
  public static class QueryBuilder extends BaseQueryBuilder<QueryBuilder> {
    /**
     * Executes the query, returning a page of transactions.
     * @param client ledger API connection object
     * @return a page of transactions
     * @throws ChainException
     */
    public Page getPage(Client client) throws ChainException {
      return client.request("list-transactions", this.next, Page.class);
    }

    /**
     * Executes the query, returning a page of transactions that match the query
     * beginning with provided cursor.
     * @param client ledger API connection object
     * @param cursor string representing encoded query object
     * @return a page of transactions
     * @throws ChainException
     */
    public Page getPage(Client client, String cursor) throws ChainException {
      Query next = new Query();
      next.cursor = cursor;
      return client.request("list-transactions", next, Page.class);
    }

    /**
     * Executes the query, returning an iterable over transactions that match
     * the query.
     * @param client ledger API connection object
     * @return an iterable over transactions
     * @throws ChainException
     */
    public ItemIterable getIterable(Client client) throws ChainException {
      return new ItemIterable(client, "list-transactions", this.next);
    }

    /**
     * Executes a query on the ledger's transactions, returning an iterable over
     * pages of transactoins that match the query.
     * @param client ledger API connection object
     * @return an iterable over pages of transactions
     * @throws ChainException
     * @deprecated use {@link Transaction.ListBuilder#getPage} instead
     */
    @Deprecated
    public PageIterable getPageIterable(Client client) throws ChainException {
      return new PageIterable(client, "list-transactions", this.next);
    }

    /**
     * Specifies the timestamp of the earliest transaction to include in the query results.
     * @param time unixtime in milliseconds
     * @return updated builder
     * @deprecated use "timestamp &gt;= $1" with {@link #setFilter} instead
     */
    @Deprecated
    public QueryBuilder setStartTime(long time) {
      this.next.startTime = time;
      return this;
    }

    /**
     * Specifies the timestamp of the most recent transaction to include in the query results.
     * @param time unixtime in milliseconds
     * @deprecated use "timestamp &lt;= $1" with {@link #setFilter} instead
     * @return updated builder
     */
    @Deprecated
    public QueryBuilder setEndTime(long time) {
      this.next.endTime = time;
      return this;
    }
  }

  /**
   * A builder class for listing transactions in the ledger.
   */
  public static class ListBuilder extends BaseQueryBuilder<ListBuilder> {
    /**
     * Executes the query, returning a page of transactions.
     * @param client ledger API connection object
     * @return a page of transactions
     * @throws ChainException
     */
    public Page getPage(Client client) throws ChainException {
      return client.request("list-transactions", this.next, Page.class);
    }

    /**
     * Executes the query, returning a page of transactions that match the query
     * beginning with provided cursor.
     * @param client ledger API connection object
     * @param cursor string representing encoded query object
     * @return a page of transactions
     * @throws ChainException
     */
    public Page getPage(Client client, String cursor) throws ChainException {
      Query next = new Query();
      next.cursor = cursor;
      return client.request("list-transactions", next, Page.class);
    }

    /**
     * Executes the query, returning an iterable over transactions that match
     * the query.
     * @param client ledger API connection object
     * @return an iterable over transactions
     * @throws ChainException
     */
    public ItemIterable getIterable(Client client) throws ChainException {
      return new ItemIterable(client, "list-transactions", this.next);
    }

    /**
     * Not implemented, throws an exception.
     * @param client ledger API connection object
     * @throws ChainException
     * @deprecated use {@link #getPage} instead
     */
    @Deprecated
    public PageIterable getPageIterable(Client client) throws ChainException {
      throw new ChainException("not implemented");
    }
  }

  /**
   * An action taken by a transaction.
   */
  public static class Action {
    /**
     * A unique ID.
     */
    public String id;
    
    /**
     * The type of the action. Possible values are "issue", "transfer" and "retire".
     */
    public String type;

    /**
     * The id of the action's flavor.
     */
    @SerializedName("flavor_id")
    public String flavorId;

    /**
     * A copy of the associated tags (flavor, source account, destination
     * account, action, and token) as they existed at the time of the
     * transaction.
     */
    @SerializedName("snapshot")
    public Snapshot snapshot;

    /**
     * The id of the action's asset.
     * @deprecated use {@link #flavorId} instead
     */
    @Deprecated
    @SerializedName("asset_id")
    public String assetId;

    /**
     * The alias of the action's asset.
     * @deprecated use {@link #flavorId} instead
     */
    @Deprecated
    @SerializedName("asset_alias")
    public String assetAlias;

    /**
     * The tags of the action's asset.
     * @deprecated use {@link #snapshot} instead
     */
    @Deprecated
    @SerializedName("asset_tags")
    public Map<String, Object> assetTags;

    /**
     * The number of flavor units issued, transferred, or retired.
     */
    public long amount;

    /**
     * The ID of the account serving as the source of flavor units. Null for
     * issuances.
     */
    @SerializedName("source_account_id")
    public String sourceAccountId;

    /**
     * The alias of the account serving as the source of asset units. Null for issuances.
     * @deprecated see {@link #sourceAccountId} instead
     */
    @Deprecated
    @SerializedName("source_account_alias")
    public String sourceAccountAlias;

    /**
     * The tags of the account serving as the source of flavor units. Null for
     * issuances.
     * @deprecated use {@link #snapshot} instead
     */
    @Deprecated
    @SerializedName("source_account_tags")
    public Map<String, Object> sourceAccountTags;

    /**
     * The ID of the account receiving the flavor units. Null for retirements.
     */
    @SerializedName("destination_account_id")
    public String destinationAccountId;

    /**
     * The alias of the account receiving the asset units. Null for retirements.
     * @deprecated see {@link #destinationAccountId} instead
     */
    @Deprecated
    @SerializedName("destination_account_alias")
    public String destinationAccountAlias;

    /**
     * The tags of the account receiving the flavor units. Null for retirements.
     * @deprecated use {@link #snapshot} instead
     */
    @Deprecated
    @SerializedName("destination_account_tags")
    public Map<String, Object> destinationAccountTags;

    /**
     * User-specified, key-value data embedded into the action.
     */
    @SerializedName("tags")
    public Map<String, Object> tags;

    /**
     * User-specified, key-value data embedded into the action.
    * @deprecated use {@link Action#tags} instead
     */
    @Deprecated
    @SerializedName("reference_data")
    public Map<String, Object> referenceData;

    public static class Snapshot {
      /**
       * A snapshot of the actions's tags at the time of action creation
       */
      @SerializedName("action_tags")
      public Map<String, Object> actionTags;

      /**
       * A snapshot of the flavor's tags at the time of action creation
       */
      @SerializedName("flavor_tags")
      public Map<String, Object> flavorTags;

      /**
       * A snapshot of the source account's tags at the time of action creation
       */
      @SerializedName("source_account_tags")
      public Map<String, Object> sourceAccountTags;

      /**
       * A snapshot of the destination account's tags at the time of action creation
       */
      @SerializedName("destination_account_tags")
      public Map<String, Object> destinationAccountTags;

      /**
       * A snapshot of the tokens's tags at the time of action creation
       */
      @SerializedName("token_tags")
      public Map<String, Object> tokenTags;
    }
  }

  /**
   * A configuration object for creating and submitting transactions.
   */
  public static class Builder {
    @SerializedName("reference_data")
    @Deprecated
    protected Map<String, Object> referenceData;

    protected List<Action> actions;

    /**
     * Builds, signs, and submits a tranasaction.
     * @param client ledger API connection object
     * @return the submitted transaction object
     * @throws ChainException
     */
    public Transaction transact(Client client) throws ChainException {
      return client.request("transact", this, Transaction.class);
    }

    public Builder() {
      this.actions = new ArrayList<>();
    }

    /**
     * Specifies key-value data to be recorded in the transaction.
     * @param referenceData arbitrary key-value data
     * @return updated builder
     * @deprecated use the action's setActionTags method instead
     */
    @Deprecated
    public Builder setReferenceData(Map<String, Object> referenceData) {
      this.referenceData = referenceData;
      return this;
    }

    /**
     * Adds a key-value pair to the transaction's reference data.
     * @param key key of the reference data field
     * @param value value of reference data field
     * @return updated builder
     * @deprecated use the action's addActionTagsField method instead
     */
    @Deprecated
    public Builder addReferenceDataField(String key, Object value) {
      if (this.referenceData == null) {
        this.referenceData = new HashMap<>();
      }
      this.referenceData.put(key, value);
      return this;
    }

    /**
     * Adds an action to a transaction builder.
     * @param action action to add
     * @return updated builder
     */
    public Builder addAction(Action action) {
      this.actions.add(action);
      return this;
    }

    /**
     * Base class representing actions that can be taken within a transaction.
     */
    public static class Action extends HashMap<String, Object> {
      public Action() {}

      protected void addKeyValueField(String mapKey, String fieldKey, Object value) {
        Map<String, Object> keyValueData = (Map<String, Object>) get(mapKey);
        if (keyValueData == null) {
          keyValueData = new HashMap<String, Object>();
          put(mapKey, keyValueData);
        }
        keyValueData.put(fieldKey, value);
      }

      protected void addListItem(String mapKey, Object param) {
        List<Object> filterParams = (ArrayList<Object>) get(mapKey);
        if (filterParams == null) {
          filterParams = new ArrayList<Object>();
          put(mapKey, filterParams);
        }
        filterParams.add(param);
      }

      /**
       * Issues new units of a flavor to a destination account.
       */
      public static class Issue extends Action {

        public Issue() {
          put("type", "issue");
        }

        /**
         * Specifies the flavor, identified by its ID, to be issued.
         * @param id ID of a flavor
         * @return updated action
         */
        public Issue setFlavorId(String id) {
          put("flavor_id", id);
          return this;
        }

        /**
         * Specifies the asset, identified by its alias, to be issued. You must specify either an ID or an alias.
         * @param alias alias of an asset
         * @return updated action
         * @deprecated use {@link #setFlavorId(String)} instead
         */
        @Deprecated
        public Issue setAssetAlias(String alias) {
          put("asset_alias", alias);
          return this;
        }

        /**
         * Specifies the asset, identified by its ID, to be issued. You must specify either an ID or an alias.
         * @param id ID of an asset
         * @return updated action
         * @deprecated use {@link #setFlavorId(String)} instead
         */
        @Deprecated
        public Issue setAssetId(String id) {
          put("asset_id", id);
          return this;
        }

        /**
         * Specifies the amount to be issued.
         * @param amount number of flavor units
         * @return updated action
         */
        public Issue setAmount(long amount) {
          put("amount", amount);
          return this;
        }

        /**
         * Specifies the destination account, identified by its alias. You must specify a destination account ID or alias.
         * @param alias alias of an account
         * @return updated action
         * @deprecated see {@link #setDestinationAccountId(String)} instead
         */
        @Deprecated
        public Issue setDestinationAccountAlias(String alias) {
          put("destination_account_alias", alias);
          return this;
        }

        /**
         * Specifies the destination account, identified by its ID. You must specify a destination account ID or alias.
         * @param id an account ID
         * @return updated action
         */
        public Issue setDestinationAccountId(String id) {
          put("destination_account_id", id);
          return this;
        }

        /**
         * Specifies tags for the tokens output by the action.
         * @param tokenTags arbitrary key-value data
         * @return updated action
         */
        public Issue setTokenTags(Map<String, Object> tokenTags) {
          put("token_tags", tokenTags);
          return this;
        }

        /**
         * Adds a key-value pair to the tokens output by the action.
         * @param key key of the token tag field
         * @param value value of token tag field
         * @return updated action
         */
        public Issue addTokenTagsField(String key, Object value) {
          addKeyValueField("token_tags", key, value);
          return this;
        }

        /**
         * Specifies tags for the action.
         * @param actionTags arbitrary key-value data
         * @return updated action
         */
        public Issue setActionTags(Map<String, Object> actionTags) {
          put("action_tags", actionTags);
          return this;
        }

        /**
         * Adds a key-value pair to the tags for the action.
         * @param key key of the action tags field
         * @param value value of action tags field
         * @return updated action
         */
        public Issue addActionTagsField(String key, Object value) {
          addKeyValueField("action_tags", key, value);
          return this;
        }

        /**
         * Specifies reference data for the action.
         * @param referenceData arbitrary key-value data
         * @return updated action
         * @deprecated see {@link #setActionTags(Map)} or {@link #setTokenTags(Map)} instead
         */
        @Deprecated
        public Issue setReferenceData(Map<String, Object> referenceData) {
          put("reference_data", referenceData);
          return this;
        }

        /**
         * Adds a key-value pair to the reference data for the action.
         * @param key key of the reference data field
         * @param value value of reference data field
         * @return updated action
         * @deprecated see {@link #addActionTagsField(String, Object)} or {@link #addTokenTagsField(String, Object)} instead
         */
        @Deprecated
        public Issue addReferenceDataField(String key, Object value) {
          addKeyValueField("reference_data", key, value);
          return this;
        }
      }

      /**
       * Moves flavors from a source (an account or contract) to a destination
       * account.
       */
      public static class Transfer extends Action {

        public Transfer() {
          put("type", "transfer");
        }

        /**
         * Specifies an account, identified by its alias, as the source of the asset units to be transferred. You must specify a source account ID, account alias, or contract ID.
         * @param alias alias of an account
         * @return updated action
         * @deprecated see {@link #setSourceAccountId(String)} instead
         */
        @Deprecated
        public Transfer setSourceAccountAlias(String alias) {
          put("source_account_alias", alias);
          return this;
        }

        /**
         * Specifies an account, identified by its ID, as the source of the
         * flavor units to be transferred. You must specify a source account ID,
         * account alias, or contract ID.
         * @param id an account ID
         * @return updated action
         */
        public Transfer setSourceAccountId(String id) {
          put("source_account_id", id);
          return this;
        }

        /**
         * Specifies a contract as the source of the flavor to be transferred.
         * You must specify a source account ID, account alias, or contract ID.
         * @param id a contract ID
         * @return updated action
         * @deprecated see {@link #setSourceAccountId(String)} instead
         */
        @Deprecated
        public Transfer setSourceContractId(String id) {
          put("source_contract_id", id);
          return this;
        }

        /**
         * Specifies the flavor, identified by its ID, to be transferred.
         * @param id ID of a flavor
         * @return updated action
         */
        public Transfer setFlavorId(String id) {
          put("flavor_id", id);
          return this;
        }

        /**
         * Specifies the asset, identified by its alias, to be transferred. You must specify either an ID or an alias.
         * @param alias alias of an asset
         * @return updated action
         * @deprecated use {@link #setFlavorId(String)} instead
         */
        @Deprecated
        public Transfer setAssetAlias(String alias) {
          put("asset_alias", alias);
          return this;
        }

        /**
         * Specifies the asset, identified by its ID, to be transferred. You must specify either an ID or an alias.
         * @param id id of an asset
         * @return updated action
         * @deprecated use {@link #setFlavorId(String)} instead
         */
        @Deprecated
        public Transfer setAssetId(String id) {
          put("asset_id", id);
          return this;
        }

        /**
         * Specifies the amount to be transferred.
         * @param amount number of flavor units
         * @return updated action
         */
        public Transfer setAmount(long amount) {
          put("amount", amount);
          return this;
        }

        /**
         * Specifies the destination account, identified by its alias. You must specify a destination account ID or alias.
         * @param alias alias of an account
         * @return updated action
         * @deprecated see {@link #setDestinationAccountId(String)} instead
         */
        @Deprecated
        public Transfer setDestinationAccountAlias(String alias) {
          put("destination_account_alias", alias);
          return this;
        }

        /**
         * Specifies the destination account, identified by its ID.You must specify a destination account ID or alias.
         * @param id an account ID
         * @return updated action
         */
        public Transfer setDestinationAccountId(String id) {
          put("destination_account_id", id);
          return this;
        }

        /**
         * Specifies tags for the tokens output by the action.
         * @param tokenTags arbitrary key-value data
         * @return updated action
         */
        public Transfer setTokenTags(Map<String, Object> tokenTags) {
          put("token_tags", tokenTags);
          return this;
        }

        /**
         * Adds a key-value pair to the tokens output by the action.
         * @param key key of the token tag field
         * @param value value of token tag field
         * @return updated action
         */
        public Transfer addTokenTagsField(String key, Object value) {
          addKeyValueField("token_tags", key, value);
          return this;
        }

        /**
         * Specifies tags for the action.
         * @param actionTags arbitrary key-value data
         * @return updated action
         */
        public Transfer setActionTags(Map<String, Object> actionTags) {
          put("action_tags", actionTags);
          return this;
        }

        /**
         * Adds a key-value pair to the tags for the action.
         * @param key key of the action tags field
         * @param value value of action tags field
         * @return updated action
         */
        public Transfer addActionTagsField(String key, Object value) {
          addKeyValueField("action_tags", key, value);
          return this;
        }

        /**
         * Token filter string. See {https://dashboard.seq.com/docs/filters}.
         * @param filter a filter expression
         * @return updated action
         */
        public Transfer setFilter(String filter) {
          put("filter", filter);
          return this;
        }

        /**
         * A list of parameter values for filter string (if needed).
         * @param param a filter parameter
         * @return updated action
         */
        public Transfer addFilterParameter(Object param) {
          addListItem("filter_params", param);
          return this;
        }

        /**
         * Specifies reference data for the action.
         * @param referenceData arbitrary key-value data
         * @return updated action
         * @deprecated see {@link #setActionTags(Map)} or {@link #setTokenTags(Map)} instead
         */
        @Deprecated
        public Transfer setReferenceData(Map<String, Object> referenceData) {
          put("reference_data", referenceData);
          return this;
        }

        /**
         * Adds a key-value pair to the reference data for the action.
         * @param key key of the reference data field
         * @param value value of reference data field
         * @return updated action
         * @deprecated see {@link #addActionTagsField(String, Object)} or {@link #addTokenTagsField(String, Object)} instead
         */
        @Deprecated
        public Transfer addReferenceDataField(String key, Object value) {
          addKeyValueField("reference_data", key, value);
          return this;
        }

        /**
         * Specifies reference data for the change contract.
         * @param referenceData arbitrary key-value data
         * @return updated action
         * @deprecated this now happens automatically when using token tags
         */
        @Deprecated
        public Transfer setChangeReferenceData(Map<String, Object> referenceData) {
          put("change_reference_data", referenceData);
          return this;
        }

        /**
         * Adds a key-value pair to the reference data for the change contract.
         * @param key key of the reference data field
         * @param value value of reference data field
         * @return updated action
         * @deprecated this now happens automatically when using token tags
         */
        @Deprecated
        public Transfer addChangeReferenceDataField(String key, Object value) {
          addKeyValueField("change_reference_data", key, value);
          return this;
        }
      }

      /**
       * Moves flavors from a source (an account or contract) to a destination
       * account.
       */
      public static class Retire extends Action {

        public Retire() {
          put("type", "retire");
        }

        /**
         * Specifies an account, identified by its alias, as the source of the asset units to be retired. You must specify a source account ID, account alias, or contract ID.
         * @param alias alias of an account
         * @return updated action
         * @deprecated see {@link #setSourceAccountId(String)} instead
         */
        @Deprecated
        public Retire setSourceAccountAlias(String alias) {
          put("source_account_alias", alias);
          return this;
        }

        /**
         * Specifies an account, identified by its ID, as the source of the asset units to be retired. You must specify a source account ID, account alias, or contract ID.
         * @param id an account ID
         * @return updated action
         */
        public Retire setSourceAccountId(String id) {
          put("source_account_id", id);
          return this;
        }

        /**
         * Specifies a contract as the source of the flavor to be retired. You
         * must specify a source account ID, account alias, or contract ID.
         * @param id a contract ID
         * @return updated action
         * @deprecated see {@link #setSourceAccountId(String)} instead
         */
        @Deprecated
        public Retire setSourceContractId(String id) {
          put("source_contract_id", id);
          return this;
        }

        /**
         * Specifies the flavor, identified by its ID, to be retired.
         * @param id ID of a flavor
         * @return updated action
         */
        public Retire setFlavorId(String id) {
          put("flavor_id", id);
          return this;
        }

        /**
         * Specifies the asset, identified by its alias, to be retired. You must specify either an ID or an alias.
         * @param alias alias of an asset
         * @return updated action
         * @deprecated use {@link #setFlavorId(String)} instead
         */
        @Deprecated
        public Retire setAssetAlias(String alias) {
          put("asset_alias", alias);
          return this;
        }

        /**
         * Specifies the asset, identified by its ID, to be retired. You must specify either an ID or an alias.
         * @param id id of an asset
         * @return updated action
         * @deprecated use {@link #setFlavorId(String)} instead
         */
        @Deprecated
        public Retire setAssetId(String id) {
          put("asset_id", id);
          return this;
        }

        /**
         * Specifies the amount to be retired.
         * @param amount number of flavor units
         * @return updated action
         */
        public Retire setAmount(long amount) {
          put("amount", amount);
          return this;
        }

        /**
         * Specifies tags for the action.
         * @param actionTags arbitrary key-value data
         * @return updated action
         */
        public Retire setActionTags(Map<String, Object> actionTags) {
          put("action_tags", actionTags);
          return this;
        }

        /**
         * Adds a key-value pair to the tags for the action.
         * @param key key of the action tags field
         * @param value value of action tags field
         * @return updated action
         */
        public Retire addActionTagsField(String key, Object value) {
          addKeyValueField("action_tags", key, value);
          return this;
        }

        /**
         * Token filter string. See {https://dashboard.seq.com/docs/filters}.
         * @param filter a filter expression
         * @return updated action
         */
        public Retire setFilter(String filter) {
          put("filter", filter);
          return this;
        }

        /**
         * A list of parameter values for filter string (if needed).
         * @param param a filter parameter
         * @return updated action
         */
        public Retire addFilterParameter(Object param) {
          addListItem("filter_params", param);
          return this;
        }

        /**
         * Specifies reference data for the action.
         * @param referenceData arbitrary key-value data
         * @return updated action
         * @deprecated see {@link #setActionTags(Map)} instead
         */
        @Deprecated
        public Retire setReferenceData(Map<String, Object> referenceData) {
          put("reference_data", referenceData);
          return this;
        }

        /**
         * Adds a key-value pair to the reference data for the action.
         * @param key key of the reference data field
         * @param value value of reference data field
         * @return updated action
         * @deprecated see {@link #addActionTagsField(String, Object)} instead
         */
        @Deprecated
        public Retire addReferenceDataField(String key, Object value) {
          addKeyValueField("reference_data", key, value);
          return this;
        }

        /**
         * Specifies reference data for the change contract.
         * @param referenceData arbitrary key-value data
         * @return updated action
         * @deprecated this now happens automatically when using token tags
         */
        @Deprecated
        public Retire setChangeReferenceData(Map<String, Object> referenceData) {
          put("change_reference_data", referenceData);
          return this;
        }

        /**
         * Add a key-value pair to the reference data for the change contract.
         * @param key key of the reference data field
         * @param value value of reference data field
         * @return updated action
         * @deprecated this now happens automatically when using token tags
         */
        @Deprecated
        public Retire addChangeReferenceDataField(String key, Object value) {
          addKeyValueField("change_reference_data", key, value);
          return this;
        }
      }
    }
  }
}
