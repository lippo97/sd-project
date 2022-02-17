db = db.getSiblingDB('lpaas')
db.token.drop();
db.token.insertMany([
  {
    "credentials": {
      "username": "abc0",
      "password": "pass"
    },
    "role": "source"
  },
  {
    "credentials": {
      "username": "abc1",
      "password": "pass"
    },
    "role": "client"
  },
  {
    "credentials": {
      "username": "abc2",
      "password": "pass"
    },
    "role": "configurator"
  }
])
