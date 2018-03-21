package com.seq.exception;

import com.google.gson.annotations.SerializedName;

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
  public String seqCode;

  /**
   * Message describing the general nature of the error.
   */
  @SerializedName("message")
  public String chainMessage;

  /**
   * Additional information about the error (possibly null).
   */
  public String detail;

  /**
   * Specifies whether the error is considered to be transient and that the
   * request should be retried.
   */
  public boolean retriable;

  /**
   * Deprecated. Alias for retriable. Will be removed in 2.0.0.
   */
  public boolean temporary;

  /**
   * Unique identifier of the request to the server.
   */
  public String requestId;

  /**
   * HTTP status code returned by the server.
   */
  public int statusCode;

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
