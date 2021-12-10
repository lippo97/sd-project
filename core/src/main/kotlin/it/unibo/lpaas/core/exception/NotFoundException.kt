package it.unibo.lpaas.core.exception

class NotFoundException(id: Int, collection: String) : NonFatalError() {
    override val message: String = "Couldn't find id=$id in collection $collection."
}
