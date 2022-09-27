package com.extendaretail.dsl2png;

import com.structurizr.io.IndentingWriter;
import com.structurizr.io.plantuml.C4PlantUMLExporter;
import com.structurizr.view.View;

/**
 * A {@link C4PlantUMLExporter} that uses the {@code SHOW_DYNAMIC_LEGEND()} footer.
 *
 * @author sasjo
 */
public class C4PlantUMLDynamicLegendExporter extends C4PlantUMLExporter {

  @Override
  protected void writeHeader(View view, IndentingWriter writer) {
    super.writeHeader(view, writer);
    //    writer.writeLine("skinparam linetype ortho");
    //    writer.writeLine();
  }

  @Override
  protected void writeFooter(View view, IndentingWriter writer) {
    writer.writeLine();
    writer.writeLine("SHOW_DYNAMIC_LEGEND()");
    writer.writeLine("@enduml");
  }
}
