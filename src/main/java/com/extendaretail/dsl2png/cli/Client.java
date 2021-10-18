package com.extendaretail.dsl2png.cli;

import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.extendaretail.dsl2png.FileGlobber;
import com.extendaretail.dsl2png.FileWatcher;
import com.extendaretail.dsl2png.PngExporter;
import com.extendaretail.dsl2png.PngExporter.ExportResult;
import com.extendaretail.dsl2png.cli.Arguments.HelpException;
import com.extendaretail.dsl2png.vertx.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class Client {

  private static Logger log = LoggerFactory.getLogger(Client.class);

  private PngExporter exporter;

  private FileGlobber globber;

  private FileWatcher watcher;

  public Client() {
    this(new PngExporter(), new FileGlobber(), new FileWatcher());
  }

  public Client(PngExporter exporter, FileGlobber globber, FileWatcher watcher) {
    this.globber = globber;
    this.exporter = exporter;
    this.watcher = watcher;
  }

  public boolean run(String[] argv) {
    try {
      return run(Arguments.parse(argv));
    } catch (HelpException e) {
      System.err.println(e.getMessage());
      return e.getExitCode() == 0;
    }
  }

  public boolean run(Arguments args) {
    Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(2));
    MainVerticle mainVerticle = new MainVerticle(args.getOutput());
    vertx.deployVerticle(mainVerticle);
    
    exporter.setOutputDirectory(args.getOutput());
    List<File> files = globber.match(args.getPath());

    if (files.isEmpty()) {
      log.error("No files found to match: {}", args.getPath());
      return false;
    }

    ExportResult result = files.parallelStream().map(exporter::export)
        .reduce(new ExportResult(true), ExportResult::merge);

    if (args.isWatch()) {
      mainVerticle.previewFiles(files);

      watcher.watch(files, (f) -> {
        ExportResult r = exporter.export(f);
        if (r.isSuccess()) {
          vertx.runOnContext(v -> vertx.eventBus().publish("preview.changed",
              MainVerticle.imagesEvent("changed", r.getImages())));
        }
      });
      return true;
    } else {
      return result.isSuccess();
    }
  }
}
