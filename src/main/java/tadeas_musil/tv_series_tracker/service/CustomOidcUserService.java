package tadeas_musil.tv_series_tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import tadeas_musil.tv_series_tracker.model.GoogleUserInfo;
import tadeas_musil.tv_series_tracker.model.User;
import tadeas_musil.tv_series_tracker.repository.UserRepository;

@Service
public class CustomOidcUserService extends OidcUserService {

  @Autowired
  private UserRepository userRepository;

  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
    OidcUser oidcUser = super.loadUser(userRequest);

    try {
      return processOidcUser(userRequest, oidcUser);
    } catch (Exception ex) {
      throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
    }
  }

  private OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) {
    GoogleUserInfo googleUserInfo = new GoogleUserInfo(oidcUser.getAttributes());

    User user = userRepository.findByUsername(googleUserInfo.getEmail());
    if (user == null) {
      user = new User();
      user.setUsername(googleUserInfo.getEmail());
      userRepository.save(user);
    }
    // Here we are changing nameAttributeKey to "email", so when we call Principal.getName() we get the user's email
    return new DefaultOidcUser(oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo(), "email");
  }
}