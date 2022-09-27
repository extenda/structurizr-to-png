package com.extendaretail.dsl2png;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.extendaretail.dsl2png.PngExporter.ExportResult;
import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.export.AbstractDiagramExporter;
import com.structurizr.export.Diagram;
import com.structurizr.model.SoftwareSystem;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

class PngExporterTest extends DslFileTestBase {

  private PngExporter exporter;
  private WorkspaceReader workspaceReader;
  private DiagramRenderer diagramRenderer;

  @BeforeEach
  public void setUp() {
    diagramRenderer = spy(new MockDiagramRenderer());
    when(diagramRenderer.createDiagramExporter()).thenReturn(new C4PlantUMLDynamicLegendExporter());
    workspaceReader = mock(WorkspaceReader.class);
    exporter = new PngExporter(workspaceReader, diagramRenderer);
  }

  @Test
  void successfulPngExport(TestInfo testInfo) throws IOException, StructurizrDslParserException {
    File dslFile = new File("test.dsl");

    Workspace workspace = new Workspace("test", "Test");
    SoftwareSystem testSystem = workspace.getModel().addSoftwareSystem("Test System");
    testSystem.addContainer("Database");
    workspace.getViews().createSystemContextView(testSystem, "TestSystem-SystemContext", null);
    workspace.getViews().createContainerView(testSystem, "TestSystem-Container", null);

    when(workspaceReader.loadFromDsl(dslFile)).thenReturn(workspace);

    exporter.setOutputDirectory(new File(testDir(testInfo), "images"));
    ExportResult export = exporter.export(dslFile);
    assertTrue(export.isSuccess());
    assertEquals(2, export.getImages().size());
    verify(diagramRenderer, times(2)).renderDiagram(any(Diagram.class), any(File.class));
  }

  @Test
  void failingPngExportWithDslParseError() throws IOException, StructurizrDslParserException {
    File dslFile = new File("test.dsl");
    when(workspaceReader.loadFromDsl(dslFile)).thenThrow(new IOException("Test parse error"));
    exporter.setOutputDirectory(new File("/tmp/exporter/images"));
    ExportResult export = exporter.export(dslFile);
    assertFalse(export.isSuccess());
    verifyNoInteractions(diagramRenderer);
  }

  public static class MockDiagramRenderer implements DiagramRenderer {

    @Override
    public AbstractDiagramExporter createDiagramExporter() {
      return new C4PlantUMLDynamicLegendExporter();
    }

    @Override
    public void renderDiagram(Diagram diagram, File outputFile) throws IOException {
      // Do nothing.
    }
  }
}
