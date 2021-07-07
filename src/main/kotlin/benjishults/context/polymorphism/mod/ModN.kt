package benjishults.context.polymorphism.mod

interface ModN {

    val n: Int

    fun Int.add(other: Int): Int {
        require(this >= 0)
        require(other >= 0)
        // typing this@ModN. is not needed.  I'm specifying it here to call out that there are two receivers in scope.
        return (this + other) % this@ModN.n
    }

    companion object : (Int) -> ModN {
        override fun invoke(n: Int): ModN =
            object : ModN {
                override val n = n
            }
    }

}

// three different ways of using it

val mod3 = ModN(3)

object Mod4 : ModN by ModN(4)

class Mod7 : ModN by ModN(7) {

    fun Int.multiply(other: Int): Int {
        require(this >= 0)
        require(other >= 0)
        return (this * other) % n
    }

}

fun main(args: Array<String>) {

    // use the val
    with(mod3) {
        check(7.add(3) == 1)
    }

    // use the object
    with(Mod4) {
        check(7.add(3) == 2)
    }

    // here I just create one inline
    with(ModN(5)) {
        check(7.add(3) == 0)
    }

    // use the class
    with(Mod7()) {
        check(7.add(3) == 3)
    }

}
