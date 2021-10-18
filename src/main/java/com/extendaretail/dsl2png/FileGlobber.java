package com.extendaretail.dsl2png;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileGlobber {

  private static Logger log = LoggerFactory.getLogger(FileGlobber.class);

  private static final Set<Path> IGNORE_DIRS = Set.of(Path.of(".git"), Path.of("node_modules"));

  /**
   * Find all files matching the glob. If no files are found or if an exception occurs, this method
   * returns an empty list.
   * 
   * @param glob the glob to match
   * @return the list of matching files
   */
  public List<File> match(String glob) {
    try {
      return walkFileTree(glob);
    } catch (IOException e) {
      log.debug("Failed to locate DSL files", e);
      return Collections.emptyList();
    }
  }

  private List<File> walkFileTree(String glob) throws IOException {
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
    List<File> files = new ArrayList<>();
    Files.walkFileTree(new File(".").toPath(), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        // Match both file and normalized file to clean up ./ paths names.
        if (matcher.matches(file) || matcher.matches(file.normalize())) {
          files.add(file.toFile());
        }
        return CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
          throws IOException {
        return IGNORE_DIRS.contains(dir.normalize()) ? SKIP_SUBTREE : CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return CONTINUE;
      }
    });
    return files;
  }
}
