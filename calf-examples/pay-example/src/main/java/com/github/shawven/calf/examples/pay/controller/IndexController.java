package com.github.shawven.calf.examples.pay.controller;

import com.github.shawven.calf.examples.pay.support.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * @author Shoven
 * @date 2019-03-21 16:03
 */
@Controller
public class IndexController {

    @GetMapping(value = "/", produces = "text/html")
    public String indexHtml() throws IOException {
        return "index";
    }

    @GetMapping("/")
    public ResponseEntity indexBody() {
        return Response.ok("hello world");
    }

    @GetMapping("/hello")
    public String say(Model model) {
        model.addAttribute("message", "hello");
        return "redirect:/say";
    }

    @GetMapping("/hi")
    public String hi(RedirectAttributes model) {
        model.addAttribute("message", "hi");
        return "redirect:/say";
    }

    @GetMapping("/say")
    public ResponseEntity say(String message) {
        return Response.ok(message);
    }
}
