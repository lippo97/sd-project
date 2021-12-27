package it.unibo.lpaas.domain

class Fact(
    val functor: Functor,
    val args: List<String> = emptyList(),
) {
    companion object {
        fun of(functor: Functor, vararg args: String): Fact =
            Fact(functor, args.toList())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Fact

        if (functor != other.functor) return false
        if (args != other.args) return false

        return true
    }

    override fun hashCode(): Int {
        var result = functor.hashCode()
        result = 31 * result + args.hashCode()
        return result
    }
}
