package it.unibo.lpaas.core.exception

class DuplicateIdentifierException(id: Any, collection: String) : CoreException() {
    override val message: String = "Identifier id=$id already present in collection $collection."
}
