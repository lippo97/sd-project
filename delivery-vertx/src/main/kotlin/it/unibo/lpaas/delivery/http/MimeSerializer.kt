package it.unibo.lpaas.delivery.http

import it.unibo.lpaas.delivery.http.databind.BufferSerializer

interface MimeSerializer<out T : BufferSerializer> {

    val availableTypes: Set<MimeType>

    fun configureSerializers(fn: (T) -> Unit)

    operator fun get(mimeType: MimeType): BufferSerializer?

    fun getOrDefault(mimeType: MimeType, bufferSerializer: BufferSerializer): BufferSerializer

    companion object {

        class SimpleMimeSerializer<out T : BufferSerializer>(
            private val serializers: Map<MimeType, T>
        ) : MimeSerializer<T> {
            override val availableTypes: Set<MimeType> = serializers.keys

            override fun configureSerializers(fn: (T) -> Unit) = serializers.values.forEach(fn)

            override fun get(mimeType: MimeType): BufferSerializer? = serializers[mimeType]

            override fun getOrDefault(mimeType: MimeType, bufferSerializer: BufferSerializer): BufferSerializer =
                serializers.getOrDefault(mimeType, bufferSerializer)

        }

        fun <T : BufferSerializer>of(serializers: Map<MimeType, T>): MimeSerializer<T> =
            SimpleMimeSerializer(serializers)
    }
}
