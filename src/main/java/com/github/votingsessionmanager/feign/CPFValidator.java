package com.github.votingsessionmanager.feign;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Component
public interface CPFValidator {
    @GetMapping("/users/{cpf}")
    @ResponseBody
    ObjectNode validateCPF(@PathVariable String cpf);
}
