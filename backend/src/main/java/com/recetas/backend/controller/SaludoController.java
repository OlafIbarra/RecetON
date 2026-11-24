package com.recetas.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaludoController {

    @GetMapping("/saludo")
    public String saludo() {
        return "Holaaaaa que hace";
    }

    @GetMapping("/adios")
    public String adios() {
        return "Adios";
    }
}