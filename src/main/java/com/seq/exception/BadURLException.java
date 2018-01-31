package com.seq.exception;

/**
 * BadURLException is thrown when a malformed URL is provided.
 */
public class BadURLException extends ChainException {
  public BadURLException(String message) {
    super(message);
  }
}
