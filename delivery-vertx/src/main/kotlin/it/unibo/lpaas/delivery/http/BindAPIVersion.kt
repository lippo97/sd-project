package it.unibo.lpaas.delivery.http

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router

fun HttpServer.bindAPIVersion(apiVersion: Int, controller: Controller, vertx: Vertx): HttpServer =
    requestHandler(
        Router.router(vertx).apply {
            mountSubRouter("/v$apiVersion", controller.routes())
        }
    )

fun Router.bindApi(apiVersion: Int, controller: Controller) {
    mountSubRouter("/v$apiVersion", controller.routes())
}
