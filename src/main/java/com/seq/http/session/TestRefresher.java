package com.seq.http.session;

import com.seq.exception.ChainException;

public class TestRefresher implements Refresher {
  private String teamName;
  private String dischargeMacaroon;

  public TestRefresher(String teamName, String dischargeMacaroon) {
    this.teamName = teamName;
    this.dischargeMacaroon = dischargeMacaroon;
  }

  public String teamName() {
    return this.teamName;
  }

  public String dischargeMacaroon() {
    return this.dischargeMacaroon;
  }

  public boolean needsRefresh() {
    return true;
  }

  public void refresh(String macaroon) throws ChainException {
    return;
  }
}
