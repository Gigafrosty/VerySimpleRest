package ekraft.verysimplerest.annotation;


import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;


public final class AnnotationUtils {


  public static String getPath(Method method) {

    return getPath(method.getDeclaringClass(), method);
  }


  public static String getPath(Class clazz,
                               Method method) {

    String classPath = getPath(clazz);
    if (classPath == null) {
      return null;
    }

    Path path = method.getAnnotation(Path.class);
    if (path == null) {
      return classPath;
    }

    return classPath + "/" + path.value();
  }


  public static String getPath(Object object) {

    return getPath(object.getClass());
  }


  public static <T> String getPath(Class<T> clazz) {

    Path path = clazz.getAnnotation(Path.class);
    if (path == null) {
      return null;
    }

    String partial = getPath(clazz.getSuperclass());

    if (partial == null) {
      return "/" + path.value();
    }

    if (path.value().equals("")) {
      return partial;
    }

    return partial + "/" + path.value();
  }


  public static boolean isRestMethod(Method method) {

    return hasAnnotation(method, Path.class, GET.class, PUT.class, POST.class, DELETE.class);
  }


  public static Class getReturnType(Method method) {

    Class returnType = method.getReturnType();
    if (returnType != void.class) {
      return returnType;
    }

    Parameter[] parameters = method.getParameters();
    if (!isAsynchronous(parameters)) {
      return returnType;
    }

    return getGenericType(parameters[parameters.length - 1]);
  }


  public static Class getGenericType(Parameter parameter) {

    Type type = parameter.getParameterizedType();
    if (!(type instanceof ParameterizedType)) {
      return Object.class;
    }

    ParameterizedType parameterizedType = (ParameterizedType) type;
    if (parameterizedType.getActualTypeArguments().length == 0) {
      return Object.class;
    }

    Type genericType = parameterizedType.getActualTypeArguments()[0];
    if (!(genericType instanceof Class)) {
      return Object.class;
    }

    return (Class) genericType;
  }


  public static boolean hasAnnotation(Method method,
                                      Class... classes) {

    for (Class clazz : classes) {
      if (hasAnnotation(method, clazz)) {
        return true;
      }
    }
    return false;
  }


  public static <T extends Annotation> boolean hasAnnotation(Method method,
                                                             Class<T> clazz) {

    return method.getAnnotation(clazz) != null;
  }


  public static boolean isAsynchronous(Method method) {

    return isAsynchronous(method.getParameters());
  }


  public static boolean isAsynchronous(Parameter[] parameters) {

    return parameters.length != 0 &&
      !containsAsynchronousParameter(parameters, 0, parameters.length - 1) &&
      isAsynchronousParameter(parameters[parameters.length - 1]);

  }


  public static boolean containsAsynchronousParameter(Parameter[] parameters,
                                                      int start,
                                                      int end) {

    for (int i = start; i < end; i++) {
      if (isAsynchronousParameter(parameters[i])) {
        return true;
      }
    }
    return false;
  }


  public static boolean isAsynchronousParameter(Parameter parameter) {

    return (parameter.getType().isAssignableFrom(Consumer.class));
  }
}
