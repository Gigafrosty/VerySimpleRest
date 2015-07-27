package ekraft.verysimplerest.lambda;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import ekraft.verysimplerest.utils.RestException;
import ekraft.verysimplerest.RestHttpHandler;
import ekraft.verysimplerest.utils.RestUrlParameters;

import java.io.IOException;


public class HttpRequest<T> {

  private ObjectMapper objectMapper = new ObjectMapper();
  private boolean responded = false;

  private RestHttpHandler httpHandler;
  private HttpExchange httpExchange;
  private RestUrlParameters urlParameters;
  private JsonNode jsonNode;


  public HttpRequest(RestHttpHandler restHttpHandler,
                     HttpExchange httpExchange,
                     RestUrlParameters urlParameters,
                     String requestBody)
    throws IOException {

    this.httpHandler = restHttpHandler;
    this.httpExchange = httpExchange;
    this.urlParameters = urlParameters;

    if (requestBody.equals("")) {
      jsonNode = null;
    } else {
      jsonNode = objectMapper.readTree(requestBody);
    }
  }


  public <R> R get(int index,
                   Class<R> clazz) {

    return urlParameters.get(index, clazz);
  }


  public <R> R get(String key,
                   Class<R> clazz) {

    try {
      if (urlParameters.hasParameter(key)) {
        return urlParameters.get(key, clazz);
      }
      return jsonNode.get(key).traverse(objectMapper).readValueAs(clazz);
    } catch (IOException e) {
      throw new RestException(e);
    }
  }


  public <R> R get(Class<R> clazz) {

    try {
      return jsonNode.traverse(objectMapper).readValueAs(clazz);
    } catch (IOException e) {
      throw new RestException(e);
    }
  }


  public String get(int index) {

    return urlParameters.get(index);
  }


  public String get(String key) {

    return urlParameters.get(key);
  }


  public RestUrlParameters getParameters() {

    return urlParameters;
  }


  public synchronized void respond(T response) {

    if (responded) {
      throw new IllegalStateException("Attempted to respond when already responded!");
    }
    responded = true;
    httpHandler.respond(httpExchange, response);
  }


  public synchronized void error(int code,
                                 String cause) {

    if (responded) {
      throw new IllegalStateException("Attempted to respond when already responded!");
    }
    responded = true;
    httpHandler.error(httpExchange, code, cause);
  }
}
