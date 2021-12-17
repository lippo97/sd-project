package it.unibo.lpaas.domain.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.unibo.lpaas.domain.Version

internal class IncrementalVersionTest : FunSpec({
    context("Version creation") {
        test("should return an object with a non-negative number") {
            Version.incremental(0) shouldNotBe null
        }
        test("should start from 0 with no parameters") {
            Version.incremental() shouldBe Version.incremental(0)
        }
        test("should return null with a negative number") {
            Version.incremental(-1) shouldBe null
        }
    }

    context("Comparisons between versions") {
        test("1 should be greater than 0") {
            Version.incremental(1)!! shouldBeGreaterThan Version.incremental(0)!!
        }
        test("0 should be equal to 0") {
            Version.incremental(0)!! shouldBeEqualComparingTo Version.incremental(0)!!
        }
        test("0 should be less than 1") {
            Version.incremental(0)!! shouldBeLessThan Version.incremental(1)!!
        }
    }
})
