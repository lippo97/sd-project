package it.unibo.lpaas.domain.parse

import it.unibo.lpaas.domain.NonFatalError

class ParseException(obj: Any, type: String) : NonFatalError() {
    override val message: String = "Couldn't parse $obj to $type."
}
