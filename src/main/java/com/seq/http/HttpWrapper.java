package com.seq.http;

import com.seq.common.Utils;
import com.seq.exception.*;
import com.google.gson.Gson;
import com.squareup.okhttp.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpWrapper {
  private OkHttpClient httpClient;
  private List<URL> urls;
  private AtomicInteger urlIndex;
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private static String version = "dev"; // updated in the static initializer

  public HttpWrapper(List<URL> urls) {
    this.urls = urls;
    this.httpClient = buildHttpClient();
    this.urlIndex = new AtomicInteger(0);
  }

  /**
   * Defines an interface for deserializing HTTP responses into objects.
   * @param <T> the type of object to return
   */
  public interface ResponseCreator<T> {
    /**
     * Deserializes an HTTP response into a Java object of type T.
     * @param response HTTP response object
     * @param deserializer json deserializer
     * @return an object of type T
     * @throws ChainException
     * @throws IOException
     */
    T create(Response response, Gson deserializer) throws ChainException, IOException;
  }

  /**
   * Builds and executes an HTTP Post request.
   * @param path the path to the endpoint
   * @param body the request body
   * @param tClass object specifying the response structure
   * @return a response deserialized into type T
   * @throws ChainException
   */
  public <T> T post(String path, Object body, final Type tClass) throws ChainException {
    ResponseCreator<T> rc =
        new ResponseCreator<T>() {
          public T create(Response response, Gson deserializer) throws IOException {
            return deserializer.fromJson(response.body().charStream(), tClass);
          }
        };
    RequestBody reqBody = RequestBody.create(this.JSON, Utils.serializer.toJson(body));

    ChainException exception = null;
    for (int attempt = 1; attempt - 1 <= MAX_RETRIES; attempt++) {

      int idx = this.urlIndex.get();
      URL endpointURL;
      try {
        URI u = new URI(this.urls.get(idx % this.urls.size()).toString() + "/" + path);
        u = u.normalize();
        endpointURL = new URL(u.toString());
      } catch (MalformedURLException ex) {
        throw new BadURLException(ex.getMessage());
      } catch (URISyntaxException ex) {
        throw new BadURLException(ex.getMessage());
      }

      Request req =
          new Request.Builder()
              .header("User-Agent", "sequence-sdk-java/" + version)
              .url(endpointURL)
              .method("POST", reqBody)
              .build();

      // Wait between retrys. The first attempt will not wait at all.
      if (attempt > 1) {
        int delayMillis = retryDelayMillis(attempt - 1);
        try {
          TimeUnit.MILLISECONDS.sleep(delayMillis);
        } catch (InterruptedException e) {
        }
      }

      try {
        Response resp = this.checkError(this.httpClient.newCall(req).execute());
        return rc.create(resp, Utils.serializer);
      } catch (IOException ex) {
        // This URL's process might be unhealthy; move to the next.
        this.nextURL(idx);

        // The OkHttp library already performs retries for some
        // I/O-related errors, but we've hit this case in a leader
        // failover, so do our own retries too.
        exception = new ConfigurationException(ex.getMessage());
      } catch (ConnectivityException ex) {
        // This URL's process might be unhealthy; move to the next.
        this.nextURL(idx);

        // ConnectivityExceptions are always retriable.
        exception = ex;
      } catch (APIException ex) {
        if (!ex.retriable) {
          throw ex;
        }

        // This URL's process might be unhealthy; move to the next.
        this.nextURL(idx);
        exception = ex;
      }
    }
    throw exception;
  }

  private static final Random randomGenerator = new Random();
  private static final int MAX_RETRIES = 10;
  private static final int RETRY_BASE_DELAY_MILLIS = 40;

  // the max amount of time cored leader election could take
  private static final int RETRY_MAX_DELAY_MILLIS = 15000;

  private static int retryDelayMillis(int retryAttempt) {
    // Calculate the max delay as base * 2 ^ (retryAttempt - 1).
    int max = RETRY_BASE_DELAY_MILLIS * (1 << (retryAttempt - 1));
    max = Math.min(max, RETRY_MAX_DELAY_MILLIS);

    // To incorporate jitter, use a pseudo random delay between [max/2, max] millis.
    return randomGenerator.nextInt(max / 2) + max / 2 + 1;
  }

  private Response checkError(Response response) throws ChainException {
    String rid = response.headers().get("Chain-Request-ID");
    if (rid == null || rid.length() == 0) {
      // Header field Chain-Request-ID is set by the backend
      // API server. If this field is set, then we can expect
      // the body to be well-formed JSON. If it's not set,
      // then we are probably talking to a gateway or proxy.
      throw new ConnectivityException(response);
    }

    if ((response.code() / 100) != 2) {
      try {
        APIException err =
            Utils.serializer.fromJson(response.body().charStream(), APIException.class);
        if (err.code != null) {
          err.requestId = rid;
          err.statusCode = response.code();
          throw err;
        }
      } catch (IOException ex) {
        throw new JSONException("Unable to read body. " + ex.getMessage(), rid);
      }
    }
    return response;
  }

  private void nextURL(int failedIndex) {
    if (this.urls.size() == 1) {
      return; // No point contending on the CAS if there's only one URL.
    }

    // A request to the url at failedIndex just failed. Move to the next
    // URL in the items.
    int nextIndex = failedIndex + 1;
    this.urlIndex.compareAndSet(failedIndex, nextIndex);
  }

  private OkHttpClient buildHttpClient() {
    OkHttpClient hc = new OkHttpClient();
    hc.setReadTimeout(30, TimeUnit.SECONDS);
    hc.setWriteTimeout(30, TimeUnit.SECONDS);
    hc.setConnectTimeout(30, TimeUnit.SECONDS);
    hc.setConnectionPool(new ConnectionPool(30, TimeUnit.MINUTES.toMillis(2)));
    return hc;
  }
}
