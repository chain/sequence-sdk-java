package com.seq.api;

import com.seq.exception.APIException;
import com.seq.exception.BadURLException;
import com.seq.exception.ChainException;
import com.seq.http.Client;

import com.seq.exception.ConnectivityException;
import com.seq.exception.JSONException;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Token queries are designed to provide insights into tokens contained in an
 * account. There are two types of queries you can run against them; one is
 * "ListBuilder", one is "SumBuilder". ListBuilder simply returns a list of
 * Token group objects that match the filter (each representing an amount of
 * identical tokens); SumBuilder sums over the amount fields based on the
 * filter and the groupBy param and returns TokenSum objects.
 *
 * Please refer to TokenSum class for more details.
 */
public class Token {
  /**
   * The amount of tokens in the group.
   */
  public long amount;

  /**
   * The flavor of the tokens in the group.
   */
  @SerializedName("flavor_id")
  public String flavorId;

  /**
   * The tags of the flavor of the tokens in the group.
   */
  @SerializedName("flavor_tags")
  public Map<String, Object> flavorTags;

  /**
   * The ID of the account containing the tokens.
   */
  @SerializedName("account_id")
  public String accountId;

  /**
   * The tags of the account containing the tokens.
   */
  @SerializedName("account_tags")
  public Map<String, Object> accountTags;

  /**
   * The tags of the tokens in the group.
   */
  @SerializedName("tags")
  public Map<String, Object> tags;

  /**
   * A single page of tokens returned from a query.
   */
  public static class Page extends BasePage<Token> {}

  /**
   * Iterable interface for consuming individual tokens from a query.
   */
  public static class Iterable extends BaseItemIterable<Token> {
    public Iterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }

  /**
   * A builder class for querying tokens in the ledger.
   *
   * <p>List all tokens after a certain time:</p>
   * <pre>{@code
   * Token.Iterable tokens = new Token.ListBuilder()
   *   .setFilter("timestamp > $1")
   *   .addFilterParameter("1985-10-26T01:21:00Z")
   *   .getIterable(ledger);
   * for (Token token : tokens) {
   *   System.out.println("timestamp: " + token.timestamp);
   *   System.out.println("amount: " + token.amount);
   * }
   * }</pre>
   */
  public static class ListBuilder extends BaseQueryBuilder<ListBuilder> {
    /**
     * Executes the query, returning a page of tokens that match the query.
     * @param client ledger API connection object
     * @return a page of tokens
     * @throws ChainException
     */
    public Page getPage(Client client) throws ChainException {
      return client.request("list-tokens", this.next, Page.class);
    }

    /**
     * Executes the query, returning a page of tokens that match the query
     * beginning with provided cursor.
     * @param client ledger API connection object
     * @param cursor string representing encoded query object
     * @return a page of tokens
     * @throws ChainException
     */
    public Page getPage(Client client, String cursor) throws ChainException {
      Query next = new Query();
      next.cursor = cursor;
      return client.request("list-tokens", next, Page.class);
    }

    /**
     * Executes the query, returning an iterable over tokens that match the
     * query.
     * @param client ledger API connection object
     * @return an iterable over tokens
     * @throws ChainException
     */
    public Iterable getIterable(Client client) throws ChainException {
      return new Iterable(client, "list-tokens", this.next);
    }

    @Deprecated
    public BasePageIterable getPageIterable(Client client) throws ChainException {
      throw new ChainException("Deprecated");
    }
  }

  /**
   * A builder class for querying token sums in the ledger.
   */
  public static class SumBuilder extends BaseQueryBuilder<SumBuilder> {
    /**
     * Executes the query, returning a page of token sums that match the query.
     * @param client ledger API connection object
     * @return a page of token sums
     * @throws ChainException
     */
    public TokenSum.Page getPage(Client client) throws ChainException {
      return client.request("sum-tokens", this.next, TokenSum.Page.class);
    }

    /**
     * Executes the query, returning a page of token sums that match the query
     * beginning with provided cursor.
     * @param client ledger API connection object
     * @param cursor string representing encoded query object
     * @return a page of token sums
     * @throws ChainException
     */
    public TokenSum.Page getPage(Client client, String cursor) throws ChainException {
      Query next = new Query();
      next.cursor = cursor;
      return client.request("sum-tokens", next, TokenSum.Page.class);
    }

    /**
     * Executes the query, returning an iterable over token sums that match the
     * query.
     * @param client ledger API connection object
     * @return an iterable over token sums
     * @throws ChainException
     */
    public TokenSum.Iterable getIterable(Client client) throws ChainException {
      return new TokenSum.Iterable(client, "sum-tokens", this.next);
    }

    @Deprecated
    public BasePageIterable getPageIterable(Client client) throws ChainException {
      throw new ChainException("Deprecated");
    }

    /**
     * Specifies the fields on which token values will be summed.
     * @param groupBy a list of token fields
     * @return updated builder
     */
    public SumBuilder setGroupBy(List<String> groupBy) {
      this.next.groupBy = new ArrayList<>(groupBy);
      return this;
    }

    /**
     * Adds a field on which token values will be summed.
     * @param field name of a token field
     * @return updated builder
     */
    public SumBuilder addGroupByField(String field) {
      if (this.next.groupBy == null) {
        this.next.groupBy = new ArrayList<>();
      }
      this.next.groupBy.add(field);
      return this;
    }
  }
}
