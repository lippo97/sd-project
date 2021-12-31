package it.unibo.lpaas.domain.databind

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.databind.impl.factDeserializer
import org.junit.jupiter.api.assertThrows
import java.io.IOException

class FactDeserializerTest : FunSpec({

    data class Test(val fact: Fact)

    val mapper = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(
            SimpleModule()
                .addDeserializer(Fact::class.java, factDeserializer)
        )
    }

    test("A fact should be read as a string") {
        val json = """
        {
          "fact": "super(mario)"
        }
        """.trimIndent()

        val read = mapper.readValue(json, Test::class.java)
        read shouldBe Test(Fact.of(Functor("super"), "mario"))
    }

    test("A rule with true on the right is considered a fact") {
        val json = """
        {
          "fact": "super(luigi) :- true"
        }
        """.trimIndent()

        val read = mapper.readValue(json, Test::class.java)
        read shouldBe Test(Fact.of(Functor("super"), "luigi"))
    }

    test("A rule should make it throw IOException") {
        val json = """
        {
          "fact": "super(mario) :- false"
        }
        """.trimIndent()

        assertThrows<IOException> {
            mapper.readValue(json, Test::class.java)
        }
    }
})
