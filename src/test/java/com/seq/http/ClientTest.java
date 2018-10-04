package com.seq.http;

import com.seq.TestUtils;
import com.google.gson.stream.JsonReader;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

public class ClientTest {
    static Client client;
    @Test
    public void testClientVersion() throws Exception {
        client = TestUtils.generateClient();
        boolean foundVersion = false;
        InputStream in = Client.class.getClassLoader().getResourceAsStream("properties.json");
        JsonReader reader = new JsonReader(new InputStreamReader(in));
        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.nextName().equals("version")){
                assertEquals(Client.getVersion(),reader.nextString());
                foundVersion = true;
                break;
            }
        }
        reader.endObject();
        assertTrue(foundVersion);
    }
}
