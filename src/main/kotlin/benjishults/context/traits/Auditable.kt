package benjishults.context.traits

import java.time.Instant

interface Auditable {
    var created: Instant?
    var lastModified: Instant?

    companion object : () -> Auditable {
        override fun invoke(): Auditable =
            object : Auditable {
                override var created: Instant? = null
                override var lastModified: Instant? = null
            }
    }

}
