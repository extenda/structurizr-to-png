package com.extendaretail.dsl2png;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;

public class LocalPort {

  public static DeploymentOptions getLocalPort() {
    try {
      ServerSocket socket = new ServerSocket(0);
      int port = socket.getLocalPort();
      socket.close();
      return new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
