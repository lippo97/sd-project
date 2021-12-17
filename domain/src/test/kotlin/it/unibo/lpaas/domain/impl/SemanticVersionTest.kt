package it.unibo.lpaas.domain.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.unibo.lpaas.domain.Version

internal class SemanticVersionTest : FunSpec({
    context("Version creation") {
        test("should return an object with non-negative numbers") {
            Version.semantic(0, 1, 0) shouldNotBe null
        }
        test("should return null with at least a negative number") {
            Version.semantic(0, -1, 0) shouldBe null
        }
    }

    context("Comparisons between versions") {
        test("1.0.0 should be greater than 0.10.0") {
            Version.semantic(1, 0, 0)!! shouldBeGreaterThan Version.semantic(0, 10, 0)!!
        }
        test("1.2.0 should be greater than 1.1.0") {
            Version.semantic(1, 2, 0)!! shouldBeGreaterThan Version.semantic(1, 1, 0)!!
        }
        test("0.1.0 should be equal to 0.1.0") {
            Version.semantic(0, 1, 0)!! shouldBeEqualComparingTo Version.semantic(0, 1, 0)!!
        }
        test("0.2.1 should be less than 0.2.3") {
            Version.semantic(0, 2, 1)!! shouldBeLessThan Version.semantic(0, 2, 3)!!
        }
    }
})
