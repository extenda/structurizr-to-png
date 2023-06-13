package com.extendaretail.dsl2png;

import com.structurizr.export.IndentingWriter;
import com.structurizr.export.plantuml.C4PlantUMLExporter;
import com.structurizr.view.ModelView;

/**
 * A {@link C4PlantUMLExporter} that uses the {@code SHOW_DYNAMIC_LEGEND()} footer.
 *
 * @author sasjo
 * @deprecated Support has been added through view properties in the base class
 */
@Deprecated
public class C4PlantUMLDynamicLegendExporter extends C4PlantUMLExporter {

  @Override
  protected void writeHeader(ModelView view, IndentingWriter writer) {
    super.writeHeader(view, writer);
    //    writer.writeLine("skinparam linetype ortho");
    //    writer.writeLine();
  }

  @Override
  protected void writeFooter(ModelView view, IndentingWriter writer) {
    writer.writeLine();
    writer.writeLine("SHOW_DYNAMIC_LEGEND()");
    writer.writeLine("@enduml");
  }
}
