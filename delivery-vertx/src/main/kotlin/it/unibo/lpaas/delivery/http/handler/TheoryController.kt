package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.TheoryDependencies
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.delivery.http.databind.MimeMap

interface TheoryController : Controller {

    companion object {

        @JvmStatic
        fun make(
            vertx: Vertx,
            theoryDependencies: TheoryDependencies,
            mimeMap: MimeMap<BufferSerializer>,
            authOptions: Controller.AuthOptions,
        ): TheoryController = object : TheoryController {
            override fun routes(): Router = Router.router(vertx)
        }
    }
}
