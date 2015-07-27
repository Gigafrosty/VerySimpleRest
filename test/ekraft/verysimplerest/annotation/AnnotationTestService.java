package ekraft.verysimplerest.annotation;


import ekraft.verysimplerest.examples.ExampleRestServiceBase;
import ekraft.verysimplerest.utils.RestException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


@Path("")
public class AnnotationTestService
  extends ExampleRestServiceBase {

  private List<String> called = new ArrayList<>();


  public String[] getCalled() {

    String[] result = called.toArray(new String[called.size()]);
    called.clear();
    return result;
  }


  @GET
  @Path("get1")
  public void get1() {

    called.add("getVoid");
  }


  @PUT
  @Path("put1")
  public void put1() {

    called.add("putVoid");
  }


  @POST
  @Path("post1")
  public void post1() {

    called.add("postVoid");
  }


  @DELETE
  @Path("delete1")
  public void delete1() {

    called.add("deleteVoid");
  }


  @GET
  @PUT
  @POST
  @DELETE
  @Path("all1")
  public void all1() {

    called.add("allVoid");
  }


  @GET
  @Path("get2")
  public String get2() {

    called.add("getString");
    return "get2";
  }


  @PUT
  @Path("put2")
  public String put2() {

    called.add("putString");
    return "put2";
  }


  @POST
  @Path("post2")
  public String post2() {

    called.add("postString");
    return "post2";
  }


  @DELETE
  @Path("delete2")
  public String delete2() {

    called.add("deleteString");
    return "delete2";
  }


  @GET
  @PUT
  @POST
  @DELETE
  @Path("all2")
  public String all2() {

    called.add("allString");
    return "all2";
  }


  // GET cannot pass parameter via REQUEST body, must be via URL
  @GET
  @Path("get3/*")
  public void get3(String id) {

    called.add("getVoid," + id);
  }


  @PUT
  @Path("put3")
  public void put3(String id) {

    called.add("putVoid," + id);
  }


  @POST
  @Path("post3")
  public void post3(String id) {

    called.add("postVoid," + id);
  }


  @DELETE
  @Path("delete3")
  public void delete3(String id) {

    called.add("deleteVoid," + id);
  }


  @GET
  @PUT
  @POST
  @DELETE
  @Path("all3/*")
  public void all3(String id) {

    called.add("allVoid," + id);
  }


  @GET
  @Path("get4")
  public void get4(Consumer<Boolean> callback) {

    run(callback, () -> {
      called.add("getCallback");
    });
  }


  @PUT
  @Path("put4")
  public void put4(Consumer<Boolean> callback) {

    run(callback, () -> {
      called.add("putCallback");
    });
  }


  @POST
  @Path("post4")
  public void post4(Consumer<Boolean> callback) {

    run(callback, () -> {
      called.add("postCallback");
    });
  }


  @DELETE
  @Path("delete4")
  public void delete4(Consumer<Boolean> callback) {

    run(callback, () -> {
      called.add("deleteCallback");
    });
  }


  @GET
  @PUT
  @POST
  @DELETE
  @Path("all4")
  public void all4(Consumer<Boolean> callback) {

    run(callback, () -> {
      called.add("allCallback");
    });
  }


  @GET
  @Path("get5/{id}")
  public void get5(String id, Consumer<Boolean> callback) {

    run(callback, () -> {
      called.add("getCallback," + id);
    });
  }


  @PUT
  @Path("put5")
  public void put5(String id, Consumer<Boolean> callback) {

    run(callback, () -> {
      called.add("putCallback," + id);
    });
  }


  @POST
  @Path("post5")
  public void post5(String id, Consumer<Boolean> callback) {

    run(callback, () -> {
      called.add("postCallback," + id);
    });
  }


  @DELETE
  @Path("delete5")
  public void delete5(String id, Consumer<Boolean> callback) {

    run(callback, () -> {
      called.add("deleteCallback," + id);
    });
  }


  @GET
  @PUT
  @POST
  @DELETE
  @Path("all5/{id}")
  public void all5(String id, Consumer<Boolean> callback) {

    run(callback, () -> {
      called.add("allCallback," + id);
    });
  }


  @GET
  @Path("timeout")
  public void timeout(Consumer<Boolean> callback) {

    try {
      Thread.sleep(6000);
    } catch (InterruptedException e) {
      RestException.log(e);
    }

    try {
      callback.accept(true);
    } catch (Exception e) {
      RestException.log(e);
    }
  }


  public static final Method GET1 = getMethod(AnnotationTestService.class, "get1");
  public static final Method PUT1 = getMethod(AnnotationTestService.class, "put1");
  public static final Method POST1 = getMethod(AnnotationTestService.class, "post1");
  public static final Method DELETE1 = getMethod(AnnotationTestService.class, "delete1");
  public static final Method ALL1 = getMethod(AnnotationTestService.class, "all1");

  public static final Method GET2 = getMethod(AnnotationTestService.class, "get2");
  public static final Method PUT2 = getMethod(AnnotationTestService.class, "put2");
  public static final Method POST2 = getMethod(AnnotationTestService.class, "post2");
  public static final Method DELETE2 = getMethod(AnnotationTestService.class, "delete2");
  public static final Method ALL2 = getMethod(AnnotationTestService.class, "all2");

  public static final Method GET3 = getMethod(AnnotationTestService.class, "get3", String.class);
  public static final Method PUT3 = getMethod(AnnotationTestService.class, "put3", String.class);
  public static final Method POST3 = getMethod(AnnotationTestService.class, "post3", String.class);
  public static final Method DELETE3 = getMethod(AnnotationTestService.class, "delete3", String.class);
  public static final Method ALL3 = getMethod(AnnotationTestService.class, "all3", String.class);

  public static final Method GET4 = getMethod(AnnotationTestService.class, "get4", Consumer.class);
  public static final Method PUT4 = getMethod(AnnotationTestService.class, "put4", Consumer.class);
  public static final Method POST4 = getMethod(AnnotationTestService.class, "post4", Consumer.class);
  public static final Method DELETE4 = getMethod(AnnotationTestService.class, "delete4", Consumer.class);
  public static final Method ALL4 = getMethod(AnnotationTestService.class, "all4", Consumer.class);

  public static final Method GET5 = getMethod(AnnotationTestService.class, "get5", String.class, Consumer.class);
  public static final Method PUT5 = getMethod(AnnotationTestService.class, "put5", String.class, Consumer.class);
  public static final Method POST5 = getMethod(AnnotationTestService.class, "post5", String.class, Consumer.class);
  public static final Method DELETE5 = getMethod(AnnotationTestService.class, "delete5", String.class, Consumer.class);
  public static final Method ALL5 = getMethod(AnnotationTestService.class, "all5", String.class, Consumer.class);
}
