package it.unibo.lpaas

import com.fasterxml.jackson.databind.module.SimpleModule
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId

fun SimpleModule.addDomainTypeMappings(): SimpleModule = apply {
    addAbstractTypeMapping(IncrementalVersion::class.java, IntegerIncrementalVersion::class.java)
    addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
    addAbstractTypeMapping(TheoryId::class.java, StringId::class.java)
    addAbstractTypeMapping(SolutionId::class.java, StringId::class.java)
}
