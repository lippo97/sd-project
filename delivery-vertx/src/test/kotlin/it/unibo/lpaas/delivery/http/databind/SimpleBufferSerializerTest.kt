package it.unibo.lpaas.delivery.http.databind

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vertx.core.buffer.Buffer
import it.unibo.lpaas.domain.databind.Serializer
import java.io.StringWriter

internal class SimpleBufferSerializerTest : FunSpec({
    data class Inner(val a: Int, val b: String)

    data class Outer(val inner: Inner, val other: String)

    val objectMapper = ObjectMapper().apply {
        registerKotlinModule()
    }

    val serializer = BufferSerializer.of(objectMapper.writer())

    val plainObject = Inner(30, "hello")
    val nestedObject = Outer(
        Inner(10, "ciao"),
        "goodbye",
    )

    context("When serializing a plain data structure") {
        test("it should use the provided mapper") {
            val serialized = serializer.serializeToString(plainObject)
            val testValue = StringWriter().let {
                objectMapper.writeValue(it, plainObject)
                it.buffer.toString()
            }

            serialized shouldBe testValue
        }

        test("it should serialize to buffers too") {
            val serialized = serializer.serializeToBuffer(plainObject)
            val testValue = StringWriter().let {
                objectMapper.writeValue(it, plainObject)
                Buffer.buffer(it.buffer.toString())
            }

            serialized shouldBe testValue
        }
    }

    context("When serializing a nested data structure") {
        test("it should use the provided mapper") {
            val serialized = serializer.serializeToString(nestedObject)
            val testValue = StringWriter().let {
                objectMapper.writeValue(it, nestedObject)
                it.buffer.toString()
            }

            serialized shouldBe testValue
        }

        test("it should serialize to buffers too") {
            val serialized = serializer.serializeToBuffer(nestedObject)
            val testValue = StringWriter().let {
                objectMapper.writeValue(it, nestedObject)
                Buffer.buffer(it.buffer.toString())
            }

            serialized shouldBe testValue
        }
    }

    context("When using a YAML mapper") {
        test("it should produce YAML formatted strings") {
            Serializer.of(ObjectMapper(YAMLFactory()).enable(SerializationFeature.INDENT_OUTPUT).writer()).apply {
                serializeToString(Inner(0, "ciao")) shouldBe """
                    ---
                    a: 0
                    b: "ciao"
                    
                """.trimIndent()
            }
        }
    }
})
