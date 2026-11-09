package org.delcom.usernamepbo2026springbootcashflow.usecase;

import org.delcom.usernamepbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.usernamepbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.usernamepbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.usernamepbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.usernamepbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FinanceUseCaseTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void balanceIsIncomeMinusExpense() throws Exception {
        useCase.addIncome("Gaji", "5000000");
        useCase.addExpense("Makan", "25000");
        assertEquals(4_975_000.0, useCase.getBalance(), 0.001);
        assertEquals(5_000_000.0, useCase.getTotalIncome(), 0.001);
        assertEquals(25_000.0, useCase.getTotalExpense(), 0.001);
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(ValidationException.class, () -> useCase.addIncome("  ", "1000"));
        assertThrows(ValidationException.class, () -> useCase.addIncome("Gaji", "-5"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("Makan", "0"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("Makan", "abc"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("Makan", ""));
    }

    @Test
    void removeValidatesIdAndExistence() throws Exception {
        Transaction t = useCase.addIncome("Gaji", "1000");
        assertThrows(ValidationException.class, () -> useCase.removeTransaction(0));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(99));
        useCase.removeTransaction(t.getId());
        assertTrue(useCase.getAllTransactions().isEmpty());
        assertEquals(0.0, useCase.getBalance(), 0.001);
    }

    @Test
    void searchIsCaseInsensitiveAndDoesNotMutate() throws Exception {
        useCase.addIncome("Gaji Bulanan", "1000");
        useCase.addExpense("Makan Siang", "10");
        assertEquals(1, useCase.searchTransactions("gaji").size());
        assertEquals(2, useCase.getAllTransactions().size());
    }

    @Test
    void sortUsesDomainComparators() throws Exception {
        useCase.addIncome("A", "300");
        useCase.addExpense("B", "100");
        useCase.addIncome("C", "200");

        List<Transaction> asc = useCase.sortTransactions(SortOption.AMOUNT_ASC);
        assertEquals(100.0, asc.get(0).getAmount(), 0.001);

        List<Transaction> desc = useCase.sortTransactions(SortOption.AMOUNT_DESC);
        assertEquals(300.0, desc.get(0).getAmount(), 0.001);

        assertTrue(useCase.sortTransactions(SortOption.INCOME_FIRST).get(0).isIncome());
        assertTrue(useCase.sortTransactions(SortOption.EXPENSE_FIRST).get(0).isExpense());
    }
}
