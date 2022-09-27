package com.extendaretail.dsl2png;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.structurizr.io.AbstractDiagramExporter;
import com.structurizr.io.Diagram;
import com.structurizr.io.dot.DOTExporter;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphvizDiagramRendererTest {

  private GraphvizDiagramRenderer.ProcessFactory processFactory;
  private GraphvizDiagramRenderer diagramRenderer;

  @BeforeEach
  public void setUp() {
    processFactory = mock(GraphvizDiagramRenderer.ProcessFactory.class);
    diagramRenderer = new GraphvizDiagramRenderer(processFactory);
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
    AbstractDiagramExporter exporter = assertDoesNotThrow(diagramRenderer::createDiagramExporter);
    assertInstanceOf(DOTExporter.class, exporter);
  }

  @Test
  void renderDiagramSuccessful() throws IOException, InterruptedException {
    Process process = mock(Process.class);
    when(processFactory.startProcess(anyList())).thenReturn(process);
    when(process.waitFor()).thenReturn(0);

    File outputFile = new File("target", "out.png");
    Diagram diagram = mock(Diagram.class);
    when(diagram.getDefinition()).thenReturn("test");
    assertDoesNotThrow(() -> diagramRenderer.renderDiagram(diagram, outputFile));
    verify(processFactory)
        .startProcess(
            asList(
                "dot",
                "-Tpng",
                outputFile.getCanonicalPath() + ".dot",
                "-Gdpi=120",
                "-o",
                outputFile.getCanonicalPath()));
    verify(diagram).getDefinition();
  }

  @Test
  void renderDiagramCanThrowException() throws IOException, InterruptedException {
    Process process = mock(Process.class);
    when(processFactory.startProcess(anyList())).thenReturn(process);
    when(process.waitFor()).thenReturn(1);

    File outputFile = new File("target", "out.png");
    Diagram diagram = mock(Diagram.class);
    when(diagram.getDefinition()).thenReturn("test");
    assertThrows(IOException.class, () -> diagramRenderer.renderDiagram(diagram, outputFile));
    verify(diagram).getDefinition();
  }
}
