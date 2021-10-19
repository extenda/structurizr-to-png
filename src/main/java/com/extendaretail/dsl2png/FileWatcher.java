package com.extendaretail.dsl2png;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWatcher {

  private static Logger log = LoggerFactory.getLogger(FileWatcher.class);
  private FileSystem fs;

  public FileWatcher() {
    this(FileSystems.getDefault());
  }

  public FileWatcher(FileSystem fs) {
    this.fs = fs;
  }

  private void register(Path file, WatchService watcher) {
    try {
      file.getParent().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
    } catch (IOException | RuntimeException e) {
      log.error("File watch registration failed for {}.", file, e);
    }
  }

  /**
   * Watch the file system for changes to the selected paths. This method will block the calling
   * thread until the program is interrupted.
   *
   * @param files the DSL files to watch
   * @param onChange the consumer to invoke with the changed files
   */
  public void watch(List<File> files, Consumer<File> onChange) {
    try (WatchService watcher = fs.newWatchService()) {
      Set<Path> paths = files.stream().map(File::toPath).collect(Collectors.toSet());
      paths.forEach(file -> register(file, watcher));

      log.info("Watching files... Ctrl+C to exit.");

      try {
        WatchKey key;
        while ((key = watcher.take()) != null) {
          Path parent = (Path) key.watchable();
          key.pollEvents().stream()
              .map(WatchEvent::context)
              .map(Path.class::cast)
              .map(parent::resolve)
              .filter(paths::contains)
              .map(Path::toFile)
              .forEach(
                  f -> {
                    log.debug("{} changed", f);
                    onChange.accept(f);
                  });
          key.reset();
        }
      } catch (InterruptedException e) {
        log.trace("File watcher interrupted.", e);
        Thread.currentThread().interrupt();
        return;
      }
    } catch (IOException e) {
      log.error("Failed to create file watcher", e);
      return;
    }
  }
}
