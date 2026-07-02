package com.project.anhgagifcode.infrastructure.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping({"", "/"})
    public String index() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/eggs")
    public String eggs() {
        return "admin/eggs";
    }

    @GetMapping("/early-hatch")
    public String earlyHatch() {
        return "admin/early-hatch";
    }

    @GetMapping("/customers")
    public String customers() {
        return "admin/customers";
    }

    @GetMapping("/gift-accounts")
    public String giftAccounts() {
        return "admin/gift-accounts";
    }

    @GetMapping("/gift-pools")
    public String giftPools() {
        return "admin/gift-pools";
    }

    @GetMapping("/products")
    public String products() {
        return "admin/products";
    }

    @GetMapping("/settings")
    public String settings() {
        return "admin/settings";
    }
}
