package com.nasa.asteral.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nasa.asteral.model.Authority;
import com.nasa.asteral.model.MyMenu;


@Service
public class MenuService {

    private static final List<MyMenu> ANONYMOUS_MENU = Arrays.asList(
            new MyMenu("Asteroid Feed", "/"),
            new MyMenu("Login", "/login")
    );

    private static final List<MyMenu> USER_MENU = Arrays.asList(
            new MyMenu("Asteroid Feed", "/"),
            new MyMenu("My Favorites", "/user/favorite/all"),
            new MyMenu("My Profile", "/user/profile")
    );

    public List<MyMenu> retrieveMenuByAuthority(String authority) {
    	return Authority.USER.getValue().equals(authority) ? USER_MENU : ANONYMOUS_MENU;
    }


}
