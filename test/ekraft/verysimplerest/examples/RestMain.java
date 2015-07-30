package ekraft.verysimplerest.examples;


import ekraft.verysimplerest.RestServer;
import ekraft.verysimplerest.utils.RestException;


public class RestMain {

  private static boolean keepRunning = true;


  private static void shutdown() {

    System.out.println("Shutting down...");
    keepRunning = false;
  }


  public static void main(String[] args) {

    RestException.setHandler(Throwable::printStackTrace);

    int port = 8080;
    RestServer restServer = new RestServer(port);
    restServer.setDebug(true);

    registerLambdaExamples(restServer);
    restServer.addService(new AnnotationTodoRestService());
    restServer.addService(new AsyncAnnotationTodoService());
    new LambdaTodoRestService().register(restServer);
    new AsyncLambdaTodoService().register(restServer);

    System.out.println("Entering loop.");
    while (keepRunning) {
      try {
        //System.out.println("In loop...");
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.out.println("Exiting loop.");

    restServer.shutdown();
    System.out.println("Shut down.");
  }


  public static void registerLambdaExamples(RestServer restServer) {

    // A Useful way to stop the server from the command line!
    restServer.all("shutdown", RestMain::shutdown);

    // Return some simple values.
    restServer.get("hello", () -> "world");
    restServer.put("hello", () -> "moon");

    // Parse matches elements from HTTP URL, String example.
    restServer.get("string/double/*", request -> {
      request.respond(request.get(0, String.class) + request.get(0, String.class));
    });

    // Parse matches elements from HTTP URL, Integer example.
    restServer.get("multi/*/*", request -> {
      int value1 = request.get(0, int.class);
      int value2 = request.get(1, int.class);
      request.respond(value1 * value2);
    });

    // Parse items from message body as a map.
    restServer.get("string/split", request -> {
      String line = request.get("line", String.class);
      String token = request.get("token", String.class);
      request.respond(line.split(token));
    });

    // Return a custom data storage class: TodoItem
    restServer.all("test/new/{name}", request -> {
      TodoItem todoItem = new TodoItem("A quick-generated Todo Item", request.get("name", String.class));
      request.respond(todoItem);
    });

    // Parse message body as a TodoItem class
    restServer.all("test", request -> {
      TodoItem todoItem = request.get(TodoItem.class);
      request.respond(
        todoItem.getName() + " received with description \"" + todoItem.getDescription() + "\" and date " + todoItem.getDue().getTime());
    });
  }
}
