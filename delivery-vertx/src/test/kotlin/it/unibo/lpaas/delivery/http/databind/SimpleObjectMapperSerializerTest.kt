package it.unibo.lpaas.delivery.http.databind

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.lpaas.http.databind.ObjectMapperSerializer

internal class SimpleObjectMapperSerializerTest : FunSpec({

    data class Test(val a: Int, val b: String)

    context("Default ObjectMapperSerializer instances") {
        test("should serialize data structures (JSON)") {
            ObjectMapperSerializer.json()
                .apply {
                    objectMapper.registerKotlinModule()
                    objectMapper.disable(SerializationFeature.INDENT_OUTPUT)
                }
                .serializeToString(Test(30, "ciao")) shouldBe """
                    {"a":30,"b":"ciao"}
            """.trimIndent()
        }
        test("should serialize data structures (YAML)") {
            ObjectMapperSerializer.yaml()
                .apply { objectMapper.registerKotlinModule() }
                .serializeToString(Test(30, "ciao")) shouldBe """
                    ---
                    a: 30
                    b: "ciao"
                    
            """.trimIndent()
        }
        test("should serialize data structures (XML)") {
            ObjectMapperSerializer.xml()
                .apply {
                    objectMapper.registerKotlinModule()
                }
                .serializeToString(Test(30, "ciao")) shouldBe """
                    <Test><a>30</a><b>ciao</b></Test>
            """.trimIndent()
        }
    }
})
