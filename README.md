# DropTables

Yet Another Java + MongoDB Web App

DropTables is built on top of DropWizard and Morphia.
* http://dropwizard.io/
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
