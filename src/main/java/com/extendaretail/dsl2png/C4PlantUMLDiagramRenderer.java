package com.extendaretail.dsl2png;

import com.structurizr.export.AbstractDiagramExporter;

/**
 * Render a diagram with PlantUML. The C4 PlantUML exporter is used as DSL exporter.
 *
 * @author sasjo
 */
public class C4PlantUMLDiagramRenderer extends AbstractPlantUMLDiagramRenderer {

  @Override
  public AbstractDiagramExporter createDiagramExporter() {
    return new C4PlantUMLDynamicLegendExporter();
  }
}
