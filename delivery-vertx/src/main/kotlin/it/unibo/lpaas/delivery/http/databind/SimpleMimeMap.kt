package it.unibo.lpaas.delivery.http.databind

class SimpleMimeMap<out T : BufferSerializer>(
    private val serializers: Map<MimeType, T>,
) : MimeMap<T> {
    override val availableTypes: Set<MimeType> = serializers.keys

    override val availableSerializers: Collection<T> = serializers.values

    override fun configureSerializers(fn: (T) -> Unit) = serializers.values.forEach(fn)

    override fun serializerForMimeType(mimeType: MimeType): BufferSerializer? = serializers[mimeType]
}
