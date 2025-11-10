package com.ltl.ltl.service.tableau;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.ltl.ltl.model.AtomicProposition;
import com.ltl.ltl.model.Symbol;
import com.ltl.ltl.model.formulas.Globally;
import com.ltl.ltl.model.formulas.Or;
import com.ltl.ltl.model.formulas.Release;
import com.ltl.ltl.model.formulas.Until;
import com.ltl.ltl.model.tableau.TableauEntry;
import com.ltl.ltl.model.tableau.TableauNode;

public class TableauProofTest {

	private static List<String> toStrings(TableauNode n) {
		return n.getEntries().stream().map(e -> e.getFormula().toString()).collect(Collectors.toList());
	}

	@Test
	void expandGlobally_noBranching_addsInnerAndNext() {
		TableauEntry gA = new TableauEntry(new Globally(new AtomicProposition(Symbol.A)));
		ArrayList<TableauEntry> entries = new ArrayList<>(List.of(gA));
		TableauNode node = new TableauNode(entries);

		TableauNode expanded = TableauProof.expandNodeNoBranching(node, gA);

		List<String> strs = toStrings(expanded);
		assertTrue(strs.contains("A"), "Should contain inner formula A");
		assertTrue(strs.contains("X G A"), "Should contain postponed Next(G A)");
		assertTrue(gA.isChecked(), "Expanded entry should be marked checked");
	}

	@Test
	void expandOr_leftAndRightBranch_addsRespectiveDisjuncts() {
		TableauEntry or = new TableauEntry(new Or(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B)));
		ArrayList<TableauEntry> entries = new ArrayList<>(List.of(or));
		TableauNode node = new TableauNode(entries);

		TableauNode left = TableauProof.expandNodeLeftBranching(node, or);
		TableauNode right = TableauProof.expandNodeRightBranching(node, or);

		List<String> leftStrs = toStrings(left);
		List<String> rightStrs = toStrings(right);
		assertTrue(leftStrs.contains("A"), "Left branch should contain A");
		assertTrue(rightStrs.contains("B"), "Right branch should contain B");
		assertTrue(or.isChecked(), "Expanded entry should be marked checked");
	}

	@Test
	void expandUntil_leftBranch_hasRightImmediate_rightBranch_hasLeftAndPostpone() {
		AtomicProposition p = new AtomicProposition(Symbol.A);
		AtomicProposition q = new AtomicProposition(Symbol.B);
		TableauEntry until = new TableauEntry(new Until(p, q));
		ArrayList<TableauEntry> entries = new ArrayList<>(List.of(until));
		TableauNode node = new TableauNode(entries);

		TableauNode left = TableauProof.expandNodeLeftBranching(node, until);
		TableauNode right = TableauProof.expandNodeRightBranching(node, until);

		List<String> leftStrs = toStrings(left);
		List<String> rightStrs = toStrings(right);
		assertTrue(leftStrs.contains("B"), "Left branch of Until should contain right operand (q)");
		assertTrue(rightStrs.contains("(A & X (A U B))"), "Right branch should contain A & X(A U B)");
	}

	@Test
	void expandRelease_leftBranch_hasBoth_rightBranch_hasRightAndPostpone() {
		AtomicProposition p = new AtomicProposition(Symbol.A);
		AtomicProposition q = new AtomicProposition(Symbol.B);
		TableauEntry rel = new TableauEntry(new Release(p, q));
		ArrayList<TableauEntry> entries = new ArrayList<>(List.of(rel));
		TableauNode node = new TableauNode(entries);

		TableauNode left = TableauProof.expandNodeLeftBranching(node, rel);
		TableauNode right = TableauProof.expandNodeRightBranching(node, rel);

		List<String> leftStrs = toStrings(left);
		List<String> rightStrs = toStrings(right);
		assertTrue(leftStrs.contains("(A & B)"), "Left branch of Release should contain A & B");
		assertTrue(rightStrs.contains("(B & X (A R B))"), "Right branch should contain B & X(A R B)");
	}

	@Test
	void expandDoesNotDropExistingEntries() {
		TableauEntry gA = new TableauEntry(new Globally(new AtomicProposition(Symbol.A)));
		TableauEntry extra = new TableauEntry(new AtomicProposition(Symbol.B));
		ArrayList<TableauEntry> entries = new ArrayList<>(List.of(gA, extra));
		TableauNode node = new TableauNode(entries);

		TableauNode expanded = TableauProof.expandNodeNoBranching(node, gA);
		List<String> strs = toStrings(expanded);
		assertTrue(strs.contains("B"), "Existing entries should be preserved after expansion");
	}

	
}
