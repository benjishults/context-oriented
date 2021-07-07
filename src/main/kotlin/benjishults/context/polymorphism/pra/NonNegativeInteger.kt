package benjishults.context.polymorphism.pra

// when people claim it takes 300 pages to prove that 2+2=4, show them this:

// definition of non-negative integers
sealed interface NonNegativeInteger {

    // every non-negative integer has a successor
    fun successor(): PositiveInteger =
        PositiveInteger(this)

    // add any two non-negative integers to get another non-negative integer
    fun add(other: NonNegativeInteger): NonNegativeInteger

    // multiply any two non-negative integers to get another non-negative integer
    fun multiply(other: NonNegativeInteger): NonNegativeInteger

}

// a positive integer is a non-negative integer with a predecessor
data class PositiveInteger(val predecessor: NonNegativeInteger) : NonNegativeInteger {

    // addition is defined recursively
    override fun add(other: NonNegativeInteger) =
        predecessor.add(other).successor()

    // multiplication is defined recursively
    override fun multiply(other: NonNegativeInteger) =
        predecessor.multiply(other).add(other)

}

// definition of zero
object Zero : NonNegativeInteger {

    // special rule for zero
    override fun add(other: NonNegativeInteger) = other

    // special rule for zero
    override fun multiply(other: NonNegativeInteger) = Zero

}

// define 1 - 4
val one = Zero.successor()
val two = one.successor()
val three = two.successor()
val four = three.successor()

fun main(args: Array<String>) {
    check(Zero.add(Zero) == Zero)
    check(two.add(two) == four)
    check(two.multiply(two) == four)
    check(one.multiply(three) == three)
    check(three.multiply(one) == three)
}
