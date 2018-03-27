package com.seq.api;

import com.seq.http.*;
import com.seq.exception.*;
import com.google.gson.annotations.SerializedName;

/**
 * An object describing summary information about a ledger.
 */
public class Stats {
  /**
   * The number of flavors in the ledger.
   */
  @SerializedName("flavor_count")
  public long flavorCount;

  /**
   * The number of accounts in the ledger.
   */
  @SerializedName("account_count")
  public long accountCount;

  /**
   * The number of transactions in the ledger.
   */
  @SerializedName("tx_count")
  public long txCount;

  /**
   * The ledger type. Value can be 'dev' or 'prod'.
   */
  @SerializedName("ledger_type")
  public String ledgerType;

  /**
   * Gets stats from the ledger.
   * @param client ledger API connection object
   * @return a Stats object
   * @throws ChainException
   */
  public static Stats get(Client client) throws ChainException {
    return client.request("stats", null, Stats.class);
  }
}
