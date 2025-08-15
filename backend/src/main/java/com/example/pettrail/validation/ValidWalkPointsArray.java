package com.example.pettrail.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = WalkPointsArrayValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidWalkPointsArray {
    String message() default "Invalid walk points array";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
