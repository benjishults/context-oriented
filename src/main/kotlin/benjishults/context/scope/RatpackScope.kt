package benjishults.context.scope

import ratpack.exec.Operation
import ratpack.exec.Promise
import ratpack.test.exec.ExecHarness

interface RatpackScope {

    companion object : RatpackScope

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

fun main() {

    // using `with` for context-oriented programming
    ExecHarness.harness().use { harness ->
        harness
            .`yield` {
                with(RatpackScope) {
                    Promise
                        .value("hi")
                        // we get this method from the context
                        .nextOpIf(
                            { it == "hello" },
                            { throw Error() },
                            { println("good") }
                        )
                }
            }
            .valueOrThrow
    }

}
