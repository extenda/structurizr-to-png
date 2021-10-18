package com.extendaretail.dsl2png.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import org.junit.jupiter.api.Test;
import com.extendaretail.dsl2png.cli.Arguments.HelpException;

public class ArgumentsTest {

  @Test
  public void defaultPath() throws HelpException {
    Arguments args = Arguments.parse(new String[0]);
    assertEquals("**/*.dsl", args.getPath());
  }

  @Test
  public void setPath() throws HelpException {
    Arguments args = Arguments.parse(new String[] {"--path=demo.dsl"});
    assertEquals("demo.dsl", args.getPath());
  }

  @Test
  public void isWatch() throws HelpException {
    Arguments args = Arguments.parse(new String[] {"--watch"});
    assertTrue(args.isWatch());
  }

  @Test
  public void defaultNoWatch() throws HelpException {
    Arguments args = Arguments.parse(new String[0]);
    assertFalse(args.isWatch());
  }

  @Test
  public void defaultOutput() throws HelpException {
    Arguments args = Arguments.parse(new String[0]);
    assertEquals(new File("images"), args.getOutput());
  }

  @Test
  public void setOutput() throws HelpException {
    Arguments args = Arguments.parse(new String[] {"-o", "c4-diagrams"});
    assertEquals(new File("c4-diagrams"), args.getOutput());
  }

  @Test
  public void helpUsage() {
    try {
      Arguments.parse(new String[] {"--help"});
      fail("Expected help exception");
    } catch (HelpException e) {
      assertEquals(0, e.getExitCode());
    }
  }
  
  @Test
  public void helpIllegalArgs() {
    try {
      Arguments.parse(new String[] {"--missing"});
      fail("Expected help exception");
    } catch (HelpException e) {
      assertEquals(1, e.getExitCode());
    }
  }
}
