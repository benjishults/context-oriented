package benjishults.context.traits

import org.slf4j.Logger
import ratpack.exec.Operation
import ratpack.exec.Promise
import smartthings.logging.slf4j.KVLogger

interface Logging {
    val logger: Logger

    companion object : (Logger) -> Logging {
        override fun invoke(logger: Logger): Logging =
            object : Logging {
                override val logger = logger
            }
    }

    fun <T> Promise<T>.logResultOrError(
        key: String,
        resultLogMapKey: String,
        loggingMap: Map<String, Any?>,
    ): Promise<T> =
        this
            .onError { throwable: Throwable ->
                KVLogger.error(
                    logger,
                    key,
                    loggingMap,
                    throwable
                )
                throw throwable
            }

    fun Operation.logSuccessOrError(
        key: String,
        loggingMap: Map<String, Any?>,
    ): Operation =
        this
            .next {
                KVLogger.info(
                    logger,
                    key,
                    loggingMap
                )
            }
            .onError { throwable: Throwable ->
                KVLogger.error(
                    logger,
                    key,
                    loggingMap,
                    throwable
                )
                throw throwable
            }

}
