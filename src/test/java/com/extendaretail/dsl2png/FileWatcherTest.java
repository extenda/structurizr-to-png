package com.extendaretail.dsl2png;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileWatcherTest {

  private WatchService ws;
  private FileWatcher watcher;
  ExecutorService executor;

  @BeforeEach
  public void setUp() throws IOException {
    FileSystem fs = mock(FileSystem.class);
    ws = mock(WatchService.class);
    when(fs.newWatchService()).thenReturn(ws);
    watcher = new FileWatcher(fs);
    executor = Executors.newSingleThreadExecutor();
  }

  @AfterEach
  public void tearDown() {
    executor.shutdownNow();
  }

  @Test
  public void notifyOnChange() throws IOException, InterruptedException {
    Path mockPath = mock(Path.class);
    File mockFile = mock(File.class);
    Path mockParent = mock(Path.class);
    when(mockParent.resolve(any(Path.class))).thenReturn(mockPath);
    when(mockPath.getParent()).thenReturn(mockParent);
    when(mockPath.toFile()).thenReturn(mockFile);
    when(mockFile.toPath()).thenReturn(mockPath);

    BlockingQueue<File> changes = new LinkedBlockingQueue<>();

    WatchKey key = mock(WatchKey.class);
    when(key.watchable()).thenReturn(mockParent);

    @SuppressWarnings("unchecked")
    WatchEvent<Path> event = mock(WatchEvent.class);
    when(event.context()).thenReturn(mockPath);
    when(key.pollEvents()).thenReturn(Arrays.asList(event));
    when(ws.take()).thenReturn(key).thenReturn(null);

    executor.execute(
        () -> {
          watcher.watch(Arrays.asList(mockPath.toFile()), f -> changes.add(f));
        });

    File changed = changes.poll(2, TimeUnit.SECONDS);
    assertEquals(mockFile, changed);

    verify(mockParent).register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
    verify(ws, times(2)).take();
  }
}
