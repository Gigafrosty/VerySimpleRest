package ekraft.verysimplerest;


import com.fasterxml.jackson.databind.ObjectMapper;
import ekraft.verysimplerest.annotation.AnnotationUtils;
import ekraft.verysimplerest.utils.RestClientCommunicationParameters;
import ekraft.verysimplerest.utils.RestException;
import sun.misc.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import static ekraft.verysimplerest.utils.RestConstants.DELETE;
import static ekraft.verysimplerest.utils.RestConstants.GET;
import static ekraft.verysimplerest.utils.RestConstants.POST;
import static ekraft.verysimplerest.utils.RestConstants.PUT;


public class RestClient {

  private ObjectMapper objectMapper = new ObjectMapper();

  private String host;
  private int port;


  public RestClient(String host,
                    int port) {

    this.host = host;
    this.port = port;
  }


  public void get(Method method) {

    get(AnnotationUtils.getPath(method), (Object[]) null);
  }


  public void get(Method method,
                  Object... parameters) {

    get(AnnotationUtils.getPath(method), parameters);
  }


  public void get(String path) {

    communicate(GET, path, (Object[]) null);
  }


  public void get(String path,
                  Object... parameters) {

    communicate(GET, path, parameters);
  }


  public <T> T get(Method method,
                   Class<T> returnType) {

    return get(AnnotationUtils.getPath(method), returnType, (Object[]) null);
  }


  public <T> T get(Method method,
                   Class<T> returnType,
                   Object... parameters) {

    return get(AnnotationUtils.getPath(method), returnType, parameters);
  }


  public <T> T get(String path,
                   Class<T> returnType) {

    return format(communicate(GET, path, (Object[]) null), returnType);
  }


  public <T> T get(String path,
                   Class<T> returnType,
                   Object... parameters) {

    return format(communicate(GET, path, parameters), returnType);
  }


  public void put(Method method) {

    put(AnnotationUtils.getPath(method), (Object[]) null);
  }


  public void put(Method method,
                  Object... parameters) {

    put(AnnotationUtils.getPath(method), parameters);
  }


  public void put(String path) {

    communicate(PUT, path, (Object[]) null);
  }


  public void put(String path,
                  Object... parameters) {

    communicate(PUT, path, parameters);
  }


  public <T> T put(Method method,
                   Class<T> returnType) {

    return put(AnnotationUtils.getPath(method), returnType, (Object[]) null);
  }


  public <T> T put(Method method,
                   Class<T> returnType,
                   Object... parameters) {

    return put(AnnotationUtils.getPath(method), returnType, parameters);
  }


  public <T> T put(String path,
                   Class<T> returnType) {

    return format(communicate(PUT, path, (Object[]) null), returnType);
  }


  public <T> T put(String path,
                   Class<T> returnType,
                   Object... parameters) {

    return format(communicate(PUT, path, parameters), returnType);
  }


  public void post(Method method) {

    post(AnnotationUtils.getPath(method), (Object[]) null);
  }


  public void post(Method method,
                   Object... parameters) {

    post(AnnotationUtils.getPath(method), parameters);
  }


  public void post(String path) {

    communicate(POST, path, (Object[]) null);
  }


  public void post(String path,
                   Object... parameters) {

    communicate(POST, path, parameters);
  }


  public <T> T post(Method method,
                    Class<T> returnType) {

    return post(AnnotationUtils.getPath(method), returnType, (Object[]) null);
  }


  public <T> T post(Method method,
                    Class<T> returnType,
                    Object... parameters) {

    return post(AnnotationUtils.getPath(method), returnType, parameters);
  }


  public <T> T post(String path,
                    Class<T> returnType) {

    return format(communicate(POST, path, (Object[]) null), returnType);
  }


  public <T> T post(String path,
                    Class<T> returnType,
                    Object... parameters) {

    return format(communicate(POST, path, parameters), returnType);
  }


  public void delete(Method method) {

    delete(AnnotationUtils.getPath(method), (Object[]) null);
  }


  public void delete(Method method,
                     Object... parameters) {

    delete(AnnotationUtils.getPath(method), parameters);
  }


  public void delete(String path) {

    communicate(DELETE, path, (Object[]) null);
  }


  public void delete(String path,
                     Object... parameters) {

    communicate(DELETE, path, parameters);
  }


  public <T> T delete(Method method,
                      Class<T> returnType) {

    return delete(AnnotationUtils.getPath(method), returnType, (Object[]) null);
  }


  public <T> T delete(Method method,
                      Class<T> returnType,
                      Object... parameters) {

    return delete(AnnotationUtils.getPath(method), returnType, parameters);
  }


  public <T> T delete(String path,
                      Class<T> returnType) {

    return format(communicate(DELETE, path, (Object[]) null), returnType);
  }


  public <T> T delete(String path,
                      Class<T> returnType,
                      Object... parameters) {

    return format(communicate(DELETE, path, parameters), returnType);
  }


  private <T> T format(String result,
                       Class<T> returnType) {

    if (result == null) {
      return null;
    }

    try {
      return objectMapper.readValue(result, returnType);
    } catch (IOException e) {
      throw new RestException(e);
    }
  }


  private String communicate(String method,
                             String path,
                             Object... parameters) {

    try {
      RestClientCommunicationParameters communicationParameters = new RestClientCommunicationParameters(path,
        parameters);
      path = communicationParameters.getUrl();
      parameters = communicationParameters.getParameters();

      if (parameters == null) {
        return communicate(method, path, "");
      }

      if (parameters.length == 1) {
        return communicate(method, path, objectMapper.writeValueAsString(parameters[0]));
      } else {
        return communicate(method, path, objectMapper.writeValueAsString(parameters));
      }
    } catch (IOException e) {
      throw new RestException(e);
    }
  }


  private String communicate(String method,
                             String path,
                             String rawRequestBody) {

    return communicate(host, port, method, path, rawRequestBody, false);
  }


  public static String communicate(String host,
                                   int port,
                                   String method,
                                   String path,
                                   String rawRequestBody,
                                   boolean getErrorMessage) {

    if (rawRequestBody == null) {
      rawRequestBody = "";
    }

    try {
      URL url = new URL("http", host, port, path);
      HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
      try {

        httpURLConnection.setRequestMethod(method);

        if (!method.equals(GET)) {
          httpURLConnection.setDoOutput(true);
          OutputStream outputStream = httpURLConnection.getOutputStream();
          outputStream.write(rawRequestBody.getBytes());
          outputStream.close();
        }

        httpURLConnection.connect();

        try {
          InputStream inputStream = httpURLConnection.getInputStream();
          byte[] response = IOUtils.readFully(inputStream, -1, false);
          inputStream.close();

          return new String(response);
        } catch (IOException e) {
          if (!getErrorMessage) {
            throw e;
          }

          InputStream inputStream = httpURLConnection.getErrorStream();
          byte[] response = IOUtils.readFully(inputStream, -1, false);
          inputStream.close();

          return new String(response);
        }
      } catch (FileNotFoundException e) {
        RestException.log(e);
        return null;
      } finally {
        httpURLConnection.disconnect();
      }

    } catch (IOException e) {
      throw new RestException(e);
    }
  }
}
