package zetasql.java;

import java.util.Iterator;

import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedStatement;

//import org.json.simple.

public class App {

    public static void main(String[] args) {
        String projectId = null;
        String table = null;
        String statement = null;

        if (args.length < 3) {
            projectId = "bigquery-public-data";
            table = "samples.wikipedia";
            statement = "INSERT INTO `bigquery-public-data.samples.wikipedia` (title) VALUES ('random title');\n"
                    + "SELECT title, language FROM `bigquery-public-data.samples.wikipedia` WHERE title = 'random title';";
        } else {
            projectId = args[0];
            table = args[1];
            statement = args[2];
        }

        BigQueryAnalyzer analyzer = new BigQueryAnalyzer(projectId, table);
        Iterator<ResolvedStatement> statements = analyzer.analyze(statement);

        if (statements != null) {
            statements.forEachRemaining(output -> System.out.println(output.debugString()));
        }
    }
}
