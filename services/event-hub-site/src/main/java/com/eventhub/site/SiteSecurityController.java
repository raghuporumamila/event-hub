package com.eventhub.site;

import com.eventhub.model.Organization;
import com.eventhub.model.User;
import com.eventhub.model.Workspace;
import com.eventhub.site.config.ApiEndPointUri;
import com.eventhub.site.form.RegistrationForm;
import com.eventhub.site.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

@Controller
@SessionAttributes("user")

@RequestMapping("/site/security/v1")
public class SiteSecurityController {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ApiEndPointUri apiEndPointUri;

    @Autowired
    CustomUserDetailsService customUserDetailsService;
    @GetMapping(value="/showLogin")
    public String showLogin() {
        return "login_bk";
    }

    /*
    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView login(@RequestParam(name="email") String email, @RequestParam(name="name") String name) {
        ModelAndView modelAndView = new ModelAndView();

        User user = customUserDetailsService.getUserDetails(email);

        modelAndView.addObject("user", user);
        if (user != null && user.getId() != null) {
            modelAndView.setViewName("dashboard");
        } else {
            RegistrationForm registrationForm = new RegistrationForm();
            registrationForm.setEmail(email);
            registrationForm.setName(name);
            modelAndView.addObject("registrationForm", registrationForm);
            modelAndView.setViewName("registration");
        }
        return modelAndView;
    }*/

    @PostMapping(value = "/register")
    public ModelAndView register(@ModelAttribute RegistrationForm registrationForm) {
        ModelAndView modelAndView = new ModelAndView();

        //User user = getUserDetails(registrationForm.getEmail(), registrationForm.getName());

        //modelAndView.addObject("user", user);
        ResponseEntity<Organization> orgResponse = restTemplate.postForEntity( apiEndPointUri.getDaoApiEndpoint() + "/organizations", registrationForm.getOrg() , Organization.class );
        assert orgResponse.getBody() != null;
        Long orgId = orgResponse.getBody().getId();
        User user = registrationForm.getUser(orgResponse.getBody(), null);
        ResponseEntity<User> userResponse = restTemplate.postForEntity( apiEndPointUri.getDaoApiEndpoint() + "/organizations/" + orgId + "/users", user , User.class );
        User userDetails = userResponse.getBody();
        if (userDetails != null && userDetails.getDefaultWorkspace() == null) {
            Workspace workspace = new Workspace();
            userDetails.setDefaultWorkspace(workspace);
        }
        modelAndView.addObject("user", userDetails);

        modelAndView.setViewName("dashboard");
        return modelAndView;
    }


}
