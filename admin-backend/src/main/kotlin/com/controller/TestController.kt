package com.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/")
    fun home(): String {
        return "forward:/index.html"
    }

    @GetMapping("/test")
    fun test(): String {
        return "Test endpoint works!"
    }
}
