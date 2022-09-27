package com.extendaretail.dsl2png;

import com.structurizr.export.AbstractDiagramExporter;
import com.structurizr.export.Diagram;
import java.io.File;
import java.io.IOException;

/**
 * Render a Structurizr diagram as an image.
 *
 * @author sasjo
 */
public interface DiagramRenderer {

  /**
   * Create the output file name for a diagram.
   *
   * @param diagram diagram to render
   * @param parentDirectory the parent directory
   * @return the diagram image output filename.
   */
  default File getOutputFileName(Diagram diagram, File parentDirectory) {
    return new File(
        parentDirectory, String.format("structurizr-%s.%s", diagram.getKey(), getImageExtension()));
  }

  /**
   * Returns the image file extension, for example <code>png</code>.
   *
   * @return the image file extension.
   */
  default String getImageExtension() {
    return "png";
  }

  /**
   * Create the diagram exporter to use to produce diagrams that are compatible with this renderer.
   *
   * @return the diagram exporter to use.
   */
  AbstractDiagramExporter createDiagramExporter();

  /**
   * Render a Structurizr diagram as an image.
   *
   * @param diagram diagram to export
   * @param outputFile the output image file
   * @throws IOException if failing to render the diagram into an image.
   */
  void renderDiagram(Diagram diagram, File outputFile) throws IOException;
}
