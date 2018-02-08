package com.seq.api;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.seq.exception.*;
import com.seq.http.*;

/**
 * A transaction is an atomic update to the state of the ledger. Transactions
 * can issue new asset units, transfer of asset units from one account to
 * another, and/or the retire asset units from an account.
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
   */
  @SerializedName("reference_data")
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
  public static class PageIterable extends BasePageIterable<Page> {
    public PageIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * A builder class for querying transactions in the ledger.
   */
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
     */
    public PageIterable getPageIterable(Client client) throws ChainException {
      return new PageIterable(client, "list-transactions", this.next);
    }

    /**
     * Specifies the timestamp of the earliest transaction to include in the query results.
     * @param time unixtime in milliseconds
     * @return updated builder
     */
    public QueryBuilder setStartTime(long time) {
      this.next.startTime = time;
      return this;
    }

    /**
     * Specifies the timestamp of the most recent transaction to include in the query results.
     * @param time unixtime in milliseconds
     * @return updated builder
     */
    public QueryBuilder setEndTime(long time) {
      this.next.endTime = time;
      return this;
    }
  }

  /**
   * An action taken by a transaction.
   */
  public static class Action {
    /**
     * The type of the action. Possible values are "issue", "transfer" and "retire".
     */
    public String type;

    /**
     * The id of the action's asset.
     */
    @SerializedName("asset_id")
    public String assetId;

    /**
     * The alias of the action's asset.
     */
    @SerializedName("asset_alias")
    public String assetAlias;

    /**
     * The tags of the action's asset.
     */
    @SerializedName("asset_tags")
    public Map<String, Object> assetTags;

    /**
     * The number of asset units issues, transferred, or retired.
     */
    public long amount;

    /**
     * The ID of the account serving as the source of asset units. Null for issuances.
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
     * The tags of the account serving as the source of asset units. Null for issuances.
     */
    @SerializedName("source_account_tags")
    public Map<String, Object> sourceAccountTags;

    /**
     * The ID of the account receiving the asset units. Null for retirements.
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
     * The tags of the account receiving the asset units. Null for retirements.
     *
     */
    @SerializedName("destination_account_tags")
    public Map<String, Object> destinationAccountTags;

    /**
     * User-specified, key-value data embedded into the action.
     */
    @SerializedName("reference_data")
    public Map<String, Object> referenceData;
  }

  /**
   * A configuration object for creating and submitting transactions.
   */
  public static class Builder {
    @SerializedName("reference_data")
    protected Map<String, Object> referenceData;

    protected List<Action> actions;

    /**
     * Builds, signs, and submits a tranasaction.
     * @param client ledger API connection object
     * @return the submitted transaction object
     * @throws ChainException
     */
    public Transaction transact(Client client) throws ChainException {
      JsonObject tpl = client.request("build-transaction", this, JsonObject.class);

      HashMap<String, Object> body = new HashMap();
      body.put("transaction", tpl);
      tpl = client.request("sign-transaction", body, JsonObject.class);

      body = new HashMap<>();
      body.put("transaction", tpl);
      return client.request("submit-transaction", body, Transaction.class);
    }

    public Builder() {
      this.actions = new ArrayList<>();
    }

    /**
     * Specifies key-value data to be recorded in the transaction.
     * @param referenceData arbitrary key-value data
     * @return updated builder
     */
    public Builder setReferenceData(Map<String, Object> referenceData) {
      this.referenceData = referenceData;
      return this;
    }

    /**
     * Adds a key-value pair to the transaction's reference data.
     * @param key key of the reference data field
     * @param value value of reference data field
     * @return updated builder
     */
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
        Map<String, Object> referenceData = (Map<String, Object>) get(mapKey);
        if (referenceData == null) {
          referenceData = new HashMap<String, Object>();
          put(mapKey, referenceData);
        }
        referenceData.put(fieldKey, value);
      }

      /**
       * Issues new units of an asset to a destination account.
       */
      public static class Issue extends Action {

        public Issue() {
          put("type", "issue");
        }

        /**
         * Specifies the asset, identified by its alias, to be issued. You must specify either an ID or an alias.
         * @param alias alias of an asset
         * @return updated action
         */
        public Issue setAssetAlias(String alias) {
          put("asset_alias", alias);
          return this;
        }

        /**
         * Specifies the asset, identified by its ID, to be issued. You must specify either an ID or an alias.
         * @param id ID of an asset
         * @return updated action
         */
        public Issue setAssetId(String id) {
          put("asset_id", id);
          return this;
        }

        /**
         * Specifies the amount to be issued.
         * @param amount number of asset units
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
         * Specifies reference data for the action.
         * @param referenceData arbitrary key-value data
         * @return updated action
         */
        public Issue setReferenceData(Map<String, Object> referenceData) {
          put("reference_data", referenceData);
          return this;
        }

        /**
         * Adds a key-value pair to the reference data for the action.
         * @param key key of the reference data field
         * @param value value of reference data field
         * @return updated action
         */
        public Issue addReferenceDataField(String key, Object value) {
          addKeyValueField("reference_data", key, value);
          return this;
        }
      }

      /**
       * Moves assets from a source (an account or contract) to a destination account.
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
         * Specifies an account, identified by its ID, as the source of the asset units to be transferred.You must specify a source account ID, account alias, or contract ID.
         * @param id an account ID
         * @return updated action
         */
        public Transfer setSourceAccountId(String id) {
          put("source_account_id", id);
          return this;
        }

        /**
         * Specifies a contract as the source of the asset to be transferred. You must specify a source account ID, account alias, or contract ID.
         * @param id a contract ID
         * @return updated action
         */
        public Transfer setSourceContractId(String id) {
          put("source_contract_id", id);
          return this;
        }

        /**
         * Specifies the asset, identified by its alias, to be transferred. You must specify either an ID or an alias.
         * @param alias alias of an asset
         * @return updated action
         */
        public Transfer setAssetAlias(String alias) {
          put("asset_alias", alias);
          return this;
        }

        /**
         * Specifies the asset, identified by its ID, to be transferred. You must specify either an ID or an alias.
         * @param id id of an asset
         * @return updated action
         */
        public Transfer setAssetId(String id) {
          put("asset_id", id);
          return this;
        }

        /**
         * Specifies the amount to be transferred.
         * @param amount number of asset units
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
         * Specifies reference data for the action.
         * @param referenceData arbitrary key-value data
         * @return updated action
         */
        public Transfer setReferenceData(Map<String, Object> referenceData) {
          put("reference_data", referenceData);
          return this;
        }

        /**
         * Adds a key-value pair to the reference data for the action.
         * @param key key of the reference data field
         * @param value value of reference data field
         * @return updated action
         */
        public Transfer addReferenceDataField(String key, Object value) {
          addKeyValueField("reference_data", key, value);
          return this;
        }

        /**
         * Specifies reference data for the change contract.
         * @param referenceData arbitrary key-value data
         * @return updated action
         */
        public Transfer setChangeReferenceData(Map<String, Object> referenceData) {
          put("change_reference_data", referenceData);
          return this;
        }

        /**
         * Adds a key-value pair to the reference data for the change contract.
         * @param key key of the reference data field
         * @param value value of reference data field
         * @return updated action
         */
        public Transfer addChangeReferenceDataField(String key, Object value) {
          addKeyValueField("change_reference_data", key, value);
          return this;
        }
      }

      /**
       * Moves assets from a source (an account or contract) to a destination account.
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
         * Specifies a contract as the source of the asset to be retired. You must specify a source account ID, account alias, or contract ID.
         * @param id a contract ID
         * @return updated action
         */
        public Retire setSourceContractId(String id) {
          put("source_contract_id", id);
          return this;
        }

        /**
         * Specifies the asset, identified by its alias, to be retired. You must specify either an ID or an alias.
         * @param alias alias of an asset
         * @return updated action
         */
        public Retire setAssetAlias(String alias) {
          put("asset_alias", alias);
          return this;
        }

        /**
         * Specifies the asset, identified by its ID, to be retired. You must specify either an ID or an alias.
         * @param id id of an asset
         * @return updated action
         */
        public Retire setAssetId(String id) {
          put("asset_id", id);
          return this;
        }

        /**
         * Specifies the amount to be retired.
         * @param amount number of asset units
         * @return updated action
         */
        public Retire setAmount(long amount) {
          put("amount", amount);
          return this;
        }

        /**
         * Specifies reference data for the action.
         * @param referenceData arbitrary key-value data
         * @return updated action
         */
        public Retire setReferenceData(Map<String, Object> referenceData) {
          put("reference_data", referenceData);
          return this;
        }

        /**
         * Adds a key-value pair to the reference data for the action.
         * @param key key of the reference data field
         * @param value value of reference data field
         * @return updated action
         */
        public Retire addReferenceDataField(String key, Object value) {
          addKeyValueField("reference_data", key, value);
          return this;
        }

        /**
         * Specifies reference data for the change contract.
         * @param referenceData arbitrary key-value data
         * @return updated action
         */
        public Retire setChangeReferenceData(Map<String, Object> referenceData) {
          put("change_reference_data", referenceData);
          return this;
        }

        /**
         * Add a key-value pair to the reference data for the change contract.
         * @param key key of the reference data field
         * @param value value of reference data field
         * @return updated action
         */
        public Retire addChangeReferenceDataField(String key, Object value) {
          addKeyValueField("change_reference_data", key, value);
          return this;
        }
      }
    }
  }
}
