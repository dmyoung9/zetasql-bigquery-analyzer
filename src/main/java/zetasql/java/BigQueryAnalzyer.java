package zetasql.java;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.zetasql.AnalyzerOptions;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedStatement;
import com.google.zetasql.toolkit.ZetaSQLToolkitAnalyzer;
import com.google.zetasql.toolkit.catalog.bigquery.BigQueryCatalog;
import com.google.zetasql.toolkit.options.BigQueryLanguageOptions;

public class BigQueryAnalzyer {

	private static final String KEYFILE = "credentials.json";

	private String projectId = "";
	private String table = "";

	private BigQuery client = null;
	private BigQueryCatalog catalog = null;
	private ZetaSQLToolkitAnalyzer analyzer = null;

	public BigQueryAnalzyer(String projectId, String table) {
		this.projectId = projectId;
		this.table = table;

		this.client = getBigQueryClient();
		this.catalog = getBigQueryCatalog();
		this.analyzer = getAnalyzer();
	}

	private BigQuery getBigQueryClient() {
		BigQuery client = null;

		try {
			client = BigQueryOptions.newBuilder()
					.setProjectId(this.projectId)
					.setCredentials(ServiceAccountCredentials
							.fromStream(new FileInputStream(KEYFILE)))
					.build()
					.getService();
		} catch (FileNotFoundException fnfe) {
			System.out.println("Couldn't find key file");
		} catch (IOException ioe) {
			System.out.println("IO exception!");
		}

		return client;
	}

	private BigQueryCatalog getBigQueryCatalog() {
		BigQueryCatalog catalog = new BigQueryCatalog(this.projectId, this.client);
		catalog.addTable(this.projectId + '.' + this.table);

		return catalog;
	}

	private ZetaSQLToolkitAnalyzer getAnalyzer() {
		AnalyzerOptions options = new AnalyzerOptions();
		options.setLanguageOptions(BigQueryLanguageOptions.get());

		ZetaSQLToolkitAnalyzer analyzer = new ZetaSQLToolkitAnalyzer(options);
		return analyzer;
	}

	public Iterator<ResolvedStatement> analyze(String statement) {
		Iterator<ResolvedStatement> statementIterator = analyzer.analyzeStatements(statement, this.catalog);
		return statementIterator;
	}
}

