package com.seq.api;

import com.seq.exception.APIException;
import com.seq.exception.BadURLException;
import com.seq.exception.ChainException;
import com.seq.http.Client;
import com.seq.exception.ConnectivityException;
import com.seq.exception.JSONException;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * An entry in the ledger that contains value that can be spent.
 */
public class Contract {
  /**
   * A unique ID.
   */
  @SerializedName("id")
  public String id;

  /**
   * The type of the contract. Currently, this is always "account".
   */
  public String type;

  /**
   * The ID of the transaction in which the contract appears.
   */
  @SerializedName("transaction_id")
  public String transactionId;

  /**
   * The ID of the asset held by the contract.
   */
  @SerializedName("asset_id")
  public String assetId;

  /**
   * The alias of the asset held by the contract.
   */
  @SerializedName("asset_alias")
  public String assetAlias;

  /**
   * The tags of the asset held by the contract.
   */
  @SerializedName("asset_tags")
  public Map<String, Object> assetTags;

  /**
   * The number of units of the asset held by the contract.
   */
  public long amount;

  /**
   * The ID of the account controlling the contract.
   */
  @SerializedName("account_id")
  public String accountId;

  /**
   * The alias of the account controlling the contract.
   * @deprecated see {@link #accountId} instead
   */
  @Deprecated
  @SerializedName("account_alias")
  public String accountAlias;

  /**
   * The tags of the account controlling the contract.
   */
  @SerializedName("account_tags")
  public Map<String, Object> accountTags;

  /**
   * User-specified key-value data embedded in the contract.
   */
  @SerializedName("reference_data")
  public Map<String, Object> referenceData;

  /**
   * A single page of contracts returned from a query.
   */
  public static class Page extends BasePage<Contract> {}

  /**
   * Iterable interface for consuming individual contracts from a query.
   */
  public static class ItemIterable extends BaseItemIterable<Contract> {
    public ItemIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * Iterable interface for consuming pages of contracts from a query.
   */
  public static class PageIterable extends BasePageIterable<Page> {
    public PageIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * A builder class for querying contracts in the ledger.
   */
  public static class QueryBuilder extends BaseQueryBuilder<QueryBuilder> {
    /**
     * Executes the query, returning a page of contracts that match the query.
     * @param client ledger API connection object
     * @return a page of contracts
     * @throws ChainException
     */
    public Page getPage(Client client) throws ChainException {
      return client.request("list-contracts", this.next, Page.class);
    }

    /**
     * Executes the query, returning an iterable over contracts that match the query.
     * @param client ledger API connection object
     * @return an iterable over contracts
     * @throws ChainException
     */
    public ItemIterable getIterable(Client client) throws ChainException {
      return new ItemIterable(client, "list-contracts", this.next);
    }

    /**
     * Executes the query, returning an iterable over pages of contracts that match the query.
     * @param client ledger API connection object
     * @return an iterable over pages of contracts
     * @throws ChainException
     */
    public PageIterable getPageIterable(Client client) throws ChainException {
      return new PageIterable(client, "list-contracts", this.next);
    }

    /**
     * Indicates that the query should be run over the state of the ledger at a given point in time.
     * @param timestampMS timestamp in milliseconds
     * @return updated builder
     */
    public QueryBuilder setTimestamp(long timestampMS) {
      this.next.timestamp = timestampMS;
      return this;
    }
  }
}
