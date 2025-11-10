package com.ltl.ltl.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ltl.ltl.ltl.web.dto.TableauNodeDto;
import com.ltl.ltl.model.IFormula;
import com.ltl.ltl.model.tableau.TableauEntry;
import com.ltl.ltl.model.tableau.TableauNode;
import com.ltl.ltl.service.AstParser;
import com.ltl.ltl.service.syntax.Lexer;
import com.ltl.ltl.service.syntax.ParseException;
import com.ltl.ltl.service.tableau.TableauTreeConstruction;

@Service
public class TableauService {

    public TableauNodeDto buildTree(String formulaString) throws ParseException {
        IFormula formula = new AstParser(new Lexer(formulaString)).parseFormula();
        TableauNode root = nodeFromFormula(formula);
        TableauTreeConstruction builder = new TableauTreeConstruction(root);
        TableauNode tree = builder.build();
        return toDto(tree);
    }

    private TableauNode nodeFromFormula(IFormula f) {
        List<TableauEntry> entries = new ArrayList<>();
        entries.add(new TableauEntry(f));
        return new TableauNode(entries);
    }

    private TableauNodeDto toDto(TableauNode node) {
        TableauNodeDto dto = new TableauNodeDto();
        dto.closed = node.getIsClosed();
        dto.formulas = node.getEntries().stream()
                .map(e -> e.getFormula() == null ? "null" : e.getFormula().toString())
                .toList();
    dto.entries = node.getEntries().stream()
        .map(e -> new TableauNodeDto.EntryDto(
            e.getFormula() == null ? "null" : e.getFormula().toString(),
            e.isChecked()
        ))
        .toList();
        dto.children = node.getChildren().stream().map(this::toDto).toList();
        return dto;
    }
}
