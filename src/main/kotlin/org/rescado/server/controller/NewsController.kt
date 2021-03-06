package org.rescado.server.controller

import org.rescado.server.controller.dto.build
import org.rescado.server.controller.dto.res.error.Forbidden
import org.rescado.server.controller.dto.toNewsArrayDTO
import org.rescado.server.persistence.entity.Account
import org.rescado.server.service.AccountService
import org.rescado.server.service.CardService
import org.rescado.server.service.MessageService
import org.rescado.server.service.NewsService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/news")
class NewsController(
    private val newsService: NewsService,
    private val cardService: CardService,
    private val accountService: AccountService,
    private val messageService: MessageService,
) {

    @GetMapping
    fun get(): ResponseEntity<*> {
        val user = SecurityContextHolder.getContext().authentication.principal
        if (user !is Account)
            return Forbidden(error = messageService["error.ResourceForbidden.message"]).build()

        val likes = cardService.getLikedByAccountWithAnimals(user).map { it.animal }
        val followings = accountService.getByIdWithFollowing(user.id)!!.following.flatMap { it.animals } // TODO check if animals arent lazy

        return newsService.getByReferences(likes + followings).toNewsArrayDTO().build()
    }
}
