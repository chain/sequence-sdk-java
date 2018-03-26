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
  public long amount;

  /**
   * The type of the action.
   * Currently, there are three options: "issue", "transfer", "retire".
   */
  public String type;

  /**
   * A unique ID.
   */
  public String id;

  /**
   * The ID of the transaction in which the action appears.
   */
  @SerializedName("transaction_id")
  public String transactionId;

  /**
   * Time of the action.
   */
  public Date timestamp;

  /**
   * The ID of the flavor of the tokens held by the action.
   */
  @SerializedName("flavor_id")
  public String flavorId;

  /**
   * The ID of the asset held by the action.
   * @deprecated Use {@link #flavorId} instead.
   */
  @SerializedName("asset_id")
  @Deprecated
  public String assetId;

  /**
   * The alias of the asset held by the action.
   * @deprecated Use {@link #flavorId} instead.
   */
  @Deprecated
  @SerializedName("asset_alias")
  public String assetAlias;

  /**
   * The tags of the asset held by the action.
   */
  @Deprecated
  @SerializedName("asset_tags")
  public Map<String, Object> assetTags;

  /**
   * The ID of the source account executing the action.
   */
  @SerializedName("source_account_id")
  public String sourceAccountId;

  /**
   * The tags of the source account executing the action.
   * @deprecated Use {@link #snapshot} instead.
   */
  @SerializedName("source_account_tags")
  @Deprecated
  public Map<String, Object> sourceAccountTags;

  /**
   * The ID of the destination account affected by the action.
   */
  @SerializedName("destination_account_id")
  public String destinationAccountId;

  /**
   * The tags of the destination account affected by the action.
   * @deprecated Use {@link #snapshot} instead.
   */
  @SerializedName("destination_account_tags")
  @Deprecated
  public Map<String, Object> destinationAccountTags;

  /**
   * User-specified key-value data embedded in the action.
   * @deprecated Use {@link #tags} instead.
   */
  @SerializedName("reference_data")
  @Deprecated
  public Object referenceData;

  /**
   * A copy of the associated tags (flavor, source account, destination account,
   * action, and token) as they existed at the time of the transaction.
   */
  @SerializedName("snapshot")
  public Action.Snapshot snapshot;

  /**
   * User-specified key-value data embedded in the action.
   */
  @SerializedName("tags")
  public Object tags;

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

  /**
   * Iterable interface for consuming pages of actions from a query.
   */
  public static class PageIterable extends BasePageIterable<Page> {
    public PageIterable(Client client, String path, Query nextQuery) {
      super(client, path, nextQuery, Page.class);
    }
  }
}
