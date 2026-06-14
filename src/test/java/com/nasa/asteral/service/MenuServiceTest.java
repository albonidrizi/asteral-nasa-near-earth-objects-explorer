package com.nasa.asteral.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nasa.asteral.model.Authority;
import com.nasa.asteral.model.MyMenu;

class MenuServiceTest {

    private MenuService menuService;

    @BeforeEach
    void setUp() {
        menuService = new MenuService();
    }

    @Test
    void retrieveMenuByAuthority_withUserAuthority_shouldReturnUserMenu() {
        // Act
        List<MyMenu> menu = menuService.retrieveMenuByAuthority(Authority.USER.getValue());

        // Assert
        assertNotNull(menu);
        assertEquals(3, menu.size());
        assertEquals("Asteroid Feed", menu.get(0).getLabel());
        assertEquals("/", menu.get(0).getLink());
        assertEquals("My Favorites", menu.get(1).getLabel());
        assertEquals("/user/favorite/all", menu.get(1).getLink());
        assertEquals("My Profile", menu.get(2).getLabel());
        assertEquals("/user/profile", menu.get(2).getLink());
    }

    @Test
    void retrieveMenuByAuthority_withAnonymousAuthority_shouldReturnAnonymousMenu() {
        // Act
        List<MyMenu> menu = menuService.retrieveMenuByAuthority(Authority.ANONYMOUS.getValue());

        // Assert
        assertNotNull(menu);
        assertEquals(2, menu.size());
        assertEquals("Asteroid Feed", menu.get(0).getLabel());
        assertEquals("/", menu.get(0).getLink());
        assertEquals("Login", menu.get(1).getLabel());
        assertEquals("/login", menu.get(1).getLink());
    }

    @Test
    void retrieveMenuByAuthority_withNullAuthority_shouldReturnAnonymousMenu() {
        // Act
        List<MyMenu> menu = menuService.retrieveMenuByAuthority(null);

        // Assert
        assertNotNull(menu);
        assertEquals(2, menu.size());
        assertEquals("Asteroid Feed", menu.get(0).getLabel());
        assertEquals("Login", menu.get(1).getLabel());
    }

    @Test
    void retrieveMenuByAuthority_withUnknownAuthority_shouldReturnAnonymousMenu() {
        // Act
        List<MyMenu> menu = menuService.retrieveMenuByAuthority("UNKNOWN_ROLE");

        // Assert
        assertNotNull(menu);
        assertEquals(2, menu.size());
        assertEquals("Asteroid Feed", menu.get(0).getLabel());
        assertEquals("Login", menu.get(1).getLabel());
    }
}
