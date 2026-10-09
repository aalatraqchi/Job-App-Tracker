package com.ashraf.jobapptracker.web;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class RequestProtectionController {
    @GetMapping("/csrf")
    @ResponseBody
    public ResponseEntity<TokenResponse> csrf(CsrfToken token) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(new TokenResponse(token.getToken(), token.getHeaderName(), token.getParameterName()));
    }

    @RequestMapping("/request-expired")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String requestExpired() {
        return "tracker/request-expired";
    }

    public record TokenResponse(String token, String headerName, String parameterName) { }
}
