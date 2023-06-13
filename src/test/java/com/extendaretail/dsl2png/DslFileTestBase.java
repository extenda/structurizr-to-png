package com.extendaretail.dsl2png;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

public class DslFileTestBase {

  public File testDir(TestInfo testInfo) {
    return new File(
        "target/dslFiles/"
            + testInfo
                .getTestMethod()
                .map(Method::getName)
                .orElseThrow(() -> new IllegalStateException("Missing test method name")));
  }

  @BeforeEach
  public void createDirectory(TestInfo testInfo) throws IOException {
    Files.createDirectories(testDir(testInfo).toPath());
  }

  public File createValidDsl(TestInfo testInfo) throws IOException {
    return Files.writeString(
            new File(testDir(testInfo), "valid.dsl").toPath(),
            """
        workspace {
          model {
            user = person "User" "A user"
            existing = softwareSystem "Existing System" "Legacy system" "Existing System"
            test = softwareSystem "Test System" "My System" {
              testContainer = container "Test Container" {
                testComponent = component "Test Component"
              }

            }

            user -> test "Uses"
            test -> existing "Uses"
          }

          views {
            systemContext test {
              include *
            }

            container test {
              include *
            }

            component testContainer {
              include *
            }

            container test user-defined-name {
              include *
            }
          }
        }
        """,
            CREATE,
            TRUNCATE_EXISTING)
        .toFile();
  }

  public File createInvalidDsl(TestInfo testInfo) throws IOException {
    return Files.writeString(
            new File(testDir(testInfo), "invalid.dsl").toPath(),
            """
        workspace {
          model {
            user = personX "User" "A user"
          }

          views {
            systemContext system {
              include *
            }
          }
        }
        """,
            CREATE,
            TRUNCATE_EXISTING)
        .toFile();
  }

  @AfterEach
  public void removeFiles(TestInfo testInfo) throws IOException {
    Files.walkFileTree(
        testDir(testInfo).toPath(),
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
        });
    Files.deleteIfExists(testDir(testInfo).toPath());
  }
}
