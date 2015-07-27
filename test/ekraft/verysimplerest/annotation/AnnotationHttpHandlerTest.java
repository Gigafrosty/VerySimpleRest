package ekraft.verysimplerest.annotation;


import ekraft.verysimplerest.RestClient;
import ekraft.verysimplerest.RestServer;
import ekraft.verysimplerest.examples.AsyncAnnotationTodoService;
import ekraft.verysimplerest.examples.AnnotationTodoRestService;
import ekraft.verysimplerest.utils.RestException;
import ekraft.verysimplerest.utils.RestUrlParameters;
import ekraft.verysimplerest.utils.TestUtils;
import org.junit.After;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;


public class AnnotationHttpHandlerTest {

  private static final int TEST_PORT = 8884;
  private RestServer server = new RestServer(TEST_PORT);
  private RestClient client = new RestClient("localhost", TEST_PORT);
  private AnnotationTestService service = new AnnotationTestService();


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
  public void serverStartsAndStops()
    throws Exception {

    AnnotationTodoRestService annotationTodoRestService = new AnnotationTodoRestService();
    AsyncAnnotationTodoService asyncAnnotationTodoService = new AsyncAnnotationTodoService();

    assertFalse(TestUtils.serverIsUp(TEST_PORT));
    server.addService(annotationTodoRestService);
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.shutdownService(annotationTodoRestService);
    assertFalse(TestUtils.serverIsUp(TEST_PORT));

    assertFalse(TestUtils.serverIsUp(TEST_PORT));
    server.addService(annotationTodoRestService);
    server.addService(asyncAnnotationTodoService);
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.shutdownService(annotationTodoRestService);
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.shutdownService(asyncAnnotationTodoService);
    assertFalse(TestUtils.serverIsUp(TEST_PORT));

    assertFalse(TestUtils.serverIsUp(TEST_PORT));
    server.addService(annotationTodoRestService);
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.addService(annotationTodoRestService);
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.shutdownService(annotationTodoRestService);
    assertFalse(TestUtils.serverIsUp(TEST_PORT));
  }


  @Test
  public void pathsAreCalculatedRight()
    throws Exception {

    assertEquals("/services/get1", AnnotationUtils.getPath(AnnotationTestService.GET1));
    assertEquals("/services/put1", AnnotationUtils.getPath(AnnotationTestService.PUT1));
    assertEquals("/services/post1", AnnotationUtils.getPath(AnnotationTestService.POST1));
    assertEquals("/services/delete1", AnnotationUtils.getPath(AnnotationTestService.DELETE1));
    assertEquals("/services/all1", AnnotationUtils.getPath(AnnotationTestService.ALL1));

    assertEquals("/services/get2", AnnotationUtils.getPath(AnnotationTestService.GET2));
    assertEquals("/services/put2", AnnotationUtils.getPath(AnnotationTestService.PUT2));
    assertEquals("/services/post2", AnnotationUtils.getPath(AnnotationTestService.POST2));
    assertEquals("/services/delete2", AnnotationUtils.getPath(AnnotationTestService.DELETE2));
    assertEquals("/services/all2", AnnotationUtils.getPath(AnnotationTestService.ALL2));

    assertEquals("/services/get3/*", AnnotationUtils.getPath(AnnotationTestService.GET3));
    assertEquals("/services/put3", AnnotationUtils.getPath(AnnotationTestService.PUT3));
    assertEquals("/services/post3", AnnotationUtils.getPath(AnnotationTestService.POST3));
    assertEquals("/services/delete3", AnnotationUtils.getPath(AnnotationTestService.DELETE3));
    assertEquals("/services/all3/*", AnnotationUtils.getPath(AnnotationTestService.ALL3));

    assertEquals("/services/todo", AnnotationUtils.getPath(AnnotationTodoRestService.GET));
    assertEquals("/services/todo", AnnotationUtils.getPath(AnnotationTodoRestService.PUT));
    assertEquals("/services/todo", AnnotationUtils.getPath(AnnotationTodoRestService.POST));
    assertEquals("/services/todo", AnnotationUtils.getPath(AnnotationTodoRestService.DELETE));
    assertEquals("/services/todo/*", AnnotationUtils.getPath(AnnotationTodoRestService.GET_ID));
    assertEquals("/services/todo/*", AnnotationUtils.getPath(AnnotationTodoRestService.PUT_ID));
    assertEquals("/services/todo/*", AnnotationUtils.getPath(AnnotationTodoRestService.DELETE_ID));

    assertEquals("/services/async", AnnotationUtils.getPath(AsyncAnnotationTodoService.GET));
    assertEquals("/services/async", AnnotationUtils.getPath(AsyncAnnotationTodoService.PUT));
    assertEquals("/services/async", AnnotationUtils.getPath(AsyncAnnotationTodoService.POST));
    assertEquals("/services/async", AnnotationUtils.getPath(AsyncAnnotationTodoService.DELETE));
    assertEquals("/services/async/{id}", AnnotationUtils.getPath(AsyncAnnotationTodoService.GET_ID));
    assertEquals("/services/async/{id}", AnnotationUtils.getPath(AsyncAnnotationTodoService.PUT_ID));
    assertEquals("/services/async/{id}", AnnotationUtils.getPath(AsyncAnnotationTodoService.DELETE_ID));
  }


  @Test
  public void handlesRemovingNonexistentServices()
    throws Exception {

    AnnotationTodoRestService annotationTodoRestService = new AnnotationTodoRestService();
    AsyncAnnotationTodoService asyncAnnotationTodoService = new AsyncAnnotationTodoService();

    assertFalse(TestUtils.serverIsUp(TEST_PORT));
    server.addService(annotationTodoRestService);
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.shutdownService(asyncAnnotationTodoService);
    assertTrue(TestUtils.serverIsUp(TEST_PORT));
    server.shutdownService(annotationTodoRestService);
    assertFalse(TestUtils.serverIsUp(TEST_PORT));

    server.shutdownService(annotationTodoRestService);
    assertFalse(TestUtils.serverIsUp(TEST_PORT));
  }


  @Test
  public void testVoidNoParameters() {

    server.addService(service);

    assertArrayEquals(new String[0], service.getCalled());

    assertTrue(client.get(AnnotationTestService.GET1, Boolean.class));
    assertArrayEquals(new String[]{"getVoid"}, service.getCalled());
    assertNull(client.put(AnnotationTestService.GET1, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.post(AnnotationTestService.GET1, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.delete(AnnotationTestService.GET1, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.PUT1, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertTrue(client.put(AnnotationTestService.PUT1, Boolean.class));
    assertArrayEquals(new String[]{"putVoid"}, service.getCalled());
    assertNull(client.post(AnnotationTestService.PUT1, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.delete(AnnotationTestService.PUT1, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.POST1, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.put(AnnotationTestService.POST1, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertTrue(client.post(AnnotationTestService.POST1, Boolean.class));
    assertArrayEquals(new String[]{"postVoid"}, service.getCalled());
    assertNull(client.delete(AnnotationTestService.POST1, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.DELETE1, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.put(AnnotationTestService.DELETE1, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.post(AnnotationTestService.DELETE1, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertTrue(client.delete(AnnotationTestService.DELETE1, Boolean.class));
    assertArrayEquals(new String[]{"deleteVoid"}, service.getCalled());

    assertTrue(client.get(AnnotationTestService.ALL1, Boolean.class));
    assertArrayEquals(new String[]{"allVoid"}, service.getCalled());
    assertTrue(client.put(AnnotationTestService.ALL1, Boolean.class));
    assertArrayEquals(new String[]{"allVoid"}, service.getCalled());
    assertTrue(client.post(AnnotationTestService.ALL1, Boolean.class));
    assertArrayEquals(new String[]{"allVoid"}, service.getCalled());
    assertTrue(client.delete(AnnotationTestService.ALL1, Boolean.class));
    assertArrayEquals(new String[]{"allVoid"}, service.getCalled());
  }


  @Test
  public void testVoidNoParametersNoReturnTypes() {

    server.addService(service);

    assertArrayEquals(new String[0], service.getCalled());

    client.get(AnnotationTestService.GET1);
    assertArrayEquals(new String[]{"getVoid"}, service.getCalled());
    client.put(AnnotationTestService.GET1);
    assertArrayEquals(new String[0], service.getCalled());
    client.post(AnnotationTestService.GET1);
    assertArrayEquals(new String[0], service.getCalled());
    client.delete(AnnotationTestService.GET1);
    assertArrayEquals(new String[0], service.getCalled());

    client.get(AnnotationTestService.PUT1);
    assertArrayEquals(new String[0], service.getCalled());
    client.put(AnnotationTestService.PUT1);
    assertArrayEquals(new String[]{"putVoid"}, service.getCalled());
    client.post(AnnotationTestService.PUT1);
    assertArrayEquals(new String[0], service.getCalled());
    client.delete(AnnotationTestService.PUT1);
    assertArrayEquals(new String[0], service.getCalled());

    client.get(AnnotationTestService.POST1);
    assertArrayEquals(new String[0], service.getCalled());
    client.put(AnnotationTestService.POST1);
    assertArrayEquals(new String[0], service.getCalled());
    client.post(AnnotationTestService.POST1);
    assertArrayEquals(new String[]{"postVoid"}, service.getCalled());
    client.delete(AnnotationTestService.POST1);
    assertArrayEquals(new String[0], service.getCalled());

    client.get(AnnotationTestService.DELETE1);
    assertArrayEquals(new String[0], service.getCalled());
    client.put(AnnotationTestService.DELETE1);
    assertArrayEquals(new String[0], service.getCalled());
    client.post(AnnotationTestService.DELETE1);
    assertArrayEquals(new String[0], service.getCalled());
    client.delete(AnnotationTestService.DELETE1);
    assertArrayEquals(new String[]{"deleteVoid"}, service.getCalled());

    client.get(AnnotationTestService.ALL1);
    assertArrayEquals(new String[]{"allVoid"}, service.getCalled());
    client.put(AnnotationTestService.ALL1);
    assertArrayEquals(new String[]{"allVoid"}, service.getCalled());
    client.post(AnnotationTestService.ALL1);
    assertArrayEquals(new String[]{"allVoid"}, service.getCalled());
    client.delete(AnnotationTestService.ALL1);
    assertArrayEquals(new String[]{"allVoid"}, service.getCalled());
  }


  @Test
  public void testStringReturnType() {

    server.addService(service);

    assertArrayEquals(new String[0], service.getCalled());

    assertEquals("get2", client.get(AnnotationTestService.GET2, String.class));
    assertArrayEquals(new String[]{"getString"}, service.getCalled());
    assertNull(client.put(AnnotationTestService.GET2, String.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.post(AnnotationTestService.GET2, String.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.delete(AnnotationTestService.GET2, String.class));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.PUT2, String.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertEquals("put2", client.put(AnnotationTestService.PUT2, String.class));
    assertArrayEquals(new String[]{"putString"}, service.getCalled());
    assertNull(client.post(AnnotationTestService.PUT2, String.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.delete(AnnotationTestService.PUT2, String.class));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.POST2, String.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.put(AnnotationTestService.POST2, String.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertEquals("post2", client.post(AnnotationTestService.POST2, String.class));
    assertArrayEquals(new String[]{"postString"}, service.getCalled());
    assertNull(client.delete(AnnotationTestService.POST2, String.class));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.DELETE2, String.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.put(AnnotationTestService.DELETE2, String.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.post(AnnotationTestService.DELETE2, String.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertEquals("delete2", client.delete(AnnotationTestService.DELETE2, String.class));
    assertArrayEquals(new String[]{"deleteString"}, service.getCalled());

    assertEquals("all2", client.get(AnnotationTestService.ALL2, String.class));
    assertArrayEquals(new String[]{"allString"}, service.getCalled());
    assertEquals("all2", client.put(AnnotationTestService.ALL2, String.class));
    assertArrayEquals(new String[]{"allString"}, service.getCalled());
    assertEquals("all2", client.post(AnnotationTestService.ALL2, String.class));
    assertArrayEquals(new String[]{"allString"}, service.getCalled());
    assertEquals("all2", client.delete(AnnotationTestService.ALL2, String.class));
    assertArrayEquals(new String[]{"allString"}, service.getCalled());
  }


  @Test
  public void testVoidStringParameter() {

    server.addService(service);

    assertArrayEquals(new String[0], service.getCalled());

    assertTrue(client.get(AnnotationTestService.GET3, Boolean.class, "name"));
    assertArrayEquals(new String[]{"getVoid,name"}, service.getCalled());
    assertNull(client.put(AnnotationTestService.GET3, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.post(AnnotationTestService.GET3, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.delete(AnnotationTestService.GET3, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.PUT3, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertTrue(client.put(AnnotationTestService.PUT3, Boolean.class, "name"));
    assertArrayEquals(new String[]{"putVoid,name"}, service.getCalled());
    assertNull(client.post(AnnotationTestService.PUT3, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.delete(AnnotationTestService.PUT3, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.POST3, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.put(AnnotationTestService.POST3, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertTrue(client.post(AnnotationTestService.POST3, Boolean.class, "name"));
    assertArrayEquals(new String[]{"postVoid,name"}, service.getCalled());
    assertNull(client.delete(AnnotationTestService.POST3, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.DELETE3, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.put(AnnotationTestService.DELETE3, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.post(AnnotationTestService.DELETE3, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertTrue(client.delete(AnnotationTestService.DELETE3, Boolean.class, "name"));
    assertArrayEquals(new String[]{"deleteVoid,name"}, service.getCalled());

    assertTrue(client.get(AnnotationTestService.ALL3, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allVoid,name"}, service.getCalled());
    assertTrue(client.put(AnnotationTestService.ALL3, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allVoid,name"}, service.getCalled());
    assertTrue(client.post(AnnotationTestService.ALL3, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allVoid,name"}, service.getCalled());
    assertTrue(client.delete(AnnotationTestService.ALL3, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allVoid,name"}, service.getCalled());
  }


  @Test
  public void testVoidStringParameterNoReturnTypes() {

    server.addService(service);

    assertArrayEquals(new String[0], service.getCalled());

    client.get(AnnotationTestService.GET3, "name");
    assertArrayEquals(new String[]{"getVoid,name"}, service.getCalled());
    client.put(AnnotationTestService.GET3, "name");
    assertArrayEquals(new String[0], service.getCalled());
    client.post(AnnotationTestService.GET3, "name");
    assertArrayEquals(new String[0], service.getCalled());
    client.delete(AnnotationTestService.GET3, "name");
    assertArrayEquals(new String[0], service.getCalled());

    client.get(AnnotationTestService.PUT3, "name");
    assertArrayEquals(new String[0], service.getCalled());
    client.put(AnnotationTestService.PUT3, "name");
    assertArrayEquals(new String[]{"putVoid,name"}, service.getCalled());
    client.post(AnnotationTestService.PUT3, "name");
    assertArrayEquals(new String[0], service.getCalled());
    client.delete(AnnotationTestService.PUT3, "name");
    assertArrayEquals(new String[0], service.getCalled());

    client.get(AnnotationTestService.POST3, "name");
    assertArrayEquals(new String[0], service.getCalled());
    client.put(AnnotationTestService.POST3, "name");
    assertArrayEquals(new String[0], service.getCalled());
    client.post(AnnotationTestService.POST3, "name");
    assertArrayEquals(new String[]{"postVoid,name"}, service.getCalled());
    client.delete(AnnotationTestService.POST3, "name");
    assertArrayEquals(new String[0], service.getCalled());

    client.get(AnnotationTestService.DELETE3, "name");
    assertArrayEquals(new String[0], service.getCalled());
    client.put(AnnotationTestService.DELETE3, "name");
    assertArrayEquals(new String[0], service.getCalled());
    client.post(AnnotationTestService.DELETE3, "name");
    assertArrayEquals(new String[0], service.getCalled());
    client.delete(AnnotationTestService.DELETE3, "name");
    assertArrayEquals(new String[]{"deleteVoid,name"}, service.getCalled());

    client.get(AnnotationTestService.ALL3, "name");
    assertArrayEquals(new String[]{"allVoid,name"}, service.getCalled());
    client.put(AnnotationTestService.ALL3, "name");
    assertArrayEquals(new String[]{"allVoid,name"}, service.getCalled());
    client.post(AnnotationTestService.ALL3, "name");
    assertArrayEquals(new String[]{"allVoid,name"}, service.getCalled());
    client.delete(AnnotationTestService.ALL3, "name");
    assertArrayEquals(new String[]{"allVoid,name"}, service.getCalled());
  }


  @Test
  public void testAsynchronousNoParameters() {

    server.addService(service);

    assertArrayEquals(new String[0], service.getCalled());

    assertTrue(client.get(AnnotationTestService.GET4, Boolean.class));
    assertArrayEquals(new String[]{"getCallback"}, service.getCalled());
    assertNull(client.put(AnnotationTestService.GET4, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.post(AnnotationTestService.GET4, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.delete(AnnotationTestService.GET4, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.PUT4, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertTrue(client.put(AnnotationTestService.PUT4, Boolean.class));
    assertArrayEquals(new String[]{"putCallback"}, service.getCalled());
    assertNull(client.post(AnnotationTestService.PUT4, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.delete(AnnotationTestService.PUT4, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.POST4, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.put(AnnotationTestService.POST4, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertTrue(client.post(AnnotationTestService.POST4, Boolean.class));
    assertArrayEquals(new String[]{"postCallback"}, service.getCalled());
    assertNull(client.delete(AnnotationTestService.POST4, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.DELETE4, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.put(AnnotationTestService.DELETE4, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.post(AnnotationTestService.DELETE4, Boolean.class));
    assertArrayEquals(new String[0], service.getCalled());
    assertTrue(client.delete(AnnotationTestService.DELETE4, Boolean.class));
    assertArrayEquals(new String[]{"deleteCallback"}, service.getCalled());

    assertTrue(client.get(AnnotationTestService.ALL4, Boolean.class));
    assertArrayEquals(new String[]{"allCallback"}, service.getCalled());
    assertTrue(client.put(AnnotationTestService.ALL4, Boolean.class));
    assertArrayEquals(new String[]{"allCallback"}, service.getCalled());
    assertTrue(client.post(AnnotationTestService.ALL4, Boolean.class));
    assertArrayEquals(new String[]{"allCallback"}, service.getCalled());
    assertTrue(client.delete(AnnotationTestService.ALL4, Boolean.class));
    assertArrayEquals(new String[]{"allCallback"}, service.getCalled());
  }


  @Test
  public void testAsynchronousWithParameter() {

    server.addService(service);

    assertArrayEquals(new String[0], service.getCalled());

    assertTrue(client.get(AnnotationTestService.GET5, Boolean.class, "name"));
    assertArrayEquals(new String[]{"getCallback,name"}, service.getCalled());
    assertNull(client.put(AnnotationTestService.GET5, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.post(AnnotationTestService.GET5, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.delete(AnnotationTestService.GET5, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.PUT5, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertTrue(client.put(AnnotationTestService.PUT5, Boolean.class, "name"));
    assertArrayEquals(new String[]{"putCallback,name"}, service.getCalled());
    assertNull(client.post(AnnotationTestService.PUT5, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.delete(AnnotationTestService.PUT5, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.POST5, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.put(AnnotationTestService.POST5, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertTrue(client.post(AnnotationTestService.POST5, Boolean.class, "name"));
    assertArrayEquals(new String[]{"postCallback,name"}, service.getCalled());
    assertNull(client.delete(AnnotationTestService.POST5, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());

    assertNull(client.get(AnnotationTestService.DELETE5, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.put(AnnotationTestService.DELETE5, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertNull(client.post(AnnotationTestService.DELETE5, Boolean.class, "name"));
    assertArrayEquals(new String[0], service.getCalled());
    assertTrue(client.delete(AnnotationTestService.DELETE5, Boolean.class, "name"));
    assertArrayEquals(new String[]{"deleteCallback,name"}, service.getCalled());

    assertTrue(client.get(AnnotationTestService.ALL5, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allCallback,name"}, service.getCalled());
    assertTrue(client.put(AnnotationTestService.ALL5, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allCallback,name"}, service.getCalled());
    assertTrue(client.post(AnnotationTestService.ALL5, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allCallback,name"}, service.getCalled());
    assertTrue(client.delete(AnnotationTestService.ALL5, Boolean.class, "name"));
    assertArrayEquals(new String[]{"allCallback,name"}, service.getCalled());
  }


  @Test
  public void testRestUrlParametersAsParameter()
    throws Exception {

    List<String> myId = new ArrayList<>();
    List<RestUrlParameters> myParameters = new ArrayList<>();
    List<Integer> myValue = new ArrayList<>();
    List<String> myWorld = new ArrayList<>();

    @Path("test")
    class Test {

      @PUT
      @Path("{id}/*/*")
      public void test(String id,
                       int value,
                       RestUrlParameters parameters) {

        myId.add(id);
        myParameters.add(parameters);
        myValue.add(value);
      }


      @POST
      @Path("{id}/*")
      public void test2(String id,
                        int value,
                        RestUrlParameters parameters,
                        String world) {

        myId.add(id);
        myParameters.add(parameters);
        myValue.add(value);
        myWorld.add(world);
      }


      @DELETE
      @Path("{id}/*")
      public void test3(String id,
                        RestUrlParameters parameters,
                        int value,
                        String world) {

        myId.add(id);
        myParameters.add(parameters);
        myValue.add(value);
        myWorld.add(world);
      }
    }

    Method method = Test.class.getMethod("test", String.class, int.class, RestUrlParameters.class);
    Method method2 = Test.class.getMethod("test2", String.class, int.class, RestUrlParameters.class, String.class);
    Method method3 = Test.class.getMethod("test3", String.class, RestUrlParameters.class, int.class, String.class);

    server.addService(new Test());

    client.put(method, "name", 12345, "Hello World");

    assertEquals("name", myId.get(0));
    assertEquals(new Integer(12345), myValue.get(0));

    RestUrlParameters parameters = myParameters.get(0);
    assertEquals(3, parameters.size());
    assertEquals("name", parameters.get(0));
    assertEquals("name", parameters.get("id"));
    assertEquals(new Integer(12345), parameters.get(1, int.class));
    // The space should be broken off since it's in the URL.
    assertEquals("Hello", parameters.get(2));

    assertEquals(0, myWorld.size());

    myId.clear();
    myParameters.clear();
    myValue.clear();
    myWorld.clear();

    client.post(method2, "name", 12345, "Hello World");

    assertEquals("name", myId.get(0));
    assertEquals(new Integer(12345), myValue.get(0));

    parameters = myParameters.get(0);
    assertEquals(2, parameters.size());
    assertEquals("name", parameters.get(0));
    assertEquals("name", parameters.get("id"));
    assertEquals(new Integer(12345), parameters.get(1, int.class));
    // The space should not be broken off since it's in the request body.
    assertEquals("Hello World", myWorld.get(0));

    try {
      client.delete(method3, "name", 12345, "Hello World");
      fail("Should not be able to put RestUrlParameters before parameter variables.");
    } catch (Exception e) {
      // Expected behavior.
    }
  }


  @Test
  public void rejectAsynchronousFunctionsWithReturnValue() {

    @Path("path")
    class RejectClass {

      @GET
      public boolean rejectMethod(Consumer<String> callback) {

        callback.accept(null);
        return true;
      }

    }

    try {
      server.addService(new RejectClass());
      fail("Should have rejected service.");
    } catch (IllegalArgumentException e) {
      String expected = "Asynchronous methods are not allowed to have a return type: " +
        AnnotationHttpHandlerTest.class.getName() + "$1" +
        "RejectClass.rejectMethod()";
      assertEquals(expected, e.getMessage());
    }
  }


  @Test
  public void exceptionWhenNotEnoughData() {

    server.addService(service);

    client.put(AnnotationTestService.PUT3, "name");
    assertArrayEquals(new String[]{"putVoid,name"}, service.getCalled());

    String path = AnnotationUtils.getPath(AnnotationTestService.PUT3);

    String result = RestClient.communicate("localhost", TEST_PORT, "PUT", path, "", true);
    assertEquals("Ran out of parameters and had empty method body!", result);
  }


  @Test
  public void annotationTimeoutTest()
    throws Exception {

    List<Throwable> list = new ArrayList<>();
    RestException.addHandler(list::add);

    server.addService(new AnnotationTestService());

    String response = RestClient.communicate("localhost", TEST_PORT, "GET", "/services/timeout", null, true);
    assertEquals("Timed Out.", response);

    Thread.sleep(2000);

    assertEquals(1, list.size());
    Throwable throwable = list.get(0);
    assertEquals(IOException.class, throwable.getClass());
    assertEquals("headers already sent", throwable.getMessage());
  }
}
