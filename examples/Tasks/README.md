# About

A TaskSet is a set of tasks performed in one or more environments (dev, qa, prod, etc.).

For each environment, the task can be performed if and only if each preceding task in the TaskSet was successful.

# TaskSet Schema

A TaskSet document consists of a name, description, status, start & end dates, an array of one or more tasks, and a metadata hash.

Each task similarly has a name, description, status, start & end dates, and a metadata hash.


```json
{
  "_id": "54fab9fa9ccf79f3f0c929aa",
  "name": "Task Set #50",
  "description": "Deployment Task Set",
  "status": "PASS",
  "startTime": "2001-10-29T01:46:40Z",
  "endTime": "2001-10-29T01:49:40Z",
  "tasks": [
    {
      "name": "Task #50.0",
      "description": "Deploy to dev",
      "status": "PASS",
      "startTime": "2001-10-29T01:46:40Z",
      "endTime": "2001-10-29T01:47:40Z",
      "metadata": {
        "user": "leia",
        "environment": "dev"
      }
    },
    {
      "name": "Task #50.1",
      "description": "Deploy to qa",
      "status": "PASS",
      "startTime": "2001-10-29T01:46:40Z",
      "endTime": "2001-10-29T01:48:40Z",
      "metadata": {
        "user": "han",
        "environment": "qa"
      }
    },
    {
      "name": "Task #50.2",
      "description": "Deploy to prod",
      "status": "PASS",
      "startTime": "2001-10-29T01:46:40Z",
      "endTime": "2001-10-29T01:49:40Z",
      "metadata": {
        "user": "chewie",
        "environment": "prod"
      }
    }
  ],
  "metadata": {}
}
```

# Usage

## Generate the test data

```shell
mongo
use test
load("testData.js")
db.taskSets.find()
```

## Generate the report

First, upload the report generator:

```shell
curl -X POST -H "Content-Type: application/json" -d @report.json "http://localhost:9000/scripts/"
```

Now, execute it:

```shell
curl -X POST -H "Content-Type: application/json" -d "{}" "http://localhost:9000/scripts/54f9262fb0db29cfff711b99/results" > results.html
```

The time interval is configurable:

```shell
curl -X POST -H "Content-Type: application/json" -d "{\"queryStartTime\":\"Oct 30, 2001\", \"queryEndTime\":\"Oct 31, 2001\"}" "http://localhost:9000/scripts/54f9262fb0db29cfff711b99/results" > results.html
```

# Implementation details

```json
{
  "_id": "54f9262fb0db29cfff711b99",
  "name": "TaskSet Report",
  "description": "Generates descriptive statistics for TaskSet documents found within a specified date range",
  "author": "r-gerard",
  "groovyTemplate": "// omitted for brevity",
  "groovyScript": "// omitted for brevity",
  "defaultParameters": {
    "collection": "taskSets",
    "queryStartTime": "Sep 8, 2001",
    "queryEndTime": "Dec 18, 2001"
  }
}
```

Refer to `template.groovy` and `script.groovy` for the content of the groovyTemplate and groovyScript fields.
