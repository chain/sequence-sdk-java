package com.seq.api;

import com.seq.http.Client;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class representing api query results.
 * @param <T> type of api object
 */
public abstract class BasePage<T> {
  /**
   * Client object that makes the query requests.
   */
  protected Client client;

  /**
   * BasePage of api objects returned from the most recent query.
   */
  @Expose(serialize = false)
  @SerializedName("items")
  public List<T> items;

  /**
   * Specifies if the current page of results is the last.
   */
  @Expose(serialize = false)
  @SerializedName("last_page")
  public boolean lastPage;

  /**
   * Specifies the details of the next query.
   */
  public String cursor;

  public BasePage() {
    this.items = new ArrayList<>();
    this.lastPage = false;
  }
}
