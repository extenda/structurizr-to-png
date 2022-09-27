package com.extendaretail.dsl2png;

import static java.util.Arrays.asList;

import com.structurizr.export.AbstractDiagramExporter;
import com.structurizr.export.Diagram;
import com.structurizr.export.dot.DOTExporter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Render a diagram with Graphviz. The Structurizr DOT exporter is used as DSL exporter.
 *
 * @author sasjo
 */
public class GraphvizDiagramRenderer implements DiagramRenderer {

  private static final int DEFAULT_DPI = 120;

  private final ProcessFactory processFactory;

  public GraphvizDiagramRenderer() {
    this(commands -> new ProcessBuilder().command(commands).start());
  }

  public GraphvizDiagramRenderer(ProcessFactory processFactory) {
    this.processFactory = processFactory;
  }

  @Override
  public AbstractDiagramExporter createDiagramExporter() {
    return new DOTExporter();
  }

  @Override
  public void renderDiagram(Diagram diagram, File outputFile) throws IOException {
    int dpi = Integer.getInteger("graphviz.dpi", DEFAULT_DPI);
    Path dotFile =
        outputFile.toPath().getParent().resolve(outputFile.toPath().getFileName() + ".dot");
    Files.writeString(dotFile, diagram.getDefinition());
    try {
      int exitCode =
          processFactory
              .startProcess(
                  asList(
                      "dot",
                      "-Tpng",
                      dotFile.toAbsolutePath().toString(),
                      "-Gdpi=" + dpi,
                      // "-Gsplines=ortho",
                      "-o",
                      outputFile.getCanonicalPath()))
              .waitFor();
      if (exitCode != 0) {
        throw new IOException("Failed to generate " + outputFile + " with exit code " + exitCode);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while waiting for dot.", e);
    } finally {
      Files.delete(dotFile);
    }
  }

  /** Factory to create a {@link Process}. */
  @FunctionalInterface
  public interface ProcessFactory {
    /**
     * Start a new process with the given command list.
     *
     * @param commands the commands to pass to the process
     * @return the started process
     * @throws IOException if failing to start the process.
     */
    Process startProcess(List<String> commands) throws IOException;
  }
}
