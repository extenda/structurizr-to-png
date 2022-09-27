package com.extendaretail.dsl2png;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.export.AbstractDiagramExporter;
import com.structurizr.export.Diagram;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Export a Structurizr DSL to PNG images. The exporter uses C4PlantUML to create PNG images.
 *
 * @author sasjo
 */
public class PngExporter {

  private static final Logger log = LoggerFactory.getLogger(PngExporter.class);
  private File outputDirectory;
  private final WorkspaceReader workspaceReader;
  private DiagramRenderer diagramRenderer;

  public PngExporter(int themesPort, DiagramRenderer diagramRenderer) {
    this(new WorkspaceReader(themesPort), diagramRenderer);
  }

  public PngExporter(WorkspaceReader workspaceReader, DiagramRenderer diagramRenderer) {
    this.workspaceReader = workspaceReader;
    this.diagramRenderer = diagramRenderer;
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
      Workspace workspace;
      try {
        workspace = workspaceReader.loadFromDsl(dslFile);
      } catch (StructurizrDslParserException | IOException e) {
        log.error("Invalid DSL: {}", e.getMessage(), e);
        return new ExportResult(false, Collections.emptyList());
      }

      File imageOutputDir = getOutputDirectory(dslFile);
      imageOutputDir.mkdirs();
      log.debug("Export PNGs to {}", imageOutputDir);
      AbstractDiagramExporter exporter = diagramRenderer.createDiagramExporter();
      ExportResult result =
          exporter.export(workspace).parallelStream()
              .map(diagram -> writeImage(diagram, dslFile, imageOutputDir))
              .reduce(new ExportResult(true), ExportResult::merge);

      log.info("Exported {} images", result.getImages().size());
      return result;
    }
  }

  private ExportResult writeImage(Diagram diagram, File dslFile, File imageOutputDir) {
    try (DslFileMDC c = new DslFileMDC(dslFile)) {
      long t0 = System.currentTimeMillis();
      File outputFile = diagramRenderer.getOutputFileName(diagram, imageOutputDir);
      try {
        diagramRenderer.renderDiagram(diagram, outputFile);
      } catch (IOException e) {
        log.error("Failed to write {}", outputFile, e);
        return new ExportResult(false, outputFile);
      }
      if (log.isInfoEnabled()) {
        log.info("{} {}", outputFile, durationMillis(t0));
      }
      return new ExportResult(true, outputFile);
    }
  }

  private static String durationMillis(long t0) {
    return System.currentTimeMillis() - t0 + "ms";
  }

  public void setDiagramRenderer(DiagramRenderer diagramRenderer) {
    this.diagramRenderer = diagramRenderer;
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
