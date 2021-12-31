package it.unibo.lpaas.persistence

import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.Repository

class BaseMemoryRepository<Id, Value, Resource>(
    private var memory: Map<Id, Value>,
    private val resourceName: String,
    private val make: (id: Id, value: Value) -> Resource,
) : Repository<Id, Value, Resource>
    where Id : Any, Value : Any, Resource : Any {

    private fun makeEntry(tuple: Map.Entry<Id, Value>): Resource =
        make(tuple.key, tuple.value)

    override suspend fun findAll(): List<Resource> =
        memory.entries.map(::makeEntry)

    override suspend fun findByName(name: Id): Resource = make(
        name,
        memory.getOrElse(name) { throw NotFoundException(name, resourceName) }
    )

    override suspend fun create(name: Id, data: Value): Resource {
        if (memory.containsKey(name)) throw DuplicateIdentifierException(name, resourceName)
        memory = memory + (name to data)
        return make(name, data)
    }

    override suspend fun updateByName(name: Id, data: Value): Resource {
        if (!memory.containsKey(name)) throw NotFoundException(name, resourceName)
        memory = memory.toMutableMap().apply {
            this[name] = data
        }
        return make(name, data)
    }

    override suspend fun deleteByName(name: Id): Resource {
        return findByName(name).also {
            memory = memory - name
        }
    }
}
