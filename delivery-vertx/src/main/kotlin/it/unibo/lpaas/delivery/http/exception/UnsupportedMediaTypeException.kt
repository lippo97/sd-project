package it.unibo.lpaas.delivery.http.exception

import it.unibo.lpaas.http.databind.MimeType

class UnsupportedMediaTypeException(contentType: MimeType) : DeliveryException() {
    override val message: String = "Content type $contentType is not supported."
}
