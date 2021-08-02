package benjishults.context.polymorphism.adder

import kotlin.random.Random

interface Adder {

    fun Double.add(other: Double): Double =
        this + other

    fun sum(vararg numbs: Double) =
        numbs.reduce { acc, next ->
            acc.add(next)
        }

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

class Maths : Adder {

    fun epsilon(init: Int, max: Int, expr: (Int) -> Double): Double {
        require(init <= max)
        return (init..max)
            .map(expr)
            .reduce { acc, next ->
                // here, we're able to use `add` because we are in an Adder
                acc.add(next)
            }
    }

}

fun main(args: Array<String>) {

    println(
        listOf<Adder>(object : Adder {}, Subtracter, CrazyAdder())
            .map {
                with(it) {
                    7.0.add(4.0)
                }
            })

    with(object : Adder {}) {
        check(sum(2.0, 2.0, 2.0, 2.0, 2.0) == 10.0)
    }

    with(Subtracter) {
        check(7.0.add(3.0) == 4.0)
    }

    with(CrazyAdder()) {
        check(7.0.add(23.0) == 13.206839527806123)
    }

    val maths = Maths()
    check(
        maths.epsilon(1, 10) {
            1.0 / (it * it)
        } == 1.5497677311665408)

}
