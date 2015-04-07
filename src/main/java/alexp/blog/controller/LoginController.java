package alexp.blog.controller;

import alexp.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    // POST request must be used for logout if CSRF enabled, so this page contains hidden form to submit via JS
    @RequestMapping(value = "/logout", method= RequestMethod.GET)
    public String logout() {
        if (!userService.isAuthenticated()) {
            return "redirect:posts";
        }

        return "logout";
    }
}
