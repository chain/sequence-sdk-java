package com.seq.api;

import java.util.*;

import com.seq.http.*;
import com.seq.exception.*;

/**
 * Namespace for development-only methods.
 */
public class DevUtils {
  /**
   * Resets all data in the ledger.
   * @param client ledger API connection object
   */
  public static void reset(Client client) throws ChainException {
    Map<String, Object> params = new HashMap<>();
    client.request("reset", params, Object.class);
  }
}
