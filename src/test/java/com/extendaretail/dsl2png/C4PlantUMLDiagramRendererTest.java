package com.extendaretail.dsl2png;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.structurizr.export.AbstractExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class C4PlantUMLDiagramRendererTest {

  private C4PlantUMLDiagramRenderer diagramRenderer;

  @BeforeEach
  public void setUp() {
    diagramRenderer = new C4PlantUMLDiagramRenderer();
  }

  @Test
  void createDiagramExporter() {
    AbstractExporter exporter = assertDoesNotThrow(diagramRenderer::createDiagramExporter);
    assertInstanceOf(C4PlantUMLDynamicLegendExporter.class, exporter);
  }
}
