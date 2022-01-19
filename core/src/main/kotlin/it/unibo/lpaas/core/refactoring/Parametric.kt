package it.unibo.lpaas.core.refactoring

import it.unibo.lpaas.core.Tag
import it.unibo.lpaas.core.TheoryUseCases
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId

interface Tagged {
    val tag: Tag
        get() = Tag(this.javaClass.simpleName)
}

fun interface F0<T> : Tagged {
    suspend fun execute(): T
}

fun interface F1<A, T> : Tagged {
    suspend fun execute(a: A): T
}

fun interface F2<A, B, T> : Tagged {
    suspend fun execute(a: A, b: B): T
}

object F {
    fun <A, T>tagged(tag: Tag, fn: suspend (A) -> T): F1<A, T> = object : F1<A, T> {
        override val tag: Tag = tag

        override suspend fun execute(a: A): T = fn(a)
    }

    fun <A, B, T>tagged(tag: Tag, fn: suspend (A, B) -> T): F2<A, B, T> = object : F2<A, B, T> {
        override val tag: Tag = tag

        override suspend fun execute(a: A, b: B): T = fn(a, b)
    }
}

interface RoutingContext {
    val body: Map<String, String>

    fun <A> jsonAs(name: String, clazz: Class<A>): A

    fun fail(statusCode: Int): Unit
}

interface Route {
    val ctx: RoutingContext

    fun handler(fn: (RoutingContext) -> Unit): Route

    fun suspendHandler(fn: suspend (RoutingContext) -> Unit): Route
}


fun <T> Route.useCaseHandler(f0: F0<T>, fn: (RoutingContext) -> Unit): Route = suspendHandler { ctx ->
    fn(ctx)
    f0.execute()
}


fun <A, T> Route.useCaseHandler(f1: F1<A, T>, fn: (RoutingContext) -> A): Route = suspendHandler { ctx ->
    f1.execute(fn(ctx))
}

fun <A, B, T> Route.useCaseHandler(f2: F2<A, B, T>, fn: (RoutingContext) -> Pair<A, B>): Route =
    this
        .suspendHandler { ctx ->
            val (a, b) = fn(ctx)
            f2.execute(a, b)
        }

object CreateTheory : F2<TheoryId, Theory.Data, Theory> {

    override val tag: Tag = TheoryUseCases.Tags.createTheory

    override suspend fun execute(id: TheoryId, data: Theory.Data): Theory =
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

suspend fun main() {
    println(CreateTheory.tag)
    val createTheoryTagged = F.tagged<String, Int>(Tag("")) { it.length }
    val sum: F2<Int, Int, Int> = F.tagged(Tag("")) { a: Int, b: Int -> a + b }

    println(sum.execute(2,5))
}
