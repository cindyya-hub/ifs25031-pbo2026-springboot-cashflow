package org.delcom.usernamepbo2026springbootcashflow.domain.repository;

import org.delcom.usernamepbo2026springbootcashflow.domain.entity.Transaction;

/**
 * Port spesifik Transaction yang memperluas generic {@link IRepository}.
 * Menambahkan method pembuatan transaksi baru dengan penomoran ID otomatis.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /** Menyimpan transaksi pemasukan baru. */
    Transaction saveIncome(String description, double amount);

    /** Menyimpan transaksi pengeluaran baru. */
    Transaction saveExpense(String description, double amount);
}
