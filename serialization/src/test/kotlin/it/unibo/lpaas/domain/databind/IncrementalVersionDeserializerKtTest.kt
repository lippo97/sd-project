package it.unibo.lpaas.domain.databind

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.lpaas.domain.databind.impl.incrementalVersionDeserializer
import it.unibo.lpaas.domain.impl.IncrementalVersion

internal class IncrementalVersionDeserializerKtTest : FunSpec({

    data class Test(val version: IncrementalVersion, val name: String)

    test("Id should be read as a string") {
        val json = """
            {
                "version": "10",
                "name": "mario"
            }
        """.trimIndent()
        val mapper = ObjectMapper().apply {
            registerModule(
                SimpleModule()
                    .addDeserializer(IncrementalVersion::class.java, incrementalVersionDeserializer)
            )
            registerKotlinModule()
        }

        val read = mapper.readValue(json, Test::class.java)
        read shouldBe Test(IncrementalVersion.of(10)!!, "mario")
    }
})
