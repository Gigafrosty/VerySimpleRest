package ekraft.verysimplerest.examples;


import ekraft.verysimplerest.RestServer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;


public class LambdaTodoRestService {

  public static final String PATH = "/lambda/todo";
  public static final String PATH_ID = "/lambda/todo/{id}";

  private int nextId = 0;
  private Map<Integer, TodoItem> todoMap = Collections.synchronizedMap(new LinkedHashMap<>());


  public void register(RestServer server) {

    server.get(PATH, (Supplier<String[]>) this::get);
    server.post(PATH, request -> {
      post(request.get(TodoItem[].class));
      request.respond(true);
    });
    server.put(PATH, request -> {
      put(request.get(TodoItem.class));
      request.respond(true);
    });
    server.delete(PATH, (Runnable) this::delete);

    server.get(PATH_ID, request -> {
      request.respond(get(request.get("id", int.class)));
    });
    server.put(PATH_ID, request -> {
      put(request.get("id", int.class), request.get(TodoItem.class));
      request.respond(true);
    });
    server.delete(PATH_ID, request -> {
      delete(request.get("id", int.class));
      request.respond(true);
    });
  }


  public TodoItem[] getTodoItems() {

    return todoMap.values().toArray(new TodoItem[todoMap.size()]);
  }


  private String[] get() {

    Integer[] ids = todoMap.keySet().toArray(new Integer[todoMap.size()]);
    String[] result = new String[ids.length];

    for (int i = 0; i < ids.length; i++) {
      result[i] = PATH + "/" + ids[i];
    }

    return result;
  }


  private void post(TodoItem[] newList) {

    todoMap.clear();
    for (TodoItem todoItem : newList) {
      todoMap.put(++nextId, todoItem);
    }
  }


  private int put(TodoItem todoItem) {

    todoMap.put(++nextId, todoItem);
    return nextId;
  }


  private void delete() {

    todoMap.clear();
  }


  private TodoItem get(int id) {

    return todoMap.get(id);
  }


  private void put(int id,
                   TodoItem todoItem) {

    todoMap.put(id, todoItem);
  }


  private void delete(int id) {

    todoMap.remove(id);
  }
}
