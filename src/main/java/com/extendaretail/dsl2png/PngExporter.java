package com.extendaretail.dsl2png;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.io.Diagram;
import com.structurizr.io.plantuml.AbstractPlantUMLExporter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Export a Structurizr DSL to PNG images. The exporter uses C4PlantUML to create PNG images.
 *
 * @author sasjo
 */
public class PngExporter {

  private Logger log = LoggerFactory.getLogger(PngExporter.class);
  private File outputDirectory;
  private SourceStringReaderFactory plantUmlFactory;

  public PngExporter() {
    this(SourceStringReader::new);
  }

  public PngExporter(SourceStringReaderFactory plantUmlFactory) {
    this.plantUmlFactory = plantUmlFactory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  private File getOutputDirectory(File dslFile) {
    if (!outputDirectory.isAbsolute()) {
      return new File(dslFile.getParentFile(), outputDirectory.getPath());
    }
    return outputDirectory;
  }

  public ExportResult export(File dslFile) {
    try (DslFileMDC c = new DslFileMDC(dslFile)) {
      long t0 = System.currentTimeMillis();
      Workspace workspace;
      try {
        workspace = WorkspaceReader.loadFromDsl(dslFile);
      } catch (StructurizrDslParserException | IOException e) {
        log.error("Invalid DSL: {}", e.getMessage(), e);
        return new ExportResult(false, Collections.emptyList());
      }

      log.debug("Loaded {}", durationMillis(t0));

      File imageOutputDir = getOutputDirectory(dslFile);
      imageOutputDir.mkdirs();
      log.debug("Export PNGs to {}", imageOutputDir);
      AbstractPlantUMLExporter exporter = new C4PlantUMLDynamicLegendExporter();
      ExportResult result =
          exporter.export(workspace).parallelStream()
              .map((diagram) -> writePngImage(diagram, dslFile, imageOutputDir))
              .reduce(new ExportResult(true), ExportResult::merge);

      log.info("Exported {} images {}", result.getImages().size(), durationMillis(t0));
      return result;
    }
  }

  private ExportResult writePngImage(Diagram diagram, File dslFile, File imageOutputDir) {
    try (DslFileMDC c = new DslFileMDC(dslFile)) {
      long t0 = System.currentTimeMillis();
      File pngFile =
          new File(imageOutputDir, String.format("structurizr-%s.png", diagram.getKey()));
      try (OutputStream os = new FileOutputStream(pngFile)) {
        SourceStringReader reader =
            plantUmlFactory.newInstance(diagram.getDefinition(), StandardCharsets.UTF_8);
        reader.outputImage(os, new FileFormatOption(FileFormat.PNG));
      } catch (IOException e) {
        log.error("Failed to write {}", pngFile, e);
        return new ExportResult(false, pngFile);
      }
      log.info("{} {}", pngFile, durationMillis(t0));
      return new ExportResult(true, pngFile);
    }
  }

  private static String durationMillis(long t0) {
    return System.currentTimeMillis() - t0 + "ms";
  }

  @FunctionalInterface
  public static interface SourceStringReaderFactory {
    SourceStringReader newInstance(String definition, Charset charset);
  }

  public static class ExportResult {
    private List<File> images;
    private boolean success;

    private ExportResult() {}

    public ExportResult(boolean success, File... images) {
      this(success, Arrays.asList(images));
    }

    public ExportResult(boolean success, List<File> images) {
      this.success = success;
      this.images = new ArrayList<>(images);
    }

    /**
     * Merge two results. The method returns a new result. If one of the results is a failure, the
     * merged result will always be a failure.
     *
     * @param a the first result
     * @param b the second result
     * @return a new result which is the combination of the two.
     */
    public static ExportResult merge(ExportResult a, ExportResult b) {
      ExportResult result = new ExportResult();
      result.success = a.success && b.success;
      result.images = new ArrayList<>();
      result.images.addAll(a.images);
      result.images.addAll(b.images);
      return result;
    }

    public boolean isSuccess() {
      return success;
    }

    public List<File> getImages() {
      return images;
    }
  }
}
