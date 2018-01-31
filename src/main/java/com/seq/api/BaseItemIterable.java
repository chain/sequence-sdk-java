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
  private Query nextQuery;
  private final Type itemClass;

  public BaseItemIterable(Client client, String path, Query nextQuery, final Type itemClass) {
    this.client = client;
    this.path = path;
    this.nextQuery = nextQuery;
    this.itemClass = itemClass;
  }

  private BasePage<T> getPage() throws ChainException {
    return this.client.request(this.path, this.nextQuery, this.itemClass);
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
          if (!lastPage) {
            try {
              BasePage<T> page = getPage();
              this.pos = 0;
              this.items = page.items;
              this.lastPage = page.lastPage;
              nextQuery = page.next;

              return this.items.size() > 0;
            } catch (ChainException e) {
              return false;
            }
          } else {
            return false;
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
