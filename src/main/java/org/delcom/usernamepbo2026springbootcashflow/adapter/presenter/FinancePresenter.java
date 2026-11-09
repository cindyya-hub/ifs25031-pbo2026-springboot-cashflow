package org.delcom.usernamepbo2026springbootcashflow.adapter.presenter;

import org.delcom.usernamepbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.usernamepbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.usernamepbo2026springbootcashflow.domain.entity.TransactionType;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Memisahkan logika tampilan dari entity, use case, dan view.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Format uang: 1000000 -> "Rp 1,000,000.00". */
    public String formatMoney(double amount) {
        return String.format(Locale.US, "Rp %,.2f", amount);
    }

    /** Label jenis transaksi. */
    public String formatType(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** Format satu transaksi: "[id] keterangan | Rp 1,000,000.00 | [Pemasukan]". */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                formatType(transaction.getType())
        );
    }

    /** Format waktu transaksi. */
    public String formatTime(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Format saldo: "Saldo: Rp 4,975,000.00". */
    public String formatBalance(double balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /** Format daftar transaksi multi-baris, atau pesan kosong jika tidak ada data. */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        if (transactions.isEmpty()) {
            return header + "\n" + emptyMessage;
        }
        return header + "\n" + transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
    }

    /** Label opsi pengurutan untuk ditampilkan di UI. */
    public String formatSortOption(SortOption option) {
        if (option == null) {
            return "";
        }
        return switch (option) {
            case AMOUNT_ASC -> "Jumlah terkecil";
            case AMOUNT_DESC -> "Jumlah terbesar";
            case INCOME_FIRST -> "Pemasukan dulu";
            case EXPENSE_FIRST -> "Pengeluaran dulu";
        };
    }

    // ---- pesan notifikasi ----

    public String showAddSuccess(Transaction transaction) {
        return formatType(transaction.getType()) + " berhasil ditambahkan: " + format(transaction);
    }

    public String showRemoveSuccess() {
        return "Transaksi berhasil dihapus.";
    }

    public String showRemoveFailed(int id) {
        return "Gagal menghapus: transaksi dengan ID " + id + " tidak ditemukan.";
    }

    public String showValidationError(String message) {
        return "Validasi gagal: " + message + ".";
    }

    public String showNotFound(String keyword) {
        return "Transaksi dengan keterangan \"" + keyword + "\" tidak ditemukan.";
    }
}
