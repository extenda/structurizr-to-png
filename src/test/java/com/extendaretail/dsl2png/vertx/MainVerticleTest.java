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
import java.nio.file.Files;
import java.util.Arrays;
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
    File outputDirectory = new File("images");
    File image = new File(dsl.getParentFile(), "images/structurizr-Vertx-SystemContext.png");
    Files.createDirectories(image.toPath().getParent());
    Files.writeString(image.toPath(), "test image");
    mainVerticle = new MainVerticle(outputDirectory);
    mainVerticle.previewFiles(Arrays.asList(dsl));
  }

  @Test
  public void getFiles() {
    assertEquals(1, mainVerticle.getFiles().size());
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
                    "/images/structurizr-Vertx-SystemContext.png"))
        .compose(HttpClientRequest::send)
        .compose(HttpClientResponse::body)
        .onSuccess(
            body -> {
              testContext.verify(
                  () -> {
                    assertEquals("test image", body.toString());
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
