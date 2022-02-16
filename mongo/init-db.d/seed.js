db = db.getSiblingDB('lpaas')
db.token.drop();
db.token.insertMany([
  {
    "token": "abc0",
    "role": "source"
  },
  {
    "token": "abc1",
    "role": "client"
  },
  {
    "token": "abc2",
    "role": "configurator"
  }
])
