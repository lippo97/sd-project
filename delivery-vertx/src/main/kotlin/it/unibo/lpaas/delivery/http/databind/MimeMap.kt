package it.unibo.lpaas.delivery.http.databind

interface MimeMap<out T : BufferSerializer> {

    val availableTypes: Set<MimeType>

    val availableSerializers: Collection<T>

    fun configureSerializers(fn: (T) -> Unit)

    fun serializerForMimeType(mimeType: MimeType): BufferSerializer?

    fun serializerOrDefault(mimeType: MimeType, bufferSerializer: BufferSerializer): BufferSerializer =
        serializerForMimeType(mimeType) ?: bufferSerializer

    companion object {

        @JvmStatic
        fun <T : BufferSerializer> of(serializers: Map<MimeType, T>): MimeMap<T> =
            SimpleMimeMap(serializers)

        @JvmStatic
        fun <T : BufferSerializer> of(vararg pairs: Pair<MimeType, T>): MimeMap<T> = of(pairs.toMap())

        @JvmStatic
        @JvmName("makeDefault")
        fun default(): MimeMap<ObjectMapperSerializer> =
            of(
                MimeType.JSON to ObjectMapperSerializer.json(),
                MimeType.YAML to ObjectMapperSerializer.yaml(),
                MimeType.XML to ObjectMapperSerializer.xml(),
            )
    }
}
