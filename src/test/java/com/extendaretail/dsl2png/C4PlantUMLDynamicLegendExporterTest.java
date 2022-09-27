package com.extendaretail.dsl2png;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.structurizr.Workspace;
import com.structurizr.export.Diagram;
import com.structurizr.model.SoftwareSystem;
import java.util.Collection;
import org.junit.jupiter.api.Test;

class C4PlantUMLDynamicLegendExporterTest {

  @Test
  void itRendersDynamicLegend() {
    Workspace workspace = new Workspace("test", "Test");
    SoftwareSystem testSystem = workspace.getModel().addSoftwareSystem("Test System");
    workspace.getViews().createSystemContextView(testSystem, "TestSystem-SystemContext", null);
    Collection<Diagram> diagrams = new C4PlantUMLDynamicLegendExporter().export(workspace);
    assertEquals(1, diagrams.size());
    String puml = diagrams.stream().findFirst().get().getDefinition();
    assertTrue(puml.contains("SHOW_DYNAMIC_LEGEND()\n@enduml"));
  }
}
