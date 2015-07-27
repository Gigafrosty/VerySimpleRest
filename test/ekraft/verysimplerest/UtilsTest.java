package ekraft.verysimplerest;


import ekraft.verysimplerest.annotation.AnnotationUtils;
import ekraft.verysimplerest.lambda.HttpRequest;
import ekraft.verysimplerest.utils.RestException;
import ekraft.verysimplerest.utils.RestUrlParameters;
import ekraft.verysimplerest.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class UtilsTest {

  private static final int TEST_PORT = 8885;
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
  public void uselessThingsForFullTestCoverage() {

    // Whatever
    new AnnotationUtils();
  }


  @Test
  public void testRestUrlParameters() {

    List<RestUrlParameters> passBack = new ArrayList<>();
    server.put("/put/*/{id}/is/{value}", (HttpRequest<Boolean> request) -> {
      passBack.add(request.getParameters());
      request.respond(true);
    });

    client.put("/put/12345/name/is/true");
    RestUrlParameters restUrlParameters = passBack.get(0);

    assertTrue(restUrlParameters.hasParameter("id"));
    assertFalse(restUrlParameters.hasParameter("di"));

    assertEquals(3, restUrlParameters.size());

    assertEquals("12345", restUrlParameters.get(0));
    assertEquals(new Integer(12345), restUrlParameters.get(0, Integer.class));
    assertNotEquals(12345, restUrlParameters.get(0));

    assertEquals("name", restUrlParameters.get(1));
    assertEquals("name", restUrlParameters.get(1, String.class));
    assertEquals("name", restUrlParameters.get("id"));
    assertEquals("name", restUrlParameters.get("id", String.class));

    assertEquals("true", restUrlParameters.get(2));
    assertTrue(restUrlParameters.get(2, boolean.class));
    assertEquals("true", restUrlParameters.get("value"));
    assertTrue(restUrlParameters.get("value", boolean.class));
  }


  @Test
  public void testHttpRequestParameters() {

    List<HttpRequest> passBack = new ArrayList<>();
    server.put("/put/*/{id}/is/{value}", (HttpRequest<Boolean> request) -> {
      passBack.add(request);
      request.respond(true);
    });

    Map<String, String> parameters = new HashMap<>();
    parameters.put("key1", "value1");
    parameters.put("key2", "12345");
    parameters.put("key3", "true");

    client.put("/put/12345/name/is/true", parameters);
    HttpRequest httpRequest = passBack.get(0);

    assertTrue(httpRequest.getParameters().hasParameter("id"));
    assertFalse(httpRequest.getParameters().hasParameter("di"));

    assertEquals(3, httpRequest.getParameters().size());

    assertEquals("12345", httpRequest.get(0));
    assertEquals(12345, httpRequest.get(0, Integer.class));
    assertNotEquals(12345, httpRequest.get(0));

    assertEquals("name", httpRequest.get(1));
    assertEquals("name", httpRequest.get(1, String.class));
    assertEquals("name", httpRequest.get("id"));
    assertEquals("name", httpRequest.get("id", String.class));

    assertEquals("true", httpRequest.get(2));
    assertTrue((boolean) httpRequest.get(2, boolean.class));
    assertEquals("true", httpRequest.get("value"));
    assertTrue((boolean) httpRequest.get("value", boolean.class));

    assertEquals("value1", httpRequest.get("key1", String.class));

    assertEquals("12345", httpRequest.get("key2", String.class));
    assertEquals(12345, httpRequest.get("key2", Integer.class));

    assertEquals("true", httpRequest.get("key3", String.class));
    assertTrue((boolean) httpRequest.get("key3", boolean.class));

    Map map = (Map) httpRequest.get(Map.class);
    assertEquals(3, map.size());
    assertEquals("value1", map.get("key1"));
    assertEquals("12345", map.get("key2"));
    assertEquals("true", map.get("key3"));

    ExampleData data = (ExampleData) httpRequest.get(ExampleData.class);
    assertEquals("value1", data.key1);
    assertEquals(12345, data.key2);
    assertTrue(data.key3);
  }


  static class ExampleData {

    public String key1;
    public int key2;
    public boolean key3;
  }


  @Test
  public void annotationUtilsTestsNullPathTest()
    throws Exception {

    class Test {

      public void test() {

      }
    }

    Method method = Test.class.getMethod("test");
    assertNull(AnnotationUtils.getPath(method));
    assertNull(AnnotationUtils.getPath(Test.class, method));
  }


  @Test
  public void annotationUtilsTestsGenericsReturnType()
    throws Exception {

    class Test {

      public void test(Consumer<String> callback) {

      }
    }

    Method method = Test.class.getMethod("test", Consumer.class);
    assertEquals(String.class, AnnotationUtils.getReturnType(method));
  }


  @Test
  public void annotationUtilsTestsAmbiguousReturnType()
    throws Exception {

    class Test {

      public void test(Consumer callback) {

      }
    }

    Method method = Test.class.getMethod("test", Consumer.class);
    assertEquals(Object.class, AnnotationUtils.getReturnType(method));
  }


  @Test
  public void annotationUtilsTestsWildcardReturnType()
    throws Exception {

    class Test {

      public void test(Consumer<?> callback) {

      }
    }

    Method method = Test.class.getMethod("test", Consumer.class);
    assertEquals(Object.class, AnnotationUtils.getReturnType(method));
  }


  @Test
  public void annotationUtilsTestsAsynchronousCallbackDetect()
    throws Exception {

    class Test {

      public void test1(Consumer callback) {

      }


      public void test2(Consumer callback1,
                        Consumer callback2) {

      }
    }

    Method method1 = Test.class.getMethod("test1", Consumer.class);
    Method method2 = Test.class.getMethod("test2", Consumer.class, Consumer.class);

    assertTrue(AnnotationUtils.isAsynchronous(method1));
    assertFalse(AnnotationUtils.isAsynchronous(method2));
  }


  @Test
  public void restExceptionTest() {

    List<Throwable> list = new ArrayList<>();
    RestException.addHandler(list::add);

    try {
      throw new RestException("Message");
    } catch (RestException e) {
      assertEquals("Message", e.getMessage());
      assertNull(e.getCause());

      assertEquals(1, list.size());
      Throwable throwable = list.remove(0);
      assertEquals(RestException.class, throwable.getClass());
      assertEquals("Message", throwable.getMessage());
    }

    try {
      throw new RestException(new NullPointerException());
    } catch (RestException e) {
      assertNotEquals("Message", e.getMessage());
      assertEquals(NullPointerException.class, e.getCause().getClass());

      assertEquals(1, list.size());
      Throwable throwable = list.remove(0);
      assertEquals(RestException.class, throwable.getClass());
      assertEquals(NullPointerException.class, throwable.getCause().getClass());
      assertNotEquals("Message", throwable.getMessage());
    }

    try {
      throw new RestException("Message", new NullPointerException());
    } catch (RestException e) {
      assertEquals("Message", e.getMessage());
      assertEquals(NullPointerException.class, e.getCause().getClass());

      assertEquals(1, list.size());
      Throwable throwable = list.remove(0);
      assertEquals(RestException.class, throwable.getClass());
      assertEquals(NullPointerException.class, throwable.getCause().getClass());
      assertEquals("Message", throwable.getMessage());
    }

    RestException.log(new NullPointerException("Another Message"));
    assertEquals(1, list.size());
    Throwable throwable = list.remove(0);
    assertEquals(NullPointerException.class, throwable.getClass());
    assertEquals("Another Message", throwable.getMessage());
  }


  @Test
  public void httpRequestCannotSendErrorAfterResponse() {

    List<Throwable> result = Collections.synchronizedList(new ArrayList<>());

    server.get("/get", request -> {
      request.respond(true);
      try {
        request.error(404, "This shouldn't work.");
      } catch (Exception e) {
        result.add(e);
      }
    });

    assertTrue(client.get("/get", boolean.class));

    long timeout = System.currentTimeMillis() + 1000;
    while (result.size() == 0) {
      if (System.currentTimeMillis() > timeout) {
        fail("Timed out waiting for error after respond.");
      }
    }

    assertEquals(1, result.size());
    assertEquals(IllegalStateException.class, result.get(0).getClass());
    assertEquals("Attempted to respond when already responded!", result.get(0).getMessage());
  }


  @Test
  public void httpRequestCannotSendResponseAfterError() {

    List<Throwable> result = Collections.synchronizedList(new ArrayList<>());

    server.get("/get", request -> {
      request.error(404, "This should work.");
      try {
        request.respond(true);
      } catch (Exception e) {
        result.add(e);
      }
    });

    assertEquals("This should work.", RestClient.communicate("localhost", TEST_PORT, "GET", "/get", null, true));

    long timeout = System.currentTimeMillis() + 1000;
    while (result.size() == 0) {
      if (System.currentTimeMillis() > timeout) {
        fail("Timed out waiting for respond after error.");
      }
    }

    assertEquals(1, result.size());
    assertEquals(IllegalStateException.class, result.get(0).getClass());
    assertEquals("Attempted to respond when already responded!", result.get(0).getMessage());
  }
}
