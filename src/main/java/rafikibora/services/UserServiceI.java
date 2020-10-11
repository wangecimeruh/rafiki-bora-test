package rafikibora.services;

import org.springframework.http.ResponseEntity;
import rafikibora.dto.*;

public interface UserServiceI {
    ResponseEntity<LoginResponse> login2(LoginRequest loginRequest, String accessToken, String refreshToken);

    ResponseEntity<LoginResponse> refresh(String accessToken, String refreshToken);

    AuthenticationResponse login(LoginRequest loginRequest) throws Exception;

    void save(UserDto user);


    UserSummary getUserProfile();
}
