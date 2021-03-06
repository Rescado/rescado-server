package org.rescado.server.persistence.entity

import org.rescado.server.persistence.CompositeAccountAnimalId
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "likes")
@IdClass(CompositeAccountAnimalId::class)
open class Like(

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    open var account: Account,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", referencedColumnName = "id")
    open var animal: Animal,

    @Column(name = "timestamp")
    open var timestamp: ZonedDateTime,

    @Column(name = "reference")
    open var reference: String?,

    @Column(name = "unread_count")
    open var unreadCount: Int,
)
