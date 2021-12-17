package it.unibo.lpaas.domain.databind

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.lpaas.domain.databind.impl.stringIdDeserializer
import it.unibo.lpaas.domain.impl.StringId

internal class StringIDDeserializerTest : FunSpec({

    data class Test(val id: StringId, val name: String)

    test("Id should be read as a string") {
        val json = """
            {
                "id": "007",
                "name": "mario"
            }
        """.trimIndent()
        val mapper = ObjectMapper().apply {
            registerModule(
                SimpleModule()
                    .addDeserializer(StringId::class.java, stringIdDeserializer)
            )
            registerKotlinModule()
        }

        val read = mapper.readValue(json, Test::class.java)
        read shouldBe Test(StringId("007"), "mario")
    }
})
