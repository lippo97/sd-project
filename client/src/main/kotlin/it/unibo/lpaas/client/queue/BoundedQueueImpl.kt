package it.unibo.lpaas.client.queue

class BoundedQueueImpl<T>(
    private val queue: MutableList<T>,
    private val maxSize: Int,
) : BoundedQueue<T> {

    override fun isFull(): Boolean = queue.size == maxSize

    override fun isEmpty(): Boolean = queue.isEmpty()

    override fun push(t: T) {
        queue.add(t)
    }

    override fun pop(): T {
        return queue.removeAt(0)
    }

    override fun peek(): T? = queue.getOrNull(0)
}
