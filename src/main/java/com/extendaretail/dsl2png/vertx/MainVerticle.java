package com.extendaretail.dsl2png.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  private static Logger log = LoggerFactory.getLogger(MainVerticle.class);

  private List<File> files = new CopyOnWriteArrayList<>();
  private File outputDirectory;

  private Integer listenPort = 3000;

  public MainVerticle(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public List<File> listImages() {
    Stream<File> stream;
    if (outputDirectory.isAbsolute()) {
      stream = Stream.of(outputDirectory);
    } else {
      stream = files.stream().map((f) -> new File(f.getParentFile(), outputDirectory.getPath()));
    }

    return stream
        .flatMap(
            (f) ->
                Stream.of(
                    Optional.ofNullable(f.listFiles((dir, name) -> name.endsWith(".png")))
                        .orElse(new File[0])))
        .collect(Collectors.toList());
  }

  /**
   * Set the preview files.
   *
   * @param files the DSL files to preview
   */
  public void previewFiles(List<File> files) {
    if (this.files.isEmpty()) {
      log.info("Access URL\n\n\thttp://localhost:" + listenPort);
      this.files.addAll(files);
    }
  }

  /**
   * Returns the list of files known to the previewer.
   *
   * @return the list of known files.
   */
  public List<File> getFiles() {
    return Collections.unmodifiableList(files);
  }

  /**
   * Create an image event.
   *
   * @param type the image event type
   * @param images the list of images to include
   * @return a JSON preview event
   */
  public static JsonObject imagesEvent(String type, List<File> images) {
    JsonObject json = new JsonObject();

    Map<String, String> webImages =
        images.stream()
            .sorted()
            .collect(
                Collectors.toMap(
                    File::getName,
                    f -> "/images/" + f.getName() + "?" + System.currentTimeMillis()));

    json.put("type", type);
    json.put("images", webImages);

    return json;
  }

  private Optional<File> findImage(String imagePath) {
    String name = Path.of(imagePath).getFileName().toString();
    return listImages().stream().filter((f) -> f.getName().equals(name)).findFirst();
  }
  ;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    SockJSHandler sockJSHandler =
        SockJSHandler.create(
            vertx,
            new SockJSHandlerOptions().setHeartbeatInterval(2000).setRegisterWriteHandler(true));

    Router router = Router.router(vertx);

    PermittedOptions previewEvent = new PermittedOptions().setAddressRegex("preview\\..+");

    router.mountSubRouter(
        "/eventbus",
        sockJSHandler.bridge(
            new SockJSBridgeOptions().addOutboundPermitted(previewEvent),
            be -> {
              if (be.type() == BridgeEventType.REGISTERED
                  && "preview.init".equals(be.getRawMessage().getString("address"))) {
                be.socket()
                    .write(
                        new JsonObject()
                            .put("type", "publish")
                            .put("address", "preview.init")
                            .put("body", imagesEvent("init", listImages()))
                            .toBuffer());
              }
              be.complete(true);
            }));

    router.route("/*").handler(StaticHandler.create("preview"));
    router
        .route("/images/*")
        .handler(
            (ctx) ->
                findImage(ctx.request().path())
                    .map(File::getPath)
                    .ifPresentOrElse(
                        ctx.response()::sendFile, () -> ctx.response().setStatusCode(404).end()));

    router.route("/themes/*").handler(StaticHandler.create("themes"));

    listenPort = config().getInteger("http.port", 3000);
    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(listenPort)
        .<Void>mapEmpty()
        .onComplete(startPromise);
  }
}
