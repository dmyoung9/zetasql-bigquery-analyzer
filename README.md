# zetasql-bigquery-analyzer

This project aims to act as a "wrapper" around Google's [ZetaSQL](https://github.com/Google/zetasql) library, for the purpose of generating AST diagrams out of arbitrary BigQuery operations.


Setup
---

Building this app has only been tested on [Ubuntu 22.04.1 LTS](http://old-releases.ubuntu.com/releases/22.04.1/), but should be compatible with any newer release.

### Requirements
The versions specified below are what was used to build the project, and are the only versions that have been tested against.

* [Gradle 6.6.1](https://gradle.org/releases/)
* [Maven 3.9.1](https://maven.apache.org/download.cgi)
* [Python 3.11.0](https://www.python.org/downloads/release/python-3110/)
* [Poetry 1.4.2](https://python-poetry.org/docs/)

### Gradle and Maven Build
The Gradle and Maven build systems is used to build the Java portion of this app, and its dependencies.

To build this portion of the app, execute the following command from the root folder of this project:
```bash
$ gradle build
```

This will build the `zeta-toolkit-core` folder with Maven, the `src` folder using Gradle, and all of their dependencies.

### Python Environment
This project leverages Poetry for Python dependency management.

To install the required dependencies for the Python portion, execute the following command from the root folder of this project:
```bash
$ poetry install --without=dev
```

### Authentication
This app uses the Google BigQuery API, and thus requires service account credentials for a Google Cloud project with the BigQuery API enabled.

If you already have a service account with the BigQuery API enabled, skip to [step 8](#step8).

The [documentation regarding these credentials](https://cloud.google.com/iam/docs/service-account-overview) is out of the scope of this project, but a quick overview of the steps is as follows:
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create a new project.
3. Go to [Enabled APIs & Services](https://console.cloud.google.com/apis), and choose `Enable APIs and Services`.
4. Search for `BigQuery API`, choose it, and choose `Enable`.
5. Go to [Credentials](https://console.cloud.google.com/apis/credentials).
6. Choose `Create Credentials`, and then `Service Account`.
7. Give this service account a unique name and ID, and then choose `Done`. Changing optional access scopes is not required for this project.
<a name="step8"></a>
8. Back on [Credentials](https://console.cloud.google.com/apis/credentials), click on the newly created service account (it will be named `[service account ID]@[project ID].iam.gserviceaccount.com`).
9. Choose `Keys`, `Add Key`, `Create new Key`, and then `JSON`.
10. Save the newly created file somewhere **secure** on your computer. It **cannot** be recovered if lost.
11. Copy the file from step 10 into the root folder of this project, and name it `credentials.json`.

This file will be used by the app to authenticate with your service account. Always ensure that your credentials are protected in a production environment.


Usage
---

To run this app, simply start the Flask backend via:
```bash
$ python ./main.py
```

While running, the backend serves a single endpoint: `/gradle`. Sending a `POST` request with the following body data will trigger the app to generate an AST diagram, and output it to a file.

| Parameter | Description | Example Value |
|-----------|-------------|---------------|
| `projectId` | The project ID of your Google Cloud project. | bigquery-public-data |
| `table` | The BigQuery table to be analyzed. | samples.wikipedia |
| `statement` | An SQL statement to be analyzed. | INSERT INTO `bigquery-public-data.samples.wikipedia` (title) VALUES ('random title'); |

A complete request looks like:
```bash
$ curl \
    --location
    --request POST '127.0.0.1:5000/gradle' \
    --header 'Content-Type: application/json' \
    --data-raw '{
        "projectId": "bigquery-public-data",
        "table": "samples.wikipedia",
        "statement": "INSERT INTO `bigquery-public-data.samples.wikipedia` (title) VALUES (\'random title\');"
    }'
```

