package it.unibo.lpaas.core.exception

class DuplicateIdentifierException(id: Int, collection: String) : NonFatalError() {
    override val message: String = "Identifier id=$id already present in collection $collection."
}
