package com.extendaretail.dsl2png;

import com.structurizr.io.AbstractDiagramExporter;
import com.structurizr.io.Diagram;
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
 * Render a diagram with PlantUML. The C4 structurizr exporter is used as DSL exporter.
 *
 * @author sasjo
 */
public class PlantUMLDiagramRenderer implements DiagramRenderer {

  private SourceStringReaderFactory plantUmlFactory;

  @FunctionalInterface
  public interface SourceStringReaderFactory {
    SourceStringReader newInstance(String definition, Charset charset);
  }

  public PlantUMLDiagramRenderer() {
    this(SourceStringReader::new);
  }

  public PlantUMLDiagramRenderer(SourceStringReaderFactory plantUmlFactory) {
    this.plantUmlFactory = plantUmlFactory;
  }

  @Override
  public AbstractDiagramExporter createDiagramExporter() {
    return new C4PlantUMLDynamicLegendExporter();
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
