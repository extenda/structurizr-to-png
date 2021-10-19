package com.extendaretail.dsl2png;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

class FileGlobberTest extends DslFileTestBase {

  private FileGlobber glob = new FileGlobber();

  @Test
  void noMatch() {
    List<File> match = glob.match("nomatch.dsl");
    assertTrue(match.isEmpty());
  }

  @Test
  void invalidRootDirectory() {
    List<File> match = glob.match("/missing/**/*.dsl");
    assertTrue(match.isEmpty());
  }

  @Test
  void multiMatch(TestInfo testInfo) throws IOException {
    File validDsl = createValidDsl(testInfo);
    File invalidDsl = createInvalidDsl(testInfo);
    List<File> match = glob.match(validDsl.getParent() + "/*.dsl");
    match.sort(File::compareTo);
    assertEquals(
        Arrays.asList(new File(".", invalidDsl.getPath()), new File(".", validDsl.getPath())),
        match);
  }

  @Test
  void defaultMatch() throws IOException {
    List<File> match = glob.match("**/*.dsl");
    assertTrue(match.contains(new File("./demo.dsl")), () -> "Matches should include demo.dsl");
  }

  @Test
  void exactMatch(TestInfo testInfo) throws IOException {
    File validDsl = createValidDsl(testInfo);
    List<File> match = glob.match(validDsl.getPath());
    assertEquals(Arrays.asList(new File(".", validDsl.getPath())), match);
  }
}
