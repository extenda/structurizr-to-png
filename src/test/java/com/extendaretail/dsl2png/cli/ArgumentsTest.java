package com.extendaretail.dsl2png.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.extendaretail.dsl2png.cli.Arguments.HelpException;
import java.io.File;
import org.junit.jupiter.api.Test;

class ArgumentsTest {

  @Test
  void defaultPath() throws HelpException {
    Arguments args = Arguments.parse(new String[0]);
    assertEquals("**/*.dsl", args.getPath());
  }

  @Test
  void setPath() throws HelpException {
    Arguments args = Arguments.parse(new String[] {"--path=demo.dsl"});
    assertEquals("demo.dsl", args.getPath());
  }

  @Test
  void isWatch() throws HelpException {
    Arguments args = Arguments.parse(new String[] {"--watch"});
    assertTrue(args.isWatch());
  }

  @Test
  void defaultNoWatch() throws HelpException {
    Arguments args = Arguments.parse(new String[0]);
    assertFalse(args.isWatch());
  }

  @Test
  void defaultOutput() throws HelpException {
    Arguments args = Arguments.parse(new String[0]);
    assertEquals(new File("images"), args.getOutput());
  }

  @Test
  void setOutput() throws HelpException {
    Arguments args = Arguments.parse(new String[] {"-o", "c4-diagrams"});
    assertEquals(new File("c4-diagrams"), args.getOutput());
  }

  @Test
  void setRenderer() throws HelpException {
    Arguments args = Arguments.parse(new String[] {"-r", "graphviz"});
    assertEquals(Arguments.Renderer.graphviz, args.getRenderer());
  }

  @Test
  void helpUsage() {
    try {
      Arguments.parse(new String[] {"--help"});
      fail("Expected help exception");
    } catch (HelpException e) {
      assertEquals(0, e.getExitCode());
    }
  }

  @Test
  void helpIllegalArgs() {
    try {
      Arguments.parse(new String[] {"--missing"});
      fail("Expected help exception");
    } catch (HelpException e) {
      assertEquals(1, e.getExitCode());
    }
  }
}
