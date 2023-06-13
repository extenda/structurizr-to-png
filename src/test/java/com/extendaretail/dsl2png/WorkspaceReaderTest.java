package com.extendaretail.dsl2png;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.extendaretail.dsl2png.vertx.MainVerticle;
import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.model.Location;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class WorkspaceReaderTest extends DslFileTestBase {

  private Integer httpPort;

  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) {
    DeploymentOptions config = LocalPort.getLocalPort();
    httpPort = config.getConfig().getInteger("http.port");
    vertx
        .deployVerticle(new MainVerticle(new File("target")), config)
        .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  void dslWithValidSyntax(TestInfo testInfo, Vertx vertx, VertxTestContext testContext) {
    testContext.verify(
        () -> {
          Workspace workspace = new WorkspaceReader(httpPort).loadFromDsl(createValidDsl(testInfo));

          assertEquals(2, workspace.getModel().getSoftwareSystems().size());
          assertEquals(4, workspace.getViews().getViews().size());

          // Location should've been decorated.
          assertEquals(
              Location.External,
              workspace.getModel().getSoftwareSystemWithName("Existing System").getLocation());

          testContext.completeNow();
        });
  }

  @Test
  void dslWithSyntaxError(TestInfo testInfo, VertxTestContext testContext) throws IOException {
    testContext.verify(
        () -> {
          StructurizrDslParserException e =
              assertThrows(
                  StructurizrDslParserException.class,
                  () -> new WorkspaceReader(httpPort).loadFromDsl(createInvalidDsl(testInfo)));
          assertThat(e)
              .hasMessageContaining("Unexpected tokens")
              .hasMessageContaining("at line 3: user = personX \"User\" \"A user\"");
          testContext.completeNow();
        });
  }
}
