package com.seq.api;

import com.seq.exception.APIException;
import com.seq.exception.BadURLException;
import com.seq.exception.ChainException;
import com.seq.http.Client;

import com.seq.exception.ConnectivityException;
import com.seq.exception.JSONException;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A summation of contract amounts. Contracts are selected using a filter, and
 * their values are summed using the common values of one or more contract
 * fields.
 * @deprecated Use Token.SumBuilder instead.
 */
@Deprecated
public class Balance {
  /**
   * List of parameters along which contract amounts were summed.
   */
  @SerializedName("sum_by")
  public Map<String, String> sumBy;

  /**
   * Summation of contract amounts.
   */
  public long amount;

  /**
   * A single page of balances returned from a query.
   */
  @Deprecated
  public static class Page extends BasePage<Balance> {}

  /**
   * Iterable interface for consuming individual balances from a query.
   */
  @Deprecated
  public static class ItemIterable extends BaseItemIterable<Balance> {
    public ItemIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * Iterable interface for consuming pages of balances from a query.
   */
  @Deprecated
  public static class PageIterable extends BasePageIterable<Page> {
    public PageIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * A builder class for querying balances in the ledger.
   */
  @Deprecated
  public static class QueryBuilder extends BaseQueryBuilder<QueryBuilder> {
    /**
     * Executes the query, returning a page of balances that match the query.
     * @param client ledger API connection object
     * @return a page of balances
     * @throws ChainException
     */
    public Page getPage(Client client) throws ChainException {
      return client.request("list-balances", this.next, Page.class);
    }

    /**
     * Executes the query, returning an iterable over balances that match the
     * query.
     * @param client ledger API connection object
     * @return an iterable over balances
     * @throws ChainException
     */
    public ItemIterable getIterable(Client client) throws ChainException {
      return new ItemIterable(client, "list-balances", this.next);
    }

    /**
     * Executes the query, returning an iterable over pages of balances that
     * match the query.
     * @param client ledger API connection object
     * @return an iterable over pages of balances
     * @throws ChainException
     */
    public PageIterable getPageIterable(Client client) throws ChainException {
      return new PageIterable(client, "list-balances", this.next);
    }

    /**
     * Specifies the fields along which contract values will be summed.
     * @param sumBy a list of contract fields
     * @return updated builder
     */
    public QueryBuilder setSumBy(List<String> sumBy) {
      this.next.sumBy = new ArrayList<>(sumBy);
      return this;
    }

    /**
     * Adds a field along which contract values will be summed.
     * @param field name of a contract field
     * @return updated builder
     */
    public QueryBuilder addSumByField(String field) {
      if (this.next.sumBy == null) {
        this.next.sumBy = new ArrayList<>();
      }
      this.next.sumBy.add(field);
      return this;
    }
  }
}
