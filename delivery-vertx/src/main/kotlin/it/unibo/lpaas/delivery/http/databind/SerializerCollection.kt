package it.unibo.lpaas.delivery.http.databind

interface SerializerCollection<out T : BufferSerializer> {

    val availableTypes: Set<MimeType>

    val availableSerializers: Collection<T>

    fun configureSerializers(fn: (T) -> Unit)

    fun serializerForMimeType(mimeType: MimeType): BufferSerializer?

    fun serializerOrDefault(mimeType: MimeType, bufferSerializer: BufferSerializer): BufferSerializer =
        serializerForMimeType(mimeType) ?: bufferSerializer

    companion object {

        @JvmStatic
        fun <T : BufferSerializer> of(serializers: Map<MimeType, T>): SerializerCollection<T> =
            SimpleSerializerCollection(serializers)

        @JvmStatic
        fun <T : BufferSerializer> of(vararg pairs: Pair<MimeType, T>): SerializerCollection<T> = of(pairs.toMap())

        @JvmStatic
        @JvmName("makeDefault")
        fun default(): SerializerCollection<ObjectMapperSerializer> =
            of(
                MimeType.JSON to ObjectMapperSerializer.json(),
                MimeType.YAML to ObjectMapperSerializer.yaml(),
                MimeType.XML to ObjectMapperSerializer.xml(),
            )
    }
}
