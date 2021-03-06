package org.rescado.server.controller.dto.validation

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [InRangeValidator::class])
annotation class InRange(
    val message: String = "{validation.InRange.message}",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = [],

    val min: Double,
    val max: Double,
)

class InRangeValidator : ConstraintValidator<InRange, Double> {

    private var min = 0.0
    private var max = 0.0

    override fun initialize(constraintAnnotation: InRange) {
        this.min = constraintAnnotation.min
        this.max = constraintAnnotation.max
    }

    override fun isValid(value: Double?, context: ConstraintValidatorContext?): Boolean {
        return if (value == null) true else value in this.min..this.max
    }
}
