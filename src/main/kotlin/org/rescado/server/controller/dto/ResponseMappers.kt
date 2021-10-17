package org.rescado.server.controller.dto

import org.locationtech.jts.geom.Point
import org.rescado.server.controller.dto.res.AccountDTO
import org.rescado.server.controller.dto.res.AdminDTO
import org.rescado.server.controller.dto.res.AnimalDTO
import org.rescado.server.controller.dto.res.AuthenticationDTO
import org.rescado.server.controller.dto.res.CoordinatesDTO
import org.rescado.server.controller.dto.res.Response
import org.rescado.server.controller.dto.res.SessionDTO
import org.rescado.server.controller.dto.res.ShelterDTO
import org.rescado.server.persistence.entity.Account
import org.rescado.server.persistence.entity.Admin
import org.rescado.server.persistence.entity.Animal
import org.rescado.server.persistence.entity.Session
import org.rescado.server.persistence.entity.Shelter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.Duration
import java.time.ZonedDateTime

// region Response mappers

fun Response.build(httpStatusOverride: HttpStatus? = null) = ResponseEntity(
    this,
    this.httpHeaders,
    httpStatusOverride ?: this.httpStatus
)

fun List<Response>.build(httpStatusOverride: HttpStatus? = null) = ResponseEntity(
    this,
    this.firstOrNull()?.httpHeaders ?: HttpHeaders.EMPTY,
    httpStatusOverride ?: this.firstOrNull()?.httpStatus ?: HttpStatus.OK
)
// endregion
// region Point mappers

fun Point.toCoordinatesDTO() = CoordinatesDTO(
    latitude = y,
    longitude = x,
)
// endregion
// region Account mappers

fun Account.toNewAccountDTO(authorization: String) = AuthenticationDTO(
    httpStatus = HttpStatus.CREATED,
    authorization = authorization,
    status = status.name,
    uuid = uuid,
    email = email,
    name = name,
    shelter = shelter?.toShelterDTO(true),
)

fun Account.toAuthenticationDTO(authorization: String) = AuthenticationDTO(
    authorization = authorization,
    status = status.name,
    uuid = uuid,
    email = email,
    name = name,
    shelter = shelter?.toShelterDTO(true),
)

fun Account.toAccountDTO() = AccountDTO(
    status = status.name,
    uuid = uuid,
    email = email,
    name = name,
    shelter = shelter?.toShelterDTO(true),
)
// endregion
// region Admin mappers

fun Admin.toAdminDTO() = AdminDTO(
    username = username,
)

// endregion
// region Session mappers

fun Session.toSessionDTO() = SessionDTO(
    refreshToken = refreshToken,
    agent = agent,
    firstLogin = firstLogin,
    lastLogin = lastLogin,
    ipAddress = ipAddress,
    coordinates = geometry?.toCoordinatesDTO(),
)

fun List<Session>.toSessionArrayDTO() = this.map { it.toSessionDTO() }
// endregion
// region Shelter mappers

fun Shelter.toShelterDTO(shortVersion: Boolean = false) = if (shortVersion) ShelterDTO(
    id = id,
    name = name,
    city = city,
    country = country,
    coordinates = geometry.toCoordinatesDTO(),
    logo = logo.reference,
) else ShelterDTO(
    id = id,
    name = name,
    email = email,
    website = website,
    newsfeed = newsfeed,
    address = address,
    postalCode = postalCode,
    city = city,
    country = country,
    coordinates = geometry.toCoordinatesDTO(),
    logo = logo.reference,
    banner = banner?.reference,
)

fun List<Shelter>.toShelterArrayDTO() = this.map { it.toShelterDTO() }
// endregion
// region Animal mappers

fun Animal.toAnimalDTO(now: ZonedDateTime? = null) = if (now == null) AnimalDTO(
    id = id,
    name = name,
    kind = kind.name,
    breed = breed,
    sex = sex.name,
    photos = photos.map { it.reference },
    shelter = shelter.toShelterDTO(true),
)else AnimalDTO(
    id = id,
    name = name,
    description = description,
    kind = kind.name,
    breed = breed,
    sex = sex.name,
    age = (Duration.between(birthday, now).toDays() / 365).toInt(),
    weight = weight,
    vaccinated = vaccinated,
    sterilized = sterilized,
    photos = photos.map { it.reference },
    shelter = shelter.toShelterDTO(true),
)

fun List<Animal>.toAnimalArrayDTO(now: ZonedDateTime) = this.map { it.toAnimalDTO(now) }
// endregion
