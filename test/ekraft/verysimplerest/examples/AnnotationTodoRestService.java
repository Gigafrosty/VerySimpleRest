package ekraft.verysimplerest.examples;


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


@Path("todo")
public class AnnotationTodoRestService
  extends ExampleRestServiceBase {

  private int nextId = 0;
  private Map<Integer, TodoItem> todoMap = Collections.synchronizedMap(new LinkedHashMap<>());


  public TodoItem[] getTodoItems() {

    return todoMap.values().toArray(new TodoItem[todoMap.size()]);
  }


  @GET
  public String[] get() {

    Integer[] ids = todoMap.keySet().toArray(new Integer[todoMap.size()]);
    String[] result = new String[ids.length];
    String base = AnnotationUtils.getPath(AnnotationTodoRestService.class);

    for (int i = 0; i < ids.length; i++) {
      result[i] = base + "/" + ids[i];
    }

    return result;
  }


  @POST
  public void post(TodoItem[] newList) {

    todoMap.clear();
    for (TodoItem todoItem : newList) {
      todoMap.put(++nextId, todoItem);
    }
  }


  @PUT
  public int put(TodoItem todoItem) {

    todoMap.put(++nextId, todoItem);
    return nextId;
  }


  @DELETE
  public void delete() {

    todoMap.clear();
  }


  @GET
  @Path("*")
  public TodoItem get(int id) {

    return todoMap.get(id);
  }


  @PUT
  @Path("*")
  public void put(int id,
                  TodoItem todoItem) {

    todoMap.put(id, todoItem);
  }


  @DELETE
  @Path("*")
  public void delete(int id) {

    todoMap.remove(id);
  }


  public static final Method GET = getMethod(AnnotationTodoRestService.class, "get");
  public static final Method POST = getMethod(AnnotationTodoRestService.class, "post", TodoItem[].class);
  public static final Method PUT = getMethod(AnnotationTodoRestService.class, "put", TodoItem.class);
  public static final Method DELETE = getMethod(AnnotationTodoRestService.class, "delete");

  public static final Method GET_ID = getMethod(AnnotationTodoRestService.class, "get", int.class);
  public static final Method PUT_ID = getMethod(AnnotationTodoRestService.class, "put", int.class, TodoItem.class);
  public static final Method DELETE_ID = getMethod(AnnotationTodoRestService.class, "delete", int.class);
}
