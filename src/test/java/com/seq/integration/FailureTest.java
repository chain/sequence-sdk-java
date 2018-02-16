package com.seq.integration;

import com.seq.TestUtils;
import com.seq.api.*;
import com.seq.exception.APIException;
import com.seq.exception.ConfigurationException;
import com.seq.http.Client;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * FailureTest asserts that single-item versions
 * of batch endpoints throw exceptions on error.
 */
public class FailureTest {
  static Client client;

  @Test
  public void run() throws Exception {
    testErrorCode();
    testCreateAccount();
    testCreateAsset();
    testCreateFlavor();
    testTransact();
  }

  public void testErrorCode() throws Exception {
    client = TestUtils.generateClient();
    try {
      new Account.Builder().create(client);
    } catch (APIException e) {
      assertEquals(e.code, "CH202");
      assertEquals(e.seqCode, "SEQ202");
      return;
    }
    throw new Exception("expecting APIException");
  }


  public void testCreateAccount() throws Exception {
    client = TestUtils.generateClient();
    try {
      new Account.Builder().create(client);
    } catch (APIException e) {
      return;
    }
    throw new Exception("expecting APIException");
  }

  public void testCreateAsset() throws Exception {
    client = TestUtils.generateClient();
    try {
      new Asset.Builder().create(client);
    } catch (APIException e) {
      return;
    }
    throw new Exception("expecting APIException");
  }

  public void testCreateFlavor() throws Exception {
    client = TestUtils.generateClient();
    try {
      new Flavor.Builder().create(client);
    } catch (APIException e) {
      return;
    }
    throw new Exception("expecting APIException");
  }

  public void testTransact() throws Exception {
    client = TestUtils.generateClient();
    try {
      new Transaction.Builder().addAction(new Transaction.Builder.Action.Issue()).transact(client);
    } catch (APIException e) {
      return;
    }
    throw new Exception("expecting APIException");
  }
}
