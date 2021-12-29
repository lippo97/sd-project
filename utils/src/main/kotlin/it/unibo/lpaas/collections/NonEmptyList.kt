package it.unibo.lpaas.collections

class NonEmptyList<T>(
    val head: T,
    val tail: List<T>
) : AbstractList<T>() {

    private constructor (list: List<T>) : this(list.first(), list.drop(1))

    override fun isEmpty(): Boolean = false

    override val size: Int = 1 + tail.size

    override operator fun get(index: Int): T {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("$index is not in 1..${size - 1}")
        return if (index == 0) head else tail[index - 1]
    }

    operator fun plus(collection: Collection<T>): NonEmptyList<T> =
        NonEmptyList(head, tail + collection)

    fun toList(): List<T> = listOf(head) + tail

    companion object {
        fun <T> fromListUnsafe(list: List<T>): NonEmptyList<T> {
            require(list.isNotEmpty())
            return NonEmptyList(list)
        }

        fun <T> fromList(list: List<T>): NonEmptyList<T>? =
            runCatching { fromListUnsafe(list) }
                .getOrNull()
    }
}

fun <T> NonEmptyList<T>.sortedWith(comparator: Comparator<in T>): NonEmptyList<T> =
    NonEmptyList.fromListUnsafe(toList().sortedWith(comparator))

fun <T> nonEmptyListOf(head: T, vararg elems: T): NonEmptyList<T> = NonEmptyList(head, elems.toList())
