package benjishults.context.polymorphism

import benjishults.context.scope.RatpackScope
import ratpack.exec.Promise
import ratpack.func.BiFunction
import smartthings.siam.client.common.SiamResponse
import java.time.Duration

interface SiamResponseScope {
    val throwExceptionOnSiamFailureDetermination: () -> Boolean

    /**
     * If [throwExceptionOnSiamFailureDetermination].invoke() is false, this replaces the error with a success with value [dummyValueFactory].invoke() and logs
     * the error with key "${[failureLogKey]}-ignored".
     *
     * Otherwise, this throws the error and does appropriate observability tasks.
     */
    fun <T> Promise<SiamResponse<T>>.mapSiamResponse(
        dummyValueFactory: () -> SiamResponse<T>,
        observer: ObservabilityScope,
        logMap: Map<String, *> = emptyMap<String, Any?>(),
        request: Any? = null,
        failureLogKey: String = observer.failureLogKey
    ): Promise<SiamResponse<T>> {
        val incoming: Promise<SiamResponse<T>> = this
        return incoming
            .mapError { throwable ->
                // e.g. connection failure
                val errorLogMap = logMap + ("request" to request.toString())
                if (throwExceptionOnSiamFailureDetermination()) {
                    observer.observeAndThrow(
                        throwable,
                        errorLogMap
                    )
                } else {
                    observer.observeAtError(throwable, errorLogMap, "$failureLogKey-ignored")
                    dummyValueFactory()
                }
            }
            .observeNonException(
                observer,
                logMap,
                failureLogKey,
                throwExceptionOnSiamFailureDetermination()
            )
    }

    /**
     * Throws the error and does appropriate observability tasks ignoring [throwExceptionOnSiamFailureDetermination].
     */
    fun <T> Promise<SiamResponse<T>>.mapSiamResponse(
        observer: ObservabilityScope,
        logMap: Map<String, *> = emptyMap<String, Any?>(),
        request: Any? = null,
        failureLogKey: String = observer.failureLogKey
    ): Promise<SiamResponse<T>> {
        val incoming: Promise<SiamResponse<T>> = this
        return incoming
            .mapError { throwable ->
                // e.g. connection failure
                observer.observeAndThrow(
                    throwable,
                    logMap + ("request" to request.toString()),
                    failureLogKey
                )
            }
            .observeNonException(observer, logMap, failureLogKey)
    }

    private fun <T> Promise<SiamResponse<T>>.observeNonException(
        observer: ObservabilityScope,
        logMap: Map<String, *>,
        failureLogKey: String,
        throwExceptionOnSiamFailure: Boolean = true
    ): Promise<SiamResponse<T>> {
        val incoming: Promise<SiamResponse<T>> = this
        return incoming.next { response: SiamResponse<T> ->
            val logMapWithStatus = logMap + ("status" to response.status)
            if (response.isFailed) {
                // e.g. 401 from SIAM
                if (throwExceptionOnSiamFailure) {
                    observer.observeAndThrow(
                        response.error,
                        logMapWithStatus,
                        failureLogKey
                    )
                } else {
                    observer.observeAtError(
                        response.error,
                        logMapWithStatus,
                        "$failureLogKey-ignored"
                    )
                }
            } else {
                observer.observeJustMarkSuccess()
            }
        }
    }

    fun <R> Promise<SiamResponse<R>>.retrySiamResponse(
        maxRetries: Int,
        promiseDurationOnError: BiFunction<in Int, in SiamResponse<R>, Promise<Duration>>,
        retryCondition: (SiamResponse<R>) -> Boolean = { response ->
            response.isFailed
        }
    ): Promise<SiamResponse<R>> =
        with(RatpackScope) {
            conditionallyRetry(
                maxRetries,
                promiseDurationOnError,
                retryCondition
            ) { response: SiamResponse<R> -> response.error }
        }

    companion object {

        @JvmStatic
        val siamResponse: SiamResponse<Void> = SiamResponse<Void>(200)

    }

}
