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

public class BigQueryAnalyzer {

	private static final String KEYFILE = "/credentials.json";

	private String projectId = "";
	private String table = "";

	private BigQuery client = null;
	private BigQueryCatalog catalog = null;
	private ZetaSQLToolkitAnalyzer analyzer = null;

	public BigQueryAnalyzer(String projectId, String table) {
		this.projectId = projectId;
		this.table = table;

		this.client = getBigQueryClient();
		this.catalog = getBigQueryCatalog();
		this.analyzer = getAnalyzer();
	}

	private ServiceAccountCredentials loadCredentialsFromFile(String keyfilePath) {
		ServiceAccountCredentials creds = null;

		try {
			FileInputStream keyfile = new FileInputStream(keyfilePath);
			creds = ServiceAccountCredentials.fromStream(keyfile);
		} catch (FileNotFoundException fnfe) {
			System.err.println("Couldn't find key file at: " + keyfilePath);
			// System.err.println(fnfe);
		} catch (IOException ioe) {
			System.err.println("IO exception reading key file at: " + keyfilePath);
			System.err.println(ioe);
		}

		return creds;
	}

	private BigQuery getBigQueryClient() {
		String currentWorkingDirectory = System.getProperty("user.dir");
		String keyfilePath = currentWorkingDirectory + KEYFILE;

		ServiceAccountCredentials creds = loadCredentialsFromFile(keyfilePath);
		if (creds == null) {
			System.err.println("No credentials could be loaded.");
			return null;
		}

		BigQueryOptions.Builder builder = BigQueryOptions.newBuilder().setProjectId(this.projectId)
				.setCredentials(creds);
		BigQuery client = builder.build().getService();

		return client;
	}

	private BigQueryCatalog getBigQueryCatalog() {
		if (this.client == null) {
			System.err.println("No client available.");
			return null;
		}

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
		if (this.catalog == null) {
			System.err.println("No catalog could be built.");
			return null;
		}

		Iterator<ResolvedStatement> statementIterator = analyzer.analyzeStatements(statement, this.catalog);
		return statementIterator;
	}
}
