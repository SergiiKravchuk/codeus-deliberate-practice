package org.codeus.database.fundamentals.recap.setup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.codeus.database.fundamentals.recap.util.SqlUtil.normalizeSql;

public class ProblemSolutionAnalyser {

  private final Connection connection;

  public record Report(boolean isMetricsExtracted, boolean isOptimizationAchieved, boolean isOptimizationMechanismIntended,
                       String analysisBeforeOptimization, String analysisAfterOptimization) {}

  public ProblemSolutionAnalyser(Connection connection) {
    this.connection = connection;
  }

  public void printOptimizationResult(Report report) {
    if (!report.isMetricsExtracted()) System.out.println("🟨 Could not retrieve execution time");
    else {
      var message = report.isOptimizationAchieved()
        ? "✅ Query time has been dramatically optimized"
        : "❌ Query time hasn't been dramatically optimized";
      System.out.println(message);
    }
  }

  public void printOptimizationMechanismResult(Report report) {
    var message = report.isOptimizationMechanismIntended()
      ? "✅ Used the intended optimization mechanisms"
      : "🟨 Used the non-intended optimization mechanisms, check performance metrics";

    System.out.println(message);
  }

  private void printQueryAnalysis(String marker, String analysis) {
    int borderLength = 120;
    System.out.println("-".repeat(borderLength));
    System.out.printf("QUERY ANALYSIS [%s]%n", marker);
    System.out.println("-".repeat(borderLength));
    System.out.println(analysis);
    System.out.println("-".repeat(borderLength));
  }

  public void printFullReport(Report report) {
    printOptimizationMechanismResult(report);
    printOptimizationResult(report);
    printQueryAnalysis("BEFORE OPTIMIZATION", report.analysisBeforeOptimization());
    printQueryAnalysis("AFTER OPTIMIZATION", report.analysisAfterOptimization());
  }

  public Report analyze(MetadataHolder.ProblemMetadata metadata, Consumer<String> executeSolution) {
    var analysisBeforeOptimization = analyzeQuery(metadata.query());
    executeSolution.accept(metadata.filename());
    var analysisAfterOptimization = analyzeQuery(metadata.query());

    var execTimeBefore = extractExecutionTime(analysisBeforeOptimization);
    var execTimeAfter = extractExecutionTime(analysisAfterOptimization);

    var isMetricsExtracted = execTimeBefore.isPresent() && execTimeAfter.isPresent();
    var isOptimizationAchieved = isMetricsExtracted && isOptimized(execTimeBefore.get(), execTimeAfter.get());
    var isOptimizationMechanismIntended = this.makeOptimizationMechanismResult(metadata);

    return new Report(isMetricsExtracted, isOptimizationAchieved, isOptimizationMechanismIntended,
      analysisBeforeOptimization, analysisAfterOptimization);
  }

  private String analyzeQuery(String query) {
    var analysisQuery = "EXPLAIN ANALYSE " + query;
    var builder = new StringBuilder();
    try (var statement = connection.createStatement();
         var resultSet = statement.executeQuery(analysisQuery)) {
      while (resultSet.next()) {
        builder.append(resultSet.getString("QUERY PLAN"));
        builder.append("\n");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return builder.toString();
  }

  private boolean makeOptimizationMechanismResult(MetadataHolder.ProblemMetadata metadata) {
    if (metadata.indexes() != null && !metadata.indexes().isEmpty()) {
      return verifyIndexes(metadata.tables(), metadata.indexes());
    }

    if (metadata.views() != null && !metadata.views().isEmpty()) {
      return verifyViews(metadata.views());
    }

    return false;
  }

  private boolean verifyIndexes(List<String> tables, List<String> indexExpressions) {
    var tablesNames = tables.stream().map(name -> "'" + name + "'").collect(Collectors.joining(","));
    String query = "SELECT indexdef FROM pg_catalog.pg_indexes WHERE tablename IN (%s);".formatted(tablesNames);
    try (var statement = connection.createStatement();
         var rs = statement.executeQuery(query)) {

      while (rs.next()) {
        String indexDef = rs.getString("indexdef");
        // Check if each metadata index expression exists in DB indexes
        for (String indexExpression : indexExpressions)
          if (indexDef.endsWith(indexExpression)) return true;
      }

      return false;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  private boolean verifyViews(List<String> viewExpressions) {
    try (Statement statement = connection.createStatement()) {
      // Retrieve all views from DB
      String query = """
        SELECT viewname, definition
        FROM pg_catalog.pg_views
        WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
        UNION
        SELECT matviewname as viewname, definition
        FROM pg_catalog.pg_matviews
        WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
        """;
      ResultSet rs = statement.executeQuery(query);

      // Retrieve the views from the DB
      while (rs.next()) {
        var viewDef = rs.getString("definition");
        // Check if each metadata view expression exists in DB views
        for (String viewExpression : viewExpressions)
          if (normalizeSql(viewDef).contains(normalizeSql(viewExpression))) return true;
      }

      // If all metadata view expressions exist, return true
      return false;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }


  private Optional<Float> extractExecutionTime(String analysisReport) {
    //TODO: perhaps capture units as well (ms, could be seconds)
    Pattern pattern = Pattern.compile("Execution Time: (\\d+\\.\\d+) ms");

    Matcher matcher = pattern.matcher(analysisReport);
    if (matcher.find()) {
      String executionTime = matcher.group(1);
      return Optional.ofNullable(executionTime).map(Float::parseFloat);
    } else {
      return Optional.empty();
    }
  }

  private boolean isOptimized(Float beforeTime, Float afterTime) {
    return afterTime < (beforeTime / 5);
  }
}
