package com.extendaretail.dsl2png;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.ComponentView;
import com.structurizr.view.Configuration;
import com.structurizr.view.ContainerView;
import com.structurizr.view.ModelView;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.SystemLandscapeView;
import com.structurizr.view.ThemeUtils;
import com.structurizr.view.View;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load a decorated workspace from a DSL file.
 *
 * @author sasjo
 */
public class WorkspaceReader {

  private static Logger log = LoggerFactory.getLogger(WorkspaceReader.class);

  /** Set of element tags that indicates it is an external system. */
  private static final Set<String> EXTERNAL_TAGS =
      Set.of("external", "existing system", "external system");

  /** Predicate to check if a software system is tagged as existing or external. */
  private final Predicate<SoftwareSystem> hasExternalTag =
      s -> {
        Set<String> tags =
            Stream.of(s.getTags().toLowerCase().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        return EXTERNAL_TAGS.stream().anyMatch(tags::contains);
      };

  private int themePort;

  public WorkspaceReader(int themePort) {
    this.themePort = themePort;
  }

  public Workspace loadFromDsl(File dslFile) throws IOException, StructurizrDslParserException {
    Workspace workspace = parseDsl(dslFile);
    addTheme(workspace);
    // setExternalLocation(workspace);
    workspace
        .getViews()
        .getViews()
        .forEach(
            view ->
                view.addProperty(
                    DiagramRenderer.DIAGRAM_NAME_PROPERTY, generateViewBasename(view)));

    return workspace;
  }

  /**
   * Generate a predictable basename for exported views. The generated view cannot be trusted and
   * has changed over time in Structurizr. If an explicit key is defined it will be used, otherwise
   * a backwards compatible name is generated.
   *
   * @return the base filename for the exportable view
   */
  private String generateViewBasename(View view) {
    if (isGeneratedKey(view)) {
      if (view instanceof SystemLandscapeView) {
        return "SystemLandscape";
      }
      if (view instanceof SystemContextView systemContext) {
        return generateViewBasename(systemContext, "SystemContext");
      }
      if (view instanceof ContainerView containerView) {
        return generateViewBasename(containerView, "Container");
      }
      if (view instanceof ComponentView componentView) {
        return generateViewBasename(componentView, componentView.getContainerId(), "Component");
      }
    }
    return view.getKey();
  }

  private boolean isGeneratedKey(View view) {
    return view.getKey().matches("^(SystemLandscape|SystemContext|Container|Component)-\\d{3}$");
  }

  private String generateViewBasename(ModelView view, String... name) {
    String systemId = view.getSoftwareSystem().getName().replaceAll("\\s", "");
    final String delimiter = "-";
    return systemId + delimiter + String.join(delimiter, name);
  }

  private Workspace parseDsl(File dslFile) throws StructurizrDslParserException {
    log.info("Load file.");
    StructurizrDslParser dslParser = new StructurizrDslParser();
    dslParser.parse(dslFile);
    return dslParser.getWorkspace();
  }

  private void addTheme(Workspace workspace) throws IOException {
    Configuration config = workspace.getViews().getConfiguration();
    if (config.getThemes().length == 0) {
      log.debug("Add default 'dsl-to-png theme' theme");
      config.addTheme("http://127.0.0.1:" + themePort + "/themes/theme.json");
    }
    try {
      ThemeUtils.loadThemes(workspace);
    } catch (Exception e) {
      throw new IOException("Failed to load theme", e);
    }
  }

  //  private void setExternalLocation(Workspace workspace) {
  //    workspace.getModel().getSoftwareSystems().stream()
  //        .filter(hasExternalTag)
  //        .forEach(
  //            s -> {
  //              s.setLocation(Location.External);
  //            });
  //  }
}
