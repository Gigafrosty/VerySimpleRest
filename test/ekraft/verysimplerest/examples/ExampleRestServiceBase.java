package ekraft.verysimplerest.examples;


import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;


@Path("services")
public abstract class ExampleRestServiceBase {

  // Not needed ; just to show that asynchronous calls passed to another thread
  // will respond correctly.
  private Executor executor = Executors.newFixedThreadPool(10);


  protected void run(Consumer<Boolean> callback,
                     Runnable runnable) {

    run(callback, () -> {
      runnable.run();
      return Boolean.TRUE;
    });
  }


  protected <T> void run(Consumer<T> callback,
                         Supplier<T> supplier) {

    executor.execute(() -> callback.accept(supplier.get()));
  }


  public static Method getMethod(Class clazz, String name, Class... parameters) {

    try {
      return clazz.getMethod(name, parameters);
    } catch (Exception e) {
      return null;
    }
  }
}
