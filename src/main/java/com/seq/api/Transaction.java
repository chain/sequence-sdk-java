package com.seq.api;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

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
  @Expose
  public String id;

  /**
   * Time of transaction.
   */
  @Expose
  public Date timestamp;

  /**
   * Sequence number of the transaction.
   */
  @SerializedName("sequence_number")
  @Expose
  public long sequenceNumber;

  /**
   * List of actions taken by the transaction.
   */
  @Expose
  public List<Action> actions;

  /**
   * User-specified key-value data embedded into the transaction.
   */
  @Expose
  public Map<String, Object> tags;

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
  }

  /**
   * An action taken by a transaction.
   */
  public static class Action {
    /**
     * A unique ID.
     */
    @Expose
    public String id;

    /**
     * The type of the action. Possible values are "issue", "transfer" and "retire".
     */
    @Expose
    public String type;

    /**
     * The id of the action's flavor.
     */
    @SerializedName("flavor_id")
    @Expose
    public String flavorId;

    /**
     * A copy of the associated tags (flavor, source account, destination
     * account, action, and token) as they existed at the time of the
     * transaction.
     */
    @SerializedName("snapshot")
    @Expose
    public Snapshot snapshot;

    /**
     * The number of flavor units issued, transferred, or retired.
     */
    @Expose
    public long amount;

    /**
     * The ID of the account serving as the source of flavor units. Null for
     * issuances.
     */
    @SerializedName("source_account_id")
    @Expose
    public String sourceAccountId;

    /**
     * The ID of the account receiving the flavor units. Null for retirements.
     */
    @SerializedName("destination_account_id")
    @Expose
    public String destinationAccountId;

    /**
     * User-specified, key-value data embedded into the action.
     */
    @SerializedName("tags")
    @Expose
    public Map<String, Object> tags;

    public static class Snapshot {
      /**
       * A snapshot of the actions's tags at the time of action creation
       */
      @SerializedName("action_tags")
      @Expose
      public Map<String, Object> actionTags;

      /**
       * A snapshot of the flavor's tags at the time of action creation
       */
      @SerializedName("flavor_tags")
      @Expose
      public Map<String, Object> flavorTags;

      /**
       * A snapshot of the source account's tags at the time of action creation
       */
      @SerializedName("source_account_tags")
      @Expose
      public Map<String, Object> sourceAccountTags;

      /**
       * A snapshot of the destination account's tags at the time of action creation
       */
      @SerializedName("destination_account_tags")
      @Expose
      public Map<String, Object> destinationAccountTags;

      /**
       * A snapshot of the tokens's tags at the time of action creation
       */
      @SerializedName("token_tags")
      @Expose
      public Map<String, Object> tokenTags;

      /**
       * A snapshot of the transaction's tags at the time of action creation
       */
      @SerializedName("transaction_tags")
      @Expose
      public Map<String, Object> transactionTags;
    }
  }

  /**
   * A configuration object for creating and submitting transactions.
   */
  public static class Builder {
    @Expose
    protected List<Action> actions;

    @SerializedName("transaction_tags")
    @Expose
    protected Map<String, Object> transactionTags;

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
      this.transactionTags = new HashMap<>();
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
     * Adds a key-value pair to the transaction's tags.
     * @param key key of the tag field
     * @param value value of tag field
     * @return updated builder
     */
    public Builder addTransactionTagsField(String key, Object value) {
      this.transactionTags.put(key, value);
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
         * Specifies the amount to be issued.
         * @param amount number of flavor units
         * @return updated action
         */
        public Issue setAmount(long amount) {
          put("amount", amount);
          return this;
        }

        /**
         * Specifies the destination account, identified by its ID. You must
         * specify a destination account ID.
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
      }

      /**
       * Moves flavors from a source account to a destination account.
       */
      public static class Transfer extends Action {

        public Transfer() {
          put("type", "transfer");
        }

        /**
         * Specifies an account, identified by its ID, as the source of the
         * flavor units to be transferred. You must specify a source account ID.
         * @param id an account ID
         * @return updated action
         */
        public Transfer setSourceAccountId(String id) {
          put("source_account_id", id);
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
         * Specifies the amount to be transferred.
         * @param amount number of flavor units
         * @return updated action
         */
        public Transfer setAmount(long amount) {
          put("amount", amount);
          return this;
        }

        /**
         * Specifies the destination account, identified by its ID. You must
         * specify a destination account ID.
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
      }

      /**
       * Moves flavors from a source account to a destination account.
       */
      public static class Retire extends Action {

        public Retire() {
          put("type", "retire");
        }

        /**
         * Specifies an account, identified by its ID, as the source of the
         * tokens to be retired. You must specify a source account ID.
         * @param id an account ID
         * @return updated action
         */
        public Retire setSourceAccountId(String id) {
          put("source_account_id", id);
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
      }
    }
  }
}
