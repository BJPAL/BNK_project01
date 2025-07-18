package com.example.fund.auth.controller;

import com.example.fund.user.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private static final String SESSION_KEY = "user";

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        User user = (User) session.getAttribute(SESSION_KEY);

        if (user != null) {
            model.addAttribute("name", user.getName());

            int maxInactiveInterval = session.getMaxInactiveInterval(); // 초 단위
            long lastAccessedTime = session.getLastAccessedTime();
            long currentTime = System.currentTimeMillis();
            long remaining = (lastAccessedTime + (maxInactiveInterval * 1000L)) - currentTime;

            if (remaining > 0) {

                model.addAttribute("sessionRemaining", remaining / 1000 - 1);
            }
        }

        return "home"; // templates/home.html
    }

    @GetMapping("/user/")
    public String userPage() {
        return "user/index";
    }
}
