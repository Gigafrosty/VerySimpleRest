package ekraft.verysimplerest.lambda;


import ekraft.verysimplerest.RestClient;
import ekraft.verysimplerest.RestServer;
import ekraft.verysimplerest.utils.RestException;
import ekraft.verysimplerest.utils.TestUtils;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class LambdaHttpHandlerTests {

  private static final int TEST_PORT = 8883;
  private RestServer server = new RestServer(TEST_PORT);
  private RestClient client = new RestClient("localhost", TEST_PORT);


  @After
  public void cleanup()
    throws Exception {

    RestException.setHandler(null);

    server.shutdown();
    while (TestUtils.serverIsUp(TEST_PORT)) {
      Thread.sleep(1);
    }
  }


  @Test
  public void serverStartsAndStops()
    throws Exception {

    assertFalse(TestUtils.serverIsUp(TEST_PORT));
    server.get("path", () -> "Do nothing");
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.removePath("path");
    assertFalse(TestUtils.serverIsUp(TEST_PORT));

    assertFalse(TestUtils.serverIsUp(TEST_PORT));
    server.get("path", () -> "Do nothing");
    server.get("path2", () -> "Also Do nothing");
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.removePath("path");
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.removePath("path2");
    assertFalse(TestUtils.serverIsUp(TEST_PORT));
  }


  @Test
  public void leadingSlashIsOptional()
    throws Exception {

    assertFalse(TestUtils.serverIsUp(TEST_PORT));
    server.get("path", () -> "Do nothing");
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.removePath("/path");
    assertFalse(TestUtils.serverIsUp(TEST_PORT));

    assertFalse(TestUtils.serverIsUp(TEST_PORT));
    server.get("/path", () -> "Do nothing");
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.removePath("path");
    assertFalse(TestUtils.serverIsUp(TEST_PORT));
  }


  @Test
  public void handlesRemovingNonexistentPaths()
    throws Exception {

    assertFalse(TestUtils.serverIsUp(TEST_PORT));
    server.get("path", () -> "Do nothing");
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.removePath("notpath");
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.removePath("path");
    assertFalse(TestUtils.serverIsUp(TEST_PORT));

    server.removePath("path");
    assertFalse(TestUtils.serverIsUp(TEST_PORT));
  }


  @Test
  public void testVoidNoParameters() {

    server.get("get", LambdaTestService::get1);
    server.put("put", LambdaTestService::put1);
    server.post("post", LambdaTestService::post1);
    server.delete("delete", LambdaTestService::delete1);
    server.all("all", LambdaTestService::all1);

    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertTrue(client.get("/get", Boolean.class));
    assertArrayEquals(new String[]{"getVoid"}, LambdaTestService.getCalled());
    assertNull(client.put("/get", Boolean.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.post("/get", Boolean.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.delete("/get", Boolean.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertNull(client.get("/put", Boolean.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertTrue(client.put("/put", Boolean.class));
    assertArrayEquals(new String[]{"putVoid"}, LambdaTestService.getCalled());
    assertNull(client.post("/put", Boolean.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.delete("/put", Boolean.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertNull(client.get("/post", Boolean.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.put("/post", Boolean.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertTrue(client.post("/post", Boolean.class));
    assertArrayEquals(new String[]{"postVoid"}, LambdaTestService.getCalled());
    assertNull(client.delete("/post", Boolean.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertNull(client.get("/delete", Boolean.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.put("/delete", Boolean.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.post("/delete", Boolean.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertTrue(client.delete("/delete", Boolean.class));
    assertArrayEquals(new String[]{"deleteVoid"}, LambdaTestService.getCalled());

    assertTrue(client.get("/all", Boolean.class));
    assertArrayEquals(new String[]{"allVoid"}, LambdaTestService.getCalled());
    assertTrue(client.put("/all", Boolean.class));
    assertArrayEquals(new String[]{"allVoid"}, LambdaTestService.getCalled());
    assertTrue(client.post("/all", Boolean.class));
    assertArrayEquals(new String[]{"allVoid"}, LambdaTestService.getCalled());
    assertTrue(client.delete("/all", Boolean.class));
    assertArrayEquals(new String[]{"allVoid"}, LambdaTestService.getCalled());
  }


  @Test
  public void testStringReturnType() {

    String get = "/get";
    String put = "/put";
    String post = "/post";
    String delete = "/delete";
    String all = "/all";

    server.get(get, LambdaTestService::get2);
    server.put(put, LambdaTestService::put2);
    server.post(post, LambdaTestService::post2);
    server.delete(delete, LambdaTestService::delete2);
    server.all(all, LambdaTestService::all2);

    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertEquals("get2", client.get(get, String.class));
    assertArrayEquals(new String[]{"getString"}, LambdaTestService.getCalled());
    assertNull(client.put(get, String.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.post(get, String.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.delete(get, String.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertNull(client.get(put, String.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertEquals("put2", client.put(put, String.class));
    assertArrayEquals(new String[]{"putString"}, LambdaTestService.getCalled());
    assertNull(client.post(put, String.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.delete(put, String.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertNull(client.get(post, String.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.put(post, String.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertEquals("post2", client.post(post, String.class));
    assertArrayEquals(new String[]{"postString"}, LambdaTestService.getCalled());
    assertNull(client.delete(post, String.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertNull(client.get(delete, String.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.put(delete, String.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.post(delete, String.class));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertEquals("delete2", client.delete(delete, String.class));
    assertArrayEquals(new String[]{"deleteString"}, LambdaTestService.getCalled());

    assertEquals("all2", client.get(all, String.class));
    assertArrayEquals(new String[]{"allString"}, LambdaTestService.getCalled());
    assertEquals("all2", client.put(all, String.class));
    assertArrayEquals(new String[]{"allString"}, LambdaTestService.getCalled());
    assertEquals("all2", client.post(all, String.class));
    assertArrayEquals(new String[]{"allString"}, LambdaTestService.getCalled());
    assertEquals("all2", client.delete(all, String.class));
    assertArrayEquals(new String[]{"allString"}, LambdaTestService.getCalled());
  }


  @Test
  public void testVoidAsteriskParameter() {

    String get = "/get/*";
    String put = "/put/*";
    String post = "/post/*";
    String delete = "/delete/*";
    String all = "/all/*";

    server.get(get, request -> {
      LambdaTestService.get3(request.get(0));
      request.respond(true);
    });
    server.put(put, request -> {
      LambdaTestService.put3(request.get(0));
      request.respond(true);
    });
    server.post(post, request -> {
      LambdaTestService.post3(request.get(0));
      request.respond(true);
    });
    server.delete(delete, request -> {
      LambdaTestService.delete3(request.get(0));
      request.respond(true);
    });
    server.all(all, request -> {
      LambdaTestService.all3(request.get(0));
      request.respond(true);
    });

    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertTrue(client.get(get, Boolean.class, "name"));
    assertArrayEquals(new String[]{"getVoid,name"}, LambdaTestService.getCalled());
    assertNull(client.put(get, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.post(get, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.delete(get, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertNull(client.get(put, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertTrue(client.put(put, Boolean.class, "name"));
    assertArrayEquals(new String[]{"putVoid,name"}, LambdaTestService.getCalled());
    assertNull(client.post(put, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.delete(put, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertNull(client.get(post, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.put(post, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertTrue(client.post(post, Boolean.class, "name"));
    assertArrayEquals(new String[]{"postVoid,name"}, LambdaTestService.getCalled());
    assertNull(client.delete(post, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertNull(client.get(delete, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.put(delete, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.post(delete, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertTrue(client.delete(delete, Boolean.class, "name"));
    assertArrayEquals(new String[]{"deleteVoid,name"}, LambdaTestService.getCalled());

    assertTrue(client.get(all, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allVoid,name"}, LambdaTestService.getCalled());
    assertTrue(client.put(all, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allVoid,name"}, LambdaTestService.getCalled());
    assertTrue(client.post(all, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allVoid,name"}, LambdaTestService.getCalled());
    assertTrue(client.delete(all, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allVoid,name"}, LambdaTestService.getCalled());
  }


  @Test
  public void testVoidNamedParameter() {

    String get = "/get/{id}";
    String put = "/put/{id}";
    String post = "/post/{id}";
    String delete = "/delete/{id}";
    String all = "/all/{id}";

    server.get(get, request -> {
      LambdaTestService.get3(request.get("id"));
      request.respond(true);
    });
    server.put(put, request -> {
      LambdaTestService.put3(request.get("id"));
      request.respond(true);
    });
    server.post(post, request -> {
      LambdaTestService.post3(request.get("id"));
      request.respond(true);
    });
    server.delete(delete, request -> {
      LambdaTestService.delete3(request.get("id"));
      request.respond(true);
    });
    server.all(all, request -> {
      LambdaTestService.all3(request.get("id"));
      request.respond(true);
    });

    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertTrue(client.get(get, Boolean.class, "name"));
    assertArrayEquals(new String[]{"getVoid,name"}, LambdaTestService.getCalled());
    assertNull(client.put(get, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.post(get, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.delete(get, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertNull(client.get(put, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertTrue(client.put(put, Boolean.class, "name"));
    assertArrayEquals(new String[]{"putVoid,name"}, LambdaTestService.getCalled());
    assertNull(client.post(put, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.delete(put, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertNull(client.get(post, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.put(post, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertTrue(client.post(post, Boolean.class, "name"));
    assertArrayEquals(new String[]{"postVoid,name"}, LambdaTestService.getCalled());
    assertNull(client.delete(post, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());

    assertNull(client.get(delete, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.put(delete, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertNull(client.post(delete, Boolean.class, "name"));
    assertArrayEquals(new String[0], LambdaTestService.getCalled());
    assertTrue(client.delete(delete, Boolean.class, "name"));
    assertArrayEquals(new String[]{"deleteVoid,name"}, LambdaTestService.getCalled());

    assertTrue(client.get(all, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allVoid,name"}, LambdaTestService.getCalled());
    assertTrue(client.put(all, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allVoid,name"}, LambdaTestService.getCalled());
    assertTrue(client.post(all, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allVoid,name"}, LambdaTestService.getCalled());
    assertTrue(client.delete(all, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allVoid,name"}, LambdaTestService.getCalled());
  }


  @Test
  public void handleMalformedClientData() {

    server.put("test", () -> "no error");

    String response = RestClient.communicate("localhost", TEST_PORT, "PUT", "/test", "\"", true);
    assertEquals(
      "Unexpected end-of-input: was expecting closing quote for a string value\n" + " at [Source: \"; line: 1, column: 3]",
      response);
  }


  @Test
  public void handlesThrownException() {

    server.put("test", () -> {
      throw new RestException("Expected Error");
    });

    String response = RestClient.communicate("localhost", TEST_PORT, "PUT", "/test", null, true);
    assertEquals("Expected Error", response);
  }


  @Test
  public void lambdaTimeoutTest()
    throws Exception {

    List<Throwable> list = new ArrayList<>();
    RestException.setHandler(list::add);

    server.get("/services/timeout", request -> {
      try {
        Thread.sleep(6000);
      } catch (InterruptedException e) {
        RestException.log(e);
      }

      try {
        request.respond(true);
      } catch (Exception e) {
        RestException.log(e);
      }
    });

    String response = RestClient.communicate("localhost", TEST_PORT, "GET", "/services/timeout", null, true);
    assertEquals("Timed Out.", response);

    Thread.sleep(2000);

    assertEquals(1, list.size());
    Throwable throwable = list.get(0);
    assertEquals(IllegalStateException.class, throwable.getClass());
    assertEquals("Attempted to respond when already responded!", throwable.getMessage());
  }
}
