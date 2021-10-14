package gifts.givin.matching.common.stages

import mu.KLogger

interface Stage<T> {
    operator fun invoke(logger: KLogger, func: (T.() -> Unit)) {
        logger.info { "Starting stage ${this::class.simpleName} ${extraLogs(func)}" }
        run(func)
    }

    fun run(func: (T.() -> Unit))
    fun extraLogs(func: T.() -> Unit) = ""
}
