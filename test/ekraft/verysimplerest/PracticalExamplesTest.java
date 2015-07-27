package ekraft.verysimplerest;


import ekraft.verysimplerest.examples.AnnotationTodoRestService;
import ekraft.verysimplerest.examples.AsyncAnnotationTodoService;
import ekraft.verysimplerest.examples.AsyncLambdaTodoService;
import ekraft.verysimplerest.examples.LambdaTodoRestService;
import ekraft.verysimplerest.examples.TodoItem;
import ekraft.verysimplerest.utils.RestException;
import ekraft.verysimplerest.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class PracticalExamplesTest {

  private static final int TEST_PORT = 8888;

  private RestServer server;
  private RestClient client;


  @Before
  public void setup() {

    server = new RestServer(TEST_PORT);
    client = new RestClient("localhost", TEST_PORT);
    RestException.addHandler(System.out::println);
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
  public void todoItemAnnotationTest()
    throws Exception {

    String path = "/services/todo";
    AnnotationTodoRestService todoService = new AnnotationTodoRestService();
    server.addService(todoService);

    assertArrayEquals(new String[0], client.get(AnnotationTodoRestService.GET, String[].class));

    TodoItem item1 = new TodoItem("Walk the dog", "Dog", "2012-10-31 17:30:00");
    TodoItem item2 = new TodoItem("Pick up Milk and Eggs", "Groceries", "2012-10-31 23:00:00");
    TodoItem item3 = new TodoItem("Dream the night away", "Sleep", "2012-10-31 23:30:00");

    assertTrue(client.put(AnnotationTodoRestService.PUT, boolean.class, item1));
    assertTrue(client.put(AnnotationTodoRestService.PUT, boolean.class, item2));
    assertTrue(client.put(AnnotationTodoRestService.PUT, boolean.class, item3));

    assertArrayEquals(new TodoItem[] {item1, item2, item3}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/1", path + "/2", path + "/3",},
      client.get(AnnotationTodoRestService.GET, String[].class)
      );

    assertEquals(item1, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 1));
    assertEquals(item2, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 2));
    assertEquals(item3, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 3));

    assertTrue(client.delete(AnnotationTodoRestService.DELETE_ID, boolean.class, 2));

    assertArrayEquals(new TodoItem[] {item1, item3}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/1", path + "/3",},
      client.get(AnnotationTodoRestService.GET, String[].class)
    );

    assertEquals(item1, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 1));
    assertEquals(null, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 2));
    assertEquals(item3, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 3));

    assertTrue(client.put(AnnotationTodoRestService.PUT_ID, boolean.class, 1, item2));

    assertArrayEquals(new TodoItem[] {item2, item3}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/1", path + "/3",},
      client.get(AnnotationTodoRestService.GET, String[].class)
    );

    assertEquals(item2, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 1));
    assertEquals(null, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 2));
    assertEquals(item3, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 3));

    TodoItem item4 = new TodoItem("Crush all humans", "Crush", "2112-10-31 17:30:00");
    TodoItem item5 = new TodoItem("Kill all humans", "Kill", "2112-10-31 23:00:00");
    TodoItem item6 = new TodoItem("Destroy all humans", "Destroy", "2112-10-31 23:30:00");
    TodoItem item7 = new TodoItem("Burma Shave", "Ad", "2112-10-31 23:59:59");

    assertTrue(client.post(AnnotationTodoRestService.POST, boolean.class, new TodoItem[] {
      item4, item5, item6, item7,
    }));

    assertArrayEquals(new TodoItem[] {item4, item5, item6, item7}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/4", path + "/5", path + "/6", path + "/7",},
      client.get(AnnotationTodoRestService.GET, String[].class)
    );

    assertEquals(null, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 1));
    assertEquals(null, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 2));
    assertEquals(null, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 3));
    assertEquals(item4, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 4));
    assertEquals(item5, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 5));
    assertEquals(item6, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 6));
    assertEquals(item7, client.get(AnnotationTodoRestService.GET_ID, TodoItem.class, 7));

    assertTrue(client.delete(AnnotationTodoRestService.DELETE, boolean.class));

    assertArrayEquals(new String[0], client.get(AnnotationTodoRestService.GET, String[].class));
  }


  @Test
  public void todoItemAsyncAnnotationTest()
    throws Exception {

    String path = "/services/async";
    AsyncAnnotationTodoService todoService = new AsyncAnnotationTodoService();
    server.addService(todoService);

    assertArrayEquals(new String[0], client.get(AsyncAnnotationTodoService.GET, String[].class));

    TodoItem item1 = new TodoItem("Walk the dog", "Dog", "2012-10-31 17:30:00");
    TodoItem item2 = new TodoItem("Pick up Milk and Eggs", "Groceries", "2012-10-31 23:00:00");
    TodoItem item3 = new TodoItem("Dream the night away", "Sleep", "2012-10-31 23:30:00");

    assertTrue(client.put(AsyncAnnotationTodoService.PUT, boolean.class, item1));
    assertTrue(client.put(AsyncAnnotationTodoService.PUT, boolean.class, item2));
    assertTrue(client.put(AsyncAnnotationTodoService.PUT, boolean.class, item3));

    assertArrayEquals(new TodoItem[] {item1, item2, item3}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/1", path + "/2", path + "/3",},
      client.get(AsyncAnnotationTodoService.GET, String[].class)
    );

    assertEquals(item1, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 1));
    assertEquals(item2, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 2));
    assertEquals(item3, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 3));

    assertTrue(client.delete(AsyncAnnotationTodoService.DELETE_ID, boolean.class, 2));

    assertArrayEquals(new TodoItem[] {item1, item3}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/1", path + "/3",},
      client.get(AsyncAnnotationTodoService.GET, String[].class)
    );

    assertEquals(item1, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 1));
    assertEquals(null, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 2));
    assertEquals(item3, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 3));

    assertTrue(client.put(AsyncAnnotationTodoService.PUT_ID, boolean.class, 1, item2));

    assertArrayEquals(new TodoItem[] {item2, item3}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/1", path + "/3",},
      client.get(AsyncAnnotationTodoService.GET, String[].class)
    );

    assertEquals(item2, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 1));
    assertEquals(null, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 2));
    assertEquals(item3, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 3));

    TodoItem item4 = new TodoItem("Crush all humans", "Crush", "2112-10-31 17:30:00");
    TodoItem item5 = new TodoItem("Kill all humans", "Kill", "2112-10-31 23:00:00");
    TodoItem item6 = new TodoItem("Destroy all humans", "Destroy", "2112-10-31 23:30:00");
    TodoItem item7 = new TodoItem("Burma Shave", "Ad", "2112-10-31 23:59:59");

    assertTrue(client.post(AsyncAnnotationTodoService.POST, boolean.class, new TodoItem[] {
      item4, item5, item6, item7,
    }));

    assertArrayEquals(new TodoItem[] {item4, item5, item6, item7}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/4", path + "/5", path + "/6", path + "/7",},
      client.get(AsyncAnnotationTodoService.GET, String[].class)
    );

    assertEquals(null, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 1));
    assertEquals(null, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 2));
    assertEquals(null, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 3));
    assertEquals(item4, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 4));
    assertEquals(item5, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 5));
    assertEquals(item6, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 6));
    assertEquals(item7, client.get(AsyncAnnotationTodoService.GET_ID, TodoItem.class, 7));

    assertTrue(client.delete(AsyncAnnotationTodoService.DELETE, boolean.class));

    assertArrayEquals(new String[0], client.get(AsyncAnnotationTodoService.GET, String[].class));
  }


  @Test
  public void todoItemLambdaTest()
    throws Exception {

    String path = "/lambda/todo";
    LambdaTodoRestService todoService = new LambdaTodoRestService();
    todoService.register(server);

    assertArrayEquals(new String[0], client.get(LambdaTodoRestService.PATH, String[].class));

    TodoItem item1 = new TodoItem("Walk the dog", "Dog", "2012-10-31 17:30:00");
    TodoItem item2 = new TodoItem("Pick up Milk and Eggs", "Groceries", "2012-10-31 23:00:00");
    TodoItem item3 = new TodoItem("Dream the night away", "Sleep", "2012-10-31 23:30:00");

    assertTrue(client.put(LambdaTodoRestService.PATH, boolean.class, item1));
    assertTrue(client.put(LambdaTodoRestService.PATH, boolean.class, item2));
    assertTrue(client.put(LambdaTodoRestService.PATH, boolean.class, item3));

    assertArrayEquals(new TodoItem[] {item1, item2, item3}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/1", path + "/2", path + "/3",},
      client.get(LambdaTodoRestService.PATH, String[].class)
    );

    assertEquals(item1, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 1));
    assertEquals(item2, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 2));
    assertEquals(item3, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 3));

    assertTrue(client.delete(LambdaTodoRestService.PATH_ID, boolean.class, 2));

    assertArrayEquals(new TodoItem[] {item1, item3}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/1", path + "/3",},
      client.get(LambdaTodoRestService.PATH, String[].class)
    );

    assertEquals(item1, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 1));
    assertEquals(null, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 2));
    assertEquals(item3, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 3));

    assertTrue(client.put(LambdaTodoRestService.PATH_ID, boolean.class, 1, item2));

    assertArrayEquals(new TodoItem[] {item2, item3}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/1", path + "/3",},
      client.get(LambdaTodoRestService.PATH, String[].class)
    );

    assertEquals(item2, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 1));
    assertEquals(null, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 2));
    assertEquals(item3, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 3));

    TodoItem item4 = new TodoItem("Crush all humans", "Crush", "2112-10-31 17:30:00");
    TodoItem item5 = new TodoItem("Kill all humans", "Kill", "2112-10-31 23:00:00");
    TodoItem item6 = new TodoItem("Destroy all humans", "Destroy", "2112-10-31 23:30:00");
    TodoItem item7 = new TodoItem("Burma Shave", "Ad", "2112-10-31 23:59:59");

    assertTrue(client.post(LambdaTodoRestService.PATH, boolean.class, new TodoItem[] {
      item4, item5, item6, item7,
    }));

    assertArrayEquals(new TodoItem[] {item4, item5, item6, item7}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/4", path + "/5", path + "/6", path + "/7",},
      client.get(LambdaTodoRestService.PATH, String[].class)
    );

    assertEquals(null, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 1));
    assertEquals(null, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 2));
    assertEquals(null, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 3));
    assertEquals(item4, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 4));
    assertEquals(item5, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 5));
    assertEquals(item6, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 6));
    assertEquals(item7, client.get(LambdaTodoRestService.PATH_ID, TodoItem.class, 7));

    assertTrue(client.delete(LambdaTodoRestService.PATH, boolean.class));

    assertArrayEquals(new String[0], client.get(LambdaTodoRestService.PATH, String[].class));
  }


  @Test
  public void todoItemAsyncLambdaTest()
    throws Exception {

    String path = "/lambda/async";
    AsyncLambdaTodoService todoService = new AsyncLambdaTodoService();
    todoService.register(server);

    assertArrayEquals(new String[0], client.get(AsyncLambdaTodoService.PATH, String[].class));

    TodoItem item1 = new TodoItem("Walk the dog", "Dog", "2012-10-31 17:30:00");
    TodoItem item2 = new TodoItem("Pick up Milk and Eggs", "Groceries", "2012-10-31 23:00:00");
    TodoItem item3 = new TodoItem("Dream the night away", "Sleep", "2012-10-31 23:30:00");

    assertTrue(client.put(AsyncLambdaTodoService.PATH, boolean.class, item1));
    assertTrue(client.put(AsyncLambdaTodoService.PATH, boolean.class, item2));
    assertTrue(client.put(AsyncLambdaTodoService.PATH, boolean.class, item3));

    assertArrayEquals(new TodoItem[] {item1, item2, item3}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/1", path + "/2", path + "/3",},
      client.get(AsyncLambdaTodoService.PATH, String[].class)
    );

    assertEquals(item1, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 1));
    assertEquals(item2, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 2));
    assertEquals(item3, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 3));

    assertTrue(client.delete(AsyncLambdaTodoService.PATH_ID, boolean.class, 2));

    assertArrayEquals(new TodoItem[] {item1, item3}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/1", path + "/3",},
      client.get(AsyncLambdaTodoService.PATH, String[].class)
    );

    assertEquals(item1, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 1));
    assertEquals(null, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 2));
    assertEquals(item3, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 3));

    assertTrue(client.put(AsyncLambdaTodoService.PATH_ID, boolean.class, 1, item2));

    assertArrayEquals(new TodoItem[] {item2, item3}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/1", path + "/3",},
      client.get(AsyncLambdaTodoService.PATH, String[].class)
    );

    assertEquals(item2, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 1));
    assertEquals(null, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 2));
    assertEquals(item3, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 3));

    TodoItem item4 = new TodoItem("Crush all humans", "Crush", "2112-10-31 17:30:00");
    TodoItem item5 = new TodoItem("Kill all humans", "Kill", "2112-10-31 23:00:00");
    TodoItem item6 = new TodoItem("Destroy all humans", "Destroy", "2112-10-31 23:30:00");
    TodoItem item7 = new TodoItem("Burma Shave", "Ad", "2112-10-31 23:59:59");

    assertTrue(client.post(AsyncLambdaTodoService.PATH, boolean.class, new TodoItem[] {
      item4, item5, item6, item7,
    }));

    assertArrayEquals(new TodoItem[] {item4, item5, item6, item7}, todoService.getTodoItems());

    assertArrayEquals(new String[] {path + "/4", path + "/5", path + "/6", path + "/7",},
      client.get(AsyncLambdaTodoService.PATH, String[].class)
    );

    assertEquals(null, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 1));
    assertEquals(null, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 2));
    assertEquals(null, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 3));
    assertEquals(item4, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 4));
    assertEquals(item5, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 5));
    assertEquals(item6, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 6));
    assertEquals(item7, client.get(AsyncLambdaTodoService.PATH_ID, TodoItem.class, 7));

    assertTrue(client.delete(AsyncLambdaTodoService.PATH, boolean.class));

    assertArrayEquals(new String[0], client.get(AsyncLambdaTodoService.PATH, String[].class));
  }
}
