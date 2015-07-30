package ekraft.verysimplerest.utils;


import java.util.function.Consumer;


public class RestException
  extends RuntimeException {

  private static Consumer<Throwable> handler = null;


  public RestException(String message) {

    super(message);
    log(this);
  }


  public RestException(String message,
                       Throwable cause) {

    super(message, cause);
    log(this);
  }


  public RestException(Throwable cause) {

    super(cause);
    log(this);
  }


  public static synchronized void log(Throwable restException) {

    if (handler != null) {
      handler.accept(restException);
    }
  }


  public static synchronized void setHandler(Consumer<Throwable> handler) {

    RestException.handler = handler;
  }
}
