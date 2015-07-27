package ekraft.verysimplerest.utils;


import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class TestUtils {


  public static boolean serverIsUp(int port) {

    try {
      URL url = new URL("http", "localhost", port, "/");
      HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

      httpURLConnection.setDoInput(true);
      httpURLConnection.setDoOutput(true);
      httpURLConnection.setRequestMethod("GET");
      httpURLConnection.connect();

      OutputStream outputStream = httpURLConnection.getOutputStream();
      outputStream.write("".getBytes());
      outputStream.close();

      httpURLConnection.disconnect();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
