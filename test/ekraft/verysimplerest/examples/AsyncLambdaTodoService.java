package ekraft.verysimplerest.examples;


import ekraft.verysimplerest.RestServer;
import ekraft.verysimplerest.annotation.AnnotationUtils;
import ekraft.verysimplerest.lambda.HttpRequest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;


public class AsyncLambdaTodoService {

  public static final String PATH = "/lambda/async";
  public static final String PATH_ID = "/lambda/async/{id}";

  private int nextId = 0;
  private Map<Integer, TodoItem> todoMap = Collections.synchronizedMap(new LinkedHashMap<>());

  // Not needed ; just to show that asynchronous calls passed to another thread
  // will respond correctly.
  private Executor executor = Executors.newFixedThreadPool(10);


  public void register(RestServer server) {

    server.get(PATH, (HttpRequest<String[]> request) -> {
      run(request, this::get);
    });
    server.post(PATH, (HttpRequest<Boolean> request) -> {
      run(request, () -> post(request.get(TodoItem[].class)));
    });
    server.put(PATH, (HttpRequest<Boolean> request) -> {
      run(request, () -> put(request.get(TodoItem.class)));
    });
    server.delete(PATH, (HttpRequest<Boolean> request) -> {
      run(request, (Runnable) this::delete);
    });

    server.get(PATH_ID, request -> {
      run(request, () -> get(request.get(0, int.class)));
    });
    server.put(PATH_ID, (HttpRequest<Boolean> request) -> {
      run(request, () -> put(request.get(0, int.class), request.get(TodoItem.class)));
    });
    server.delete(PATH_ID, (HttpRequest<Boolean> request) -> {
      run(request, () -> delete(request.get(0, int.class)));
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


  private <T> void run(HttpRequest<T> request,
                       Supplier<T> supplier) {

    executor.execute(() -> request.respond(supplier.get()));
  }


  private void run(HttpRequest<Boolean> request,
                   Runnable runnable) {

    run(request, () -> {
      runnable.run();
      return Boolean.TRUE;
    });
  }
}
