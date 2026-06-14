package com.nasa.asteral.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nasa.asteral.model.db.MyUser;
import com.nasa.asteral.repository.MyUserRepository;

import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.Size;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@Validated
public class RegistrationController {

    private final MyUserRepository myUserRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam @Size(min = 3, max = 50) String username,
            @RequestParam @Size(min = 12, max = 72) String password,
            HttpServletRequest request,
            Model model) {

        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        if (myUserRepository.findUserByUsernameIgnoreCase(normalizedUsername).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }

        MyUser newUser = new MyUser();
        newUser.setUsername(normalizedUsername);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole("USER");

        try {
            myUserRepository.saveAndFlush(newUser);
        } catch (DataIntegrityViolationException exception) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }

        try {
            request.login(normalizedUsername, password);
        } catch (ServletException e) {
            model.addAttribute("error", "Registration successful but auto-login failed.");
            return "login";
        }

        return "redirect:/";
    }
}
