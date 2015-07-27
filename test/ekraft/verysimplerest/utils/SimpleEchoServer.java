package ekraft.verysimplerest.utils;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;


public class SimpleEchoServer
  implements HttpHandler {

  private HttpServer server;
  private String lastEcho = null;


  public SimpleEchoServer(int port)
    throws IOException {

    server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
    server.setExecutor(null);
    server.start();

    server.createContext("/", this);
  }


  @Override
  public void handle(HttpExchange httpExchange)
    throws IOException {

    String method = httpExchange.getRequestMethod();
    String path = httpExchange.getRequestURI().getPath();

    InputStream inputStream = httpExchange.getRequestBody();
    String rawRequest = new String(IOUtils.readFully(inputStream, -1, false));
    inputStream.close();

    if (rawRequest.startsWith("\"")) {
      rawRequest = rawRequest.substring(1, rawRequest.length() - 1);
    }

    lastEcho = method + "," + path + "," + rawRequest;
    String response = "\"" + method + "," + path + "," + rawRequest.replaceAll("\"", "\\\\\"") + "\"";

    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());

    OutputStream outputStream = httpExchange.getResponseBody();
    outputStream.write(response.getBytes());
    outputStream.close();

    httpExchange.close();
  }


  public String getLastEcho() {

    return lastEcho;
  }


  public void shutdown() {

    server.stop(0);
  }
}
