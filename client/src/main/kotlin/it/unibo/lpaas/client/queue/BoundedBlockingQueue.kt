package it.unibo.lpaas.client.queue

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class BoundedBlockingQueue<T>(private val maxSize: Int) : BoundedQueue<T> {

    val blockingQueue: BlockingQueue<T> = LinkedBlockingQueue(maxSize)

    override fun isFull(): Boolean = blockingQueue.size == maxSize

    override fun isEmpty(): Boolean = blockingQueue.isEmpty()

    override fun push(t: T) {
        blockingQueue.put(t)
    }

    override fun pop(): T {
        TODO("Not yet implemented")
    }

    override fun peek(): T? {
        TODO("Not yet implemented")
    }
}
