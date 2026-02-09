package app.demo;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.logging.LoggingSystemProperty;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

/**
 * <p>
 * In our environment we deploy to an external Tomcat server which enables hot redployment in a long-lived JVM.
 * If previous applications set console logging pattern to the empty string (our previous workaround to
 * disable console logging) then even if THIS application does not set the value, that now erroneous state
 * persists!
 * </p>
 *
 * <p>
 * The issue only reproduces in a long-lived JVM (e.g., external Tomcat) where LoggingSystemProperties.apply()
 * runs multiple times across redeploys. In this scenario, if CONSOLE_LOG_PATTERN is set to an empty string, then
 * a subsequent deployment with logging.console.enabled=false still finds an empty log pattern causing Logback error.
 * </p>
 *
 * <p>
 * Proposed fix to {@code org.springframework.boot.logging.LoggingSystemProperties}:
 * </p>
 *
 * <pre><code>
 * protected void apply(@Nullable LogFile logFile, PropertyResolver resolver) {
 *     setSystemProperty(LoggingSystemProperty.APPLICATION_NAME, resolver);
 *     setSystemProperty(LoggingSystemProperty.APPLICATION_GROUP, resolver);
 *     setSystemProperty(LoggingSystemProperty.PID, new ApplicationPid().toString());
 *     setSystemProperty(LoggingSystemProperty.CONSOLE_CHARSET, resolver, getDefaultConsoleCharset().name());
 *     setSystemProperty(LoggingSystemProperty.FILE_CHARSET, resolver, getDefaultFileCharset().name());
 *     setSystemProperty(LoggingSystemProperty.CONSOLE_THRESHOLD, resolver, this::thresholdMapper);
 *     setSystemProperty(LoggingSystemProperty.FILE_THRESHOLD, resolver, this::thresholdMapper);
 *     setSystemProperty(LoggingSystemProperty.EXCEPTION_CONVERSION_WORD, resolver);
 *
 *     // FIX - This change prevents Spring Boot from setting CONSOLE_LOG_PATTERN to an empty string when
 *     // console logging is disabled. While it does not clean up pre-existing system properties in long-lived JVMs,
 *     // it ensures that Boot no longer introduces invalid global state and prevents the issue in fresh JVMs and
 *     // future deployments.
 *     if (this.environment.getProperty("logging.console.enabled", Boolean.class, true)) {
 *       setSystemProperty(LoggingSystemProperty.CONSOLE_PATTERN, resolver);
 *     }
 *
 *     setSystemProperty(LoggingSystemProperty.FILE_PATTERN, resolver);
 *     setSystemProperty(LoggingSystemProperty.CONSOLE_STRUCTURED_FORMAT, resolver);
 *     setSystemProperty(LoggingSystemProperty.FILE_STRUCTURED_FORMAT, resolver);
 *     setSystemProperty(LoggingSystemProperty.LEVEL_PATTERN, resolver);
 *     setSystemProperty(LoggingSystemProperty.DATEFORMAT_PATTERN, resolver);
 *     setSystemProperty(LoggingSystemProperty.CORRELATION_PATTERN, resolver);
 *
 *     if (logFile != null) {
 *       logFile.applyToSystemProperties();
 *     }
 *
 *     if (!this.environment.getProperty("logging.console.enabled", Boolean.class, true)) {
 *       setSystemProperty(LoggingSystemProperty.CONSOLE_THRESHOLD.getEnvironmentVariableName(), "OFF");
 *     }
 * }
 * </code></pre>
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

  static {
    System.err.println("logging.pattern.console = [" + System.getProperty("logging.pattern.console") + "]"); // null
    System.err.println("CONSOLE_LOG_PATTERN = [" + System.getProperty("CONSOLE_LOG_PATTERN") + "]"); // empty string
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  /*
  LoggingSystemProperty system properties:
    FILE_LOG_CHARSET = [UTF-8]
    CONSOLE_LOG_THRESHOLD = [OFF]
    LOG_FILE = [/var/log/isp_logs/gh-46592.log]
    PID = [159354]
    CONSOLE_LOG_CHARSET = [UTF-8]
    CONSOLE_LOG_PATTERN = []
   */
  @Component
  public static class LoggingSystemPropertiesDebugRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
      List<String> loggingSystemVariableNames = Stream.of(LoggingSystemProperty.values()).map(LoggingSystemProperty::getEnvironmentVariableName).toList();
      System.err.println("LoggingSystemProperty system properties:");
      for (String key : System.getProperties().stringPropertyNames()) {
        if (loggingSystemVariableNames.contains(key)) {
          System.err.println("  " + key + " = [" + System.getProperty(key) + "]");
        }
      }
    }
  }

  @Component
  public static class LoggingDebugRunner implements ApplicationRunner {
    private final Environment env;

    public LoggingDebugRunner(Environment env) {
      this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
      System.err.println("Spring env logging.pattern.console = ["+ env.getProperty("logging.pattern.console") + "]"); // null
    }
  }

  @Component
  public static class LogbackDebugRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
      LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
      for (Logger logger : ctx.getLoggerList()) {
        logger.iteratorForAppenders().forEachRemaining(appender -> {
          if (appender instanceof ConsoleAppender<?> ca) {
            if (ca.getEncoder() instanceof PatternLayoutEncoder ple) {
              System.err.println("Logback console pattern = [" + ple.getPattern() + "]"); // empty string
            }
          }
        });
      }
    }
  }
}
