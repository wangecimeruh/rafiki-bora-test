package rafikibora.security.conf;


 import rafikibora.security.util.SecurityCipher;
 import rafikibora.services.CustomUserDetailsService;
 import rafikibora.services.TokenProviderI;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
 import org.springframework.util.StringUtils;
 import org.springframework.web.filter.OncePerRequestFilter;

 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;


 public class TokenAuthenticationFilter extends OncePerRequestFilter {
     @Value("${authentication-test.auth.accessTokenCookieName}")
     private String accessTokenCookieName;

     @Value("${authentication-test.auth.refreshTokenCookieName}")
     private String refreshTokenCookieName;

     @Autowired
     private TokenProviderI tokenProviderI;

     @Autowired
     private CustomUserDetailsService customUserDetailsService;

     @Override
     protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
         try {
             String jwt = getJwtToken(httpServletRequest, true);
             if (StringUtils.hasText(jwt) && tokenProviderI.validateToken(jwt)) {
                 String username = tokenProviderI.getUsernameFromToken(jwt);
                 UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                 UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                 authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                 SecurityContextHolder.getContext().setAuthentication(authentication);
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }

         filterChain.doFilter(httpServletRequest, httpServletResponse);
     }

     private String getJwtFromRequest(HttpServletRequest request) {
         String bearerToken = request.getHeader("Authorization");
         if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
             String accessToken = bearerToken.substring(7);
             if (accessToken == null) return null;

             return SecurityCipher.decrypt(accessToken);
         }
         return null;
     }

     private String getJwtFromCookie(HttpServletRequest request) {
         Cookie[] cookies = request.getCookies();
         for (Cookie cookie : cookies) {
             if (accessTokenCookieName.equals(cookie.getName())) {
                 String accessToken = cookie.getValue();
                 if (accessToken == null) return null;

                 return SecurityCipher.decrypt(accessToken);
             }
         }
         return null;
     }

     private String getJwtToken(HttpServletRequest request, boolean fromCookie) {
         if (fromCookie) return getJwtFromCookie(request);

         return getJwtFromRequest(request);
     }
 }
