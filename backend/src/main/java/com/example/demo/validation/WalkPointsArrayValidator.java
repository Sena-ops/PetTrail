package com.example.demo.validation;

import com.example.demo.dto.WalkPointRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class WalkPointsArrayValidator implements ConstraintValidator<ValidWalkPointsArray, List<WalkPointRequest>> {

    @Override
    public boolean isValid(List<WalkPointRequest> points, ConstraintValidatorContext context) {
        if (points == null) {
            return false;
        }
        
        // Check if array is empty
        if (points.isEmpty()) {
            addConstraintViolation(context, "Payload must have 1..5000 points.");
            return false;
        }
        
        // Check if array is too large
        if (points.size() > 5000) {
            addConstraintViolation(context, "Payload must have 1..5000 points.");
            return false;
        }
        
        return true;
    }
    
    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
