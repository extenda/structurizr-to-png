package com.extendaretail.dsl2png;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import com.structurizr.Workspace;
import com.structurizr.io.Diagram;
import com.structurizr.model.SoftwareSystem;

public class C4PlantUMLDynamicLegendExporterTest {

  @Test
  public void itRendersDynamicLegend() {
    Workspace workspace = new Workspace("test", "Test");
    SoftwareSystem testSystem = workspace.getModel().addSoftwareSystem("Test System");
    workspace.getViews().createSystemContextView(testSystem, "TestSystem-SystemContext", null);
    Collection<Diagram> diagrams = new C4PlantUMLDynamicLegendExporter().export(workspace);
    assertEquals(1, diagrams.size());
    String puml = diagrams.stream().findFirst().get().getDefinition();
    assertTrue(puml.contains("SHOW_DYNAMIC_LEGEND()\n@enduml"));
  }
}
