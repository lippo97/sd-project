package it.unibo.lpaas.core.timer

interface Timer<ID> {
    fun setTimeout(time: Long, fn: suspend () -> Unit): ID
    fun setInterval(time: Long, fn: suspend () -> Unit): ID
    fun clear(id: ID)

    companion object
}
