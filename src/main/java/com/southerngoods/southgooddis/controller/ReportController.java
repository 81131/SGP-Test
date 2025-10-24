package com.southerngoods.southgooddis.controller;

import com.southerngoods.southgooddis.dto.ItemSaleDTO;
import com.southerngoods.southgooddis.model.Income;
import com.southerngoods.southgooddis.model.ManualExpense;
import com.southerngoods.southgooddis.model.PurchaseOrder;
import com.southerngoods.southgooddis.model.StockMovement;
import com.southerngoods.southgooddis.repository.IncomeRepository;
import com.southerngoods.southgooddis.repository.ManualExpenseRepository;
import com.southerngoods.southgooddis.repository.PurchaseOrderRepository;
import com.southerngoods.southgooddis.repository.StockMovementRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ReportController {

    private final IncomeRepository incomeRepository;
    private final ManualExpenseRepository manualExpenseRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockMovementRepository stockMovementRepository;

    public static class TransactionlogDTO {
        private LocalDate date;
        private String description;
        private String type;
        private double amount;

        public TransactionlogDTO(LocalDate date, String description, String type, double amount) {
            this.date = date;
            this.description = description;
            this.type = type;
            this.amount = amount;
        }
        public LocalDate getDate() { return date; }
        public String getDescription() { return description; }
        public String getType() { return type; }
        public double getAmount() { return amount; }
    }

    public static class ItemTrendDTO {
        private String itemName;
        private int totalQuantity;
        private String supplierName;
        private LocalDate purchaseDate;
        private long daysInInventory;

        public ItemTrendDTO(String itemName, int totalQuantity, String supplierName, LocalDate purchaseDate, long daysInInventory) {
            this.itemName = itemName;
            this.totalQuantity = totalQuantity;
            this.supplierName = supplierName;
            this.purchaseDate = purchaseDate;
            this.daysInInventory = daysInInventory;
        }

        public String getItemName() { return itemName; }
        public int getTotalQuantity() { return totalQuantity; }
        public String getSupplierName() { return supplierName; }
        public LocalDate getPurchaseDate() { return purchaseDate; }
        public long getDaysInInventory() { return daysInInventory; }
    }


    public ReportController(IncomeRepository incomeRepository,
                            ManualExpenseRepository manualExpenseRepository,
                            PurchaseOrderRepository purchaseOrderRepository,
                            StockMovementRepository stockMovementRepository) {
        this.incomeRepository = incomeRepository;
        this.manualExpenseRepository = manualExpenseRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @GetMapping("/reports")
    public String showReportsDashboard(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now();

        //  Financial Summary Logic
        List<Income> incomeList = incomeRepository.findByIncomeDateBetweenOrderByIncomeDateDesc(startDate, endDate);
        List<PurchaseOrder> purchases = purchaseOrderRepository.findPurchasesBetweenDates(startDate, endDate);
        List<ManualExpense> manualExpenses = manualExpenseRepository.findByExpenseDateBetweenOrderByExpenseDateDesc(startDate, endDate);

        double totalIncome = incomeList.stream()
                .mapToDouble(income -> income.getAmount().doubleValue())
                .sum();

        double purchaseExpenses = purchases.stream()
                .mapToDouble(p -> p.getTotalQuantityPurchased() * p.getUnitPrice().doubleValue())
                .sum();

        double otherExpenses = manualExpenses.stream()
                .mapToDouble(expense -> expense.getAmount().doubleValue())
                .sum();

        double totalExpenses = purchaseExpenses + otherExpenses;
        double netProfit = totalIncome - totalExpenses;

        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("netProfit", netProfit);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // Detailed Logs Logic
        List<TransactionlogDTO> detailedLogs = new ArrayList<>();
        incomeList.forEach(income -> detailedLogs.add(new TransactionlogDTO(
                income.getIncomeDate(),
                income.getIncomeType() != null ? income.getIncomeType() : "Sale",
                "Income",
                income.getAmount().doubleValue()
        )));
        purchases.forEach(po -> detailedLogs.add(new TransactionlogDTO(
                po.getPurchaseDate(),
                "Purchase: " + po.getProduct().getName(),
                "Expense",
                po.getTotalQuantityPurchased() * po.getUnitPrice().doubleValue()
        )));
        manualExpenses.forEach(expense -> detailedLogs.add(new TransactionlogDTO(
                expense.getExpenseDate(),
                expense.getDescription(),
                "Expense",
                expense.getAmount().doubleValue()
        )));
        detailedLogs.sort(Comparator.comparing(TransactionlogDTO::getDate).reversed());
        model.addAttribute("detailedLogs", detailedLogs);

        return "reports";
    }

    @GetMapping("/trends")
    public String showTrendsPage(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now();

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        List<StockMovement> sales = stockMovementRepository.findSalesBetweenDates(startDate, endDate);

        Map<PurchaseOrder, Integer> salesByPO = sales.stream()
                .collect(Collectors.groupingBy(
                        sm -> sm.getStockBatch().getPurchaseOrder(),
                        Collectors.summingInt(sm -> Math.abs(sm.getQuantity()))
                ));

        List<ItemTrendDTO> trends = salesByPO.entrySet().stream()
                .map(entry -> {
                    PurchaseOrder po = entry.getKey();
                    int totalQuantity = entry.getValue();
                    String supplierName = po.getSupplier().getFirstName() + " " + po.getSupplier().getLastName();
                    long daysInInventory = ChronoUnit.DAYS.between(po.getPurchaseDate(), LocalDate.now());

                    return new ItemTrendDTO(
                            po.getProduct().getName(),
                            totalQuantity,
                            supplierName,
                            po.getPurchaseDate(),
                            daysInInventory
                    );
                })
                .sorted(Comparator.comparingInt(ItemTrendDTO::getTotalQuantity).reversed())
                .collect(Collectors.toList());

        model.addAttribute("fastMovingItems", trends);
        model.addAttribute("slowMovingItems", trends.stream()
                .sorted(Comparator.comparingInt(ItemTrendDTO::getTotalQuantity))
                .collect(Collectors.toList()));

        return "trends";
    }

    @PostMapping("/reports/expense/add")
    public String addManualExpense(@RequestParam String description,
                                   @RequestParam float amount,
                                   @RequestParam LocalDate expenseDate,
                                   @RequestParam String category,
                                   RedirectAttributes redirectAttributes) {
        ManualExpense expense = new ManualExpense();
        expense.setDescription(description);
        expense.setAmount(BigDecimal.valueOf(amount));
        expense.setExpenseDate(expenseDate);
        expense.setCategory(category);
        manualExpenseRepository.save(expense);

        redirectAttributes.addFlashAttribute("success", "Manual expense recorded successfully.");
        return "redirect:/reports?startDate=" + expenseDate + "&endDate=" + expenseDate;
    }
}