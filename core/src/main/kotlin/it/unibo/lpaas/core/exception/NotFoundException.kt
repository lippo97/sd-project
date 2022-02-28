package it.unibo.lpaas.core.exception

class NotFoundException(id: Any, collection: String) : CoreException() {
    override val message: String = "Couldn't find id=$id in collection $collection."
}
