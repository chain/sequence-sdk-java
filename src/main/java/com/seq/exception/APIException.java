package com.seq.exception;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Map;

/**
 * APIException is thrown when the ledger API encounters an error handling a
 * user request. Errors could be due to user error, or due to server issues.
 * <br>
 * Each error contains a brief description in addition to an error code. The
 * error code can be used by technical support to diagnose the exact cause of
 * the error.
 */
public class APIException extends ChainException {
  /**
   * An error code of the format "SEQXXX".
   */
  @SerializedName("seq_code")
  @Expose
  public String seqCode;

  /**
   * Message describing the general nature of the error.
   */
  @SerializedName("message")
  @Expose
  public String chainMessage;

  /**
   * Additional message about the error (possibly null).
   */
  @Expose
  public String detail;

  /**
   * Additional specifics about the error, such as which
   * action failed in a transaction, or for what reason (possibly null).
   */
  @Expose
  public APIExceptionData data;

    /**
   * Specifies whether the error is considered to be transient and that the
   * request should be retried.
   */
  @Expose
  public boolean retriable;

  /**
   * Unique identifier of the request to the server.
   */
  @Expose
  public String requestId;

  /**
   * HTTP status code returned by the server.
   */
  @Expose
  public int statusCode;

  /**
   * Additional specifics about the error, including any
   * nested sub-errors related to individual actions.
   */
  public static class APIExceptionData {
    /**
     * Nested errors specific to individual actions in the transaction. Nested
     * errors may contain their own API exception data object.
     */
    @Expose
    public List<APIException> actions;

    /**
     * Lists fields omitted from an action.
     */
    @Expose
    public List<String> missing_fields;

    /**
     * The first field with incorrect data in an action .
     */
    @Expose
    public String error_fields;

    /**
     * The index in the transaction of the action with an error
     */
    @Expose
    public Integer index;
  }

  @Override
  public String getMessage() {
    String s = "";

    if (this.seqCode != null && this.seqCode.length() > 0) {
      s += "Code: " + this.seqCode + " ";
    }

    s += "Message: " + this.chainMessage;

    if (this.detail != null && this.detail.length() > 0) {
      s += " Detail: " + this.detail;
    }

    if (this.requestId != null) {
      s += " Request-ID: " + this.requestId;
    }

    return s;
  }
}
