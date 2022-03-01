rootProject.name = "lpaas"

enableFeaturePreview("VERSION_CATALOGS")

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
include("examples:monolith-inmemory")
include("examples:monolith-mongo")
include("examples:authentication-service")
include("examples:lpaas-service")

include("client-api")
include("client-repl")
