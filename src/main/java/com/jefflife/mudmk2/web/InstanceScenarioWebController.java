package com.jefflife.mudmk2.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class InstanceScenarioWebController {

    @GetMapping("/instance-scenario-management")
    public String instanceScenarioManagementPage(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            model.addAttribute("userName", principal.getAttribute("name"));
            model.addAttribute("userPicture", principal.getAttribute("picture"));
        }
        return "web/instance-scenario-management";
    }
}
