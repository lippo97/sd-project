package it.unibo.lpaas.client.repl.console

import io.vertx.core.Future
import it.unibo.lpaas.client.repl.Formatter
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.parsing.ParseException
import it.unibo.tuprolog.core.parsing.parse

abstract class EnhancedConsole(
    private val parseExceptionFormatter: Formatter<ParseException>
) : Console {
    private fun shellReadLine(): Future<String> =
        putStr("?- ")
            .flatMap { readLine() }
            .flatMap { if (it == "") shellReadLine() else Future.succeededFuture(it) }

    protected fun shellReadStruct(): Future<Struct> =
        shellReadLine()
            .flatMap {
                runCatching {
                    Struct.parse(it)
                }
                    .map { Future.succeededFuture(it) }
                    .recover {
                        if (it is ParseException)
                            putStrLn(parseExceptionFormatter.format(it))
                                .flatMap { shellReadStruct() }
                        else
                            throw it
                    }
                    .getOrThrow()
            }
}
