package com.example.srping_boot_api_calculator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
public class CalculatorController {

    @GetMapping("calculate")
    public Integer plus (@RequestParam String operator,@RequestParam Integer number1,@RequestParam Integer number2) throws Exception {
        switch (operator) {
            case "+": return number1 + number2;
            case "-": return number1 - number2;
            case "/": return number1 / number2;
            case "*": return number1 * number2;
            default: throw new Exception("Unknow Operator");
        }
    }
}
