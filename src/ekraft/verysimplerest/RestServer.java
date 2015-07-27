package ekraft.verysimplerest;


import com.sun.net.httpserver.HttpServer;
import ekraft.verysimplerest.annotation.AnnotationHttpHandler;
import ekraft.verysimplerest.lambda.HttpRequest;
import ekraft.verysimplerest.lambda.HttpRequestHandler;
import ekraft.verysimplerest.lambda.LambdaHttpHandler;
import ekraft.verysimplerest.utils.RestException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static ekraft.verysimplerest.utils.RestConstants.DELETE;
import static ekraft.verysimplerest.utils.RestConstants.GET;
import static ekraft.verysimplerest.utils.RestConstants.POST;
import static ekraft.verysimplerest.utils.RestConstants.PUT;


public class RestServer {

  private static final int DEFAULT_TIMEOUT = 5;
  private static final int DEFAULT_THREAD_POOL_SIZE = 100;
  private static final int DEFAULT_BACKLOG_SIZE = 100;

  private boolean debug = false;
  private Map<Object, RestHttpHandler> services = new LinkedHashMap<>();

  private LambdaHttpHandler lambdaHttpHandler = new LambdaHttpHandler(this);
  private HttpServer server = null;
  private InetSocketAddress inetSocketAddress;
  private ScheduledExecutorService executor;
  private int timeout;


  public RestServer(int port) {

    inetSocketAddress = new InetSocketAddress("localhost", port);
    executor = Executors.newScheduledThreadPool(DEFAULT_THREAD_POOL_SIZE);
    this.timeout = DEFAULT_TIMEOUT;
  }


  public synchronized void addService(Object service) {

    if (services.containsKey(service)) {
      return;
    }

    RestHttpHandler handler = getHttpHandler(service);
    if (handler == null) {
      throw new IllegalArgumentException("Unhandled Service");
    }

    startServer();
    server.createContext(handler.getPath(), handler);
    services.put(service, handler);
  }


  public synchronized void shutdownService(Object service) {

    if (!services.containsKey(service)) {
      return;
    }

    server.removeContext(services.get(service).getPath());
    services.remove(service);

    if (services.isEmpty()) {
      stopServer();
    }
  }


  public void get(String path,
                  Runnable runnable) {

    get(path, (HttpRequest<Boolean> request) -> run(request, runnable));
  }


  public <T> void get(String path,
                      Supplier<T> supplier) {

    get(path, (HttpRequest<T> request) -> supplier(request, supplier));
  }


  public <T> void get(String path,
                      HttpRequestHandler<T> httpRequestHandler) {

    addLambda(GET, path, httpRequestHandler);
  }


  public void put(String path,
                  Runnable runnable) {

    put(path, (HttpRequest<Boolean> request) -> run(request, runnable));
  }


  public <T> void put(String path,
                      Supplier<T> supplier) {

    put(path, (HttpRequest<T> request) -> supplier(request, supplier));
  }


  public <T> void put(String path,
                      HttpRequestHandler<T> httpRequestHandler) {

    addLambda(PUT, path, httpRequestHandler);
  }


  public void post(String path,
                   Runnable runnable) {

    post(path, (HttpRequest<Boolean> request) -> run(request, runnable));
  }


  public <T> void post(String path,
                       Supplier<T> supplier) {

    post(path, (HttpRequest<T> request) -> supplier(request, supplier));
  }


  public <T> void post(String path,
                       HttpRequestHandler<T> httpRequestHandler) {

    addLambda(POST, path, httpRequestHandler);
  }


  public void delete(String path,
                     Runnable runnable) {

    delete(path, (HttpRequest<Boolean> request) -> run(request, runnable));
  }


  public <T> void delete(String path,
                         Supplier<T> supplier) {

    delete(path, (HttpRequest<T> request) -> supplier(request, supplier));
  }


  public <T> void delete(String path,
                         HttpRequestHandler<T> httpRequestHandler) {

    addLambda(DELETE, path, httpRequestHandler);
  }


  public void all(String path,
                  Runnable runnable) {

    all(path, (HttpRequest<Boolean> request) -> run(request, runnable));
  }


  public <T> void all(String path,
                      Supplier<T> supplier) {

    all(path, (HttpRequest<T> request) -> supplier(request, supplier));
  }


  public <T> void all(String path,
                      HttpRequestHandler<T> httpRequestHandler) {

    addLambda(GET, path, httpRequestHandler);
    addLambda(PUT, path, httpRequestHandler);
    addLambda(POST, path, httpRequestHandler);
    addLambda(DELETE, path, httpRequestHandler);
  }


  public synchronized void removePath(String path) {

    new HashSet<>(services.values()).forEach(httpHandler -> {
      httpHandler.removeHandlers(path);

      if (httpHandler.isEmpty()) {
        shutdownService(httpHandler);
      }
    });
  }


  public synchronized void shutdown() {

    new HashSet<>(services.keySet()).forEach(this::shutdownService);
    lambdaHttpHandler.removeAll();
  }


  private synchronized <T> void addLambda(String method,
                                          String path,
                                          HttpRequestHandler<T> httpRequestHandler) {

    if (lambdaHttpHandler.isEmpty()) {
      addService(lambdaHttpHandler);
    }

    lambdaHttpHandler.addHandler(method, path, httpRequestHandler);
  }


  private <T> void supplier(HttpRequest<T> request,
                            Supplier<T> supplier) {

    request.respond(supplier.get());
  }


  private void run(HttpRequest<Boolean> request,
                   Runnable runnable) {

    runnable.run();
    request.respond(Boolean.TRUE);
  }


  private RestHttpHandler getHttpHandler(Object service) {

    if (service == null) {
      return null;
    }

    if (service instanceof RestHttpHandler) {
      return (RestHttpHandler) service;
    }

    return new AnnotationHttpHandler(this, service);
  }


  public synchronized String getDebugResponse() {

    String response = "";
    for (RestHttpHandler httpHandler : services.values()) {
      response += httpHandler.getDebugResponse();
    }
    return response;
  }


  private void startServer() {

    if (server != null) {
      return;
    }

    try {
      server = HttpServer.create(inetSocketAddress, DEFAULT_BACKLOG_SIZE);
      server.setExecutor(executor);
      server.start();
    } catch (IOException e) {
      throw new RestException(e.getMessage(), e);
    }
  }


  private void stopServer() {

    server.stop(0);
    server = null;

    executor.shutdownNow();
  }


  protected void timeout(Runnable runnable) {

    timeout(runnable, timeout, TimeUnit.SECONDS);
  }


  protected void timeout(Runnable runnable,
                         int timeout,
                         TimeUnit timeUnit) {

    executor.schedule(runnable, timeout, timeUnit);
  }


  public void setDebug(boolean debug) {

    this.debug = debug;
  }


  public boolean getDebug() {

    return debug;
  }
}
