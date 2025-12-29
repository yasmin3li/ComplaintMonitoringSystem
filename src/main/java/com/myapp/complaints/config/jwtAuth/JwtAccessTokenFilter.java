package com.myapp.complaints.config.jwtAuth;

import com.myapp.complaints.config.RSAKeyRecord;
import com.myapp.complaints.enums.TokenType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;
@Configuration
@RequiredArgsConstructor
@Slf4j
public class JwtAccessTokenFilter extends OncePerRequestFilter {
    private final RSAKeyRecord rsaKeyRecord;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException, JwtValidationException {
        try{

            log.warn("[JwtAccessTokenFilter:doFilterInternal] :: Started ");
            log.warn("[JwtAccessTokenFilter:doFilterInternal]Filtering the Http Request:{}",request.getRequestURI());

            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            log.warn(authHeader);
            JwtDecoder jwtDecoder =  NimbusJwtDecoder.withPublicKey(rsaKeyRecord.rsaPublicKey()).build();

            if (authHeader == null) {
                filterChain.doFilter(request,response);
                return;
            }else if (!authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String token = authHeader.substring(7);
            final Jwt jwtToken = jwtDecoder.decode(token);
            final String userName = jwtTokenUtils.getUserName(jwtToken);


//TODO
//            String uri = request.getRequestURI();
//            if (account.isMustChangePassword()
//                    && !uri.equals("/auth/change-password")
//                    && !uri.equals("/auth/logout")) {
//                response.sendError(HttpServletResponse.SC_FORBIDDEN,
//                        "You must change your password first");
//                return;
//            }


//after we passed previous tow filter we are now know that is of course there are Authorisation that is Bearer token
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            if(!userName.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null)
            {

                UserDetails userDetails = jwtTokenUtils.userDetails(userName);
                boolean validFlag = jwtTokenUtils.isTokenValid(jwtToken,userDetails) ;
                List<SimpleGrantedAuthority> scopeAuthorities = Arrays.stream(jwtToken.getClaimAsString("scope").split(" "))
                        .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                        .toList();

                List<GrantedAuthority> finalAuthorities = new ArrayList<>();
                finalAuthorities.addAll(userDetails.getAuthorities()); // Roles
                finalAuthorities.addAll(scopeAuthorities);             // SCOPEs

                if(validFlag){
                    System.out.println("im here ************************TokenValid*****************************************");
                    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                    UsernamePasswordAuthenticationToken createdToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            finalAuthorities
                    );
                    createdToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    securityContext.setAuthentication(createdToken);
                    SecurityContextHolder.setContext(securityContext);
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    log.warn("\n" +
                            " AUTH = {}", auth);

                }
            }
            log.warn("[JwtAccessTokenFilter:doFilterInternal] Completed");

            filterChain.doFilter(request,response);
        }catch (JwtValidationException jwtValidationException){
            log.error("[JwtAccessTokenFilter:doFilterInternal] Exception due to :{}", jwtValidationException.getMessage());
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException(jwtValidationException.getMessage(), jwtValidationException));
        }
    }
}

//denied is not here but in SecurityFilterChain