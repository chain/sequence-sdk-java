package com.seq.exception;

import com.google.gson.annotations.Expose;

/**
 * JSONException should be very rare, and will only arise if there is a bug in the
 * Sequence API, or if the upstream server is spoofing common Sequence API response
 * headers.
 */
public class JSONException extends ChainException {

  /**
   * Unique indentifier of the request to the server.
   */
  @Expose
  public String requestId;

  /**
   * Default constructor.
   */
  public JSONException(String message) {
    super(message);
  }

  /**
   * Initializes exception with its message and requestId attributes.
   * Use this constructor in context of an API call.
   *
   * @param message error message
   * @param requestId unique identifier of the request
   */
  public JSONException(String message, String requestId) {
    super(message);
    this.requestId = requestId;
  }

  public String getMessage() {
    String message = "Message: " + super.getMessage();
    if (requestId != null) {
      message += " Request-ID: " + requestId;
    }
    return message;
  }
}
