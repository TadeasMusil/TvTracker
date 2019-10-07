package tadeas_musil.tv_series_tracker.validation;

import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String firstFieldName;
    private String secondFieldName;
    private String message;

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {

        firstFieldName = constraintAnnotation.first();
        secondFieldName = constraintAnnotation.second();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        final Object firstFieldValue = new BeanWrapperImpl(value).getPropertyValue(firstFieldName);
        final Object secondFieldValue = new BeanWrapperImpl(value).getPropertyValue(secondFieldName);
        boolean valid = firstFieldValue == null && secondFieldValue == null
                || firstFieldValue != null && firstFieldValue.equals(secondFieldValue);
        if (!valid) {
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(firstFieldName)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
        }
        return valid;

    }
}
