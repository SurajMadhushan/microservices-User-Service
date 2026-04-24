package com.chatApplication.userService.services;

import com.chatApplication.userService.dto.AuthResponse;
import com.chatApplication.userService.dto.UserDataResponse;
import com.chatApplication.userService.models.User;
import com.chatApplication.userService.repositories.UserRepository;
import com.chatApplication.userService.security.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    // user sign up
    public ResponseEntity<AuthResponse> signUp(String userName, String email, String password) {

        AuthResponse authResponse = new AuthResponse();

        if(userName == null || email == null || password == null) {
            authResponse.setMessage("All fields are required");
            return new ResponseEntity<>(authResponse, HttpStatus.BAD_REQUEST );
        }

        // encrypt password
        String passwordEncoded = passwordEncoder().encode(password);

        User user = new User();
        user.setUsername(userName);
        user.setEmail(email);
        user.setPassword(passwordEncoded);

        try {
            userRepository.save(user);
        }
        catch (Exception e) {
            System.out.println("Error saving user: " + e.getMessage());

            authResponse.setMessage("Error occured");
            authResponse.setToken(null);
            return new ResponseEntity<>(authResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String token = generateJwtToken(email);
        authResponse.setToken(token);
        authResponse.setMessage("Signup successful");
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @Bean
    private BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    JwtUtil jwtUtil;

    // jwt token generation
    private String generateJwtToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(jwtUtil.getKey())   // ✅ FIXED
                .compact();
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                                .requestMatchers("/users/signup").permitAll()
//                                .requestMatchers("/users/login").permitAll()
//                                .anyRequest().authenticated()
//                        );
//        return http.build();
//    }

    // user login
    public ResponseEntity<AuthResponse> login(String email, String password) {
        AuthResponse authResponse = new AuthResponse();

        if(email == null || password == null) {
            authResponse.setMessage("All fields are required");
            return new ResponseEntity<>(authResponse, HttpStatus.BAD_REQUEST );
        }

        User user = userRepository.findByEmail(email);
        if(user == null) {
            authResponse.setMessage("User not found");
            return new ResponseEntity<>(authResponse, HttpStatus.NOT_FOUND);
        }

        if(!passwordEncoder().matches(password, user.getPassword())) {
            authResponse.setMessage("Invalid credentials");
            return new ResponseEntity<>(authResponse, HttpStatus.UNAUTHORIZED);
        }
        try {
            String token = generateJwtToken(email);
            authResponse.setToken(token);
            authResponse.setMessage("Login successful");
            return new ResponseEntity<>(authResponse, HttpStatus.OK);
        }
        catch (Exception e) {
            System.out.println("Error in login: " + e.getMessage());
            authResponse.setMessage("Error occured");
            return new ResponseEntity<>(authResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<UserDataResponse> getUserById(Integer userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                //return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
                return new ResponseEntity<>(
                        new UserDataResponse(0, null, null),
                        HttpStatus.NOT_FOUND
                );
            }

            UserDataResponse userData = new UserDataResponse();
            userData.setId(user.getId());
            userData.setUsername(user.getUsername());
            userData.setEmail(user.getEmail());

            return new ResponseEntity<>(userData, HttpStatus.OK);
        }
        catch (Exception e) {
            System.out.println("Error fetching user: " + e.getMessage());
            return new ResponseEntity<>(new UserDataResponse(0, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
