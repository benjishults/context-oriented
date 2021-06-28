package benjishults.context.polymorphism

import com.codahale.metrics.Meter
import org.slf4j.Logger
import smartthings.logging.slf4j.KVLogger

/**
 * Automates logging and marking metrics.
 *
 * @param baseMeterName prefix to meter names.
 * @param successLogKey log key used in success cases.
 * @param failureLogKey log key used in error cases.  If not provided, defaults to `"${successLogKey}-error"`.
 */
interface ObservabilityScope {

    val baseMeterName: String
    val logger: Logger
    val successLogKey: String
    val failureLogKey: String

    val successMeter: Meter
    val failureMeter: Meter

    /**
     * This will
     *
     * 1. log [throwable] with the [logKey] (defaults to [this.failureLogKey]) and [logMap] at error level
     * 2. mark "${[baseMeterName]}.error"
     *
     * @param logKey defaults to [failureLogKey]
     */
    fun observeAtError(
        throwable: Throwable,
        logMap: Map<String, *> = emptyMap<String, Any?>(),
        logKey: String = this.failureLogKey
    ) {
        observeAtError(failureMeter, logger, logKey, throwable, logMap)
    }

    /**
     * This will
     *
     * 1. log with the [logKey] (defaults to [failureLogKey]) and [logMap] at warn level
     * 2. mark "${[baseMeterName]}.error"
     *
     * @param logKey defaults to [successLogKey]
     */
    fun observeAtWarn(
        throwable: Throwable? = null,
        logMap: Map<String, *> = emptyMap<String, Any?>(),
        logKey: String = this.failureLogKey
    ) {
        observeAtWarn(failureMeter, logger, logKey, throwable, logMap)
    }

    /**
     * This will
     *
     * 1. log with the [logKey] (defaults to [successLogKey]) and [logMap] at info level
     * 2. mark "${[baseMeterName]}.success"
     *
     * @param logKey defaults to [successLogKey]
     */
    fun observeAtInfo(
        logMap: Map<String, *> = emptyMap<String, Any?>(),
        logKey: String = this.successLogKey
    ) {
        observeAtInfo(successMeter, logger, logKey, logMap)
    }

    /**
     * This will
     *
     * 1. log with the [logKey] (defaults to [successLogKey]) and [logMap] at debug level
     * 2. mark "${[baseMeterName]}.success"
     *
     * @param logKey defaults to [successLogKey]
     */
    fun observeAtDebug(
        logMap: Map<String, *> = emptyMap<String, Any?>(),
        logKey: String = this.successLogKey
    ) {
        observeAtDebug(successMeter, logger, logKey, logMap)
    }

    /**
     * This will
     *
     * 1. mark "${[baseMeterName]}.success"
     */
    fun observeJustMarkSuccess() {
        successMeter.mark()
    }

    /**
     * If [block] throws an exception, this will
     *
     * 1. log the [failureLogKey] (defaults to [this.failureLogKey]) and [logMapFactory]\(null) at warn level
     * 2. mark "${[baseMeterName]}.error"
     * 3. return [dummyValueFactory]\()
     *
     * Otherwise, this will
     *
     * 1. log the [successLogKey] (defaults to [this.successLogKey]) and [logMapFactory]\([block]\()) at info level
     * 2. mark "${[baseMeterName]}.success"
     * 3. return the value returned by [block].
     *
     * @param successLogKey defaults to the value of the receiver's [successLogKey]
     * @param failureLogKey defaults to `"${successLogKey}-error"`
     */
    fun <T> observeAndBury(
        dummyValueFactory: () -> T,
        successLogKey: String = this.successLogKey,
        failureLogKey: String = "$successLogKey-error",
        logMapFactory: (T?) -> Map<String, *> = { emptyMap<String, Any?>() },
        block: () -> T
    ): T =
        observeAndBury(
            dummyValueFactory,
            successMeter,
            failureMeter,
            logger,
            successLogKey,
            failureLogKey,
            logMapFactory,
            block
        )

    /**
     * If [block] throws an exception, this will
     *
     * 1. log the [failureLogKey] (defaults to [this.failureLogKey]) and [logMapFactory]\(null) at error level
     * 2. mark "${[baseMeterName]}.error"
     * 3. throw the exception
     *
     * Otherwise, this will
     *
     * 1. log the [successLogKey] (defaults to [successLogKey]) and [logMapFactory]\([block]\()) at info level
     * 2. mark "${[baseMeterName]}.success"
     * 3. return the value returned by [block].
     *
     * @param successLogKey defaults to the value of the receiver's [successLogKey]
     * @param failureLogKey defaults to `"${successLogKey}-error"`
     */
    fun <T> observeAndThrow(
        logMapFactory: (T?) -> Map<String, *> = { emptyMap<String, Any?>() },
        successLogKey: String = this.successLogKey,
        failureLogKey: String = "$successLogKey-error",
        block: () -> T
    ): T =
        observeAndThrow(
            successMeter,
            failureMeter,
            logger,
            logMapFactory,
            successLogKey,
            failureLogKey,
            block
        )

    /**
     * This will
     *
     * 1. log the [logKey] (defaults to [failureLogKey]) and [logMap] at error level
     * 2. mark "${[baseMeterName]}.error"
     * 3. throw the exception
     *
     * @param logKey defaults to [failureLogKey]
     */
    fun observeAndThrow(
        throwable: Throwable,
        logMap: Map<String, *> = emptyMap<String, Any?>(),
        logKey: String = this.failureLogKey
    ): Nothing =
        observeAndThrow(
            throwable,
            failureMeter,
            logger,
            logKey,
            logMap
        )

    companion object {

        /**
         * This will
         *
         * 1. log [throwable] with the [logKey] and [logMap] at error level
         * 2. mark [meter]
         */
        fun observeAtError(
            meter: Meter,
            logger: Logger,
            logKey: String,
            throwable: Throwable,
            logMap: Map<String, *> = emptyMap<String, Any?>()
        ) {
            observe(
                meter,
                logger,
                KVLogger.Level.ERROR,
                logKey,
                throwable,
                logMap
            )
        }

        /**
         * This will
         *
         * 1. log with the [logKey] and [logMap] at warn level
         * 2. mark [meter]
         */
        fun observeAtWarn(
            meter: Meter,
            logger: Logger,
            logKey: String,
            throwable: Throwable? = null,
            logMap: Map<String, *> = emptyMap<String, Any?>()
        ) {
            observe(
                meter,
                logger,
                KVLogger.Level.WARN,
                logKey,
                throwable,
                logMap
            )
        }

        /**
         * This will
         *
         * 1. log with the [logKey] and [logMap] at info level
         * 2. mark [meter]
         */
        fun observeAtInfo(
            meter: Meter,
            logger: Logger,
            logKey: String,
            logMap: Map<String, *> = emptyMap<String, Any?>()
        ) {
            observe(
                meter,
                logger,
                KVLogger.Level.INFO,
                logKey,
                logMap = logMap
            )
        }

        /**
         * This will
         *
         * 1. log with the [logKey] and [logMap] at debug level
         * 2. mark [meter]
         */
        fun observeAtDebug(
            meter: Meter,
            logger: Logger,
            logKey: String,
            logMap: Map<String, *> = emptyMap<String, Any?>()
        ) {
            observe(
                meter,
                logger,
                KVLogger.Level.DEBUG,
                logKey,
                logMap = logMap
            )
        }

        /**
         * This will
         *
         * 1. log with the [logKey] and [logMap] at the indicated level
         * 2. mark [meter]
         */
        fun observe(
            meter: Meter,
            logger: Logger,
            level: KVLogger.Level,
            logKey: String,
            throwable: Throwable? = null,
            logMap: Map<String, *> = emptyMap<String, Any?>()
        ) {
            KVLogger.log(logger, level, logKey, logMap, throwable)
            meter.mark()
        }

        /**
         * If [block] throws an exception, this will
         *
         * 1. log the [failureLogKey] and [logMapFactory]\(null) at warn level
         * 2. mark [failureMeter]
         * 3. return [dummyValueFactory]()
         *
         * Otherwise, this will
         *
         * 1. log the [successLogKey] and [logMapFactory]\([block]\()) at info level
         * 2. mark [successMeter]
         * 3. return the value returned by [block].
         *
         * @param failureLogKey defaults to `"${successLogKey}-error"`
         */
        fun <T> observeAndBury(
            dummyValueFactory: () -> T,
            successMeter: Meter,
            failureMeter: Meter,
            logger: Logger,
            successLogKey: String,
            failureLogKey: String = "$successLogKey-error",
            logMapFactory: (T?) -> Map<String, *> = { emptyMap<String, Any?>() },
            block: () -> T
        ): T {
            var throwable: Throwable? = null
            var value: T? = null
            return try {
                value = block()
                value
            } catch (t: Throwable) {
                throwable = t
                observeAtWarn(failureMeter, logger, failureLogKey, t, logMapFactory(null))
                dummyValueFactory()
            } finally {
                if (throwable === null) {
                    observeAtInfo(successMeter, logger, successLogKey, logMapFactory(value))
                }
            }
        }

        /**
         * If [block] throws an exception, this will
         *
         * 1. log the [failureLogKey] and [logMapFactory]\(null) at error level
         * 2. mark [failureMeter]
         * 3. throw the exception
         *
         * Otherwise, this will
         *
         * 1. log the [successLogKey] and [logMapFactory]\([block]\()) at info level
         * 2. mark [successMeter]
         * 3. return the value returned by [block].
         *
         * @param failureLogKey defaults to `"${successLogKey}-error"`
         */
        fun <T> observeAndThrow(
            successMeter: Meter,
            failureMeter: Meter,
            logger: Logger,
            logMapFactory: (T?) -> Map<String, *> = { emptyMap<String, Any?>() },
            successLogKey: String,
            failureLogKey: String = "$successLogKey-error",
            block: () -> T
        ): T {
            var throwable: Throwable? = null
            var value: T? = null
            return try {
                value = block()
                value
            } catch (t: Throwable) {
                throwable = t
                observeAtError(failureMeter, logger, failureLogKey, t, logMapFactory(null))
                throw t
            } finally {
                if (throwable === null) {
                    observeAtInfo(successMeter, logger, successLogKey, logMapFactory(value))
                }
            }
        }

        /**
         * This will
         *
         * 1. log the [logKey] and [logMap] at error level
         * 2. mark [metric]
         * 3. throw the exception
         *
         *
         */
        fun observeAndThrow(
            throwable: Throwable,
            metric: Meter,
            logger: Logger,
            logKey: String,
            logMap: Map<String, *> = emptyMap<String, Any?>()
        ): Nothing {
            observeAtError(metric, logger, logKey, throwable, logMap)
            throw throwable
        }

    }

}
