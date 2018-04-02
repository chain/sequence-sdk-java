package com.seq.http;

import com.seq.exception.*;
import com.seq.common.*;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;

import com.squareup.okhttp.CertificatePinner;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;

import javax.net.ssl.*;

/**
 * The Client object encapsulates access to the ledger API server.
 */
public class Client {
  private OkHttpClient httpClient;
  private String credential;
  private String ledgerName;
  private String teamName;
  private Boolean teamNameRequested;

  // Used to create empty, in-memory key stores.
  private static final char[] DEFAULT_KEYSTORE_PASSWORD = "password".toCharArray();
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private static String version = "dev"; // updated in the static initializer

  private static class BuildProperties {
    public String version;
  }

  private static class TeamResponse {
    @SerializedName("team_name")
    public String teamName;

    public TeamResponse() {
      this.teamName = null;
    }
  }

  static {
    InputStream in = Client.class.getClassLoader().getResourceAsStream("properties.json");
    if (in != null) {
      InputStreamReader inr = new InputStreamReader(in);
      version = Utils.serializer.fromJson(inr, BuildProperties.class).version;
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
    this.teamName = null;
    this.teamNameRequested = false;
  }

  public void getTeamName(String credential) throws ChainException {
    this.teamNameRequested = true;
    TeamResponse teamResp = new TeamResponse();
    teamResp = request("hello", new Object(), TeamResponse.class);
    this.teamName = teamResp.teamName;
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
    ResponseCreator<T> rc =
        new ResponseCreator<T>() {
          public T create(Response response, Gson deserializer) throws IOException {
            return deserializer.fromJson(response.body().charStream(), tClass);
          }
        };

    if(this.teamNameRequested == false && this.teamName == null) {
      getTeamName(this.credential);
    }
    return post(action, body, rc);
  }

  /**
   * Returns the credential (possibly null).
   * @return the credential
   */
  public String credential() {
    return credential;
  }

  /**
   * Sets the default connect timeout for new connections. A value of 0 means no timeout.
   * @param timeout the number of time units for the default timeout
   * @param unit the unit of time
   */
  public void setConnectTimeout(long timeout, TimeUnit unit) {
    this.httpClient.setConnectTimeout(timeout, unit);
  }

  /**
   * Sets the default read timeout for new connections. A value of 0 means no timeout.
   * @param timeout the number of time units for the default timeout
   * @param unit the unit of time
   */
  public void setReadTimeout(long timeout, TimeUnit unit) {
    this.httpClient.setReadTimeout(timeout, unit);
  }

  /**
   * Sets the default write timeout for new connections. A value of 0 means no timeout.
   * @param timeout the number of time units for the default timeout
   * @param unit the unit of time
   */
  public void setWriteTimeout(long timeout, TimeUnit unit) {
    this.httpClient.setWriteTimeout(timeout, unit);
  }

  /**
   * Sets the proxy information for the HTTP client.
   * @param proxy proxy object
   */
  public void setProxy(Proxy proxy) {
    this.httpClient.setProxy(proxy);
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
   * @param respCreator object specifying the response structure
   * @return a response deserialized into type T
   * @throws ChainException
   */
  private <T> T post(String path, Object body, ResponseCreator<T> respCreator)
      throws ChainException {
    RequestBody requestBody = RequestBody.create(Client.JSON, Utils.serializer.toJson(body));
    String idempotencyKey = UUID.randomUUID().toString();

    ChainException exception = null;
    for (int attempt = 1; attempt - 1 <= MAX_RETRIES; attempt++) {
      String urlParts = "https://api.seq.com";
      String addr = System.getenv("SEQADDR");
      if (addr != null) {
        urlParts = "https://" + addr;
      }

      URL endpointURL;
      try {
        if (urlParts.endsWith("/")) {
          urlParts = urlParts.substring(0, urlParts.length() - 1);
        }
        if (this.teamName != null) {
          urlParts += "/" + this.teamName;
          urlParts += "/" + this.ledgerName;
        }
        urlParts += "/" + path;
        endpointURL = new URL(urlParts);
      } catch (MalformedURLException ex) {
        throw new BadURLException(ex.getMessage());
      }

      Request req =
          new Request.Builder()
              .header("User-Agent", "sequence-sdk-java/" + version)
              .header("Credential", this.credential)
              .header("Idempotency-Key", idempotencyKey)
              .header("Name-Set", "camel|snake")
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
        return respCreator.create(resp, Utils.serializer);
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
    OkHttpClient httpClient = builder.baseHttpClient.clone();

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
        httpClient.setSslSocketFactory(sslContext.getSocketFactory());
      } catch (GeneralSecurityException | IOException ex) {
        throw new ConfigurationException("Unable to configure trusted CA certs", ex);
      }
    }

    if (builder.readTimeoutUnit != null) {
      httpClient.setReadTimeout(builder.readTimeout, builder.readTimeoutUnit);
    }
    if (builder.writeTimeoutUnit != null) {
      httpClient.setWriteTimeout(builder.writeTimeout, builder.writeTimeoutUnit);
    }
    if (builder.connectTimeoutUnit != null) {
      httpClient.setConnectTimeout(builder.connectTimeout, builder.connectTimeoutUnit);
    }
    if (builder.pool != null) {
      httpClient.setConnectionPool(builder.pool);
    }
    if (builder.proxy != null) {
      httpClient.setProxy(builder.proxy);
    }
    if (builder.logger != null) {
      httpClient.interceptors().add(new LoggingInterceptor(builder.logger, builder.logLevel));
    }

    return httpClient;
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
      try {
        APIException err =
            Utils.serializer.fromJson(response.body().charStream(), APIException.class);
        if (err.seqCode != null) {
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
    private OkHttpClient baseHttpClient;
    private String credential;
    private String ledger;
    private long connectTimeout;
    private TimeUnit connectTimeoutUnit;
    private long readTimeout;
    private TimeUnit readTimeoutUnit;
    private long writeTimeout;
    private TimeUnit writeTimeoutUnit;
    private Proxy proxy;
    private ConnectionPool pool;
    private OutputStream logger;
    private LoggingInterceptor.Level logLevel;

    public Builder() {
      this.baseHttpClient = new OkHttpClient();
      this.baseHttpClient.setFollowRedirects(false);
      this.setDefaults();
    }

    private void setDefaults() {
      this.setReadTimeout(30, TimeUnit.SECONDS);
      this.setWriteTimeout(30, TimeUnit.SECONDS);
      this.setConnectTimeout(30, TimeUnit.SECONDS);
      this.setConnectionPool(50, 2, TimeUnit.MINUTES);
      this.logLevel = LoggingInterceptor.Level.ERRORS;
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
      this.connectTimeout = timeout;
      this.connectTimeoutUnit = unit;
      return this;
    }

    /**
     * Sets the read timeout for the client
     * @param timeout the number of time units for the default timeout
     * @param unit the unit of time
     */
    public Builder setReadTimeout(long timeout, TimeUnit unit) {
      this.readTimeout = timeout;
      this.readTimeoutUnit = unit;
      return this;
    }

    /**
     * Sets the write timeout for the client
     * @param timeout the number of time units for the default timeout
     * @param unit the unit of time
     */
    public Builder setWriteTimeout(long timeout, TimeUnit unit) {
      this.writeTimeout = timeout;
      this.writeTimeoutUnit = unit;
      return this;
    }

    /**
     * Sets the proxy for the client
     * @param proxy
     */
    public Builder setProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }

    /**
     * Sets the connection pool for the client
     * @param maxIdle the maximum number of idle http connections in the pool
     * @param timeout the number of time units until an idle http connection in the pool is closed
     * @param unit the unit of time
     */
    public Builder setConnectionPool(int maxIdle, long timeout, TimeUnit unit) {
      this.pool = new ConnectionPool(maxIdle, unit.toMillis(timeout));
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
