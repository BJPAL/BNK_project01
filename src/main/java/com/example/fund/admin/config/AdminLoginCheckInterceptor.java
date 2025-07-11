package com.example.fund.admin.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class AdminLoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        HttpSession session = request.getSession(false); // false: session이 없을 시 null 반환


        if(session == null || session.getAttribute("admin") == null){
            System.out.println("인터셉터 감지 URI: " + request.getRequestURI());
            response.sendRedirect("/admin/?error=needLogin"); //로그인 정보가 없을 시 login 화면으로 이동
            return false;
        }

        return true;
    }

}
