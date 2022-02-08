package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.Variable
import it.unibo.lpaas.domain.databind.DomainSerializationModule
import it.unibo.tuprolog.core.Struct

internal class ResultDeserializerTest : FunSpec({

    data class Test(val result: Result)

    val mapper = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(DomainSerializationModule())
        registerModule(
            SimpleModule()
                .addDeserializer(Result::class.java, ResultDeserializer())
        )
    }

    fun <T> testRoundTrip(t: T, clazz: Class<T>, verbose: Boolean = false) {
        val json = mapper.writeValueAsString(t)
        val read = mapper.readValue(json, clazz)
        if (verbose) {
            println(json)
            println(read)
        }
        read shouldBe t
    }

    context("ResultDeserializer") {
        test("it should serialize a Result.Yes") {
            val yes = Result.Yes(
                query = Struct.of("atom"),
                solvedQuery = Struct.of("atom"),
                variables = mapOf(
                    Variable("X") to Struct.of("mario"),
                    Variable("Y") to Struct.of("luigi"),
                )
            )
            testRoundTrip(Test(yes), Test::class.java)
        }
        test("it should serialize a Result.No") {
            val test = Test(
                result = Result.No(
                    query = Struct.of("not_present")
                )
            )
            testRoundTrip(test, Test::class.java)
        }
        test("it should serialize a Result.Halt") {
            val test = Test(
                result = Result.Halt(
                    query = Struct.of("error"),
                    exception = Result.Error("error body")
                )
            )
            testRoundTrip(test, Test::class.java)
        }
    }
})
