package com.extendaretail.dsl2png.cli;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Arguments {

  private static final int HELP_WIDTH = 80;

  /** The relative output directory. */
  private String output;

  /** Watch flag. */
  private boolean watch;

  /** DSL glob path. */
  private String path;

  /** Image renderer. */
  private Renderer renderer = Renderer.c4plantuml;

  private Arguments() {}

  public File getOutput() {
    return new File(output);
  }

  public String getPath() {
    return path;
  }

  public boolean isWatch() {
    return watch;
  }

  public Renderer getRenderer() {
    return renderer;
  }

  private static String help(Options opts) {
    StringWriter out = new StringWriter();
    PrintWriter writer = new PrintWriter(out);
    HelpFormatter fmt = new HelpFormatter();

    fmt.printHelp(
        writer,
        HELP_WIDTH,
        "java -jar structurizr-to-png.jar",
        "Create PNG files from Structurizr DSL",
        opts,
        fmt.getLeftPadding(),
        fmt.getDescPadding(),
        "",
        true);

    return out.toString();
  }

  public static Arguments parse(String[] args) throws HelpException {
    Options opts = new Options();
    opts.addOption(
        Option.builder("o")
            .longOpt("output")
            .hasArg()
            .argName("dir")
            .type(String.class)
            .desc("Image output directory. A relative path is resolved from the DSL file.")
            .build());
    opts.addOption(
        Option.builder("w").longOpt("watch").desc("Watch for changed DSL files.").build());
    opts.addOption(
        Option.builder("p")
            .longOpt("path")
            .hasArg()
            .argName("glob")
            .type(String.class)
            .desc("Path to workspaces to render. Glob is supported.")
            .build());
    opts.addOption(
        Option.builder("r")
            .longOpt("render-with")
            .hasArg()
            .type(Renderer.class)
            .desc("The name of the diagram renderer to use.")
            .build());
    opts.addOption(Option.builder().longOpt("help").build());

    Arguments arguments = new Arguments();
    try {
      CommandLine cmd = new DefaultParser().parse(opts, args);
      arguments.output = cmd.getOptionValue("output", "images");
      arguments.watch = cmd.hasOption("watch");
      arguments.path = cmd.getOptionValue("path", "**/*.dsl");
      arguments.renderer =
          Renderer.valueOf(cmd.getOptionValue("render-with", Renderer.c4plantuml.name()));
      if (cmd.hasOption("help")) {
        throw new HelpException(help(opts), 0);
      }
    } catch (ParseException e) {
      throw new HelpException(help(opts), 1);
    }
    return arguments;
  }

  public static class HelpException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int exitCode;

    public HelpException(String message, int exitCode) {
      super(message);
      this.exitCode = exitCode;
    }

    public int getExitCode() {
      return exitCode;
    }
  }

  public enum Renderer {
    c4plantuml,
    structurizr,
    graphviz,
  }
}
