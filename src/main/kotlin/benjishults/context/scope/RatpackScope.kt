package benjishults.context.scope

import org.slf4j.Logger
import ratpack.exec.Downstream
import ratpack.exec.ExecResult
import ratpack.exec.Execution
import ratpack.exec.Operation
import ratpack.exec.Promise
import ratpack.exec.Upstream
import ratpack.exec.util.ParallelBatch
import ratpack.func.Action
import ratpack.func.BiFunction
import ratpack.func.Pair
import smartthings.logging.slf4j.KVLogger
import java.time.Duration

interface RatpackScope {

    companion object : RatpackScope

    operator fun <S, T> Pair<S, T>.component1(): S = this.left

    operator fun <S, T> Pair<S, T>.component2(): T = this.right

    fun <T> Promise<T>.nextOpIf(
        predicate: (T) -> Boolean,
        onTrue: (T) -> Operation,
        onFalse: (T) -> Operation
    ): Promise<T> =
        this.nextOp {
            if (predicate(it))
                onTrue(it)
            else
                onFalse(it)
        }

    fun <T, E : Throwable> Promise<T>.onErrorAndThrow(
        clazz: Class<E>,
        block: (E) -> Unit
    ): Promise<T> =
        this.onError(clazz) { throwable: E ->
            block(throwable)
            throw throwable
        }

    fun <T> Promise<T>.onErrorAndThrow(
        block: (Throwable) -> Unit
    ): Promise<T> =
        this.onError { throwable: Throwable ->
            block(throwable)
            throw throwable
        }

    fun <T> Promise<T>.maybeTime(
        timer: Action<Duration>?
    ): Promise<T> =
        if (timer != null)
            this.time(timer)
        else
            this

    /**
     * Operates on a map from inputs ([I]) to [Promise]s of outputs ([O]). The inputs are only used if there are
     * errors.  In that case, the inputs are used to map to the [Throwable]s.
     * @return a [Promise] of a [Pair] or a [List] of outputs ([O]) (the successes) and a [Map] from inputs ([I]) to the
     * [Throwable]s related to them.
     */
    fun <I : Any, O> Map<I, Promise<O>>.parallelBatch(): Promise<Pair<List<O>, Map<I, Throwable>>> {
        val failures = mutableMapOf<I, Throwable>()
        return map { (input, promise) ->
            promise.mapError {
                throw WrappingException(input, it)
            }
        }
            .parallelBatch()
            .map { execResults: List<ExecResult<O>> ->
                execResults.mapNotNull { execResult: ExecResult<O> ->
                    execResult.throwable?.let { throwable: Throwable ->
                        // NOTE when I is a reified type in an inline function, the cast is fine.  Couldn't leave it
                        //      that way because I wanted the WrappingException to be private.
                        @Suppress("UNCHECKED_CAST")
                        failures[(throwable as WrappingException).wrapped as I] = throwable.cause!!
                        null
                    }
                    execResult.value
                }
            }
            .map { users: List<O> ->
                Pair.of(users, failures)
            }
    }

    /**
     * Transforms the given [Promise] to a [List] of [Promise]s and converts to a [Promise] of a [List] of [ExecResult]s.
     * Prefer [parallelBatchOrThrow] or [parallelBatchAndFilter], unless you really need to use the [ExecResult]s.
     */
    fun <S, T> Promise<T>.parallelBatch(
        timer: Action<Duration>? = null,
        transformer: (T) -> List<Promise<S>>
    ): Promise<List<ExecResult<S>>> =
        this.flatMap {
            transformer(it).parallelBatch(timer)
        }

    /**
     * Converts a [List] of [Promise]s to a [Promise] of a [List] of [ExecResult]s.
     * Prefer [parallelBatchOrThrow] or [parallelBatchAndFilter], unless you really need to use the [ExecResult]s.
     */
    fun <T> List<Promise<T>>.parallelBatch(
        timer: Action<Duration>? = null
    ): Promise<List<ExecResult<T>>> =
        if (this.isEmpty())
            Promise.value(emptyList())
        else
            ParallelBatch.of(this)
                .execInit(initializeExecutionContext())
                .yieldAll()
                .maybeTime(timer)

    fun <T> List<Promise<T>>.parallelBatchFailFast(): Promise<List<T>> =
        if (this.isEmpty())
            Promise.value(emptyList())
        else
            ParallelBatch.of(this)
                .execInit(initializeExecutionContext())
                .yield()

    /**
     * Transforms the given [Promise] to a [List] of [Promise]s and converts to a [Promise] of an equal sized [List].
     * @throws [Exception] thrown by the first failed [Promise] in the [List]
     * with the [Exception]s of all remaining failed [Promise]s added as suppressed [Exception]s.
     */
    fun <S, T> Promise<T>.parallelBatchOrThrow(
        timer: Action<Duration>? = null,
        transformer: (T) -> List<Promise<S>>
    ): Promise<List<S>> =
        this.flatMap {
            transformer(it).parallelBatchOrThrow(timer)
        }

    /**
     * Converts a [List] of [Operation]s to an [Operation].
     * @throws [Exception] thrown by the first failed [Operation] in the [List]
     * with the [Exception]s of all remaining failed [Operation]s added as suppressed [Exception]s.
     */
    fun List<Operation>.parallelBatchOrThrow(
        timer: Action<Duration>? = null,
    ): Operation =
        this.map { it.promise() }.parallelBatchOrThrow(timer).operation()

    /**
     * Converts a [List] of [Promise]s to a [Promise] of an equal sized [List].
     * @throws [Exception] thrown by the first failed [Promise] in the [List]
     * with the [Exception]s of all remaining failed [Promise]s added as suppressed [Exception]s.
     */
    fun <T> List<Promise<T>>.parallelBatchOrThrow(
        timer: Action<Duration>? = null,
    ): Promise<List<T>> =
        this.parallelBatch(timer)
            .map { results: List<ExecResult<T>> ->
                results.firstOrNull { it.isError }?.let { firstFailedResult: ExecResult<T> ->
                    val throwable: Throwable = firstFailedResult.throwable
                    results.filter { it != firstFailedResult && it.isError }
                        .forEach { remainingFailedResult: ExecResult<T> ->
                            throwable.addSuppressed(remainingFailedResult.throwable)
                        }
                    throw throwable
                } ?: results.map { it.value }
            }

    /**
     * Transforms the given [Promise] to a [List] of [Operation]s and converts to a [Operation].
     * @throws [Exception] thrown by the first failed [Operation] in the [List]
     * with the [Exception]s of all remaining failed [Operation]s added as suppressed [Exception]s.
     */
    fun <T> Promise<T>.parallelBatchOpsOrThrow(
        timer: Action<Duration>? = null,
        transformer: (T) -> List<Operation>
    ): Promise<T> =
        this.nextOp {
            transformer(it).parallelBatchOrThrow(timer)
        }

    /**
     * Transforms the given [Promise] to a [List] of [Promise]s and converts to a [Promise] of a possibly smaller [List].
     * Results of failed [Promise]s are omitted.
     */
    fun <S, T> Promise<T>.parallelBatchAndFilter(
        timer: Action<Duration>? = null,
        transformer: (T) -> List<Promise<S>>
    ): Promise<List<S>> =
        this.flatMap {
            transformer(it).parallelBatchAndFilter(timer)
        }

    /**
     * Converts a [List] of [Promise]s to a [Promise] of a possibly smaller [List].
     * Results of failed [Promise]s are omitted.
     */
    fun <T> List<Promise<T>>.parallelBatchAndFilter(
        timer: Action<Duration>? = null,
    ): Promise<List<T>> =
        this.parallelBatch(timer)
            .map { results: List<ExecResult<T>> ->
                results.filter { it.isSuccess }.map { it.value }
            }

    fun Operation.fork() = Execution.fork()
        .onStart(initializeExecutionContext())
        .start(this)

    fun Operation.optionallyFork(
        condition: Boolean
    ): Operation =
        if (condition)
            Operation.of { fork() }
        else
            this

    fun <T> Promise<T>.conditionallyRetry(
        maxRetries: Int,
        promiseDurationOnError: BiFunction<in Int, in T, Promise<Duration>>,
        retryCondition: (T) -> Boolean,
        resultThrowableExtractor: (T) -> Throwable? = { null }
    ): Promise<T> {
        return transform { up: Upstream<out T> ->
            Upstream { down: Downstream<in T> ->
                conditionallyRetryAttempt(
                    1,
                    maxRetries,
                    up,
                    down,
                    promiseDurationOnError,
                    retryCondition,
                    resultThrowableExtractor
                )
            }
        }
    }

    private fun <T> conditionallyRetryAttempt(
        retryNum: Int,
        maxRetries: Int,
        up: Upstream<out T>,
        down: Downstream<in T>,
        promiseDurationOnError: BiFunction<in Int, in T, Promise<Duration>>,
        retryCondition: (T) -> Boolean,
        resultThrowableExtractor: (T) -> Throwable?
    ) {
        up.connect(
            down.onSuccess { result: T ->
                if (retryNum > maxRetries || !retryCondition(result)) {
                    // we let any response through after attempts are exhausted or if it isn't considered an error
                    down.success(result)
                } else {
                    val promiseDuration =
                        try {
                            // get the Promise<Duration>
                            promiseDurationOnError.apply(retryNum, result)
                        } catch (errorHandlerError: Throwable) {
                            resultThrowableExtractor(result)?.let {
                                errorHandlerError.addSuppressed(it)
                            }
                            down.error(errorHandlerError)
                            return@onSuccess
                        }
                    promiseDuration.connect(object : Downstream<Duration> {
                        override fun success(value: Duration) {
                            Execution.sleep(
                                value
                            ) {
                                conditionallyRetryAttempt(
                                    retryNum + 1,
                                    maxRetries,
                                    up,
                                    down,
                                    promiseDurationOnError,
                                    retryCondition,
                                    resultThrowableExtractor
                                )
                            }
                        }

                        override fun error(throwable: Throwable) {
                            down.error(throwable)
                        }

                        override fun complete() {
                            down.complete()
                        }
                    })
                }
            }
        )
    }

    // NOTE exceptions can't be generic.  :(
    private class WrappingException(val wrapped: Any, cause: Throwable) : Exception(cause)

    fun <T> Promise<T>.backOffRetryWithWarnings(
        logger: Logger,
        maxRetries: Int = 3,
        failureLogKey: String,
        logMap: Map<String, Any?> = emptyMap(),
        firstDelayMillis: Long = 20L
    ): Promise<T> {
        return retry(
            maxRetries
        ) { retry: Int, error: Throwable ->
            KVLogger.warn(
                logger,
                failureLogKey,
                logMap,
                error
            )
            Promise.value(Duration.ofMillis(firstDelayMillis * retry))
        }
    }

}

fun initializeExecutionContext(): Action<in Execution> {
    return Action { execution: Execution ->
        // ...
    }
}
