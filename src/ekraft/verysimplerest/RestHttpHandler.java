package ekraft.verysimplerest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ekraft.verysimplerest.utils.RestException;
import ekraft.verysimplerest.utils.RestUrlParameters;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

import static ekraft.verysimplerest.utils.RestConstants.DELETE;
import static ekraft.verysimplerest.utils.RestConstants.GET;
import static ekraft.verysimplerest.utils.RestConstants.POST;
import static ekraft.verysimplerest.utils.RestConstants.PUT;


public abstract class RestHttpHandler<T>
  implements HttpHandler {

  protected ObjectMapper objectMapper = new ObjectMapper();

  private Map<String, T> getMap = new LinkedHashMap<>();
  private Map<String, T> putMap = new LinkedHashMap<>();
  private Map<String, T> postMap = new LinkedHashMap<>();
  private Map<String, T> deleteMap = new LinkedHashMap<>();

  private RestServer server;


  public RestHttpHandler(RestServer server) {

    this.server = server;
  }


  public abstract String getPath();

  protected abstract void handle(HttpExchange httpExchange,
                                 T handler,
                                 RestUrlParameters urlParameters,
                                 String rawRequest);


  public void addHandler(String method,
                         String path,
                         T handler) {

    switch (method) {
      case GET:
        getMap.put(path, handler);
        break;
      case PUT:
        putMap.put(path, handler);
        break;
      case POST:
        postMap.put(path, handler);
        break;
      case DELETE:
        deleteMap.put(path, handler);
        break;
    }
  }


  public void removeHandlers(String path) {

    removeHandler(getMap.remove(path));
    removeHandler(putMap.remove(path));
    removeHandler(postMap.remove(path));
    removeHandler(deleteMap.remove(path));
  }


  public void removeHandler(T handler) {

  }


  public void removeAll() {

    getMap.clear();
    putMap.clear();
    postMap.clear();
    deleteMap.clear();
  }


  public boolean isEmpty() {

    return getMap.isEmpty() &&
      putMap.isEmpty() &&
      postMap.isEmpty() &&
      deleteMap.isEmpty();
  }


  public void handle(HttpExchange httpExchange) {

    String method = httpExchange.getRequestMethod();
    switch (method) {
      case GET:
        handle(GET, getMap, httpExchange);
        break;
      case PUT:
        handle(PUT, putMap, httpExchange);
        break;
      case POST:
        handle(POST, postMap, httpExchange);
        break;
      case DELETE:
        handle(DELETE, deleteMap, httpExchange);
        break;
      default:
        error(httpExchange, HttpURLConnection.HTTP_NOT_IMPLEMENTED, "Unimplemented Method: " + method);
        break;
    }
  }


  private void handle(String method,
                      Map<String, T> map,
                      HttpExchange httpExchange) {

    String path = httpExchange.getRequestURI().getPath();
    for (Map.Entry<String, T> entry : map.entrySet()) {
      RestUrlParameters urlParameters = getUrlParameters(path, entry.getKey());
      if (urlParameters == null) {
        continue;
      }

      handle(httpExchange, entry.getValue(), urlParameters);
      return;
    }

    if (server.getDebug()) {
      debugResponse(httpExchange);
    } else {
      error(httpExchange, HttpURLConnection.HTTP_NOT_FOUND, "URI " + path + " unavailable for method " + method);
    }
  }


  private RestUrlParameters getUrlParameters(String path,
                                             String match) {

    String[] pathTokens = path.split("/");
    String[] matchTokens = match.split("/");
    if (pathTokens.length != matchTokens.length) {
      return null;
    }

    RestUrlParameters restUrlParameters = new RestUrlParameters();

    for (int i = 0; i < pathTokens.length; i++) {
      if (!restUrlParameters.evaluate(pathTokens[i], matchTokens[i])) {
        return null;
      }
    }

    return restUrlParameters;
  }


  private void handle(HttpExchange httpExchange,
                      T handler,
                      RestUrlParameters urlParameters) {

    try {
      InputStream inputStream = httpExchange.getRequestBody();
      String rawRequest = new String(IOUtils.readFully(inputStream, -1, false));
      inputStream.close();

      handle(httpExchange, handler, urlParameters, rawRequest);
    } catch (IOException e) {
      RestException.log(e);
      error(httpExchange, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
    }
  }


  public void debugResponse(HttpExchange httpExchange) {

    rawRespond(httpExchange, HttpURLConnection.HTTP_BAD_METHOD, server.getDebugResponse());
  }


  public String getDebugResponse() {

    String response = "";
    for (String URI : getMap.keySet()) {
      response += GET + " " + URI + "\n";
    }
    for (String URI : putMap.keySet()) {
      response += PUT + " " + URI + "\n";
    }
    for (String URI : postMap.keySet()) {
      response += POST + " " + URI + "\n";
    }
    for (String URI : deleteMap.keySet()) {
      response += DELETE + " " + URI + "\n";
    }
    return response;
  }


  protected void timeout(Runnable runnable) {

    server.timeout(runnable);
  }


  public void respond(HttpExchange httpExchange,
                      Object response) {

    try {
      respond(httpExchange, HttpURLConnection.HTTP_OK, objectMapper.writeValueAsString(response));
    } catch (IOException e) {
      RestException.log(e);
      error(httpExchange, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
    }
  }


  public void rawRespond(HttpExchange httpExchange,
                         int code,
                         String rawResponse) {

    respond(httpExchange, code, rawResponse);
  }


  public void error(HttpExchange httpExchange,
                    int errorCode,
                    String cause) {

    respond(httpExchange, errorCode, cause);
  }


  public void respond(HttpExchange httpExchange,
                      int code,
                      String responseBody) {

    try {
      httpExchange.sendResponseHeaders(code, responseBody.length());

      OutputStream outputStream = httpExchange.getResponseBody();
      outputStream.write(responseBody.getBytes());
      outputStream.close();
    } catch (IOException e) {
      RestException.log(e);
    } finally {
      httpExchange.close();
    }
  }
}
