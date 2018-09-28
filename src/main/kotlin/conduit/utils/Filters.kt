package conduit.utils

import conduit.model.HttpException
import org.http4k.core.Filter
import org.http4k.core.Response

object CatchHttpExceptions {
    operator fun invoke() = Filter { next ->
        {
            try {
                next(it)
            } catch (e: HttpException) {
                Response(e.status).body(e.message ?: "")
            }
        }
    }
}