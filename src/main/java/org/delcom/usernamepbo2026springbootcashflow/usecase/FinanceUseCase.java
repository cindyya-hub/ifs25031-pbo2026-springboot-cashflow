package org.delcom.usernamepbo2026springbootcashflow.usecase;

import org.delcom.usernamepbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.usernamepbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.usernamepbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.usernamepbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.usernamepbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Use case yang menangani logika bisnis pencatatan keuangan.
 * Tidak melakukan I/O (tanpa Vaadin / System.out) — hanya memproses data dan
 * melempar checked exception domain jika operasi tidak valid atau data tidak ditemukan.
 */
public class FinanceUseCase {
    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    /** Mengambil semua transaksi. */
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    /** Menambah pemasukan setelah validasi keterangan dan jumlah. */
    public Transaction addIncome(String description, String amountText) throws ValidationException {
        String cleanDescription = validateDescription(description);
        double amount = parseAmount(amountText);
        return repository.saveIncome(cleanDescription, amount);
    }

    /** Menambah pengeluaran setelah validasi keterangan dan jumlah. */
    public Transaction addExpense(String description, String amountText) throws ValidationException {
        String cleanDescription = validateDescription(description);
        double amount = parseAmount(amountText);
        return repository.saveExpense(cleanDescription, amount);
    }

    /** Menghapus transaksi berdasarkan ID; melempar exception jika ID tidak valid / tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("ID transaksi tidak valid");
        }
        if (!repository.deleteById(id)) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Mencari transaksi yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(String keyword) {
        String lower = keyword == null ? "" : keyword.trim().toLowerCase();
        return repository.findBy(t -> t.getDescription().toLowerCase().contains(lower));
    }

    /** Mengurutkan transaksi sesuai kriteria {@link SortOption}. */
    public List<Transaction> sortTransactions(SortOption option) {
        return repository.findAll().stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total seluruh pemasukan. */
    public double getTotalIncome() {
        return repository.findBy(Transaction::isIncome).stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /** Total seluruh pengeluaran. */
    public double getTotalExpense() {
        return repository.findBy(Transaction::isExpense).stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /** Saldo = total pemasukan - total pengeluaran. */
    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    private String validateDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.trim();
    }

    private double parseAmount(String amountText) throws ValidationException {
        if (amountText == null || amountText.trim().isEmpty()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        double amount;
        try {
            amount = Double.parseDouble(amountText.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka");
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new ValidationException("Jumlah harus berupa angka");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0");
        }
        return amount;
    }
}
