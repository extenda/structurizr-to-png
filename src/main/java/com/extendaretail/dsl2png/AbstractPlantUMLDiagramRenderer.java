package com.extendaretail.dsl2png;

import com.structurizr.export.Diagram;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

/**
 * Abstract diagram renderer for PlantUML exporters.
 *
 * @author sasjo
 */
public abstract class AbstractPlantUMLDiagramRenderer implements DiagramRenderer {

  private AbstractPlantUMLDiagramRenderer.SourceStringReaderFactory plantUmlFactory;

  @FunctionalInterface
  public interface SourceStringReaderFactory {
    SourceStringReader newInstance(String definition, Charset charset);
  }

  protected AbstractPlantUMLDiagramRenderer() {
    this(SourceStringReader::new);
  }

  protected AbstractPlantUMLDiagramRenderer(
      AbstractPlantUMLDiagramRenderer.SourceStringReaderFactory plantUmlFactory) {
    this.plantUmlFactory = plantUmlFactory;
  }

  @Override
  public void renderDiagram(Diagram diagram, File outputFile) throws IOException {
    try (OutputStream os = new FileOutputStream(outputFile)) {
      SourceStringReader reader =
          plantUmlFactory.newInstance(diagram.getDefinition(), StandardCharsets.UTF_8);
      reader.outputImage(os, new FileFormatOption(FileFormat.PNG));
    }
  }
}
