package org.rescado.server.service

import org.hibernate.Hibernate
import org.locationtech.jts.geom.Point
import org.rescado.server.constant.SecurityConstants
import org.rescado.server.persistence.entity.Account
import org.rescado.server.persistence.entity.Session
import org.rescado.server.persistence.repository.SessionRepository
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.transaction.Transactional

@Service
@Transactional
class SessionService(
    private val sessionRepository: SessionRepository,
) {

    fun getByToken(token: String): Session? {
        return sessionRepository.findByRefreshToken(token)
    }

    fun getInitializedByToken(token: String): Session? {
        val session = this.getByToken(token) ?: return null
        Hibernate.initialize(session.account)
        return session
    }

    fun getByAccount(account: Account): List<Session> {
        return sessionRepository.findByAccount(account)
    }

    fun getNumberOfActiveSessions(): Int {
        val oldestPossibleRefreshDate = ZonedDateTime.now(ZoneId.systemDefault())
            .minus(SecurityConstants.REFRESH_TTL, ChronoUnit.HOURS)
        return sessionRepository.findAllByLastLoginAfter(oldestPossibleRefreshDate).size
    }

    fun create(account: Account, device: String, agent: String, ipAddress: String, geometry: Point?): Session {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val session = Session(
            account = account,
            refreshToken = UUID.randomUUID().toString(),
            device = device,
            agent = agent,
            firstLogin = now,
            lastLogin = now,
            ipAddress = ipAddress,
            geometry = geometry,
        )
        return sessionRepository.save(session)
    }

    fun refresh(session: Session, device: String, agent: String, ipAddress: String, geometry: Point?): Session? {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        if (session.lastLogin.plus(SecurityConstants.REFRESH_TTL, ChronoUnit.HOURS).isBefore(now)) {
            // token too old -- not safe.
            this.delete(session)
            return null
        }

        session.device = device
        session.agent = agent
        session.lastLogin = now
        session.ipAddress = ipAddress
        session.geometry = geometry
        return sessionRepository.save(session)
    }

    fun delete(session: Session) {
        sessionRepository.delete(session)
    }
}
