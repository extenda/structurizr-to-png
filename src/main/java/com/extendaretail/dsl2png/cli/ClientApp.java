package com.extendaretail.dsl2png.cli;

public final class ClientApp {

  private ClientApp() {}

  public static void main(String[] args) throws Exception {
    boolean result = new Client().run(args);
    System.exit(result ? 0 : 1);
  }
}
