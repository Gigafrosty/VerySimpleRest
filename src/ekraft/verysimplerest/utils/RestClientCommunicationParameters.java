package ekraft.verysimplerest.utils;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class RestClientCommunicationParameters {

  private String url;
  private Object[] parameters = null;


  public RestClientCommunicationParameters(String url,
                                           Object[] parameters)
    throws IOException {

    if (!url.contains("*") && !url.contains("{")) {
      this.url = url;
      this.parameters = parameters;
      return;
    }

    ObjectMapper objectMapper = new ObjectMapper();
    List<Object> parameterList = new ArrayList<>();
    Collections.addAll(parameterList, parameters);

    String[] splitUrl = url.split("/");
    for (int i = 0; i < splitUrl.length; i++) {
      if (!splitUrl[i].equals("*") && !splitUrl[i].matches("\\{\\w+\\}")) {
        continue;
      }

      String element = objectMapper.writeValueAsString(parameterList.remove(0));
      if (element.startsWith("\"")) {
        element = element.substring(1, element.length() - 1);
      }

      splitUrl[i] = element;
    }

    this.url = "";
    for (int i = 0; i < splitUrl.length; i++) {
      if (i > 0) {
        this.url += "/";
      }
      this.url += splitUrl[i];
    }

    this.parameters = parameterList.toArray(new Object[parameterList.size()]);
  }


  public String getUrl() {

    return url;
  }


  public Object[] getParameters() {

    if (parameters == null) {
      return null;
    }

    if (parameters.length == 0) {
      return null;
    }

    return parameters;
  }
}
