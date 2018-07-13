package com.seq.http;

import com.google.gson.GsonBuilder;
import com.seq.exception.*;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;
import com.google.gson.Gson;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.net.ssl.*;

/**
 * The Client object encapsulates access to the ledger API server.
 */
public class Client {
  private OkHttpClient httpClient;
  private String credential;
  private String ledgerName;
  private String ledgerUrl;
  private Gson serializer;

  // Used to create empty, in-memory key stores.
  private static final char[] DEFAULT_KEYSTORE_PASSWORD = "password".toCharArray();
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private static String version = "dev"; // updated in the static initializer

  private static class BuildProperties {
    public String version;
  }

  private static class HelloResponse {
    @SerializedName("team_name")
    @Expose public String teamName;

    @SerializedName("addr")
    @Expose public String addr;

    public HelloResponse() {
      this.teamName = null;
      this.addr = null;
    }
  }

  static {
    InputStream in = Client.class.getClassLoader().getResourceAsStream("properties.json");
    if (in != null) {
      Gson serializer = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .create();
      InputStreamReader inr = new InputStreamReader(in);
      version = serializer.fromJson(inr, BuildProperties.class).version;
    }
  }

  public Client(Builder builder) throws ConfigurationException {
    if (builder.ledger == null || builder.ledger.isEmpty()) {
      throw new ConfigurationException("No ledger name provided");
    }
    if (builder.credential == null || builder.credential.isEmpty()) {
      throw new ConfigurationException("No credential provided");
    }
    this.ledgerName = builder.ledger;
    this.credential = builder.credential;
    this.httpClient = buildHttpClient(builder);
    this.serializer = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .create();
  }

  public <HelloReponse> void hello() throws ChainException {
    String url = "https://api.seq.com";
    String addr = System.getenv("SEQADDR");
    if (addr != null) {
      url = "https://" + addr;
    }
    url += "/hello";
    HelloResponse resp = post(url, new Object(), HelloResponse.class);
    this.ledgerUrl = "https://" + resp.addr + "/" + resp.teamName + "/" + this.ledgerName;
  }

  /**
   * Perform a single HTTP POST request against the API for a specific action.
   *
   * @param action The requested API action
   * @param body Body payload sent to the API as JSON
   * @param tClass Type of object to be deserialized from the response JSON
   * @return the result of the post request
   * @throws ChainException
   */
  public <T> T request(String action, Object body, final Type tClass) throws ChainException {
    if (this.ledgerUrl == null) {
      hello();
    }
    String url = this.ledgerUrl + "/" + action;
    return post(url, body, tClass);
  }

  /**
   * Returns the credential (possibly null).
   * @return the credential
   */
  public String credential() {
    return credential;
  }

  /**
   * Builds and executes an HTTP Post request.
   * @param url the URL to the endpoint
   * @param body the request body
   * @param tClass Type of object to be deserialized from the response JSON
   * @return a response deserialized into type T
   * @throws ChainException
   */
  private <T> T post(String url, Object body, final Type tClass)
      throws ChainException {
    RequestBody requestBody = RequestBody.create(Client.JSON, this.serializer.toJson(body));

    byte[] bytes = new byte[10];
    new Random().nextBytes(bytes);
    String requestId = DatatypeConverter.printHexBinary(bytes).toLowerCase();

    String idempotencyKey = UUID.randomUUID().toString();

    ChainException exception = null;
    for (int attempt = 1; attempt - 1 <= MAX_RETRIES; attempt++) {
      String attemptId = requestId + '/' + attempt;

      URL endpointURL;
      try {
        endpointURL = new URL(url);
      } catch (MalformedURLException ex) {
        throw new BadURLException(ex.getMessage());
      }

      Request req =
          new Request.Builder()
              .header("User-Agent", "sequence-sdk-java/" + version)
              .header("Credential", this.credential)
              .header("Idempotency-Key", idempotencyKey)
              .header("Name-Set", "camel")
              .header("Id", attemptId)
              .url(endpointURL)
              .method("POST", requestBody)
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
        return this.serializer.fromJson(resp.body().charStream(), tClass);
      } catch (IOException ex) {
        // The OkHttp library already performs retries for some
        // I/O-related errors, but we've hit this case in a leader
        // failover, so do our own retries too.
        exception = new ConfigurationException(ex.getMessage());
      } catch (ConnectivityException ex) {
        // ConnectivityExceptions are always retriable.
        exception = ex;
      } catch (APIException ex) {
        if (!ex.retriable) {
          throw ex;
        }

        exception = ex;
      }
    }
    throw exception;
  }

  private OkHttpClient buildHttpClient(Builder builder) throws ConfigurationException {
    OkHttpClient.Builder httpClientBuilder = builder.httpClientBuilder;

    final String cafile = System.getenv("SEQTLSCA");
    if (cafile != null && cafile.length() > 0) {
      try (InputStream is = new FileInputStream(cafile)) {
        // Extract certs from PEM-encoded input.
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates =
          certificateFactory.generateCertificates(is);
        if (certificates.isEmpty()) {
          throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        // Create a new key store and input the cert.
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, DEFAULT_KEYSTORE_PASSWORD);
        int index = 0;
        for (Certificate certificate : certificates) {
          String certificateAlias = Integer.toString(index++);
          keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use key store to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, DEFAULT_KEYSTORE_PASSWORD);
        TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
          throw new IllegalStateException(
              "Unexpected default trust managers:" + Arrays.toString(trustManagers));
        }

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, trustManagers, null);
        httpClientBuilder.sslSocketFactory(
                sslContext.getSocketFactory(),
                (X509TrustManager)trustManagers[0]
        );
      } catch (GeneralSecurityException | IOException ex) {
        throw new ConfigurationException("Unable to configure trusted CA certs", ex);
      }
    }

    if (builder.logger != null) {
      httpClientBuilder.addInterceptor(new LoggingInterceptor(builder.logger, builder.logLevel));
    }

    return httpClientBuilder.build();
  }

  private static final Random randomGenerator = new Random();
  private static final int MAX_RETRIES = 10;
  private static final int RETRY_BASE_DELAY_MILLIS = 40;

  // the max amount of time ledger leader election could take
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
        APIException err =
            this.serializer.fromJson(response.body().charStream(), APIException.class);
        if (err.seqCode != null) {
          err.requestId = rid;
          err.statusCode = response.code();
          throw err;
        }
    }
    return response;
  }

  /**
   * Overrides {@link Object#hashCode()}
   * @return the hash code
   */
  @Override
  public int hashCode() {
    int code = 0;
    if (this.credential != null) {
      code = code * 31 + this.credential.hashCode();
    }
    return code;
  }

  /**
   * Overrides {@link Object#equals(Object)}
   * @param o the object to compare
   * @return a boolean specifying equality
   */
  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (!(o instanceof Client)) return false;

    Client other = (Client) o;
    return Objects.equals(this.credential, other.credential);
  }

  /**
   * A builder class for creating client objects
   */
  public static class Builder {
    private OkHttpClient.Builder httpClientBuilder;
    private String credential;
    private String ledger;
    private OutputStream logger;
    private LoggingInterceptor.Level logLevel;

    public Builder() {
      this.logLevel = LoggingInterceptor.Level.ERRORS;
      this.httpClientBuilder = new OkHttpClient.Builder()
              .followSslRedirects(false)
              .readTimeout(30, TimeUnit.SECONDS)
              .writeTimeout(30, TimeUnit.SECONDS)
              .connectTimeout(30, TimeUnit.SECONDS)
              .connectionPool(new ConnectionPool(50, 2, TimeUnit.MINUTES));
    }


    /**
     * Sets the credential for the client
     * @param credential an API credential from a user of the ledger's team
     */
    public Builder setCredential(String credential) {
      this.credential = credential;
      return this;
    }

    /**
     * Sets the ledger name for the client.
     * The client will access the named ledger.
     * @param name The ledger name
     */
    public Builder setLedgerName(String name) {
      this.ledger = name;
      return this;
    }

    /**
     * Retrieves the correct private key by trying all supported algorithms. ECDSA and RSA
     * keys are currently supported.
     * @param encodedKey ASN1 encoded private key
     */
    private PrivateKey getPrivateKey(byte[] encodedKey) throws NoSuchAlgorithmException {
      for (String algorithm : new String[] {"RSA", "ECDSA"}) {
        try {
          return KeyFactory.getInstance(algorithm)
              .generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException ignore) {
        }
      }
      return null;
    }

    /**
     * Sets the connect timeout for the client
     * @param timeout the number of time units for the default timeout
     * @param unit the unit of time
     */
    public Builder setConnectTimeout(long timeout, TimeUnit unit) {
      this.httpClientBuilder = this.httpClientBuilder.connectTimeout(timeout, unit);
      return this;
    }

    /**
     * Sets the read timeout for the client
     * @param timeout the number of time units for the default timeout
     * @param unit the unit of time
     */
    public Builder setReadTimeout(long timeout, TimeUnit unit) {
      this.httpClientBuilder = this.httpClientBuilder.readTimeout(timeout, unit);
      return this;
    }

    /**
     * Sets the write timeout for the client
     * @param timeout the number of time units for the default timeout
     * @param unit the unit of time
     */
    public Builder setWriteTimeout(long timeout, TimeUnit unit) {
      this.httpClientBuilder = this.httpClientBuilder.writeTimeout(timeout, unit);
      return this;
    }

    /**
     * Sets the proxy for the client
     * @param proxy
     */
    public Builder setProxy(Proxy proxy) {
      this.httpClientBuilder = this.httpClientBuilder.proxy(proxy);
      return this;
    }

    /**
     * Sets the connection pool for the client
     * @param maxIdle the maximum number of idle http connections in the pool
     * @param timeout the number of time units until an idle http connection in the pool is closed
     * @param unit the unit of time
     */
    public Builder setConnectionPool(int maxIdle, long timeout, TimeUnit unit) {
      this.httpClientBuilder = this.httpClientBuilder.connectionPool(new ConnectionPool(maxIdle, timeout, unit));
      return this;
    }

    /**
     * Sets the request logger.
     * @param logger the output stream to log the requests to
     */
    public Builder setLogger(OutputStream logger) {
      this.logger = logger;
      return this;
    }

    /**
     * Sets the level of the request logger.
     * @param level all, errors or none
     */
    public Builder setLogLevel(LoggingInterceptor.Level level) {
      this.logLevel = level;
      return this;
    }

    /**
     * Builds a client with all of the provided parameters.
     */
    public Client build() throws ConfigurationException {
      return new Client(this);
    }
  }
}
