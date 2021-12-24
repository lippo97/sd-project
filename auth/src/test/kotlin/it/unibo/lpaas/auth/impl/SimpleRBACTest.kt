package it.unibo.lpaas.auth.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.core.Tag

internal class SimpleRBACTest : FunSpec({
    val rbac = SimpleRBAC()
    val tag = Tag("a permission")
    test("By default no operation should be authorized") {
        Role.values().forEach {
            rbac.isAuthorized(it, tag) shouldBe false
        }
    }

    context("When a permission is added") {
        rbac.addPermission(Role.CLIENT, tag)

        test("the role should be authorized") {
            rbac.isAuthorized(Role.CLIENT, tag) shouldBe true
        }
    }

    context("When a permission is removed") {
        rbac.removePermission(Role.CLIENT, tag)

        test("the role should be no more authorized") {
            rbac.isAuthorized(Role.CLIENT, tag) shouldBe false
        }
    }

    test("It should return all the authorized roles") {
        SimpleRBAC().apply {
            Role.values().forEach {
                addPermission(it, tag)
            }
            authorizedRoles(tag) shouldContainExactlyInAnyOrder Role.values().toList()
        }
    }
})
