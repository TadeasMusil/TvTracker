package tadeas_musil.tv_series_tracker.validation;

import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class EmptyOrAtLeast8CharactersValidator implements ConstraintValidator<EmptyOrAtLeast8Characters, String> {

    @Override
    public void initialize(EmptyOrAtLeast8Characters constraintAnnotation) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {

        return StringUtils.isEmpty(password) || password.length() >= 8;

    }
}
