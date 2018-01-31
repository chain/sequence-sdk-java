package com.seq.exception;

import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * ConnectivityException is thrown when an HTTP response is received, but it does
 * not contain headers that are included in all Sequence API responses. This
 * could arise due to a badly-configured proxy, or other upstream network
 * issues.
 */
public class ConnectivityException extends ChainException {
  public ConnectivityException(Response resp) {
    super(formatMessage(resp));
  }

  private static String formatMessage(Response resp) {
    String s =
        "Response HTTP header field Chain-Request-ID is unset. There may be network issues. Please check your local network settings.";
    // TODO(kr): include client-generated reqid here once we have that.
    String body;
    try {
      body = resp.body().string();
    } catch (IOException ex) {
      body = "[unable to read response body: " + ex.toString() + "]";
    }
    return String.format("%s status=%d body=%s", s, resp.code(), body);
  }
}
