package com.extendaretail.dsl2png;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.extendaretail.dsl2png.PngExporter.ExportResult;
import com.extendaretail.dsl2png.vertx.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class PngExporterTest extends DslFileTestBase {

  private SourceStringReader sourceStringReader;
  private PngExporter exporter;

  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) throws IOException {
    vertx
        .deployVerticle(new MainVerticle(new File("target")))
        .onComplete(testContext.succeedingThenComplete());

    sourceStringReader = mock(SourceStringReader.class);
    when(sourceStringReader.outputImage(any(OutputStream.class), any(FileFormatOption.class)))
        .thenReturn(new DiagramDescription("test"));

    exporter = new PngExporter((source, charset) -> sourceStringReader);
  }

  @Test
  public void successfulPngExport(TestInfo testInfo, VertxTestContext testContext)
      throws IOException {
    testContext.verify(
        () -> {
          File dslFile = createValidDsl(testInfo);
          exporter.setOutputDirectory(new File("images"));
          ExportResult export = exporter.export(dslFile);
          assertTrue(export.isSuccess());
          assertEquals(2, export.getImages().size());
          verify(sourceStringReader, times(2))
              .outputImage(any(OutputStream.class), any(FileFormatOption.class));
          testContext.completeNow();
        });
  }

  @Test
  public void failingPngExport(TestInfo testInfo, VertxTestContext testContext) throws IOException {
    testContext.verify(
        () -> {
          File dslFile = createInvalidDsl(testInfo);
          exporter.setOutputDirectory(new File("/tmp/exporter/images"));
          ExportResult export = exporter.export(dslFile);
          assertFalse(export.isSuccess());
          verifyNoInteractions(sourceStringReader);
          testContext.completeNow();
        });
  }
}
