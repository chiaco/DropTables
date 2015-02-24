# DropTables

Yet Another Java + MongoDB Web App

DropTables is built on top of DropWizard, Groovy, and Morphia.
* http://dropwizard.io/
* http://groovy.codehaus.org/
* https://github.com/mongodb/morphia/wiki

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
| GET | /scripts | N/A | Lists the IDs of Groovy scripts in the "groovy" collection. |
| POST | /scripts | N/A | Upserts a Groovy script into the "groovy" collection. |
| GET | /scripts/{id} | N/A | Fetches the specified Groovy script from the "groovy" collection. |
| DELETE | /scripts/{id} | N/A | Deletes the specified Groovy script from the "groovy" collection. |
| POST | /scripts/{id}/results | N/A | Executes the specified Groovy script and returns the results as HTML. The request body is a JSON object of key,value pairs to use as variable bindings for the script. |

# Examples

## Push a Groovy script

Create a file called script.json:
```json
{
  "name": "Print Collections",
  "author": "r-gerard",
  "groovyTemplate": "<html><head><title>Collections</title></head><body bgcolor=\"<% print bgColor %>\"><h1>Collections</h1><ul><% print COLLECTIONS %></ul></body></html>",
  "groovyScript": "// DAO is a global variable with read-only access to Mongo\nCOLLECTIONS = DAO.getCollectionNames().collect { \"<li>\" + it + \"</li>\" }.join(\"\\n\")",
  "defaultParameters": {
    "bgColor": "#FFFFFF"
  }
}
```

Upsert the file to the /scripts endpoint:
```
curl -X POST -H "Content-Type: application/json" -d @script.json "http://localhost:9000/scripts/"

{"_id":"54e8f1dbd9a93c9b467d5380"}
```

## Execute a Groovy script

Use the _id returned by the upsert:

`curl -X POST -H "Content-Type: application/json" -d "{}" "http://localhost:9000/scripts/54e8f1dbd9a93c9b467d5380/results" > results.html`

The defaultParameters specified when the script was created can be overridden by the request body:

`curl -X POST -H "Content-Type: application/json" -d "{\"bgColor\":\"#FF0000\"}" "http://localhost:9000/scripts/54e8f1dbd9a93c9b467d5380/results" > results.html`

The contents of /tmp/droptables/groovy/54e8f1dbd9a93c9b467d5380.groovy should be:

```groovy
// DAO is a global variable with read-only access to Mongo
COLLECTIONS = DAO.getCollectionNames().collect { "<li>" + it + "</li>" }.join("\n")
```
