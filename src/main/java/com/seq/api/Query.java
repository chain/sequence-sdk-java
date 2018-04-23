package com.seq.api;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic configuration object for query requests.
 * TODO: move this into BaseQueryBuilder.
 */
public class Query {
  @Expose
  public String filter;

  @SerializedName("filter_params")
  @Expose
  public List<Object> filterParams;

  @SerializedName("page_size")
  @Expose
  public int pageSize;

  @Expose
  public String cursor;

  @Expose
  public long timeout;

  @SerializedName("start_time")
  @Expose
  public long startTime;

  @SerializedName("end_time")
  @Expose
  public long endTime;

  @Expose
  public long timestamp;

  @SerializedName("sum_by")
  @Expose
  public List<String> sumBy;

  @SerializedName("group_by")
  @Expose
  public List<String> groupBy;

  public List<String> ids;

  public Query() {
    this.filterParams = new ArrayList<>();
    this.sumBy = new ArrayList<>();
    this.groupBy = new ArrayList<>();
    this.ids = new ArrayList<>();
  }
}
