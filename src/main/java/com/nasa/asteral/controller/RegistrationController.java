package com.nasa.asteral.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.nasa.asteral.model.db.MyUser;
import com.nasa.asteral.model.request.RegistrationRequest;
import com.nasa.asteral.repository.MyUserRepository;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final MyUserRepository myUserRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registration") RegistrationRequest registration,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", bindingResult.getAllErrors().getFirst().getDefaultMessage());
            return "register";
        }

        String normalizedUsername = registration.getUsername().trim().toLowerCase(Locale.ROOT);
        if (normalizedUsername.length() < 3) {
            model.addAttribute("error", "Username must be between 3 and 50 characters.");
            return "register";
        }

        if (myUserRepository.findUserByUsernameIgnoreCase(normalizedUsername).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }

        MyUser newUser = new MyUser();
        newUser.setUsername(normalizedUsername);
        newUser.setPassword(passwordEncoder.encode(registration.getPassword()));
        newUser.setRole("USER");

        try {
            myUserRepository.saveAndFlush(newUser);
        } catch (DataIntegrityViolationException exception) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }

        try {
            request.login(normalizedUsername, registration.getPassword());
        } catch (ServletException e) {
            model.addAttribute("error", "Registration successful but auto-login failed.");
            return "login";
        }

        return "redirect:/";
    }
}
