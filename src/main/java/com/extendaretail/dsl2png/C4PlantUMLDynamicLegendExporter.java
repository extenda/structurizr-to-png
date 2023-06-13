package com.extendaretail.dsl2png;

import com.structurizr.export.IndentingWriter;
import com.structurizr.export.plantuml.C4PlantUMLExporter;
import com.structurizr.view.ModelView;

/**
 * A {@link C4PlantUMLExporter} that uses the {@code SHOW_DYNAMIC_LEGEND()} footer.
 *
 * @author sasjo
 */
public class C4PlantUMLDynamicLegendExporter extends C4PlantUMLExporter {

  @Override
  protected void writeFooter(ModelView view, IndentingWriter writer) {
    writer.writeLine();
    if (includeLegend(view)) {
      writer.writeLine("SHOW_DYNAMIC_LEGEND()");
    }
    writer.writeLine("@enduml");
  }
}
