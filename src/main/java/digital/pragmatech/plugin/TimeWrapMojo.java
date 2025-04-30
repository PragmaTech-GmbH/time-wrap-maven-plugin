package digital.pragmatech.plugin;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Mojo(name = "timer", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class TimeWrapMojo extends AbstractMojo {

  private static Map<String, Instant> startTimes = new HashMap<>();
  private static Map<String, Duration> phaseDurations = new HashMap<>();

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter(property = "timer.report", defaultValue = "build-report.html")
  private String reportFileName;

  @Parameter(property = "timer.action", defaultValue = "start")
  private String action;

  @Parameter(property = "timer.phase", defaultValue = "default")
  private String phase;

  @Parameter(property = "timer.trackHistory", defaultValue = "false")
  private boolean trackHistory;

  @Parameter(property = "timer.historyFile", defaultValue = "build-history.json")
  private String historyFile;

  public void execute() throws MojoExecutionException {
    if ("start".equals(action)) {
      startTimer();
    } else if ("stop".equals(action)) {
      stopTimer();
    } else if ("report".equals(action)) {
      generateReport();
    }
  }

  private void startTimer() {
    startTimes.put(phase, Instant.now());
    getLog().info("Started timing for phase: " + phase);
  }

  private void stopTimer() {
    if (startTimes.containsKey(phase)) {
      Instant start = startTimes.get(phase);
      Duration duration = Duration.between(start, Instant.now());
      phaseDurations.put(phase, duration);
      getLog().info("Phase " + phase + " took " + formatDuration(duration));
    } else {
      getLog().warn("No start time recorded for phase: " + phase);
    }
  }

  private void generateReport() throws MojoExecutionException {
    File reportFile = new File(project.getBuild().getDirectory(), reportFileName);

    try {
      if (!reportFile.getParentFile().exists()) {
        reportFile.getParentFile().mkdirs();
      }

      if (trackHistory) {
        saveHistory();
      }

      try (FileWriter writer = new FileWriter(reportFile)) {
        writer.write("<html><head><title>TimeWrap Build Report</title>");
        writer.write("<script src=\"https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.7.0/chart.min.js\"></script>");
        writer.write("<style>");
        writer.write("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; color: #333; }");
        writer.write(".container { max-width: 1200px; margin: 0 auto; padding: 20px; }");
        writer.write("h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }");
        writer.write("table { width: 100%; border-collapse: collapse; margin: 20px 0; background-color: white; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }");
        writer.write("th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #ddd; }");
        writer.write("th { background-color: #3498db; color: white; }");
        writer.write("tr:hover { background-color: #f1f1f1; }");
        writer.write(".chart-container { background-color: white; padding: 20px; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-top: 30px; }");
        writer.write(".header { display: flex; justify-content: space-between; align-items: center; }");
        writer.write(".timestamp { color: #7f8c8d; font-size: 0.9em; }");
        writer.write("</style>");
        writer.write("</head><body>");
        writer.write("<div class=\"container\">");
        writer.write("<div class=\"header\">");
        writer.write("<h1>TimeWrap Build Report</h1>");
        writer.write("<div class=\"timestamp\">Generated on " + java.time.LocalDateTime.now() + "</div>");
        writer.write("</div>");

        // Summary table
        writer.write("<table>");
        writer.write("<thead><tr><th>Phase</th><th>Duration</th><th>Percentage</th></tr></thead>");
        writer.write("<tbody>");

        // Calculate total build time for percentages
        long totalMillis = phaseDurations.values().stream()
          .mapToLong(Duration::toMillis)
          .sum();

        for (Map.Entry<String, Duration> entry : phaseDurations.entrySet()) {
          String phase = entry.getKey();
          Duration duration = entry.getValue();
          double percentage = (double) duration.toMillis() / totalMillis * 100;

          writer.write("<tr>");
          writer.write("<td>" + phase + "</td>");
          writer.write("<td>" + formatDuration(duration) + "</td>");
          writer.write("<td>" + String.format("%.1f%%", percentage) + "</td>");
          writer.write("</tr>");
        }
        writer.write("</tbody>");
        writer.write("</table>");

        // Chart
        writer.write("<div class=\"chart-container\">");
        writer.write("<h2>Build Time Distribution</h2>");
        writer.write("<canvas id='buildChart'></canvas>");
        writer.write("</div>");

        // If history is tracked, add history chart
        if (trackHistory) {
          writer.write("<div class=\"chart-container\">");
          writer.write("<h2>Build Time History</h2>");
          writer.write("<canvas id='historyChart'></canvas>");
          writer.write("</div>");
        }

        // JavaScript for chart
        writer.write("<script>");
        writer.write("var ctx = document.getElementById('buildChart').getContext('2d');");
        writer.write("var chart = new Chart(ctx, {");
        writer.write("  type: 'bar',");
        writer.write("  data: {");
        writer.write("    labels: [");

        boolean first = true;
        for (String phase : phaseDurations.keySet()) {
          if (!first) writer.write(",");
          writer.write("'" + phase + "'");
          first = false;
        }

        writer.write("],");
        writer.write("    datasets: [{");
        writer.write("      label: 'Build Time (seconds)',");
        writer.write("      backgroundColor: 'rgba(54, 162, 235, 0.6)',");
        writer.write("      borderColor: 'rgba(54, 162, 235, 1)',");
        writer.write("      borderWidth: 1,");
        writer.write("      data: [");

        first = true;
        for (Duration duration : phaseDurations.values()) {
          if (!first) writer.write(",");
          writer.write(String.valueOf(duration.toMillis() / 1000.0));
          first = false;
        }

        writer.write("]");
        writer.write("    }]");
        writer.write("  },");
        writer.write("  options: {");
        writer.write("    scales: {");
        writer.write("      y: {");
        writer.write("        beginAtZero: true,");
        writer.write("        title: {");
        writer.write("          display: true,");
        writer.write("          text: 'Seconds'");
        writer.write("        }");
        writer.write("      }");
        writer.write("    }");
        writer.write("  }");
        writer.write("});");
        writer.write("</script>");

        // Add history chart if enabled
        if (trackHistory) {
          writer.write("<script>");
          writer.write("fetch('" + getHistoryFilePath() + "')\n");
          writer.write("  .then(response => response.json())\n");
          writer.write("  .then(historyData => {\n");
          writer.write("    var dates = historyData.builds.map(build => build.date);\n");
          writer.write("    var totalTimes = historyData.builds.map(build => build.totalTimeSeconds);\n");
          writer.write("    \n");
          writer.write("    var historyCtx = document.getElementById('historyChart').getContext('2d');\n");
          writer.write("    var historyChart = new Chart(historyCtx, {\n");
          writer.write("      type: 'line',\n");
          writer.write("      data: {\n");
          writer.write("        labels: dates,\n");
          writer.write("        datasets: [{\n");
          writer.write("          label: 'Total Build Time (seconds)',\n");
          writer.write("          data: totalTimes,\n");
          writer.write("          borderColor: 'rgba(75, 192, 192, 1)',\n");
          writer.write("          backgroundColor: 'rgba(75, 192, 192, 0.2)',\n");
          writer.write("          borderWidth: 2,\n");
          writer.write("          tension: 0.1\n");
          writer.write("        }]\n");
          writer.write("      },\n");
          writer.write("      options: {\n");
          writer.write("        responsive: true,\n");
          writer.write("        scales: {\n");
          writer.write("          y: {\n");
          writer.write("            beginAtZero: true,\n");
          writer.write("            title: {\n");
          writer.write("              display: true,\n");
          writer.write("              text: 'Seconds'\n");
          writer.write("            }\n");
          writer.write("          },\n");
          writer.write("          x: {\n");
          writer.write("            title: {\n");
          writer.write("              display: true,\n");
          writer.write("              text: 'Build Date'\n");
          writer.write("            }\n");
          writer.write("          }\n");
          writer.write("        },\n");
          writer.write("        plugins: {\n");
          writer.write("          tooltip: {\n");
          writer.write("            callbacks: {\n");
          writer.write("              label: function(context) {\n");
          writer.write("                return 'Build time: ' + context.raw + ' seconds';\n");
          writer.write("              }\n");
          writer.write("            }\n");
          writer.write("          }\n");
          writer.write("        }\n");
          writer.write("      }\n");
          writer.write("    });\n");
          writer.write("  })\n");
          writer.write("  .catch(error => console.error('Error loading history data:', error));\n");
          writer.write("</script>");
        }

        writer.write("</div>"); // Close container div
        writer.write("</body></html>");
      }

      getLog().info("TimeWrap build report generated at " + reportFile.getAbsolutePath());

    } catch (IOException e) {
      throw new MojoExecutionException("Failed to generate report", e);
    }
  }

  private void saveHistory() throws MojoExecutionException {
    File historyFile = new File(project.getBuild().getDirectory(), this.historyFile);

    try {
      // Calculate total build time
      long totalTimeMillis = phaseDurations.values().stream()
        .mapToLong(Duration::toMillis)
        .sum();

      // Current date in ISO format
      String currentDate = java.time.LocalDate.now().toString();

      // Create the JSON entry for this build
      String newBuildEntry = "    {\n" +
        "      \"date\": \"" + currentDate + "\",\n" +
        "      \"totalTimeSeconds\": " + (totalTimeMillis / 1000.0) + ",\n" +
        "      \"phases\": {\n";

      boolean first = true;
      for (Map.Entry<String, Duration> entry : phaseDurations.entrySet()) {
        if (!first) {
          newBuildEntry += ",\n";
        }
        newBuildEntry += "        \"" + entry.getKey() + "\": " +
          (entry.getValue().toMillis() / 1000.0);
        first = false;
      }

      newBuildEntry += "\n      }\n    }";

      // Read existing history file or create new one
      if (historyFile.exists()) {
        String content = java.nio.file.Files.readString(historyFile.toPath());

        // Insert the new build before the closing bracket
        int insertPoint = content.lastIndexOf("  ]");
        if (insertPoint > 0) {
          // Add comma if not the first entry
          String separator = content.substring(0, insertPoint).trim().endsWith("}") ? ",\n" : "";
          String newContent = content.substring(0, insertPoint) +
            separator + newBuildEntry + "\n  " +
            content.substring(insertPoint);

          java.nio.file.Files.writeString(historyFile.toPath(), newContent);
        }
      } else {
        // Create parent directories if needed
        if (!historyFile.getParentFile().exists()) {
          historyFile.getParentFile().mkdirs();
        }

        // Create new history file with initial structure
        String initialContent = "{\n" +
          "  \"builds\": [\n" +
          newBuildEntry + "\n" +
          "  ]\n" +
          "}";

        java.nio.file.Files.writeString(historyFile.toPath(), initialContent);
      }

      getLog().info("Updated build history at " + historyFile.getAbsolutePath());

    } catch (IOException e) {
      throw new MojoExecutionException("Failed to save build history", e);
    }
  }

  private String getHistoryFilePath() {
    // Get the relative path from the report to the history file
    // Both should be in the same directory, so just the filename
    return this.historyFile;
  }

  private String formatDuration(Duration duration) {
    long seconds = duration.getSeconds();
    long millis = duration.toMillis() % 1000;
    return String.format("%d.%03d seconds", seconds, millis);
  }
}
