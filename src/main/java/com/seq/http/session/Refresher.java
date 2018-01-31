package com.seq.http.session;

import com.seq.exception.ChainException;

public interface Refresher {
  public boolean needsRefresh();

  public void refresh(String macaroon) throws ChainException;

  public String teamName();

  public String dischargeMacaroon();
}
