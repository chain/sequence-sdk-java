package com.seq.api;

import com.seq.exception.APIException;
import com.seq.exception.BadURLException;
import com.seq.exception.ChainException;
import com.seq.http.Client;

import com.seq.exception.ConnectivityException;
import com.seq.exception.JSONException;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Map;

/**
 * TokenSum objects are what is returned by SumBuilder method in Token class.
 * Different from other regular API objects, the amount field in TokenSum
 * represents the summation of the amount fields of those matching actions, and
 * all other fields represent the parameters by which to group actions.
 *
 * Please refer to Token class for more details about the SumBuilder method.
 */
public class TokenSum {
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
   * A single page of actions returned from a query.
   */
  public static class Page extends BasePage<TokenSum> {}

  /**
   * Iterable interface for consuming individual actions from a query.
   */
  public static class ItemIterable extends BaseItemIterable<TokenSum> {
    public ItemIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }
}
