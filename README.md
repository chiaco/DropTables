# DropTables

Yet Another Java + MongoDB Web App

DropTables is built on top of DropWizard, Groovy, and Morphia.
* http://dropwizard.io/
* http://groovy.codehaus.org/
* https://github.com/mongodb/morphia/wiki


# About Report Generators

Report Generators are Mongo documents that can be executed by DropTables to query the database and generate an HTML-formatted response containing query results.

A report generator is made of three main components: a map of default parameters, a Groovy script, and a Groovy template.
Parameters are fed into a script engine to interpret the Groovy script which in turn can query Mongo to generate one or more variable bindings and execute business logic.
Those final variable bindings are fed into a templating engine to interpret the Groovy template and generate the final HTML response.

The order of precedence of variable bindings is a hierarchy, where 0 represents the lowest precedence:

| Precedence | Binding | Details |
| ---------- | ------- | ------- |
| 0 | Default Parameters | The default parameters map in the report generator object can be overridden by anything. |
| 1 | Request Parameters | Parameters in the HTTP request body can override the default parameters. |
| 2 | Groovy Script Runtime Environment | The Groovy script engine modifies the variable bindings generated as part of the request. |
| 3 | Groovy Template Runtime Environment | The Groovy template engine interprets the set of bindings modified by the script engine. |


# Compiling

`mvn clean package`

# Running

`java -jar service/target/droptables-service.jar server droptables.json`


# APIs

| HTTP Method | Route | Query Parameters | Description |
| ----------- | ----- | ---------------- | ----------- |
| GET | /collections | N/A | Lists collection names in the database. |
| GET | /collections/{collection} | N/A | Lists IDs of documents in the collection. |
| POST | /collections/{collection}/documents | N/A | Upserts a document into the collection. |
| GET | /collections/{collection}/documents/{id} | N/A | Fetches the specified document from the collection. |
| DELETE | /collections/{collection}/documents/{id} | N/A | Deletes the specified document from the collection. |
| GET | /reports | N/A | Lists the IDs of report generators scripts in the "droptables.reports" collection. |
| POST | /reports | N/A | Upserts a report generator into the "droptables.reports" collection. |
| GET | /reports/{id} | N/A | Fetches the specified report generator from the "droptables.reports" collection. |
| DELETE | /reports/{id} | N/A | Deletes the specified report generator from the "droptables.reports" collection. |
| POST | /reports/{id}/results | N/A | Executes the specified Groovy script and returns the results as HTML. The request body is a JSON object of key,value pairs to use as variable bindings for the script. |


# Examples

## Push a Report Generator

Create a file called report.json:
```json
{
  "name": "Print Collections",
  "description": "Queries the database and prints a list of collections",
  "author": "r-gerard",
  "language": "GROOVY",
  "template": "<html><head><title>Collections</title></head><body bgcolor=\"<% print bgColor %>\"><h1>Collections</h1><ul><% print COLLECTIONS %></ul></body></html>",
  "script": "// DAO is a global variable with read-only access to Mongo\nCOLLECTIONS = DAO.getCollectionNames().collect { \"<li>\" + it + \"</li>\" }.join(\"\\n\")",
  "defaultParameters": {
    "bgColor": "#FFFFFF"
  }
}
```

Upsert the file to the /reports endpoint:
```
curl -X POST -H "Content-Type: application/json" -d @report.json "http://localhost:9000/reports/"

{"_id":"54e8f1dbd9a93c9b467d5380"}
```

## Execute a Report Generator

Use the _id returned by the upsert:

`curl -X POST -H "Content-Type: application/json" -d "{}" "http://localhost:9000/reports/54e8f1dbd9a93c9b467d5380/results" > results.html`

The defaultParameters specified when the report generator was created can be overridden by the request body:

`curl -X POST -H "Content-Type: application/json" -d "{\"bgColor\":\"#FF0000\"}" "http://localhost:9000/reports/54e8f1dbd9a93c9b467d5380/results" > results.html`

The contents of /tmp/droptables/groovy/54e8f1dbd9a93c9b467d5380.groovy should be:

```groovy
// DAO is a global variable with read-only access to Mongo
COLLECTIONS = DAO.getCollectionNames().collect { "<li>" + it + "</li>" }.join("\n")
```

# Configuring DropTables

DropTables is designed to connect to Mongo using role-based authorization with the following users:
* serviceUser: A user account to manage collections in the DB. (required)
* reportsUser: A user account used by report generators to query Mongo. (optional)

```json
{
  "mongo": {
    "dbName": "test",
    "host": "localhost",
    "port": 27017,
    "serviceUser": {
      "username": "droptables-user",
      "password": "changeme"
    },
    "reportsUser": {
      "username": "readonly-user",
      "password": "changeme"
    }
  }
}
```

If a reportsUser is not specified then the report generators will use the same connection information as the DropTables serviceUser.

```json
{
  "mongo": {
    "dbName": "test",
    "host": "localhost",
    "port": 27017,
    "serviceUser": {
      "username": "droptables-and-reports-user",
      "password": "changeme"
    }
  }
}
```

At a minimum, the serviceUser should have write-access to collections in the DB prefixed with `droptables` so that DropTables can create, delete, and modify report generators.
The reportsUser can have read-only access to collections in the DB to prevent Groovy scripts from modifying data.
