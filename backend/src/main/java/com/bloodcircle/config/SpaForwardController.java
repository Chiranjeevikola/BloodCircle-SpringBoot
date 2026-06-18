package com.bloodcircle.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Forwards all non-API, non-static routes to React's index.html
 * so that client-side routing (React Router) works correctly.
 */
@Controller
public class SpaForwardController {

    @RequestMapping(value = {
            "/",
            "/login",
            "/register",
            "/forgot-password",
            "/select-role",
            "/about",
            "/how-it-works",
            "/blood-compatibility",
            "/faq",
            "/feedback",
            "/privacy-policy",
            "/donor/**",
            "/patient/**",
            "/admin/**",
            "/search/**",
            "/dashboard",
            "/profile"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
