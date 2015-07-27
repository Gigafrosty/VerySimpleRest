package ekraft.verysimplerest.lambda;


import java.util.ArrayList;
import java.util.List;


public class LambdaTestService {

  private static List<String> called = new ArrayList<>();


  public static String[] getCalled() {

    String[] result = called.toArray(new String[called.size()]);
    called.clear();
    return result;
  }


  public static void get1() {

    called.add("getVoid");
  }


  public static void put1() {

    called.add("putVoid");
  }


  public static void post1() {

    called.add("postVoid");
  }


  public static void delete1() {

    called.add("deleteVoid");
  }


  public static void all1() {

    called.add("allVoid");
  }


  public static String get2() {

    called.add("getString");
    return "get2";
  }


  public static String put2() {

    called.add("putString");
    return "put2";
  }


  public static String post2() {

    called.add("postString");
    return "post2";
  }


  public static String delete2() {

    called.add("deleteString");
    return "delete2";
  }


  public static String all2() {

    called.add("allString");
    return "all2";
  }


  public static void get3(String id) {

    called.add("getVoid," + id);
  }


  public static void put3(String id) {

    called.add("putVoid," + id);
  }


  public static void post3(String id) {

    called.add("postVoid," + id);
  }


  public static void delete3(String id) {

    called.add("deleteVoid," + id);
  }


  public static void all3(String id) {

    called.add("allVoid," + id);
  }
}
