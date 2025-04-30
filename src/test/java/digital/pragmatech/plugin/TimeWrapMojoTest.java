package digital.pragmatech.plugin;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeWrapMojoTest {

  @Mock
  private MavenProject project;

  private TimeWrapMojo mojo;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mojo = new TimeWrapMojo();

    // Setup the project mock
    when(project.getBuild()).thenReturn(mock(org.apache.maven.model.Build.class));
    when(project.getBuild().getDirectory()).thenReturn(tempDir.toString());

    // Set properties on the mojo
    setInternalState(mojo, "project", project);
    setInternalState(mojo, "reportFileName", "test-report.html");
    setInternalState(mojo, "action", "start");
    setInternalState(mojo, "phase", "test-phase");
  }

  @Test
  void startTimerShouldRecordStartTime() throws Exception {
    // Execute the mojo
    mojo.execute();

    // Since we can't easily access the private static field directly,
    // we'll test by stopping the timer and checking the result
    setInternalState(mojo, "action", "stop");
    mojo.execute();

    // Now generate report to verify data was captured
    setInternalState(mojo, "action", "report");
    mojo.execute();

    // Verify report file was created
    File reportFile = new File(tempDir.toString(), "test-report.html");
    assertTrue(reportFile.exists(), "Report file should have been created");

    // We can't check exact content since it depends on timing,
    // but we can check that the file has reasonable content
    String content = java.nio.file.Files.readString(reportFile.toPath());
    assertTrue(content.contains("test-phase"), "Report should contain our phase name");
    assertTrue(content.contains("<canvas id='buildChart'"), "Report should contain chart element");
  }

  @Test
  void reportGenerationShouldCreateFile() throws Exception {
    // Setup duration data manually
    var phaseDurations = new HashMap<String, Duration>();
    phaseDurations.put("test-phase", Duration.ofSeconds(5));
    setInternalState(mojo, "phaseDurations", phaseDurations);

    // Generate report
    setInternalState(mojo, "action", "report");
    mojo.execute();

    // Verify report file was created
    File reportFile = new File(tempDir.toString(), "test-report.html");
    assertTrue(reportFile.exists(), "Report file should have been created");

    // Check report content
    String content = java.nio.file.Files.readString(reportFile.toPath());
    assertTrue(content.contains("test-phase"), "Report should contain our phase name");
    assertTrue(content.contains("5.000 seconds"), "Report should contain our duration");
  }

  // Helper method to set private fields for testing
  private static void setInternalState(Object target, String fieldName, Object value) {
    try {
      var field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      fail("Failed to set internal state: " + e.getMessage());
    }
  }
}
