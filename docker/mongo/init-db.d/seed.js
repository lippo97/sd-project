/* The hashed password is "password" */
db = db.getSiblingDB('lpaas')
db.user.drop();
db.user.insertMany([
  {
    "credentials": {
      "username": {
        "value": "abc0"
      },
      "password": {
        "value": "$2a$06$qSJV0tCzfA3as6Y1NwCqS.S/vxrbKc8ZrGguZ3W6yYRLZsUav/jz6"
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
        "value": "$2a$06$qSJV0tCzfA3as6Y1NwCqS.S/vxrbKc8ZrGguZ3W6yYRLZsUav/jz6"
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
        "value": "$2a$06$qSJV0tCzfA3as6Y1NwCqS.S/vxrbKc8ZrGguZ3W6yYRLZsUav/jz6"
      }
    },
    "role": "configurator"
  }
])
