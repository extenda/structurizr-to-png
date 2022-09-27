package com.extendaretail.dsl2png;

import static org.junit.jupiter.api.Assertions.*;

import com.structurizr.export.AbstractExporter;
import com.structurizr.export.plantuml.StructurizrPlantUMLExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StructurizrPlantUMLDiagramRendererTest {
  private StructurizrPlantUMLDiagramRenderer diagramRenderer;

  @BeforeEach
  public void setUp() {
    diagramRenderer = new StructurizrPlantUMLDiagramRenderer();
  }

  @Test
  void createDiagramExporter() {
    AbstractExporter exporter = assertDoesNotThrow(diagramRenderer::createDiagramExporter);
    assertInstanceOf(StructurizrPlantUMLExporter.class, exporter);
  }
}
