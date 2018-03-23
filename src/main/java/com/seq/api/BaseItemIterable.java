package com.seq.api;

import com.seq.exception.ChainException;
import com.seq.http.Client;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class BaseItemIterable<T> implements Iterable<T> {

  private Client client;
  private String path;
  private Query initialQuery;
  private final Type itemClass;

  public BaseItemIterable(Client client, String path, Query query, final Type itemClass) {
    this.client = client;
    this.path = path;
    this.initialQuery = query;
    this.itemClass = itemClass;
  }

  private BasePage<T> getPage() throws ChainException {
    return this.client.request(this.path, this.initialQuery, this.itemClass);
  }

  private BasePage<T> getPage(String cursor) throws ChainException {
    Query next = new Query();
    next.cursor = cursor;
    return this.client.request(this.path, next, this.itemClass);
  }

  public Iterator<T> iterator() {
    return new Iterator<T>() {
      private int pos = 0;
      private List<T> items = new ArrayList<>();
      private boolean lastPage = false;

      /**
       * Returns the next item in the results items.
       * @return api object of type T
       */
      public T next() {
        return items.get(pos++);
      }

      /**
       * Returns true if there is another item in the results items.
       * @return boolean
       */
      public boolean hasNext() {
        if (pos < items.size()) {
          return true;
        } else {
          if (lastPage) {
            return false;
          } else {
            try {
              BasePage<T> page;
              if (initialQuery.cursor == null) {
                page = getPage();
              } else {
                page = getPage(initialQuery.cursor);
              }
              this.pos = 0;
              this.items = page.items;
              this.lastPage = page.lastPage;
              initialQuery.cursor = page.cursor;

              return this.items.size() > 0;
            } catch (ChainException e) {
              return false;
            }
          }
        }
      }

      /**
       * This method is unsupported.
       * @throws UnsupportedOperationException
       */
      public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
      }
    };
  }
}
