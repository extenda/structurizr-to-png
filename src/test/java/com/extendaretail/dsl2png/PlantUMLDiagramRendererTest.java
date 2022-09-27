package com.extendaretail.dsl2png;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.structurizr.io.Diagram;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlantUMLDiagramRendererTest {

  private SourceStringReader sourceStringReader;
  private PlantUMLDiagramRenderer diagramRenderer;

  @BeforeEach
  public void setUp() {
    sourceStringReader = mock(SourceStringReader.class);
    diagramRenderer = new PlantUMLDiagramRenderer((source, charset) -> sourceStringReader);
  }

  @Test
  void getOutputFileName() {
    Diagram diagram = mock(Diagram.class);
    when(diagram.getKey()).thenReturn("diagram-name");
    File result = diagramRenderer.getOutputFileName(diagram, new File("target"));
    assertEquals(result, new File("target", "structurizr-diagram-name.png"));
  }

  @Test
  void createDiagramExporter() {
    assertDoesNotThrow(diagramRenderer::createDiagramExporter);
  }

  @Test
  void renderDiagramSuccessful() throws IOException {
    when(sourceStringReader.outputImage(any(OutputStream.class), any(FileFormatOption.class)))
        .thenReturn(new DiagramDescription("test"));

    File outputFile = new File("out.png");
    Diagram diagram = mock(Diagram.class);
    assertDoesNotThrow(() -> diagramRenderer.renderDiagram(diagram, outputFile));
    verify(sourceStringReader).outputImage(any(OutputStream.class), any(FileFormatOption.class));
    verify(diagram).getDefinition();
  }

  @Test
  void renderDiagramCanThrowException() throws IOException {
    when(sourceStringReader.outputImage(any(OutputStream.class), any(FileFormatOption.class)))
        .thenThrow(new IOException("Test"));

    File outputFile = new File("out.png");
    Diagram diagram = mock(Diagram.class);
    assertThrows(IOException.class, () -> diagramRenderer.renderDiagram(diagram, outputFile));
  }
}