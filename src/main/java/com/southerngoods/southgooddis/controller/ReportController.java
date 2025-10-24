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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final IncomeRepository incomeRepository;
    private final ManualExpenseRepository manualExpenseRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockMovementRepository stockMovementRepository;

    public ReportController(IncomeRepository incomeRepository,
                            ManualExpenseRepository manualExpenseRepository,
                            PurchaseOrderRepository purchaseOrderRepository,
                            StockMovementRepository stockMovementRepository) {
        this.incomeRepository = incomeRepository;
        this.manualExpenseRepository = manualExpenseRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @GetMapping
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

        // Calculate purchase expenses from PurchaseOrders
        double purchaseExpenses = purchases.stream()
                .mapToDouble(p -> p.getTotalQuantityPurchased() * p.getUnitPrice().doubleValue())
                .sum();


        double otherExpenses = manualExpenses.stream()
                .mapToDouble(expense -> expense.getAmount().doubleValue())
                .sum();

        double totalExpenses = purchaseExpenses + otherExpenses;
        double netProfit = totalIncome - totalExpenses;

        model.addAttribute("incomeList", incomeList);
        model.addAttribute("purchaseExpenses", purchases);
        model.addAttribute("manualExpenses", manualExpenses);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("netProfit", netProfit);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // Fast-Moving vs. Slow-Moving Items Logic
        List<StockMovement> sales = stockMovementRepository.findSalesBetweenDates(startDate, endDate);

        // Group sales by product name and sum the quantity sold
        Map<String, Integer> salesByItem = sales.stream()
                .collect(Collectors.groupingBy(
                        sm -> sm.getStockBatch().getPurchaseOrder().getProduct().getName(),
                        Collectors.summingInt(sm -> Math.abs(sm.getQuantity())) // Sum the absolute quantity
                ));

        List<ItemSaleDTO> sortedSales = salesByItem.entrySet().stream()
                .map(entry -> new ItemSaleDTO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(ItemSaleDTO::getTotalQuantity).reversed())
                .collect(Collectors.toList());

        model.addAttribute("fastMovingItems", sortedSales);
        model.addAttribute("slowMovingItems", sortedSales.stream()
                .sorted(Comparator.comparingInt(ItemSaleDTO::getTotalQuantity))
                .collect(Collectors.toList()));


        return "reports";
    }

    @PostMapping("/expense/add")
    public String addManualExpense(@RequestParam String description,
                                   @RequestParam float amount, // <-- This is OK
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