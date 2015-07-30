package ekraft.verysimplerest.examples;


import ekraft.verysimplerest.utils.RestUrlParameters;
import ekraft.verysimplerest.annotation.AnnotationUtils;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;


@Path("async")
public class AsyncAnnotationTodoService
  extends ExampleRestServiceBase {

  private int nextId = 0;
  private Map<Integer, TodoItem> todoMap = Collections.synchronizedMap(new LinkedHashMap<>());


  public TodoItem[] getTodoItems() {

    return todoMap.values().toArray(new TodoItem[todoMap.size()]);
  }


  @GET
  public void get(Consumer<String[]> callback) {

    run(callback, () -> {
      Integer[] ids = todoMap.keySet().toArray(new Integer[todoMap.size()]);
      String[] result = new String[ids.length];
      String base = AnnotationUtils.getPath(AsyncAnnotationTodoService.class);

      for (int i = 0; i < ids.length; i++) {
        result[i] = base + "/" + ids[i];
      }

      return result;
    });
  }


  @POST
  public void post(TodoItem[] newList,
                   Consumer<Boolean> callback) {

    run(callback, () -> {
      todoMap.clear();
      for (TodoItem todoItem : newList) {
        todoMap.put(++nextId, todoItem);
      }
    });
  }


  @PUT
  public void put(TodoItem todoItem,
                  Consumer<Integer> callback) {

    run(callback, () -> {
      todoMap.put(++nextId, todoItem);
      return nextId;
    });
  }


  @DELETE
  public void delete(Consumer<Boolean> callback) {

    run(callback, todoMap::clear);
  }


  // Functionally these are no different from "*" since Java doesn't remember variable names by default so I can't
  // reflect and match variable names with parameter names. However, you can get the RestUrlParameters as a method
  // variable instead. See "put" for an example of that.
  @GET
  @Path("{id}")
  public void get(int id,
                  Consumer<TodoItem> callback) {

    run(callback, () -> todoMap.get(id));
  }


  @PUT
  @Path("{id}")
  public void put(RestUrlParameters restUrlParameters,
                  TodoItem todoItem,
                  Consumer<Boolean> callback) {

    run(callback, () -> todoMap.put(restUrlParameters.get("id", int.class), todoItem));
  }


  @DELETE
  @Path("{id}")
  public void delete(int id,
                     Consumer<Boolean> callback) {

    run(callback, () -> todoMap.remove(id));
  }


  public static final Method GET = getMethod(AsyncAnnotationTodoService.class, "get", Consumer.class);
  public static final Method POST = getMethod(AsyncAnnotationTodoService.class, "post", TodoItem[].class, Consumer.class);
  public static final Method PUT = getMethod(AsyncAnnotationTodoService.class, "put", TodoItem.class, Consumer.class);
  public static final Method DELETE = getMethod(AsyncAnnotationTodoService.class, "delete", Consumer.class);

  public static final Method GET_ID = getMethod(AsyncAnnotationTodoService.class, "get", int.class, Consumer.class);
  public static final Method PUT_ID = getMethod(AsyncAnnotationTodoService.class, "put", RestUrlParameters.class, TodoItem.class, Consumer.class);
  public static final Method DELETE_ID = getMethod(AsyncAnnotationTodoService.class, "delete", int.class, Consumer.class);
}
