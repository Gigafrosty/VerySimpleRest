package ekraft.verysimplerest;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import ekraft.verysimplerest.utils.RestException;
import ekraft.verysimplerest.utils.SimpleEchoServer;
import ekraft.verysimplerest.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class RestClientTest {

  private static final int ECHO_TEST_PORT = 8881;
  private static final int REST_TEST_PORT = 8882;

  private SimpleEchoServer echoServer;
  private RestClient echoClient;

  private RestServer restServer;
  private RestClient restClient;


  @Before
  public void setup()
    throws Exception {

    echoServer = new SimpleEchoServer(ECHO_TEST_PORT);
    echoClient = new RestClient("localhost", ECHO_TEST_PORT);

    restServer = new RestServer(REST_TEST_PORT);
    restClient = new RestClient("localhost", REST_TEST_PORT);
  }


  @After
  public void cleanup()
    throws Exception {

    RestException.addHandler(null);

    echoServer.shutdown();
    restServer.shutdown();

    while (TestUtils.serverIsUp(ECHO_TEST_PORT)) {
      Thread.sleep(1);
    }

    while (TestUtils.serverIsUp(REST_TEST_PORT)) {
      Thread.sleep(1);
    }
  }


  @Test
  public void clientConnects()
    throws Exception {

    echoClient.get("/");
    assertEquals("GET,/,", echoServer.getLastEcho());

    echoClient.put("/");
    assertEquals("PUT,/,", echoServer.getLastEcho());

    echoClient.post("/");
    assertEquals("POST,/,", echoServer.getLastEcho());

    echoClient.delete("/");
    assertEquals("DELETE,/,", echoServer.getLastEcho());
  }


  @Test
  public void clientHandlesParametersNoReturnType()
    throws Exception {

    echoClient.get("/");
    assertEquals("GET,/,", echoServer.getLastEcho());
    echoClient.put("/");
    assertEquals("PUT,/,", echoServer.getLastEcho());
    echoClient.post("/");
    assertEquals("POST,/,", echoServer.getLastEcho());
    echoClient.delete("/");
    assertEquals("DELETE,/,", echoServer.getLastEcho());

    echoClient.get("/", (Object[]) null);
    assertEquals("GET,/,", echoServer.getLastEcho());
    echoClient.put("/", (Object[]) null);
    assertEquals("PUT,/,", echoServer.getLastEcho());
    echoClient.post("/", (Object[]) null);
    assertEquals("POST,/,", echoServer.getLastEcho());
    echoClient.delete("/", (Object[]) null);
    assertEquals("DELETE,/,", echoServer.getLastEcho());

    echoClient.get("/", new Object[0]);
    assertEquals("GET,/,", echoServer.getLastEcho());
    echoClient.put("/", new Object[0]);
    assertEquals("PUT,/,", echoServer.getLastEcho());
    echoClient.post("/", new Object[0]);
    assertEquals("POST,/,", echoServer.getLastEcho());
    echoClient.delete("/", new Object[0]);
    assertEquals("DELETE,/,", echoServer.getLastEcho());

    echoClient.get("/", "test");
    assertEquals("GET,/,", echoServer.getLastEcho());
    echoClient.put("/", "test");
    assertEquals("PUT,/,test", echoServer.getLastEcho());
    echoClient.post("/", "test");
    assertEquals("POST,/,test", echoServer.getLastEcho());
    echoClient.delete("/", "test");
    assertEquals("DELETE,/,test", echoServer.getLastEcho());

    echoClient.get("/", 4);
    assertEquals("GET,/,", echoServer.getLastEcho());
    echoClient.put("/", 5);
    assertEquals("PUT,/,5", echoServer.getLastEcho());
    echoClient.post("/", 6);
    assertEquals("POST,/,6", echoServer.getLastEcho());
    echoClient.delete("/", 7);
    assertEquals("DELETE,/,7", echoServer.getLastEcho());

    echoClient.get("/*", "name");
    assertEquals("GET,/name,", echoServer.getLastEcho());
    echoClient.put("/*", "name");
    assertEquals("PUT,/name,", echoServer.getLastEcho());
    echoClient.post("/*", "name");
    assertEquals("POST,/name,", echoServer.getLastEcho());
    echoClient.delete("/*", "name");
    assertEquals("DELETE,/name,", echoServer.getLastEcho());

    echoClient.get("/{id}", "name");
    assertEquals("GET,/name,", echoServer.getLastEcho());
    echoClient.put("/{id}", "name");
    assertEquals("PUT,/name,", echoServer.getLastEcho());
    echoClient.post("/{id}", "name");
    assertEquals("POST,/name,", echoServer.getLastEcho());
    echoClient.delete("/{id}", "name");
    assertEquals("DELETE,/name,", echoServer.getLastEcho());

    echoClient.get("/*", "name", 5);
    assertEquals("GET,/name,", echoServer.getLastEcho());
    echoClient.put("/*", "name", 6);
    assertEquals("PUT,/name,6", echoServer.getLastEcho());
    echoClient.post("/*", "name", 7);
    assertEquals("POST,/name,7", echoServer.getLastEcho());
    echoClient.delete("/*", "name", 8);
    assertEquals("DELETE,/name,8", echoServer.getLastEcho());

    echoClient.get("/{id}", "name", 5);
    assertEquals("GET,/name,", echoServer.getLastEcho());
    echoClient.put("/{id}", "name", 6);
    assertEquals("PUT,/name,6", echoServer.getLastEcho());
    echoClient.post("/{id}", "name", 7);
    assertEquals("POST,/name,7", echoServer.getLastEcho());
    echoClient.delete("/{id}", "name", 8);
    assertEquals("DELETE,/name,8", echoServer.getLastEcho());

    echoClient.get("/*", "name", 5, "blob");
    assertEquals("GET,/name,", echoServer.getLastEcho());
    echoClient.put("/*", "name", 6, "blob");
    assertEquals("PUT,/name,[6,\"blob\"]", echoServer.getLastEcho());
    echoClient.post("/*", "name", 7, "blob");
    assertEquals("POST,/name,[7,\"blob\"]", echoServer.getLastEcho());
    echoClient.delete("/*", "name", 8, "blob");
    assertEquals("DELETE,/name,[8,\"blob\"]", echoServer.getLastEcho());

    echoClient.get("/{id}", "name", 5, 'g');
    assertEquals("GET,/name,", echoServer.getLastEcho());
    echoClient.put("/{id}", "name", 6, "blob");
    assertEquals("PUT,/name,[6,\"blob\"]", echoServer.getLastEcho());
    echoClient.post("/{id}", "name", 7, "blob");
    assertEquals("POST,/name,[7,\"blob\"]", echoServer.getLastEcho());
    echoClient.delete("/{id}", "name", 8, "blob");
    assertEquals("DELETE,/name,[8,\"blob\"]", echoServer.getLastEcho());

    echoClient.get("/{id}", "name", 5, 'g');
    assertEquals("GET,/name,", echoServer.getLastEcho());
    echoClient.put("/{id}", "name", 6, "blob");
    assertEquals("PUT,/name,[6,\"blob\"]", echoServer.getLastEcho());
    echoClient.post("/{id}", "name", 7, "blob");
    assertEquals("POST,/name,[7,\"blob\"]", echoServer.getLastEcho());
    echoClient.delete("/{id}", "name", 8, "blob");
    assertEquals("DELETE,/name,[8,\"blob\"]", echoServer.getLastEcho());

    echoClient.get("/*/{id}/*", "name", 5, "blob");
    assertEquals("GET,/name/5/blob,", echoServer.getLastEcho());
    echoClient.put("/*/{id}/*", "name", 6, "blob");
    assertEquals("PUT,/name/6/blob,", echoServer.getLastEcho());
    echoClient.post("/*/{id}/*", "name", 7, "blob");
    assertEquals("POST,/name/7/blob,", echoServer.getLastEcho());
    echoClient.delete("/*/{id}/*", "name", 8, "blob");
    assertEquals("DELETE,/name/8/blob,", echoServer.getLastEcho());
  }


  @Test
  public void clientHandlesParametersWithReturnType()
    throws Exception {

    assertEquals("GET,/,", echoClient.get("/", String.class));
    assertEquals("PUT,/,", echoClient.put("/", String.class));
    assertEquals("POST,/,", echoClient.post("/", String.class));
    assertEquals("DELETE,/,", echoClient.delete("/", String.class));

    assertEquals("GET,/,", echoClient.get("/", String.class, (Object[]) null));
    assertEquals("PUT,/,", echoClient.put("/", String.class, (Object[]) null));
    assertEquals("POST,/,", echoClient.post("/", String.class, (Object[]) null));
    assertEquals("DELETE,/,", echoClient.delete("/", String.class, (Object[]) null));

    assertEquals("GET,/,", echoClient.get("/", String.class, new Object[0]));
    assertEquals("PUT,/,", echoClient.put("/", String.class, new Object[0]));
    assertEquals("POST,/,", echoClient.post("/", String.class, new Object[0]));
    assertEquals("DELETE,/,", echoClient.delete("/", String.class, new Object[0]));

    assertEquals("GET,/,", echoClient.get("/", String.class, "test"));
    assertEquals("PUT,/,test", echoClient.put("/", String.class, "test"));
    assertEquals("POST,/,test", echoClient.post("/", String.class, "test"));
    assertEquals("DELETE,/,test", echoClient.delete("/", String.class, "test"));

    assertEquals("GET,/,", echoClient.get("/", String.class, 4));
    assertEquals("PUT,/,5", echoClient.put("/", String.class, 5));
    assertEquals("POST,/,6", echoClient.post("/", String.class, 6));
    assertEquals("DELETE,/,7", echoClient.delete("/", String.class, 7));

    assertEquals("GET,/name,", echoClient.get("/*", String.class, "name"));
    assertEquals("PUT,/name,", echoClient.put("/*", String.class, "name"));
    assertEquals("POST,/name,", echoClient.post("/*", String.class, "name"));
    assertEquals("DELETE,/name,", echoClient.delete("/*", String.class, "name"));

    assertEquals("GET,/name,", echoClient.get("/{id}", String.class, "name"));
    assertEquals("PUT,/name,", echoClient.put("/{id}", String.class, "name"));
    assertEquals("POST,/name,", echoClient.post("/{id}", String.class, "name"));
    assertEquals("DELETE,/name,", echoClient.delete("/{id}", String.class, "name"));

    assertEquals("GET,/name,", echoClient.get("/*", String.class, "name", 5));
    assertEquals("PUT,/name,6", echoClient.put("/*", String.class, "name", 6));
    assertEquals("POST,/name,7", echoClient.post("/*", String.class, "name", 7));
    assertEquals("DELETE,/name,8", echoClient.delete("/*", String.class, "name", 8));

    assertEquals("GET,/name,", echoClient.get("/{id}", String.class, "name", 5));
    assertEquals("PUT,/name,6", echoClient.put("/{id}", String.class, "name", 6));
    assertEquals("POST,/name,7", echoClient.post("/{id}", String.class, "name", 7));
    assertEquals("DELETE,/name,8", echoClient.delete("/{id}", String.class, "name", 8));

    assertEquals("GET,/name,", echoClient.get("/*", String.class, "name", 5, "blob"));
    assertEquals("PUT,/name,[6,\"blob\"]", echoClient.put("/*", String.class, "name", 6, "blob"));
    assertEquals("POST,/name,[7,\"blob\"]", echoClient.post("/*", String.class, "name", 7, "blob"));
    assertEquals("DELETE,/name,[8,\"blob\"]", echoClient.delete("/*", String.class, "name", 8, "blob"));

    assertEquals("GET,/name,", echoClient.get("/{id}", String.class, "name", 5, 'g'));
    assertEquals("PUT,/name,[6,\"blob\"]", echoClient.put("/{id}", String.class, "name", 6, "blob"));
    assertEquals("POST,/name,[7,\"blob\"]", echoClient.post("/{id}", String.class, "name", 7, "blob"));
    assertEquals("DELETE,/name,[8,\"blob\"]", echoClient.delete("/{id}", String.class, "name", 8, "blob"));

    assertEquals("GET,/name,", echoClient.get("/{id}", String.class, "name", 5, 'g'));
    assertEquals("PUT,/name,[6,\"blob\"]", echoClient.put("/{id}", String.class, "name", 6, "blob"));
    assertEquals("POST,/name,[7,\"blob\"]", echoClient.post("/{id}", String.class, "name", 7, "blob"));
    assertEquals("DELETE,/name,[8,\"blob\"]", echoClient.delete("/{id}", String.class, "name", 8, "blob"));

    assertEquals("GET,/name/5/blob,", echoClient.get("/*/{id}/*", String.class, "name", 5, "blob"));
    assertEquals("PUT,/name/6/blob,", echoClient.put("/*/{id}/*", String.class, "name", 6, "blob"));
    assertEquals("POST,/name/7/blob,", echoClient.post("/*/{id}/*", String.class, "name", 7, "blob"));
    assertEquals("DELETE,/name/8/blob,", echoClient.delete("/*/{id}/*", String.class, "name", 8, "blob"));
  }


  @Test
  public void clientExceptionsOnNonDataParameter() {

    try {
      echoClient.get("/", new Object() {
        private int variable;
      });
      fail("JSon serializer should have thrown exception.");
    } catch (RestException e) {
      // Expecting exception
    }
  }


  @Test
  public void throwsExceptionOnBadReturnType() {

    restServer.get("test", () -> "12345");

    assertEquals("12345", restClient.get("/test", String.class));
    assertEquals(new Integer(12345), restClient.get("/test", Integer.class));
    try {
      restClient.get("/test", Boolean.class);
      fail("Expected to ");
    } catch (RestException e) {
      assertEquals(InvalidFormatException.class, e.getCause().getClass());
    }
  }
}
