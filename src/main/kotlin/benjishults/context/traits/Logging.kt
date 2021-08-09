package benjishults.context.traits

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.exec.Operation
import smartthings.logging.slf4j.KVLogger

interface Logging { // dispatch receiver

    companion object : (Logger) -> Logging {
        override fun invoke(logger: Logger): Logging =
            object : Logging {
                override val logger = logger
            }
    }

    val logger: Logger

    fun Operation.logSuccessOrError(
        key: String,
        loggingMap: Map<String, Any?>,
    ): Operation =
        this // extension receiver (included for illustration)
            .next {
                KVLogger.info(
                    this@Logging.logger, // dispatch receiver (included for illustration)
                    key,
                    loggingMap
                )
            }
            .onError { throwable: Throwable ->
                KVLogger.error(
                    logger,  // no need to mention the receiver---`this` is optional
                    key,
                    loggingMap,
                    throwable
                )
                throw throwable
            }

}

// This illustrates context-oriented programming by implementing an interface

class LocationDao : Logging by Logging(LoggerFactory.getLogger(LocationDao::class.java)) {

    fun deleteById(id: String): Operation =
        Operation
            .of {
                if (false) {
                    // we have access to the logger
                    logger.warn("Not Found locationId=$id")
                }
            }
            // we have access to this member extension function
            .logSuccessOrError("fetch-location-from-db", mapOf("locationId" to id))

}
