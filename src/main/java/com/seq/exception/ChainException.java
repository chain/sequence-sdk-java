package com.seq.exception;

/**
 * Base exception class for the Sequence SDK.
 */
public class ChainException extends Exception {
  public ChainException() {
    super();
  }

  public ChainException(String message) {
    super(message);
  }

  public ChainException(String message, Throwable cause) {
    super(message, cause);
  }
}
