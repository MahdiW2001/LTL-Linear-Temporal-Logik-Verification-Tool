package com.ltl.ltl.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ltl.ltl.ltl.web.dto.TableauNodeDto;
import com.ltl.ltl.service.syntax.ParseException;

@RestController
@RequestMapping("/api/tableau")
public class TableauController {

    private final TableauService tableauService;

    public TableauController(TableauService tableauService) {
        this.tableauService = tableauService;
    }

    @GetMapping("/tree")
    public ResponseEntity<?> buildTree(@RequestParam("formula") String formula) {
        try {
            TableauNodeDto dto = tableauService.buildTree(formula);
            return ResponseEntity.ok(dto);
        } catch (ParseException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
