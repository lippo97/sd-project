rootProject.name = "lpaas"
include("utils")
include("domain")
include("serialization")
include("core")
include("authorization")
include("delivery-vertx")
include("serialization-vertx")
include("authentication-vertx")
include("persistence-inmemory")
include("persistence-mongo")
include("test-lib-vertx")
include("test-fixtures-domain")
include("persistence-mongo")
include("examples:main-vertx-inmemory")
include("examples:main-vertx-mongo")
include("examples:authentication-service")
include("examples:lpaas-service")

include("client-api")
include("client-repl")
