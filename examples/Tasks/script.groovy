import com.mongodb.*

USERS = new HashMap()
DURATIONS = new TreeMap()
ENVIRONMENTS = new LinkedHashMap()
TIMESTAMPS = []

// Find all documents in the collection where the TaskSet's startTime is in the range [queryStartTime, queryEndTime]
// Assume all documents in the collection have a valid schema
query = new BasicDBObject('startTime', new BasicDBObject('$gte', new Date(queryStartTime)).append('$lte', new Date(queryEndTime)));
dbCollection = DAO.getDatastore().getDB().getCollection(collection)
dbCursor = dbCollection.find(query)
while (dbCursor.hasNext()) {
  dbObject = dbCursor.next()

  startTime = dbObject.get('startTime')
  endTime = dbObject.get('endTime')
  duration = (Long) (endTime.getTime() - startTime.getTime()) / 1000
  if (!DURATIONS.containsKey(duration)) {
    DURATIONS[duration] = 0
  }
  DURATIONS[duration]++
  TIMESTAMPS.add(['startTime':startTime, 'duration':duration, 'numSubTasks':dbObject.get('tasks').size(), 'status':dbObject.get('status')])

  for (task in dbObject.get('tasks')) {
    // A metadata map is required by the schema, but it may be empty or incomplete
    status = task.get('status')
    metadata = task.get('metadata').toMap()

    if (metadata.containsKey('user')) {
      user = metadata.get('user')
      if (!USERS.containsKey(user)) {
        USERS[user] = ['TOTAL':0, 'PASS':0, 'FAIL':0]
      }
      USERS[user]['TOTAL']++
      USERS[user][status]++
    }

    if (metadata.containsKey('environment')) {
      env = metadata.get('environment')
      if (!ENVIRONMENTS.containsKey(env)) {
        ENVIRONMENTS[env] = ['PASS':0, 'FAIL':0]
      }
      ENVIRONMENTS[env][status]++
    }

    if (user != null && env != null) {
      if (!USERS[user].containsKey(env + '_PASS')) {
        USERS[user][env + '_PASS'] = 0
        USERS[user][env + '_FAIL'] = 0
      }

      USERS[user][env + '_' + status]++
    }
  }

  // Fill in any missing gaps in the {user x env} matrix to avoid NPEs in the template
  USERS.keySet().each { user->
    ENVIRONMENTS.keySet().each { env->
      if (!USERS[user].containsKey(env + '_PASS')) {
        USERS[user][env + '_PASS'] = 0
        USERS[user][env + '_FAIL'] = 0
      }
    }
  }
}
