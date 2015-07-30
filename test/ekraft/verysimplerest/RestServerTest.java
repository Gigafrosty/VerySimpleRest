package ekraft.verysimplerest;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import ekraft.verysimplerest.annotation.AnnotationHttpHandler;
import ekraft.verysimplerest.annotation.AnnotationTestService;
import ekraft.verysimplerest.annotation.AnnotationUtils;
import ekraft.verysimplerest.examples.AnnotationTodoRestService;
import ekraft.verysimplerest.examples.AsyncAnnotationTodoService;
import ekraft.verysimplerest.utils.RestException;
import ekraft.verysimplerest.utils.TestUtils;
import ekraft.verysimplerest.utils.UnserializableData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.Path;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class RestServerTest {

  private static final int TEST_PORT = 8880;

  private RestServer server;
  private RestClient client;


  @Before
  public void setup() {

    server = new RestServer(TEST_PORT);
    client = new RestClient("localhost", TEST_PORT);
  }


  @After
  public void cleanup()
    throws Exception {

    RestException.addHandler(null);

    server.shutdown();
    while (TestUtils.serverIsUp(TEST_PORT)) {
      Thread.sleep(1);
    }
  }


  @Test
  public void serverShutsDown() {

    server.addService(new AnnotationTodoRestService());
    server.addService(new AsyncAnnotationTodoService());
    server.all("/", () -> "test");

    assertTrue(TestUtils.serverIsUp(TEST_PORT));

    server.shutdown();

    assertFalse(TestUtils.serverIsUp(TEST_PORT));

    server.shutdown();
  }


  @Test
  public void serverIgnoresNullService()
    throws Exception {

    try {
      server.addService(null);
      fail("Should have rejected null parameter.");
    } catch (IllegalArgumentException e) {
      // Expected behavior
    }

    assertFalse(TestUtils.serverIsUp(TEST_PORT));
  }


  @Test
  public void serverIgnoresServiceWithNoAnnotations()
    throws Exception {

    try {
      server.addService(new Object());
      fail("Should have rejected parameter.");
    } catch (IllegalArgumentException e) {
      // Expected behavior
    }

    assertFalse(TestUtils.serverIsUp(TEST_PORT));
  }


  @Test
  public void serverIgnoresServiceWithNoAnnotatedMethods()
    throws Exception {

    @Path("path")
    class NoAnnotatedMethods {

      public String hello() {

        return "world";
      }
    }

    try {
      server.addService(new NoAnnotatedMethods());
      fail("Should have rejected parameter.");
    } catch (IllegalArgumentException e) {
      // Expected behavior
    }

    assertFalse(TestUtils.serverIsUp(TEST_PORT));
  }


  @Test
  public void serverIgnoresServiceWithBadAsynchronous()
    throws Exception {

    @Path("path")
    class NoAnnotatedMethods {

      public String hello(Consumer<String> callback) {

        return "world";
      }
    }

    try {
      server.addService(new NoAnnotatedMethods());
      fail("Should have rejected parameter.");
    } catch (IllegalArgumentException e) {
      // Expected behavior
    }

    assertFalse(TestUtils.serverIsUp(TEST_PORT));
  }


  @Test
  public void serverRejectsInvalidPort() {

    try {
      RestServer restServer = new RestServer(-1);
      fail("Should have rejected bad port.");
    } catch (IllegalArgumentException e) {
      // Expected behavior
    }
  }


  @Test
  public void validateDebug() {

    server.setDebug(true);

    server.addService(new AnnotationTodoRestService());
    server.addService(new AsyncAnnotationTodoService());
    server.get("/lambda/get", () -> "get");
    server.put("/lambda/put", () -> "put");
    server.post("/lambda/post", () -> "post");
    server.delete("/lambda/delete", () -> "delete");
    server.all("/lambda/all", () -> "all");

    Set<String> expected = new LinkedHashSet<>();
    Collections.addAll(expected, "GET /lambda/get", "PUT /lambda/put", "POST /lambda/post", "DELETE /lambda/delete",
      "GET /lambda/all", "PUT /lambda/all", "POST /lambda/all", "DELETE /lambda/all", "GET /services/todo",
      "PUT /services/todo", "POST /services/todo", "DELETE /services/todo", "GET /services/todo/*",
      "PUT /services/todo/*", "DELETE /services/todo/*", "GET /services/async", "PUT /services/async",
      "POST /services/async", "DELETE /services/async", "GET /services/async/{id}", "PUT /services/async/{id}",
      "DELETE /services/async/{id}");

    String debug = server.getDebugResponse();
    String[] debugLines = debug.split("\n");
    for (String line : debugLines) {
      assertTrue("Unexpected Line: \"" + line + "\"", expected.remove(line));
    }

    expected.forEach(value -> System.out.println("\"" + value + "\""));
    assertEquals("The preceding lines were not consumed as expected.", 0, expected.size());

    assertEquals(debug, RestClient.communicate("localhost", TEST_PORT, "GET", "/", null, true));
    assertEquals(debug, RestClient.communicate("localhost", TEST_PORT, "PUT", "/", null, true));
    assertEquals(debug, RestClient.communicate("localhost", TEST_PORT, "POST", "/", null, true));
    assertEquals(debug, RestClient.communicate("localhost", TEST_PORT, "DELETE", "/", null, true));
  }


  @Test
  public void serverErrorsOnUnhandledMethod() {

    server.get("/lambda/get", () -> "get");
    assertEquals("Unimplemented Method: OPTIONS",
      RestClient.communicate("localhost", TEST_PORT, "OPTIONS", "/", null, true));

    try {
      RestClient.communicate("localhost", TEST_PORT, "OPTIONS", "/", null, false);
      fail("Should have thrown exception on unhandled method");
    } catch (Exception e) {
      // Exception expected
    }
  }


  @Test
  public void canRemoveSpecificMethods() {

    AnnotationTestService service = new AnnotationTestService();
    server.addService(service);

    assertTrue(client.get(AnnotationTestService.GET1, Boolean.class));
    assertArrayEquals(new String[]{"getVoid"}, service.getCalled());

    assertTrue(client.put(AnnotationTestService.PUT1, Boolean.class));
    assertArrayEquals(new String[]{"putVoid"}, service.getCalled());

    server.removePath(AnnotationUtils.getPath(AnnotationTestService.GET1));

    assertNull(client.get(AnnotationTestService.GET1, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());

    assertTrue(client.put(AnnotationTestService.PUT1, Boolean.class));
    assertArrayEquals(new String[]{"putVoid"}, service.getCalled());
  }


  @Test
  public void multipleConnectionsHandled()
    throws Exception {

    List<Throwable> throwables = new ArrayList<>();
    RestException.addHandler(throwables::add);
    List<String> signal = Collections.synchronizedList(new ArrayList<>());

    server.get("sleep", () -> {
      try {

        signal.add("Signal");
        Thread.sleep(4 * 1000);
        signal.remove(0);

      } catch (InterruptedException e) {
        signal.clear();
      }

      return "done";
    });
    server.get("hello", () -> "world");

    Thread thread = new Thread("BackgroundSleepThread") {
      public void run() {

        assertEquals("done", client.get("/sleep", String.class));
      }
    };
    thread.start();

    long timeout = System.currentTimeMillis() + 5000;
    while (signal.size() == 0) {
      if (System.currentTimeMillis() > timeout) {
        fail("Timed out waiting for signal to start.");
      }
    }

    assertEquals("world", client.get("/hello", String.class));
    assertEquals("Second call didn't happen during sleep period.", 1, signal.size());

    thread.interrupt();
    timeout = System.currentTimeMillis() + 5000;
    while (signal.size() > 0) {
      if (System.currentTimeMillis() > timeout) {
        fail("Timed out waiting for signal to stop.");
      }
    }

    assertEquals(0, throwables.size());
  }


  @Test
  public void throwsRestExceptionWhenAttemptingToStartSecondServer() {

    server.get("foo", () -> "");
    RestServer server2 = new RestServer(TEST_PORT);
    try {
      server2.get("bar", () -> "");
      server2.shutdown();
      fail("Should have failed to start test server on same port.");
    } catch (RestException e) {
      assertEquals(BindException.class, e.getCause().getClass());
    }
  }


  @Test
  public void errorWhenReturningUnserializableObject() {

    server.get("test", UnserializableData::new);
    String result = RestClient.communicate("localhost", TEST_PORT, "GET", "/test", null, true);
    assertTrue(result.startsWith("No serializer found for class ekraft.verysimplerest.utils.UnserializableData"));
  }


  @Test
  public void errorWhenAttemptingToReadIncompatibleParameter() {

    List<Throwable> errors = new ArrayList<>();
    server.put("{id}", (request) -> {
      try {
        request.get(0, UnserializableData.class);
      } catch (Exception e) {
        errors.add(e);
      }
      try {
        request.get("id", UnserializableData.class);
      } catch (Exception e) {
        errors.add(e);
      }
      try {
        request.get("name", UnserializableData.class);
      } catch (Exception e) {
        errors.add(e);
      }
      try {
        request.get(UnserializableData.class);
      } catch (Exception e) {
        errors.add(e);
      }
      try {
        request.get(Map.class);
      } catch (Exception e) {
        errors.add(e);
      }
      request.respond(request.get("id"));
    });

    Map<String, String> parameters = new HashMap<>();
    parameters.put("name", "Jeff");

    assertEquals("name", client.put("/name", String.class, parameters));
    assertEquals(4, errors.size());
    assertEquals(JsonParseException.class, errors.get(0).getCause().getClass());
    assertEquals(JsonParseException.class, errors.get(1).getCause().getClass());
    assertEquals(JsonMappingException.class, errors.get(2).getCause().getClass());
    assertEquals(UnrecognizedPropertyException.class, errors.get(3).getCause().getClass());
  }


  @Test
  public void ioExceptionOnReadRequestBody()
    throws Exception {

    URI uri = new URI("http", null, "localhost", 8080, "/services/todo", null, null);

    HttpExchange fakeHttpExchange = new HttpExchange() {
      private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


      @Override
      public Headers getRequestHeaders() {

        return null;
      }


      @Override
      public Headers getResponseHeaders() {

        return null;
      }


      @Override
      public URI getRequestURI() {

        return uri;
      }


      @Override
      public String getRequestMethod() {

        return "PUT";
      }


      @Override
      public HttpContext getHttpContext() {

        return null;
      }


      @Override
      public void close() {

      }


      @Override
      public InputStream getRequestBody() {

        return new InputStream() {

          @Override
          public int read()
            throws IOException {

            throw new IOException("The End");
          }
        };
      }


      @Override
      public OutputStream getResponseBody() {

        return byteArrayOutputStream;
      }


      @Override
      public void sendResponseHeaders(int i,
                                      long l)
        throws IOException {

      }


      @Override
      public InetSocketAddress getRemoteAddress() {

        return null;
      }


      @Override
      public int getResponseCode() {

        return 0;
      }


      @Override
      public InetSocketAddress getLocalAddress() {

        return null;
      }


      @Override
      public String getProtocol() {

        return null;
      }


      @Override
      public Object getAttribute(String s) {

        return null;
      }


      @Override
      public void setAttribute(String s,
                               Object o) {

      }


      @Override
      public void setStreams(InputStream inputStream,
                             OutputStream outputStream) {

      }


      @Override
      public HttpPrincipal getPrincipal() {

        return null;
      }
    };

    AnnotationTodoRestService annotationTodoRestService = new AnnotationTodoRestService();
    AnnotationHttpHandler httpHandler = new AnnotationHttpHandler(server, annotationTodoRestService);

    List<Throwable> throwables = new ArrayList<>();
    RestException.addHandler(throwables::add);
    httpHandler.handle(fakeHttpExchange);
    ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) fakeHttpExchange.getResponseBody();
    String errorMessage = byteArrayOutputStream.toString();

    assertEquals(1, throwables.size());
    assertEquals("The End", errorMessage);

    Throwable throwable = throwables.get(0);
    assertEquals("The End", throwable.getMessage());
    assertEquals(IOException.class, throwable.getClass());
  }
}
