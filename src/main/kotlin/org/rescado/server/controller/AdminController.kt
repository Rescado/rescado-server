package org.rescado.server.controller

import org.rescado.server.controller.dto.build
import org.rescado.server.controller.dto.req.AddAdminDTO
import org.rescado.server.controller.dto.req.AddVolunteerDTO
import org.rescado.server.controller.dto.req.PatchAdminDTO
import org.rescado.server.controller.dto.res.Response
import org.rescado.server.controller.dto.res.error.BadRequest
import org.rescado.server.controller.dto.res.error.Forbidden
import org.rescado.server.controller.dto.res.error.NotFound
import org.rescado.server.controller.dto.toAccountArrayDTO
import org.rescado.server.controller.dto.toAccountDTO
import org.rescado.server.controller.dto.toAdminDTO
import org.rescado.server.persistence.entity.Account
import org.rescado.server.persistence.entity.Admin
import org.rescado.server.service.AccountService
import org.rescado.server.service.AdminService
import org.rescado.server.service.MessageService
import org.rescado.server.service.ShelterService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/admin")
class AdminController(
    private val adminService: AdminService,
    private val accountService: AccountService,
    private val shelterService: ShelterService,
    private val messageService: MessageService,
) {

    // region Admin

    @PostMapping
    fun add(
        @Valid @RequestBody dto: AddAdminDTO,
        res: BindingResult,
    ): ResponseEntity<Response> {
        val user = SecurityContextHolder.getContext().authentication.principal
        if (user !is Admin)
            return Forbidden(error = messageService["error.ResourceForbidden.message"]).build()

        if (res.hasErrors())
            return BadRequest(errors = res.allErrors.map { it.defaultMessage as String }).build()

        return adminService.create(
            username = dto.username!!,
            password = dto.password!!,
        ).toAdminDTO().build(HttpStatus.CREATED)
    }

    @PatchMapping
    fun patch(
        @Valid @RequestBody dto: PatchAdminDTO,
        res: BindingResult,
    ): ResponseEntity<Response> {
        val user = SecurityContextHolder.getContext().authentication.principal
        if (user !is Admin)
            return Forbidden(error = messageService["error.ResourceForbidden.message"]).build()

        if (res.hasErrors())
            return BadRequest(errors = res.allErrors.map { it.defaultMessage as String }).build()

        return adminService.update(
            admin = user,
            username = dto.username,
            password = dto.password,
        ).toAdminDTO().build()
    }

    @DeleteMapping("/{adminId}")
    fun removeById(
        @PathVariable(required = false) adminId: Long? = null,
    ): ResponseEntity<Response> {
        val user = SecurityContextHolder.getContext().authentication.principal
        if (user !is Admin)
            return Forbidden(error = messageService["error.ResourceForbidden.message"]).build()

        val admin = if (adminId == null) user else adminService.getById(adminId)
            ?: return NotFound(error = messageService["error.NonExistentAdmin.message", adminId]).build()

        adminService.delete(admin)
        return Response(httpStatus = HttpStatus.NO_CONTENT).build()
    }

    // endregion
    // region Volunteer

    @GetMapping("/volunteer")
    fun getVolunteers(
        @RequestParam("shelter") shelterId: Long? = null,
    ): ResponseEntity<*> {
        val user = SecurityContextHolder.getContext().authentication.principal
        if (user !is Admin)
            return Forbidden(error = messageService["error.ResourceForbidden.message"]).build()

        if (shelterId == null)
            return accountService.getAllVolunteers().toAccountArrayDTO().build()

        val shelter = shelterService.getById(shelterId)
            ?: return NotFound(error = messageService["error.NonExistentShelter.message", shelterId]).build()

        return accountService.getAllVolunteers(shelter).toAccountArrayDTO().build()
    }

    @PostMapping("/volunteer")
    fun addVolunteer(
        @Valid @RequestBody dto: AddVolunteerDTO,
        res: BindingResult,
    ): ResponseEntity<Response> {
        val user = SecurityContextHolder.getContext().authentication.principal
        if (user !is Admin)
            return Forbidden(error = messageService["error.ResourceForbidden.message"]).build()

        if (res.hasErrors())
            return BadRequest(errors = res.allErrors.map { it.defaultMessage as String }).build()

        val account = accountService.getById(dto.accountId!!)
            ?: return BadRequest(error = messageService["error.NonExistentAccount.message", dto.accountId]).build()
        if (account.status == Account.Status.ANONYMOUS)
            return BadRequest(error = messageService["error.AccountIsAnonymous.message", dto.accountId]).build()
        val shelter = shelterService.getById(dto.shelterId!!)
            ?: return BadRequest(error = messageService["error.NonExistentShelter.message", dto.shelterId]).build()
        if (account.shelter != null)
            return BadRequest(error = messageService["error.AccountIsAlreadyVolunteer.message", dto.accountId]).build()

        return accountService.setVolunteer(account, shelter).toAccountDTO().build()
    }

    @DeleteMapping("/volunteer/{accountId}")
    fun removeVolunteerByAccountId(
        @PathVariable accountId: Long,
    ): ResponseEntity<Response> {
        val user = SecurityContextHolder.getContext().authentication.principal
        if (user !is Admin)
            return Forbidden(error = messageService["error.ResourceForbidden.message"]).build()

        val account = accountService.getById(accountId)
            ?: return BadRequest(error = messageService["error.NonExistentAccount.message", accountId]).build()
        if (account.status == Account.Status.ANONYMOUS)
            return BadRequest(error = messageService["error.AccountIsAnonymous.message", accountId]).build()
        if (account.shelter == null)
            return BadRequest(error = messageService["error.AccountIsNotVolunteer.message", accountId]).build()

        return accountService.setVolunteer(account, null).toAccountDTO().build()
    }

    // endregion
}
