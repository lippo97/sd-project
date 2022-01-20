package it.unibo.lpaas.delivery.http.exception

import it.unibo.lpaas.delivery.http.databind.MimeType

class UnsupportedMediaTypeException(contentType: MimeType) : Throwable() {
    override val message: String = "Content type $contentType is not supported."
}
