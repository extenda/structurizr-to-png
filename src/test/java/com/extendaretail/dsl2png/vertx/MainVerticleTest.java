package com.extendaretail.dsl2png.vertx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.extendaretail.dsl2png.DslFileTestBase;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class MainVerticleTest extends DslFileTestBase {

  private MainVerticle mainVerticle;

  @BeforeEach
  public void beforeEach(TestInfo testInfo) throws IOException {
    File dsl = createValidDsl(testInfo);
    File outputDirectory = new File("./images").getCanonicalFile();
    mainVerticle = new MainVerticle(outputDirectory);
    mainVerticle.previewFiles(Arrays.asList(dsl));
  }

  @Test
  public void getFiles() {
    assertEquals(1, mainVerticle.getFiles().size());
  }

  @Test
  public void listImages(VertxTestContext testContext) {
    testContext.verify(
        () -> {
          MainVerticle relativeVerticle = new MainVerticle(new File("images"));
          relativeVerticle.previewFiles(Arrays.asList(new File("demo.dsl")));
          List<File> images = relativeVerticle.listImages();
          images.sort(File::compareTo);
          assertEquals(
              Arrays.asList(
                  new File("images/structurizr-PriceTracker-Container.png"),
                  new File("images/structurizr-PriceTracker-SystemContext.png")),
              images);
          testContext.completeNow();
        });
  }

  @Test
  public void serveIndexHtml(Vertx vertx, VertxTestContext testContext) {
    HttpClient client = vertx.createHttpClient();
    vertx
        .deployVerticle(mainVerticle)
        .compose(ignore -> client.request(HttpMethod.GET, 3000, "localhost", "/index.html"))
        .compose(HttpClientRequest::send)
        .compose(HttpClientResponse::body)
        .onSuccess(
            body -> {
              assertTrue(body.toString().contains("<title>structurizr-to-png"));
            })
        .onComplete(testContext.succeedingThenComplete())
        .onFailure(testContext::failNow);
  }

  @Test
  public void serveImageFile(Vertx vertx, VertxTestContext testContext) {
    HttpClient client = vertx.createHttpClient();
    vertx
        .deployVerticle(mainVerticle)
        .compose(
            ignore ->
                client.request(
                    HttpMethod.GET,
                    3000,
                    "localhost",
                    "/images/structurizr-PriceTracker-SystemContext.png"))
        .compose(HttpClientRequest::send)
        .onSuccess(
            response -> {
              testContext.verify(
                  () -> {
                    assertEquals(200, response.statusCode());
                    assertEquals("image/png", response.getHeader("Content-Type"));
                  });
            })
        .onComplete(testContext.succeedingThenComplete())
        .onFailure(testContext::failNow);
  }

  @Test
  public void serveMissingImage(Vertx vertx, VertxTestContext testContext) {
    HttpClient client = vertx.createHttpClient();
    vertx
        .deployVerticle(mainVerticle)
        .compose(
            ignore ->
                client.request(
                    HttpMethod.GET,
                    3000,
                    "localhost",
                    "/images/structurizr-Missing-SystemContext.png"))
        .compose(HttpClientRequest::send)
        .onSuccess(
            response -> {
              assertEquals(404, response.statusCode());
            })
        .onComplete(testContext.succeedingThenComplete())
        .onFailure(testContext::failNow);
  }

  @Test
  public void serveInitEvent(Vertx vertx, VertxTestContext testContext) {
    HttpClient client = vertx.createHttpClient();
    Buffer payload =
        new JsonObject(
                """
        {
          "type":"register",
          "address":"preview.init",
          "headers": {}
        }
        """)
            .toBuffer();
    vertx
        .deployVerticle(mainVerticle)
        .compose(ignore -> client.webSocket(3000, "localhost", "/eventbus/websocket"))
        .onSuccess(
            ws -> {
              ws.handler(
                  data -> {
                    testContext.verify(
                        () -> {
                          JsonObject event = data.toJsonObject();
                          assertEquals("preview.init", event.getString("address"));
                        });
                  });
              ws.write(payload);
            })
        .onComplete(testContext.succeedingThenComplete())
        .onFailure(testContext::failNow);
  }
}
