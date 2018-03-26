package com.seq.api;

import com.seq.exception.ChainException;
import com.seq.http.Client;

import java.lang.reflect.Type;
import java.util.Iterator;

@Deprecated
public abstract class BasePageIterable<T extends BasePage> implements Iterable<T> {

  private Client client;
  private String path;
  private Query nextQuery;
  private final Type itemClass;

  public BasePageIterable(Client client, String path, Query nextQuery, final Type itemClass) {
    this.client = client;
    this.path = path;
    this.nextQuery = nextQuery;
    this.itemClass = itemClass;
  }

  private T getPage() throws ChainException {
    return this.client.request(this.path, this.nextQuery, this.itemClass);
  }

  public Iterator<T> iterator() {
    return new Iterator<T>() {
      public T currentPage;

      /**
       * Returns the next item in the results items.
       * @return page object of type T
       */
      public T next() {
        return currentPage;
      }

      /**
       * Returns true if there is another item in the results items.
       * @return boolean
       */
      public boolean hasNext() {
        if (currentPage != null && currentPage.lastPage) {
          return false;
        }

        try {
          currentPage = getPage();
          nextQuery = currentPage.next;

          if (currentPage.items.size() == 0) {
            return false;
          }
        } catch (ChainException e) {
          return false;
        }
        return true;
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
