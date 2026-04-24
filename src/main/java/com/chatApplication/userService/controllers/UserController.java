package com.chatApplication.userService.controllers;

import com.chatApplication.userService.dto.LoginRequest;
import com.chatApplication.userService.dto.SignupRequest;
import com.chatApplication.userService.dto.AuthResponse;
import com.chatApplication.userService.dto.UserDataResponse;
import com.chatApplication.userService.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;

    // user sign up
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignupRequest signupRequest) {
        String userName = signupRequest.getUserName();
        String email = signupRequest.getEmail();
        String password = signupRequest.getPassword();

        return userService.signUp(userName, email, password);

    }

    // user login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        return userService.login(email, password);
    }

    @GetMapping("/getUser/{userId}")
    public ResponseEntity<UserDataResponse> getUserById(@PathVariable Integer userId) {
        return userService.getUserById(userId);
    }
}
