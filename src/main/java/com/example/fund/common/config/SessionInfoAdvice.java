package com.example.fund.common.config;

import com.example.fund.user.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class SessionInfoAdvice {

    @ModelAttribute
    public void addSessionAttributes(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("session", session); // 필요 시 접근 가능
            model.addAttribute("sessionUser", user);

            int maxInactiveInterval = session.getMaxInactiveInterval();
            long lastAccessedTime = session.getLastAccessedTime();
            long currentTime = System.currentTimeMillis();
            long remaining = (lastAccessedTime + (maxInactiveInterval * 1000L)) - currentTime;

            if (remaining > 0) {
                model.addAttribute("sessionRemaining", remaining / 1000); // 초 단위
            }
        }
    }
}
