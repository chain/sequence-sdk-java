package com.seq.api;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic configuration object for query requests.
 * TODO: move this into BaseQueryBuilder.
 */
public class Query {
  public String filter;

  @SerializedName("filter_params")
  public List<Object> filterParams;

  @SerializedName("page_size")
  public int pageSize;

  public String cursor;

  public long timeout;

  @SerializedName("start_time")
  public long startTime;

  @SerializedName("end_time")
  public long endTime;

  public long timestamp;

  @SerializedName("sum_by")
  public List<String> sumBy;

  @SerializedName("group_by")
  public List<String> groupBy;

  public List<String> ids;

  public Query() {
    this.filterParams = new ArrayList<>();
    this.sumBy = new ArrayList<>();
    this.groupBy = new ArrayList<>();
    this.ids = new ArrayList<>();
  }
}
