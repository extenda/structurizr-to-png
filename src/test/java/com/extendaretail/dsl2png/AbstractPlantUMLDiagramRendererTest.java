package com.extendaretail.dsl2png;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.structurizr.export.AbstractDiagramExporter;
import com.structurizr.export.Diagram;
import com.structurizr.view.View;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractPlantUMLDiagramRendererTest {

  private SourceStringReader sourceStringReader;
  private AbstractPlantUMLDiagramRenderer diagramRenderer;

  @BeforeEach
  public void setUp() {
    sourceStringReader = mock(SourceStringReader.class);
    diagramRenderer = new MockPlantUMLDiagramRenderer((source, charset) -> sourceStringReader);
  }

  @Test
  void getOutputFileNameFromKey() {
    Diagram diagram = mock(Diagram.class);
    View view = mock(View.class);
    when(diagram.getView()).thenReturn(view);
    when(view.getProperties()).thenReturn(emptyMap());
    when(diagram.getKey()).thenReturn("diagram-name");
    File result = diagramRenderer.getOutputFileName(diagram, new File("target"));
    assertEquals(result, new File("target", "structurizr-diagram-name.png"));
    verify(diagram).getKey();
  }

  @Test
  void getOutputFileNameFromProperties() {
    Diagram diagram = mock(Diagram.class);
    View view = mock(View.class);
    when(diagram.getView()).thenReturn(view);
    when(view.getProperties())
        .thenReturn(Map.of(DiagramRenderer.DIAGRAM_NAME_PROPERTY, "default-name"));
    File result = diagramRenderer.getOutputFileName(diagram, new File("target"));
    assertEquals(result, new File("target", "structurizr-default-name.png"));
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

  public static class MockPlantUMLDiagramRenderer extends AbstractPlantUMLDiagramRenderer {

    private final AbstractDiagramExporter exporter = mock(AbstractDiagramExporter.class);

    public MockPlantUMLDiagramRenderer(SourceStringReaderFactory plantUmlFactory) {
      super(plantUmlFactory);
    }

    @Override
    public AbstractDiagramExporter createDiagramExporter() {
      return exporter;
    }
  }
}
