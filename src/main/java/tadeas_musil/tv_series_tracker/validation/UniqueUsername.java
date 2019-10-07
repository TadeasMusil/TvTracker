package tadeas_musil.tv_series_tracker.validation;

import org.springframework.stereotype.Component;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Component
@Documented
@Constraint(validatedBy = UniqueUsernameValidator.class)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueUsername {
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String message() default "User already exists";

    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        UniqueUsername[] value();
    }
}
