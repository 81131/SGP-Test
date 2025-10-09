package com.southerngoods.southgooddis.controller;

import com.southerngoods.southgooddis.dto.ItemSaleDTO;
import com.southerngoods.southgooddis.model.Income;
import com.southerngoods.southgooddis.model.InventoryPurchase;
import com.southerngoods.southgooddis.model.ManualExpense;
import com.southerngoods.southgooddis.model.Sale;
import com.southerngoods.southgooddis.repository.IncomeRepository;
import com.southerngoods.southgooddis.repository.InventoryPurchaseRepository;
import com.southerngoods.southgooddis.repository.ManualExpenseRepository;
import com.southerngoods.southgooddis.repository.SaleRepository; // <-- Add this import
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final IncomeRepository incomeRepository;
    private final InventoryPurchaseRepository inventoryPurchaseRepository;
    private final ManualExpenseRepository manualExpenseRepository;
    private final SaleRepository saleRepository; // <-- Inject SaleRepository

    public ReportController(IncomeRepository incomeRepository,
                            InventoryPurchaseRepository inventoryPurchaseRepository,
                            ManualExpenseRepository manualExpenseRepository,
                            SaleRepository saleRepository) { // <-- Update constructor
        this.incomeRepository = incomeRepository;
        this.inventoryPurchaseRepository = inventoryPurchaseRepository;
        this.manualExpenseRepository = manualExpenseRepository;
        this.saleRepository = saleRepository; // <-- Add to constructor
    }

    @GetMapping
    public String showReportsDashboard(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now();

        // --- Financial Summary Logic (No changes here) ---
        List<Income> incomeList = incomeRepository.findByIncomeDateBetweenOrderByIncomeDateDesc(startDate, endDate);
        List<InventoryPurchase> purchases = inventoryPurchaseRepository.findByPurchaseDateBetweenOrderByPurchaseDateDesc(startDate, endDate);
        List<ManualExpense> manualExpenses = manualExpenseRepository.findByExpenseDateBetweenOrderByExpenseDateDesc(startDate, endDate);

        double totalIncome = incomeList.stream().mapToDouble(Income::getAmount).sum();
        double purchaseExpenses = purchases.stream().mapToDouble(p -> p.getQuantityPurchased() * p.getUnitPrice()).sum();
        double otherExpenses = manualExpenses.stream().mapToDouble(ManualExpense::getAmount).sum();
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

        // --- NEW: Fast-Moving vs. Slow-Moving Items Logic ---
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate);

        // Group sales by item name and sum the quantity sold
        Map<String, Integer> salesByItem = sales.stream()
                .collect(Collectors.groupingBy(Sale::getItemName, Collectors.summingInt(Sale::getQuantitySold)));

        // Convert the map to a list of DTOs and sort it
        List<ItemSaleDTO> sortedSales = salesByItem.entrySet().stream()
                .map(entry -> new ItemSaleDTO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(ItemSaleDTO::getTotalQuantity).reversed()) // Sort descending
                .collect(Collectors.toList());

        // Add the sorted lists to the model
        model.addAttribute("fastMovingItems", sortedSales); // The full list is the "fast-moving" list
        model.addAttribute("slowMovingItems", sortedSales.stream()
                .sorted(Comparator.comparingInt(ItemSaleDTO::getTotalQuantity)) // Re-sort ascending for slow items
                .collect(Collectors.toList()));


        return "reports";
    }

    @PostMapping("/expense/add")
    public String addManualExpense(@RequestParam String description,
                                   @RequestParam float amount,
                                   @RequestParam LocalDate expenseDate,
                                   @RequestParam String category,
                                   RedirectAttributes redirectAttributes) {
        ManualExpense expense = new ManualExpense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setExpenseDate(expenseDate);
        expense.setCategory(category);
        manualExpenseRepository.save(expense);

        redirectAttributes.addFlashAttribute("success", "Manual expense recorded successfully.");
        return "redirect:/reports?startDate=" + expenseDate + "&endDate=" + expenseDate;
    }
}