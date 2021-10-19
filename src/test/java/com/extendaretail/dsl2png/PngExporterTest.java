package com.extendaretail.dsl2png;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.extendaretail.dsl2png.PngExporter.ExportResult;
import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.model.SoftwareSystem;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

class PngExporterTest extends DslFileTestBase {

  private SourceStringReader sourceStringReader;
  private PngExporter exporter;
  private WorkspaceReader workspaceReader;

  @BeforeEach
  public void setUp() throws IOException {
    sourceStringReader = mock(SourceStringReader.class);
    when(sourceStringReader.outputImage(any(OutputStream.class), any(FileFormatOption.class)))
        .thenReturn(new DiagramDescription("test"));

    workspaceReader = mock(WorkspaceReader.class);
    exporter = new PngExporter(workspaceReader, (source, charset) -> sourceStringReader);
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
    verify(sourceStringReader, times(2))
        .outputImage(any(OutputStream.class), any(FileFormatOption.class));
  }

  @Test
  void failingPngExport() throws IOException, StructurizrDslParserException {
    File dslFile = new File("test.dsl");
    when(workspaceReader.loadFromDsl(dslFile)).thenThrow(new IOException("Test parse error"));
    exporter.setOutputDirectory(new File("/tmp/exporter/images"));
    ExportResult export = exporter.export(dslFile);
    assertFalse(export.isSuccess());
    verifyNoInteractions(sourceStringReader);
  }
}
