package benjishults.context.polymorphism.adder

import kotlin.random.Random

interface Combinable<T> {

    fun T.combine(other: T): T

}

interface DoubleCombiner : Combinable<Double> {
    override fun Double.combine(other: Double): Double =
        this + other
}

object Subtracter : DoubleCombiner {

    override fun Double.combine(other: Double): Double =
        this - other

}

open class Randomizer : DoubleCombiner {

    private val random = Random(43)

    override fun Double.combine(other: Double): Double {
        return random.nextDouble(other)
    }

}

fun main() {

    println(
        listOf<DoubleCombiner>(
            object : DoubleCombiner {},
            Subtracter,
            Randomizer()
        )
            .map {
                with(it) {
                    7.0.combine(4.0)
                }
            })

    with(object : DoubleCombiner {}) {
        check(7.0.combine(4.0) == 11.0)
    }

    with(Subtracter) {
        check(7.0.combine(4.0) == 3.0)
    }

    with(Randomizer()) {
        check(7.0.combine(4.0) == 2.2968416570097605)
    }

}
