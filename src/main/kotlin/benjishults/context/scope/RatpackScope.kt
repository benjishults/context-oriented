package benjishults.context.scope

import ratpack.exec.Operation
import ratpack.exec.Promise
import ratpack.func.Pair
import ratpack.test.exec.ExecHarness

interface RatpackScope {

    companion object : RatpackScope

    operator fun <S, T> Pair<S, T>.component1(): S = this.left

    operator fun <S, T> Pair<S, T>.component2(): T = this.right

    fun <T> Promise<T>.nextOpIf(
        predicate: (T) -> Boolean,
        onTrue: (T) -> Unit,
        onFalse: (T) -> Unit
    ): Promise<T> =
        this.nextOp {
            if (predicate(it))
                Operation.of { onTrue(it) }
            else
                Operation.of { onFalse(it) }
        }

}

// This illustrates using `with` for context-oriented programming

fun main(args: Array<String>) {

    ExecHarness.harness().use { harness ->
        harness.`yield` {
            with(RatpackScope) {
                Promise.value("hi")
                    // we get this method from the context
                    .nextOpIf({ it == "hello" },
                        { throw Error() },
                        { println("good") })
            }
        }
            .valueOrThrow
    }

    // we can also implement the `RatpackScope` interface to get access to its methods.
    object : RatpackScope {

        fun <T> Promise<T>.foo(): Promise<T> =
            nextOpIf({ it == "hello" },
                { throw Error() },
                { println("good") })

    }

}

