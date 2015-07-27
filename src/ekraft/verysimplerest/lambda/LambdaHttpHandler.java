package ekraft.verysimplerest.lambda;


import com.sun.net.httpserver.HttpExchange;
import ekraft.verysimplerest.RestHttpHandler;
import ekraft.verysimplerest.RestServer;
import ekraft.verysimplerest.utils.RestException;
import ekraft.verysimplerest.utils.RestUrlParameters;

import java.io.IOException;
import java.net.HttpURLConnection;


public class LambdaHttpHandler
  extends RestHttpHandler<HttpRequestHandler> {


  public LambdaHttpHandler(RestServer server) {

    super(server);
  }


  @Override
  public void addHandler(String method,
                         String path,
                         HttpRequestHandler handler) {

    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    super.addHandler(method, path, handler);
  }


  @Override
  public void removeHandlers(String path) {

    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    super.removeHandlers(path);

  }


  public String getPath() {

    return "/";
  }


  protected void handle(HttpExchange httpExchange,
                        HttpRequestHandler httpRequestHandler,
                        RestUrlParameters urlParameters,
                        String rawRequest) {

    handleGeneric(httpExchange, httpRequestHandler, urlParameters, rawRequest);
  }


  private <T> void handleGeneric(HttpExchange httpExchange,
                                 HttpRequestHandler<T> handler,
                                 RestUrlParameters urlParameters,
                                 String rawRequest) {

    HttpRequest<T> httpRequest;
    try {
      httpRequest = new HttpRequest<>(this, httpExchange, urlParameters, rawRequest);
    } catch (IOException e) {
      RestException.log(e);
      error(httpExchange, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
      return;
    }

    timeout(() -> httpRequest.error(408, "Timed Out."));

    try {
      handler.handle(httpRequest);
    } catch (Exception e) {
      RestException.log(e);
      httpRequest.error(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
    }
  }
}
