package tadeas_musil.tv_series_tracker.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tadeas_musil.tv_series_tracker.validation.PasswordMatch;
import tadeas_musil.tv_series_tracker.validation.UniqueUsername;
import tadeas_musil.tv_series_tracker.model.User.Registration;
import tadeas_musil.tv_series_tracker.model.User.Settings;
import tadeas_musil.tv_series_tracker.validation.EmptyOrAtLeast8Characters;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@PasswordMatch(groups = { Settings.class, Registration.class }, first = "password", second = "confirmPassword")
@Table(name = "app_user")
public class User {
    
    public interface Registration {
    };

    public interface Settings {
    };

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Enter email", groups = { Registration.class })
    @Email(message = "Invalid email format", groups = { Registration.class })
    @UniqueUsername(groups = { Registration.class })
    private String username;

    @NotBlank(message = "Enter password", groups = { Registration.class })
    @Size(min = 8, message = "Password must have at least 8 characters", groups = { Registration.class })
    @EmptyOrAtLeast8Characters(groups = { Settings.class })
    private String password;

    @Transient
    private String confirmPassword;

    @Column(name = "daily_notification")
    private boolean dailyScheduleNotification = true;

    @Column(name = "new_show_notification")
    private boolean newShowNotification = true;

    @ManyToMany(fetch = FetchType.LAZY ,cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "app_user_show",
                joinColumns = @JoinColumn(name = "app_user_id"),
                inverseJoinColumns = @JoinColumn(name = "show_trakt_id"))
    private Set<Show> followedShows = new HashSet<>();
}
