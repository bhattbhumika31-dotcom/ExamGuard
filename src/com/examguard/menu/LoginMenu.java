package com.examguard.menu;

import com.examguard.model.User;
import com.examguard.service.AuthService;
import java.util.Scanner;

public class LoginMenu {

    public User showLogin() {
        Scanner sc = new Scanner(System.in);
        AuthService authService = new AuthService();

        System.out.print("Username: ");
        String username = sc.nextLine();

        System.out.print("Password: ");
        String password = sc.nextLine();

        return authService.login(username, password);
    }
}
