package com.extendaretail.dsl2png;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.model.Location;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.ThemeUtils;
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

  private static final WorkspaceReader INSTANCE = new WorkspaceReader();

  /** Set of element tags that indicates it is an external system. */
  private static final Set<String> EXTERNAL_TAGS =
      Set.of("external", "existing system", "external system");

  /** Predicate to check if a software system is tagged as existing or external. */
  private final Predicate<SoftwareSystem> hasExternalTag =
      (s) -> {
        Set<String> tags =
            Stream.of(s.getTags().toLowerCase().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        return EXTERNAL_TAGS.stream().anyMatch(tags::contains);
      };

  private WorkspaceReader() {}

  public static Workspace loadFromDsl(File dslFile)
      throws IOException, StructurizrDslParserException {
    Workspace workspace = INSTANCE.parseDsl(dslFile);
    INSTANCE.addTheme(workspace);
    INSTANCE.setExternalLocation(workspace);
    return workspace;
  }

  private Workspace parseDsl(File dslFile) throws StructurizrDslParserException {
    log.info("Load file.");
    StructurizrDslParser dslParser = new StructurizrDslParser();
    dslParser.parse(dslFile);
    return dslParser.getWorkspace();
  }

  private void addTheme(Workspace workspace) throws IOException {
    workspace.getViews().getConfiguration().addTheme("http://127.0.0.1:3000/themes/theme.json");
    try {
      ThemeUtils.loadThemes(workspace);
    } catch (Exception e) {
      throw new IOException("Failed to load theme", e);
    }
  }

  private void setExternalLocation(Workspace workspace) {
    workspace.getModel().getSoftwareSystems().stream()
        .filter(hasExternalTag)
        .forEach(
            (s) -> {
              s.setLocation(Location.External);
            });
  }
}
