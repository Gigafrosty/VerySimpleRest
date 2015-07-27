package ekraft.verysimplerest.lambda;


public interface HttpRequestHandler<T> {
  void handle(HttpRequest<T> request);
}
