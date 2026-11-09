package org.delcom.usernamepbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.usernamepbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.usernamepbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.usernamepbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.usernamepbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.usernamepbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.usernamepbo2026springbootcashflow.usecase.FinanceUseCase;

import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Vaadin aplikasi Cashflow.
 * Tambah pemasukan/pengeluaran lewat dialog, cari keterangan, urutkan lewat header
 * kolom atau pilihan SortOption, dan hapus lewat dialog konfirmasi.
 */
@Route("")
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {
    private static final String TITLE_ALL = "Daftar Transaksi";
    private static final String EMPTY_DEFAULT = "- Data transaksi belum tersedia!";

    private final FinanceUseCase useCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final Select<SortOption> sortSelect = new Select<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span(EMPTY_DEFAULT);
    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span("Rp 0.00");
    private final Span expenseTotal = new Span("Rp 0.00");
    private final Span balanceTotal = new Span("Rp 0.00");

    public FinanceView(FinanceUseCase useCase, FinancePresenter presenter) {
        this.useCase = useCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Cashflow — Catatan Keuangan");
        configureGrid();
        configureToolbar();
        add(title, buildSummaryCards(), buildToolbar(), statusLabel, grid);
        refreshAll();
    }

    // ------------------------------------------------------------------ ringkasan

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Jumlah Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                createSummaryCard("Total Pemasukan", incomeTotal, "#DCFCE7", "#16A34A", "#166534"),
                createSummaryCard("Total Pengeluaran", expenseTotal, "#FEE2E2", "#DC2626", "#991B1B"),
                createSummaryCard("Saldo", balanceTotal, "#FEF3C7", "#D97706", "#92400E")
        );
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle().set("align-items", "stretch");
        return summary;
    }

    private Div createSummaryCard(String label, Span value, String background, String accent, String valueColor) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", accent);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.1")
                .set("color", valueColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "0")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary() {
        totalCount.setText(String.valueOf(useCase.getAllTransactions().size()));
        incomeTotal.setText(presenter.formatMoney(useCase.getTotalIncome()));
        expenseTotal.setText(presenter.formatMoney(useCase.getTotalExpense()));
        balanceTotal.setText(presenter.formatMoney(useCase.getBalance()));
    }

    // ------------------------------------------------------------------ grid

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Transaction::getId);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addColumn(t -> presenter.formatMoney(t.getAmount()))
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END)
                .setSortable(true)
                .setComparator(SortOption.AMOUNT_ASC.comparator());

        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(SortOption.INCOME_FIRST.comparator());

        grid.addColumn(presenter::formatTime)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("tindakan")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.setSizeFull();
    }

    private Span createTypeBadge(Transaction transaction) {
        Span badge = new Span(presenter.formatType(transaction.getType()));
        String color = transaction.isIncome() ? "#166534" : "#991B1B";
        String background = transaction.isIncome() ? "#DCFCE7" : "#FEE2E2";
        badge.getStyle()
                .set("color", color)
                .set("background", background)
                .set("padding", "0.15rem 0.6rem")
                .set("border-radius", "999px")
                .set("font-weight", "600")
                .set("font-size", "0.8rem");
        return badge;
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        return new HorizontalLayout(deleteButton);
    }

    // ------------------------------------------------------------------ toolbar

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        sortSelect.setPlaceholder("Urutkan...");
        sortSelect.setEmptySelectionAllowed(true);
        sortSelect.setItems(SortOption.values());
        sortSelect.setItemLabelGenerator(option -> option == null ? "" : presenter.formatSortOption(option));
        sortSelect.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) {
                applySort(event.getValue());
            }
        });
    }

    private HorizontalLayout buildToolbar() {
        Button incomeButton = new Button("Tambah Pemasukan", VaadinIcon.ARROW_CIRCLE_DOWN.create(),
                event -> openAddDialog(true));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button expenseButton = new Button("Tambah Pengeluaran", VaadinIcon.ARROW_CIRCLE_UP.create(),
                event -> openAddDialog(false));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> searchTransactions());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, sortSelect, refreshButton, incomeButton, expenseButton);
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        toolbar.setAlignItems(Alignment.BASELINE);
        return toolbar;
    }

    // ------------------------------------------------------------------ dialog

    private void openAddDialog(boolean income) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();

        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setPlaceholder("contoh: 1000000");
        amountField.setWidthFull();

        VerticalLayout form = new VerticalLayout(descriptionField, amountField);
        form.setPadding(false);

        Dialog dialog = new Dialog();
        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create(), event -> {
            try {
                Transaction created = income
                        ? useCase.addIncome(descriptionField.getValue(), amountField.getValue())
                        : useCase.addExpense(descriptionField.getValue(), amountField.getValue());
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        buildDialog(dialog, income ? "Tambah Pemasukan" : "Tambah Pengeluaran",
                income ? "#16A34A" : "#DC2626", form, cancelButton, saveButton);
        dialog.open();
        descriptionField.focus();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph(
                "Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                        + "\" (ID " + transaction.getId() + ", "
                        + presenter.formatMoney(transaction.getAmount()) + ")?");
        message.getStyle().set("margin", "0");

        Dialog dialog = new Dialog();
        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        buildDialog(dialog, "Menghapus Transaksi", "#DC2626", message, cancelButton, confirmButton);
        dialog.open();
    }

    /** Merakit dialog: header berwarna, body, dan footer berisi tombol. */
    private void buildDialog(Dialog dialog, String title, String headerColor,
                             Component bodyContent, Button... footerButtons) {
        dialog.setWidth("480px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);

        Span headerTitle = new Span(title);
        headerTitle.getStyle()
                .set("color", "#FFFFFF")
                .set("font-size", "1.125rem")
                .set("font-weight", "600");
        Div header = new Div(headerTitle);
        header.getStyle()
                .set("background", headerColor)
                .set("padding", "0.9rem 1.25rem")
                .set("border-radius", "8px 8px 0 0");

        Div body = new Div(bodyContent);
        body.getStyle().set("padding", "1rem 1.25rem");

        Div footer = new Div();
        footer.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("gap", "0.75rem")
                .set("background", "#F3F4F6")
                .set("padding", "0.8rem 1.25rem")
                .set("border-radius", "0 0 8px 8px");
        footer.add(footerButtons);

        Div shell = new Div(header, body, footer);
        dialog.removeAll();
        dialog.add(shell);
    }

    // ------------------------------------------------------------------ aksi

    private void deleteTransaction(Transaction transaction) {
        try {
            useCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
            refreshAll();
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
    }

    private void searchTransactions() {
        String keyword = searchField.getValue();
        if (keyword.isBlank()) {
            refreshAll();
            return;
        }
        List<Transaction> results = useCase.searchTransactions(keyword);
        emptyState.setText(presenter.showNotFound(keyword));
        refreshGrid(results, "Hasil Pencarian: \"" + keyword + "\"");
    }

    private void applySort(SortOption option) {
        emptyState.setText(EMPTY_DEFAULT);
        refreshGrid(useCase.sortTransactions(option), "Diurutkan: " + presenter.formatSortOption(option));
    }

    private void refreshAll() {
        emptyState.setText(EMPTY_DEFAULT);
        searchField.clear();
        sortSelect.clear();
        refreshGrid(useCase.getAllTransactions(), TITLE_ALL);
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary();
        if (transactions.isEmpty()) {
            statusLabel.setText(title + " — " + emptyState.getText().replaceFirst("^- ", ""));
        } else {
            statusLabel.setText(title);
        }
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
