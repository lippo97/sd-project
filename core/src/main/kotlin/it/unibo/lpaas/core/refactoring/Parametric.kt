package it.unibo.lpaas.core.refactoring

import it.unibo.lpaas.core.Tag
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId

interface Tagged {
    val tag: Tag
        get() = Tag(this.javaClass.simpleName)
}

fun interface F0<T> : Tagged {
    fun value(): T
}

fun interface F1<A, T> : Tagged {
    fun value(a: A): T
}

fun interface F2<A, B, T> : Tagged {
    fun value(a: A, b: B): T
}


interface RoutingContext {
    val body: Map<String, String>

    fun <A> jsonAs(name: String, clazz: Class<A>): A
}

interface Route {
    val ctx: RoutingContext

    fun handler(fn: (RoutingContext) -> Unit): Route
}


fun <T> Route.useCaseHandler(f0: F0<T>, fn: (RoutingContext) -> Unit): Route = handler { ctx ->
    fn(ctx)
    f0.value()
}


fun <A, T> Route.useCaseHandler(f1: F1<A, T>, fn: (RoutingContext) -> A): Route = handler { ctx ->
    f1.value(fn(ctx))
}

fun <A, B, T> Route.useCaseHandler(f2: F2<A, B, T>, fn: (RoutingContext) -> Pair<A, B>): Route =
    this
        .handler {
            f2.tag
        }
        .handler { ctx ->
            val (a, b) = fn(ctx)
            f2.value(a, b)
        }

//val createTheory: F2<TheoryId, Theory.Data, Theory> = F2 { id, data ->
//    Theory(id, data, version = IncrementalVersion.zero)
//}

object CreateTheory : F2<TheoryId, Theory.Data, Theory> {
    override fun value(id: TheoryId, data: Theory.Data): Theory =
        Theory(id, data, version = IncrementalVersion.zero)

}

fun makeController(route: Route) {

    route
        .useCaseHandler(CreateTheory) { ctx ->
            val id = ctx.jsonAs("name", TheoryId::class.java)
            val data = ctx.jsonAs("data", Theory.Data::class.java)
            Pair(id, data)
        }
}

fun main() {
    println(CreateTheory.tag)
}
