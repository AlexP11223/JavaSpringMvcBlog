package alexp.blog.controller;

import alexp.blog.model.User;
import alexp.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private Validator userValidator;

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String showRegistrationForm(ModelMap model, HttpServletRequest request, HttpSession session) {
        model.addAttribute("user", new User());

        if (userService.isAuthenticated()) {
            return "redirect:posts";
        }

        String ref = request.getHeader("referer");

        if (!ref.contains("/register"))
            session.setAttribute("regRef", ref);

        return "registration";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String registerUser(@Valid @ModelAttribute(value = "user") User user, BindingResult result, HttpSession session) {
        userValidator.validate(user, result);

        if (result.hasErrors())
        {
            return "registration";
        }

        userService.register(user);

        userService.authenticate(user);

        Object regRef = session.getAttribute("regRef");

        return "redirect:" + (StringUtils.isEmpty(regRef) ? "posts" : regRef.toString());
    }

    @RequestMapping(value = "/check_email", method = RequestMethod.GET)
    public @ResponseBody String checkEmail(@RequestParam("email") String email) {
        return userService.emailExists(email) ? "false" : "true";
    }

    @RequestMapping(value = "/check_username", method = RequestMethod.GET)
    public @ResponseBody String checkUsername(@RequestParam("username") String username) {
        return userService.usernameExists(username) ? "false" : "true";
    }
}
