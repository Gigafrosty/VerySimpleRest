package ekraft.verysimplerest.annotation;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import ekraft.verysimplerest.RestHttpHandler;
import ekraft.verysimplerest.RestServer;
import ekraft.verysimplerest.utils.RestException;
import ekraft.verysimplerest.utils.RestUrlParameters;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static ekraft.verysimplerest.utils.RestConstants.DELETE;
import static ekraft.verysimplerest.utils.RestConstants.GET;
import static ekraft.verysimplerest.utils.RestConstants.POST;
import static ekraft.verysimplerest.utils.RestConstants.PUT;


public class AnnotationHttpHandler
  extends RestHttpHandler<Method> {

  private Map<Method, Class> returnTypes = new HashMap<>();
  private Map<Method, Class[]> parameterClasses = new HashMap<>();
  private Set<Method> asynchronous = new HashSet<>();

  private String basePath;
  private Object service;


  public AnnotationHttpHandler(RestServer server,
                               Object service) {

    super(server);

    if (service.getClass().getAnnotation(Path.class) == null) {
      throw new IllegalArgumentException("Service lacks class-level web path: " + service.getClass().getName());
    }

    this.service = service;
    this.basePath = AnnotationUtils.getPath(service);
    for (Method method : service.getClass().getMethods()) {
      if (AnnotationUtils.isRestMethod(method)) {
        addMethod(method);
      }
    }

    if (isEmpty()) {
      throw new IllegalArgumentException(
        "No valid REST methods were found for service " + service.getClass().getName());
    }
  }


  private void addMethod(Method method) {

    boolean asynchronous = AnnotationUtils.isAsynchronous(method);
    if (asynchronous && method.getReturnType() != void.class) {
      throw new IllegalArgumentException(
        "Asynchronous methods are not allowed to have a return type: " + method.getDeclaringClass().getName() + "." + method.getName() + "()");
    }

    String path = AnnotationUtils.getPath(method);
    Class returnType = AnnotationUtils.getReturnType(method);

    Parameter[] parameters = method.getParameters();
    if (asynchronous) {
      Parameter[] newParameters = new Parameter[parameters.length - 1];
      System.arraycopy(parameters, 0, newParameters, 0, newParameters.length);
      parameters = newParameters;
    }

    Class[] arguments = new Class[parameters.length];
    for (int i = 0; i < arguments.length; i++) {
      arguments[i] = parameters[i].getType();
    }

    addToMethodMaps(method, path);
    returnTypes.put(method, returnType);
    parameterClasses.put(method, arguments);

    if (asynchronous) {
      this.asynchronous.add(method);
    }
  }


  @Override
  public void removeHandler(Method handler) {

    asynchronous.remove(handler);
    returnTypes.remove(handler);
    parameterClasses.remove(handler);
  }


  private void addToMethodMaps(Method method,
                               String path) {

    boolean added = false;

    if (method.getAnnotation(PUT.class) != null) {
      addHandler(PUT, path, method);
      added = true;
    }

    if (method.getAnnotation(POST.class) != null) {
      addHandler(POST, path, method);
      added = true;
    }

    if (method.getAnnotation(DELETE.class) != null) {
      addHandler(DELETE, path, method);
      added = true;
    }

    if (!added || method.getAnnotation(GET.class) != null) {
      addHandler(GET, path, method);
    }
  }


  public String getPath() {

    return basePath;
  }


  protected void handle(HttpExchange httpExchange,
                        Method method,
                        RestUrlParameters urlParameters,
                        String rawRequest) {

    try {
      Object[] arguments = getArguments(httpExchange, method, urlParameters, rawRequest);

      timeout(() -> {
        try {
          error(httpExchange, 408, "Timed Out.");
        } catch (RestException restException) {
          // Don't need it to float up more.
        }
      });

      Object response = method.invoke(service, arguments);

      if (asynchronous.contains(method)) {
        return;
      }

      if (returnTypes.get(method) == void.class) {
        respond(httpExchange, Boolean.TRUE);
      } else {
        respond(httpExchange, response);
      }
    } catch (InvocationTargetException | IllegalAccessException | IOException e) {
      RestException.log(e);
      error(httpExchange, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
    }
  }


  private Object[] getArguments(HttpExchange httpExchange,
                                Method method,
                                RestUrlParameters urlParameters,
                                String rawRequest)
    throws IOException {

    ObjectMapper objectMapper = new ObjectMapper();

    Class[] argumentClasses = parameterClasses.get(method);
    Object[] arguments;
    if (asynchronous.contains(method)) {
      arguments = new Object[argumentClasses.length + 1];
    } else {
      arguments = new Object[argumentClasses.length];
    }

    if (asynchronous.contains(method)) {
      Class returnType = returnTypes.get(method);
      arguments[argumentClasses.length] = getCallback(httpExchange, returnType);
    }

    if (argumentClasses.length == 1) {
      try {
        arguments[0] = objectMapper.readValue(rawRequest, argumentClasses[0]);
        return arguments;
      } catch (IOException e) {
        // In the normal case this will fail.
      }
    }

    JsonNode jsonNode = null;
    int urlParameterIndex = 0;
    int nodeIndex = 0;

    for (int i = 0; i < argumentClasses.length; i++) {
      if (argumentClasses[i] == RestUrlParameters.class) {
        arguments[i] = urlParameters;
        urlParameterIndex = urlParameters.size();
        continue;
      }

      if (urlParameterIndex < urlParameters.size()) {
        arguments[i] = urlParameters.get(urlParameterIndex, argumentClasses[i]);
        urlParameterIndex++;
        continue;
      }

      if (jsonNode == null) {
        if (rawRequest.equals("")) {
          throw new IOException("Ran out of parameters and had empty method body!");
        }
        jsonNode = objectMapper.readTree(rawRequest);
      }

      JsonNode item = jsonNode.get(nodeIndex);

      // If we're only looking for one item, if trying to look at the raw request as a list doesn't give us a list
      // then try looking it as a single entry.
      if ((item == null) &&
        (nodeIndex == 0) &&
        (i == argumentClasses.length - 1)) {
        item = jsonNode;
      }

      JsonParser parser = item.traverse(objectMapper);
      arguments[i] = parser.readValueAs(argumentClasses[i]);
      nodeIndex++;
    }

    return arguments;
  }


  private <T> Consumer<T> getCallback(HttpExchange httpExchange,
                                      Class<T> clazz) {

    return (T value) -> respond(httpExchange, value);
  }
}
