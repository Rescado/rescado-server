package org.rescado.server.controller.dto.res

import java.time.ZonedDateTime

data class LikeDTO(
    val timestamp: ZonedDateTime,
    val reference: String?,
    val animal: AnimalDTO,
) : Response()
