package it.unibo.lpass.authentication.bcrypt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.be
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import it.unibo.lpaas.authentication.bcrypt.BCrypt

internal class BCryptTest : FunSpec({
    context("BCrypt test") {
        val vertx = Vertx.vertx()
        val bcrypt = BCrypt.vertx(vertx, cost = 4)
        context("When a password is hashed") {
            test("it should succeed") {
                bcrypt.hash("password").await() shouldNot be(null)
            }
            test("it should match the original password") {
                bcrypt.hash("password")
                    .flatMap { bcrypt.verify("password", it) }
                    .await() shouldBe true
            }
            test("it shouldn't match other passwords") {
                bcrypt.hash("password")
                    .flatMap { bcrypt.verify("any other password", it) }
                    .await() shouldBe false
            }
        }
    }
})
