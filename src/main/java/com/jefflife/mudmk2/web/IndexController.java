package com.jefflife.mudmk2.web;

import com.jefflife.mudmk2.config.auth.LoginUser;
import com.jefflife.mudmk2.config.auth.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class IndexController {

    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user) {
        if (user != null) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("userPicture", user.getPicture());
            // Redirect to chat page if user is logged in
            return "redirect:/chat";
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/profile")
    public String profile(Model model, @LoginUser SessionUser user) {
        if (user != null) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("userPicture", user.getPicture());
        }
        return "profile";
    }

    @GetMapping("/chat")
    public String chat(Model model, @LoginUser SessionUser user) {
        if (user != null) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("userPicture", user.getPicture());
        }
        return "chat";
    }
}
