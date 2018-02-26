package com.seq.api;

import com.seq.exception.ChainException;
import com.seq.http.Client;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class providing an interface for building queries.
 * @param <T> the QueryBuilder class that extends BaseQueryBuilder
 */
public abstract class BaseQueryBuilder<T extends BaseQueryBuilder<T>> {
  protected Query next;

  /**
   * Execute the API query and return one page of items.
   * @param client ledger API connection object
   * @return a page of S objects that satisfy the query
   * @throws ChainException
   */
  public abstract <S extends BasePage> S getPage(Client client) throws ChainException;

  /**
   * Executes the API query.
   * @param client ledger API connection object
   * @return an iterable over pages of S objects that satisfy the query
   * @throws ChainException
   */
  public abstract <S extends BasePageIterable> S getPageIterable(Client client)
      throws ChainException;

  /**
   * Executes the API query.
   * @param client ledger API connection object
   * @return an iterable over S objects that satisfy the query
   * @throws ChainException
   */
  public abstract <S extends BaseItemIterable> S getIterable(Client client) throws ChainException;

  public BaseQueryBuilder() {
    this.next = new Query();
  }

  /**
   * Specifies a cursor value for retrieving the next page.
   * @param after an opaque cursor object
   * @return updated builder
   */
  public T setAfter(String after) {
    this.next.after = after;
    return (T) this;
  }

  /**
   * Sets the filter expression for the query.
   * @param filter a filter expression
   * @return updated builder
   */
  public T setFilter(String filter) {
    this.next.filter = filter;
    return (T) this;
  }

  /**
   * Adds a filter parameter that will be interpolated into the filter expression.
   * @param param a filter parameter
   * @return updated builder
   */
  public T addFilterParameter(Object param) {
    this.next.filterParams.add(param);
    return (T) this;
  }

  /**
   * Specifies the parameters that will be interpolated into the filter expression.
   * @param params list of filter parameters
   */
  public T setFilterParameters(List<?> params) {
    this.next.filterParams = new ArrayList<>(params);
    return (T) this;
  }
}
