package com.extendaretail.dsl2png.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.extendaretail.dsl2png.FileGlobber;
import com.extendaretail.dsl2png.FileWatcher;
import com.extendaretail.dsl2png.PngExporter;
import com.extendaretail.dsl2png.PngExporter.ExportResult;
import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ClientTest {

  private PngExporter exporter;
  private FileGlobber globber;
  private FileWatcher watcher;

  @BeforeEach
  public void setUp() {
    exporter = mock(PngExporter.class);
    globber = mock(FileGlobber.class);
    watcher = mock(FileWatcher.class);

    File file = new File("./demo.dsl");
    when(globber.match(anyString())).thenReturn(Arrays.asList(file));
    when(exporter.export(any(File.class)))
        .thenReturn(
            new ExportResult(true, new File[] {new File(file.getParent(), "images/test.png")}));
  }

  @Test
  void run() {
    Client cli = new Client(exporter, globber, watcher);
    assertTrue(cli.run(new String[0]), () -> "Expected success, was failure");
    verify(globber).match("**/*.dsl");
    verify(exporter).export(new File("./demo.dsl"));
  }

  @Test
  void watch() {
    Client cli = new Client(exporter, globber, watcher);

    ArgumentCaptor<Consumer<File>> captor = ArgumentCaptor.forClass(Consumer.class);
    doNothing().when(watcher).watch(any(), any(Consumer.class));

    boolean result = cli.run(new String[] {"--watch"});

    verify(watcher, timeout(500)).watch(any(), captor.capture());
    captor.getValue().accept(new File("./demo.dsl"));
    verify(exporter, times(2)).export(new File("./demo.dsl"));

    assertTrue(result, () -> "Expected success, was failure");
  }

  @Test
  void showHelp() {
    Client cli = new Client();
    boolean result = cli.run(new String[] {"--help"});
    assertTrue(result, () -> "--help should exit with 0");
  }
}
