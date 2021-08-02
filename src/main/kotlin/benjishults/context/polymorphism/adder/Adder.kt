package benjishults.context.polymorphism.adder

import kotlin.random.Random

interface Adder {

    fun Double.add(other: Double): Double =
        this + other

}

object Subtracter : Adder {

    override fun Double.add(other: Double): Double =
        this - other

}

class CrazyAdder : Adder {

    private val random = Random(43)

    override fun Double.add(other: Double): Double {
        return random.nextDouble(other)
    }

}

fun main() {

    println(
        listOf<Adder>(object : Adder {}, Subtracter, CrazyAdder())
            .map {
                with(it) {
                    7.0.add(4.0)
                }
            })

    with(object : Adder {}) {
        check(7.0.add(4.0) == 11.0)
    }

    with(Subtracter) {
        check(7.0.add(4.0) == 3.0)
    }

    with(CrazyAdder()) {
        check(7.0.add(4.0) == 2.2968416570097605)
    }

}
