package com.ltl.ltl.service.tableau;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ltl.ltl.model.IFormula;
import com.ltl.ltl.model.tableau.TableauEntry;
import com.ltl.ltl.model.tableau.TableauNode;
import com.ltl.ltl.service.AstParser;
import com.ltl.ltl.service.syntax.Lexer;
import com.ltl.ltl.service.syntax.ParseException;

/**
 * Tests that construct a full tableau tree and serialize it to JSON for easy viewing.
 */
public class TableauTreeConstructionTest {

    private static TableauNode nodeFromFormula(IFormula f) {
        List<TableauEntry> entries = new ArrayList<>();
        entries.add(new TableauEntry(f));
        return new TableauNode(entries);
    }

    private static NodeDto toDto(TableauNode node) {
        NodeDto dto = new NodeDto();
        dto.closed = node.getIsClosed();
        // capture formulas as strings for readability in JSON
        dto.formulas = node.getEntries().stream()
                .map(e -> e.getFormula() == null ? "null" : e.getFormula().toString())
                .toList();
        dto.children = node.getChildren().stream().map(TableauTreeConstructionTest::toDto).toList();
        return dto;
    }

    private static String writeJson(String filename, Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Path outDir = Paths.get("target", "tableau");
        Files.createDirectories(outDir);
        Path outFile = outDir.resolve(filename);
        String json = mapper.writeValueAsString(obj);
        Files.writeString(outFile, json);
        System.out.println("Tableau JSON written to: " + outFile.toAbsolutePath());
        return json;
    }

    // Simple sanity test that also produces a JSON to view
    @Test
    void producesJsonForSimpleTree() throws Exception {
        IFormula formula = parse("(a|b)&!a");
        TableauNode root = nodeFromFormula(formula);
        TableauTreeConstruction builder = new TableauTreeConstruction(root);
        TableauNode tree = builder.build();

        NodeDto dto = toDto(tree);
        String json = writeJson("tableau_simple.json", dto);

        assertNotNull(json);
        assertTrue(json.contains("a") || json.contains("A"));
        assertFalse(json.isEmpty());
    }

    // Complex formula: F(G(a & X(b)))
    @Test
    void producesJsonForComplex_F_G_a_and_X_b() throws Exception {
        IFormula formula = parse("F(G(a&X(b)))");
        TableauNode root = nodeFromFormula(formula);
        TableauTreeConstruction builder = new TableauTreeConstruction(root);
        TableauNode tree = builder.build();

        NodeDto dto = toDto(tree);
        String json = writeJson("tableau_F_G_a_X_b.json", dto);

        assertNotNull(json);
        assertTrue(json.contains("F") && json.contains("G") && json.contains("X"));
    }

    private static IFormula parse(String s) throws ParseException {
        return new AstParser(new Lexer(s)).parseFormula();
    }

    // Minimal DTO used purely for JSON output in tests
    private static class NodeDto {
        public List<String> formulas;
        public Boolean closed; // null=undecided, true=accepted, false=rejected
        public List<NodeDto> children;
    }
}
