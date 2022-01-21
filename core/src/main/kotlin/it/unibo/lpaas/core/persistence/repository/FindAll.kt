package it.unibo.lpaas.core.persistence.repository

interface FindAll<Resource> {
    suspend fun findAll(): List<Resource>
}
