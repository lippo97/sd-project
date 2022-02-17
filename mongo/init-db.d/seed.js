db = db.getSiblingDB('lpaas')
db.user.drop();
db.user.insertMany([
  {
    "credentials": {
      "username": {
        "value": "abc0"
      },
      "password": {
        "value": "pass"
      }
    },
    "role": "source"
  },
  {
    "credentials": {
      "username": {
        "value": "abc1"
      },
      "password": {
         "value": "pass"
      }
    },
    "role": "client"
  },
  {
    "credentials": {
      "username": {
        "value": "abc2"
      },
      "password": {
         "value": "pass"
      }
    },
    "role": "configurator"
  }
])
