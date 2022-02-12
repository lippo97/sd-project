package it.unibo.lpaas.client.queue

interface BoundedQueue<T> {
    fun isFull(): Boolean

    fun isEmpty(): Boolean

    fun push(t: T)

    fun pop(): T

    fun peek(): T?

    companion object {
        fun <T> empty(maxSize: Int): BoundedQueue<T> = BoundedQueueImpl(mutableListOf(), maxSize)

        fun <T> of(maxSize: Int, queue: List<T>): BoundedQueue<T> = BoundedQueueImpl(queue.toMutableList(), maxSize)

        fun <T> of(maxSize: Int, vararg queue: T): BoundedQueue<T> = BoundedQueueImpl(queue.toMutableList(), maxSize)
    }
}
