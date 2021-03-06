package com.seq.api;

import com.seq.exception.APIException;
import com.seq.exception.BadURLException;
import com.seq.exception.ChainException;
import com.seq.http.Client;

import com.seq.exception.ConnectivityException;
import com.seq.exception.JSONException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

import java.util.Date;
import java.util.Map;

/**
 * ActionSum objects are what's returned by SumBuilder method in Action class.
 * Different from other regular API objects, the amount field in ActionSum represents
 * the summation of the amount fields of those matching actions, and all other fields
 * represent the parameters by which to group actions.
 *
 * Please refer to Action class for more details about the SumBuilder method.
 */
public class ActionSum {
  /**
   * Summation of the amount fields of the matching actions
   */
  @Expose
  public long amount;

  /**
   * The type of the action.
   * Currently, there are three options: "issue", "transfer", "retire".
   */
  @Expose
  public String type;

  /**
   * A unique ID.
   */
  @Expose
  public String id;

  /**
   * The ID of the transaction in which the action appears.
   */
  @SerializedName("transaction_id")
  @Expose
  public String transactionId;

  /**
   * Time of the action.
   */
  @Expose
  public Date timestamp;

  /**
   * The ID of the flavor of the tokens held by the action.
   */
  @SerializedName("flavor_id")
  @Expose
  public String flavorId;

  /**
   * The ID of the source account executing the action.
   */
  @SerializedName("source_account_id")
  @Expose
  public String sourceAccountId;

  /**
   * The ID of the destination account affected by the action.
   */
  @SerializedName("destination_account_id")
  @Expose
  public String destinationAccountId;

  /**
   * A copy of the associated tags (flavor, source account, destination account,
   * action, and token) as they existed at the time of the transaction.
   */
  @SerializedName("snapshot")
  @Expose
  public Action.Snapshot snapshot;

  /**
   * User-specified key-value data embedded in the action.
   */
  @SerializedName("tags")
  @Expose
  public Map<String, Object> tags;

  /**
   * A single page of actions returned from a query.
   */
  public static class Page extends BasePage<ActionSum> {}

  /**
   * Iterable interface for consuming individual actions from a query.
   */
  public static class ItemIterable extends BaseItemIterable<ActionSum> {
    public ItemIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }
}
