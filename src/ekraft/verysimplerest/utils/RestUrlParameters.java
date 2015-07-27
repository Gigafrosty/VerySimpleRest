package ekraft.verysimplerest.utils;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RestUrlParameters {

  private ObjectMapper objectMapper = new ObjectMapper();

  private List<String> parameters = new ArrayList<>();
  private Map<String, String> namedParameters = new HashMap<>();


  public boolean evaluate(String pathToken,
                          String matchToken) {

    if (matchToken.equals("*")) {
      addParameter(pathToken);
      return true;
    }

    if (matchToken.matches("\\{\\w+\\}")) {
      String id = matchToken.substring(1, matchToken.length() - 1);
      addParameter(pathToken);
      addNamedParameter(id, pathToken);
      return true;
    }

    return matchToken.equals(pathToken);
  }


  public void addParameter(String parameter) {

    parameters.add(parameter);
  }


  public void addNamedParameter(String name,
                                String parameter) {

    namedParameters.put(name, parameter);
  }


  public <T> T get(int index,
                   Class<T> clazz) {

    if (clazz == String.class) {
      return clazz.cast(get(index));
    }

    try {
      return objectMapper.readValue(parameters.get(index), clazz);
    } catch (IOException e) {
      throw new RestException(e);
    }
  }


  public <T> T get(String key,
                   Class<T> clazz) {

    if (clazz == String.class) {
      return clazz.cast(get(key));
    }

    try {
      return objectMapper.readValue(namedParameters.get(key), clazz);
    } catch (IOException e) {
      throw new RestException(e);
    }
  }


  public String get(int index) {

    return parameters.get(index);
  }


  public String get(String key) {

    return namedParameters.get(key);
  }


  public int size() {

    return parameters.size();
  }


  public boolean hasParameter(String key) {

    return namedParameters.containsKey(key);
  }
}
