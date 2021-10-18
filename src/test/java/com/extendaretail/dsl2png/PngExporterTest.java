package com.extendaretail.dsl2png;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import com.extendaretail.dsl2png.PngExporter.ExportResult;
import com.extendaretail.dsl2png.vertx.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class PngExporterTest extends DslFileTestBase {

  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(new File("target")))
        .onComplete(testContext.succeedingThenComplete());
  }


  @Test
  public void successfulPngExport(TestInfo testInfo, VertxTestContext testContext)
      throws IOException {
    testContext.verify(() -> {
      File dslFile = createValidDsl(testInfo);

      PngExporter exporter = new PngExporter();
      exporter.setOutputDirectory(new File("images"));
      ExportResult export = exporter.export(dslFile);
      assertTrue(export.isSuccess());
      assertEquals(2, export.getImages().size());
      export.getImages().stream().forEach((f) -> assertTrue(f.exists(), () -> f + " should exist"));
      testContext.completeNow();
    });
  }

  @Test
  public void failingPngExport(TestInfo testInfo, VertxTestContext testContext) throws IOException {
    testContext.verify(() -> {
      File dslFile = createInvalidDsl(testInfo);
      PngExporter exporter = new PngExporter();
      exporter.setOutputDirectory(new File("images"));
      ExportResult export = exporter.export(dslFile);
      assertFalse(export.isSuccess());
      assertEquals(0, export.getImages().size());
      testContext.completeNow();
    });
  }
}
