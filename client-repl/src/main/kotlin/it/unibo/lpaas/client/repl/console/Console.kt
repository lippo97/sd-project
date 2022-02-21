package it.unibo.lpaas.client.repl.console

import io.vertx.core.Future
import io.vertx.core.WorkerExecutor
import java.io.InputStream
import java.io.PrintStream
import java.util.Scanner

interface Console {

    fun putStr(out: String): Future<Void>

    fun putStrLn(out: String): Future<Void> = putStr(out + "\n")

    fun putErr(err: String): Future<Void>

    fun putErrLn(err: String): Future<Void> = putErr(err + "\n")

    fun readLine(): Future<String>

    companion object {
        fun standard(workerExecutor: WorkerExecutor): Console =
            of(workerExecutor, System.`in`, System.out, System.err)

        fun of(
            workerExecutor: WorkerExecutor,
            inputStream: InputStream,
            outputStream: PrintStream,
            errorStream: PrintStream,
        ): Console = object : Console {

            val scanner = Scanner(inputStream)

            override fun putStr(out: String): Future<Void> = workerExecutor.executeBlocking {
                outputStream.print(out)
                it.complete()
            }

            override fun putErr(err: String): Future<Void> = workerExecutor.executeBlocking {
                errorStream.print(err)
                it.complete()
            }

            override fun readLine(): Future<String> = workerExecutor.executeBlocking {
                it.complete(scanner.nextLine())
            }
        }
    }
}
