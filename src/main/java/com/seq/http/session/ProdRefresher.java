package com.seq.http.session;

import com.seq.exception.ChainException;
import com.seq.exception.ConfigurationException;
import com.seq.http.HttpWrapper;
import com.google.gson.annotations.SerializedName;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ProdRefresher implements Refresher {
  private class SessionResponse {
    @SerializedName("refresh_at")
    public Integer refreshAt;

    @SerializedName("team_name")
    public String teamName;

    @SerializedName("refresh_token")
    public String dischargeMacaroon;

    public SessionResponse() {
      this.refreshAt = 0;
      this.teamName = null;
      this.dischargeMacaroon = null;
    }
  }

  private HttpWrapper http;
  private SessionResponse resp;

  public ProdRefresher() {
    try {
      List<URL> sessionURLs =
          new ArrayList<>(Arrays.asList(new URL("https://session-api.seq.com")));
      this.http = new HttpWrapper(sessionURLs);
      this.resp = new SessionResponse();
    } catch (MalformedURLException e) {
      new ConfigurationException(e.getMessage());
    }
  }

  public String teamName() {
    return this.resp.teamName;
  }

  public String dischargeMacaroon() {
    return this.resp.dischargeMacaroon;
  }

  public boolean needsRefresh() {
    return this.resp.refreshAt < System.currentTimeMillis() / 1000;
  }

  public void refresh(String macaroon) throws ChainException {
    Map<String, String> sessionReq = new HashMap<String, String>();
    sessionReq.put("macaroon", macaroon);
    this.resp = this.http.post("/sessions/validate", sessionReq, SessionResponse.class);
  }
}
