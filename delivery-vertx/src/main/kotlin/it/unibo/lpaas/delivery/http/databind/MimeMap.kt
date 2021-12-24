package it.unibo.lpaas.delivery.http.databind

interface MimeMap<out T : BufferSerializer> {

    val availableTypes: Set<MimeType>

    fun configureSerializers(fn: (T) -> Unit)

    operator fun get(mimeType: MimeType): BufferSerializer?

    fun getOrDefault(mimeType: MimeType, bufferSerializer: BufferSerializer): BufferSerializer

    companion object {

        class SimpleMimeMap<out T : BufferSerializer>(
            private val serializers: Map<MimeType, T>
        ) : MimeMap<T> {
            override val availableTypes: Set<MimeType> = serializers.keys

            override fun configureSerializers(fn: (T) -> Unit) = serializers.values.forEach(fn)

            override fun get(mimeType: MimeType): BufferSerializer? = serializers[mimeType]

            override fun getOrDefault(mimeType: MimeType, bufferSerializer: BufferSerializer): BufferSerializer =
                serializers.getOrDefault(mimeType, bufferSerializer)
        }

        fun <T : BufferSerializer> of(serializers: Map<MimeType, T>): MimeMap<T> =
            SimpleMimeMap(serializers)

        fun <T : BufferSerializer> of(vararg pairs: Pair<MimeType, T>): MimeMap<T> = of(pairs.toMap())
    }
}
