# TimeWrap Maven Plugin

> **Bend the space-time continuum of your build process to ship faster to production**

TimeWrap is a Maven plugin that helps you visualize, optimize, and accelerate your build pipeline. It provides detailed metrics and visualizations to identify bottlenecks in your build process, allowing you to ship faster to production.

## Features

- **Build Time Visualization** - Interactive HTML charts showing where time is spent
- **Phase-by-Phase Analysis** - Break down build times by Maven phase and goal
- **Historical Tracking** - Track build time improvements over time
- **Integration with CI/CD** - Easily integrate with GitHub Actions, Jenkins, etc.
- **Bottleneck Identification** - Quickly spot which modules or phases are slowing down your builds
- **Cross-Project Reporting** - Compare build times across multiple projects

## Requirements

- Java 21 or higher
- Maven 3.9 or higher

## Installation

Add the plugin to your pom.xml:

```xml
<plugin>
    <groupId>digital.pragmatech</groupId>
    <artifactId>time-wrap-maven-plugin</artifactId>
    <version>1.0.0</version>
</plugin>
```

## Quick Start

Run your build with TimeWrap:

```bash
mvn digital.pragmatech:time-wrap-maven-plugin:timer -Dtimer.action=report
```

Or configure it to run as part of your build:

```xml
<plugin>
    <groupId>digital.pragmatech</groupId>
    <artifactId>time-wrap-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <!-- Measure and monitor each build phase -->
        <execution>
            <id>generate-report</id>
            <phase>verify</phase>
            <goals>
                <goal>timer</goal>
            </goals>
            <configuration>
                <action>report</action>
                <reportFileName>build-time-report.html</reportFileName>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Measuring Specific Build Phases

To measure individual phases, configure multiple executions:

```xml
<plugin>
    <groupId>digital.pragmatech</groupId>
    <artifactId>time-wrap-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <!-- Start timing compile phase -->
        <execution>
            <id>compile-start</id>
            <phase>process-resources</phase>
            <goals>
                <goal>timer</goal>
            </goals>
            <configuration>
                <action>start</action>
                <phase>compile</phase>
            </configuration>
        </execution>

        <!-- End timing compile phase -->
        <execution>
            <id>compile-end</id>
            <phase>compile</phase>
            <goals>
                <goal>timer</goal>
            </goals>
            <configuration>
                <action>stop</action>
                <phase>compile</phase>
            </configuration>
        </execution>

        <!-- Generate report -->
        <execution>
            <id>generate-report</id>
            <phase>verify</phase>
            <goals>
                <goal>timer</goal>
            </goals>
            <configuration>
                <action>report</action>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Configuration Options

| Option          | Description                                   | Default Value       |
|-----------------|-----------------------------------------------|---------------------|
| action          | Timer action (start, stop, report)            | start               |
| phase           | Build phase name to measure                   | default             |
| reportFileName  | Name of the HTML report file                  | build-report.html   |
| trackHistory    | Whether to track historical build times       | false               |
| historyFile     | JSON file to store history                    | build-history.json  |

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
