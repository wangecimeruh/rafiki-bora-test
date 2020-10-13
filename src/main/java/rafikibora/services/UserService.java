package rafikibora.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rafikibora.dto.*;
import rafikibora.model.users.User;
import rafikibora.repository.RoleRepository;
import rafikibora.repository.UserRepository;
import rafikibora.security.util.exceptions.RafikiBoraException;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class UserService implements UserServiceI {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;
    private final JwtProviderI jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;


    @Transactional
    public ResponseEntity<SignupResponse> save(UserDto user) {
        SignupResponse signupResponse;
        User newUser = new User();
//        Roles roles = new Roles();
//        roles.setRoleName("ADMIN");
//        roleRepository.save(roles);
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
        newUser.setEmail(user.getEmail());
        newUser.setPhoneNo(user.getPhoneNo());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        //newUser.getRoles().add(roles);
        try{
            userRepository.save(newUser);
            signupResponse = new SignupResponse(SignupResponse.responseStatus.SUCCESS, "Registration Successful");
            return ResponseEntity.status(HttpStatus.CREATED).body(signupResponse);
        }catch (Exception e){
            log.error(e.getMessage());
            signupResponse = new SignupResponse(SignupResponse.responseStatus.FAILED, "Registration Failed");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(signupResponse);
            //throw new RafikiBoraException("Something went wrong");
        }
    }



    @Override
    public ResponseEntity<AuthenticationResponse> login(LoginRequest loginRequest) {
        AuthenticationResponse authResponse;
        try {
            authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        }catch (Exception ex){
            authResponse = new AuthenticationResponse(AuthenticationResponse.responseStatus.FAILED, ex.getMessage(),null,null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
        }

        final UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginRequest.getEmail());
        String token = jwtProvider.generateToken(userDetails);
        boolean validateToken = jwtProvider.validateToken(token);
        if(!validateToken){
            jwtProvider.generateToken(userDetails);
        }
        authResponse = new AuthenticationResponse(AuthenticationResponse.responseStatus.SUCCESS, "Successful Login",token,loginRequest.getEmail());
        return ResponseEntity.ok().body(authResponse);
    }

    private void authenticate(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException e) {
            throw new RafikiBoraException("User is Disabled");
        } catch (BadCredentialsException e) {
            throw new RafikiBoraException("Invalid Credentials");
        }
    }

    @Override
    public UserSummary getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(customUserDetails.getUsername()).orElseThrow(() -> new IllegalArgumentException("User not found with email " + customUserDetails.getUsername()));
        return user.toUserSummary();
    }

    @Override
    public String deleteUser(int id) {
        userRepository.deleteById((long) id);
        return "user removed !! " + id;
    }

    @Override
    public Set<User> getUserByRole(String roleName) {

        Set<User> users = userRepository.findByRoles_RoleNameContainingIgnoreCase(roleName);

        return users;
    }


//    @Override
//     public ResponseEntity<LoginResponse> login2(LoginRequest loginRequest, String accessToken, String refreshToken) {
//         String email = loginRequest.getEmail();
//         User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found with email " + email));
//
//         Boolean accessTokenValid = tokenProviderI.validateToken(accessToken);
//         Boolean refreshTokenValid = tokenProviderI.validateToken(refreshToken);
//
//         HttpHeaders responseHeaders = new HttpHeaders();
//         Token newAccessToken;
//         Token newRefreshToken;
//         if (!accessTokenValid && !refreshTokenValid) {
//             newAccessToken = tokenProviderI.generateAccessToken(user);
//             newRefreshToken = tokenProviderI.generateRefreshToken(user);
//             addAccessTokenCookie(responseHeaders, newAccessToken);
//             addRefreshTokenCookie(responseHeaders, newRefreshToken);
//         }
//
//         if (!accessTokenValid && refreshTokenValid) {
//             newAccessToken = tokenProviderI.generateAccessToken(user);
//             addAccessTokenCookie(responseHeaders, newAccessToken);
//         }
//
//         if (accessTokenValid && refreshTokenValid) {
//             newAccessToken = tokenProviderI.generateAccessToken(user);
//             newRefreshToken = tokenProviderI.generateRefreshToken(user);
//             addAccessTokenCookie(responseHeaders, newAccessToken);
//             addRefreshTokenCookie(responseHeaders, newRefreshToken);
//         }
//
//         LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.SUCCESS, "Successful Login");
//         return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);
//
//     }

//     @Override
//     public ResponseEntity<LoginResponse> refresh(String accessToken, String refreshToken) {
//         Boolean refreshTokenValid = tokenProviderI.validateToken(refreshToken);
//         if (!refreshTokenValid) {
//             LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.FAILURE, "Invalid refresh token !");
//             return ResponseEntity.unprocessableEntity().body(loginResponse);
//         }
//
//         String currentUserEmail = tokenProviderI.getUsernameFromToken(accessToken);
//         User user = userRepository.findByEmail(currentUserEmail).orElseThrow(() -> new IllegalArgumentException("User not found with username " + currentUserEmail));
//
//         Token newAccessToken = tokenProviderI.generateAccessToken(user);
//         HttpHeaders responseHeaders = new HttpHeaders();
//         responseHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(newAccessToken.getTokenValue(), newAccessToken.getDuration()).toString());
//
//         LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.SUCCESS, "Successful Login");
//         return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);
//     }

    //    private void addAccessTokenCookie(HttpHeaders httpHeaders, Token token) {
//        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(token.getTokenValue(), token.getDuration()).toString());
//    }
//
//    private void addRefreshTokenCookie(HttpHeaders httpHeaders, Token token) {
//        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(token.getTokenValue(), token.getDuration()).toString());
//    }

}
