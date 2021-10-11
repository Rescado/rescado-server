package org.rescado.server.controller

import org.rescado.server.controller.dto.res.Response
import org.rescado.server.service.AnimalService
import org.rescado.server.util.generateResponse
import org.rescado.server.util.toAnimalArrayDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime

@RestController
@RequestMapping("/animal")
class AnimalController(
    private val animalService: AnimalService,
) {

    // TODO remove this endpoint once we have better ones
    @GetMapping
    fun getAll(): ResponseEntity<List<Response>> = generateResponse(animalService.getAll().toAnimalArrayDTO(ZonedDateTime.now()))
}