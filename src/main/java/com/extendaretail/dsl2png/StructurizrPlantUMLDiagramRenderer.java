package com.extendaretail.dsl2png;

import com.structurizr.export.AbstractDiagramExporter;
import com.structurizr.export.plantuml.StructurizrPlantUMLExporter;

/**
 * Diagram renderer based on the Structurizr PlantUML DSL export.
 *
 * @author sasjo
 */
public class StructurizrPlantUMLDiagramRenderer extends AbstractPlantUMLDiagramRenderer {

  @Override
  public AbstractDiagramExporter createDiagramExporter() {
    return new StructurizrPlantUMLExporter();
  }
}
