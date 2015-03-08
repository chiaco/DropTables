var NAMES = ["luke", "leia", "han", "chewie", "lando"]
var ENVS = ["dev", "qa", "prod"]
var LAST_TIME = 1000000000000 // ISODate("2001-09-09T01:46:40Z")

// Generates a psuedo-random data point
function createTaskSet(seed) {
  var taskSet = {
    name: "Task Set #" + seed,
    description: "Deployment Task Set",
    status: (seed % ENVS.length == ENVS.length -1 ? "PASS" : "FAIL"),
    startTime: 0,
    endTime: 0,
    tasks: [],
    metadata: {}
  }

  // 86400000 ms = 1 day
  LAST_TIME += 86400000
  for (var i=0; i<=(seed % ENVS.length); i++) {
    // 60000 ms = 1 minute
    startTime = LAST_TIME
    endTime = startTime + 60000 * (i + 1) + 30000 * (seed % 10)
    var task = {
      name: "Task #" + seed + "." + i,
      description: "Deploy to " + ENVS[i],
      status: "PASS",
      startTime: new Date(startTime),
      endTime: new Date(endTime),
      metadata: {
        user: NAMES[((seed + i) % (NAMES.length +2)) % NAMES.length],
        environment: ENVS[i]
      }
    }

    taskSet.tasks.push(task)
  }

  taskSet.tasks[taskSet.tasks.length -1].status = taskSet.status
  taskSet.startTime = taskSet.tasks[0].startTime
  taskSet.endTime = taskSet.tasks[taskSet.tasks.length -1].endTime

  return taskSet
}

db.getSiblingDB("test")
for (var i=1; i<=100; i++) {
  var taskSet = createTaskSet(i)
  db.taskSets.insert(taskSet)
}
