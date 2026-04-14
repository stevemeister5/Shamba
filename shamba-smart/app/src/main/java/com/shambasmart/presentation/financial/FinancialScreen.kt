package com.shambasmart.presentation.financial

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.Income
import com.shambasmart.data.local.entity.Expense
import com.shambasmart.data.local.entity.Loan
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialScreen(
    viewModel: FinancialViewModel = hiltViewModel()
) {
    val incomes by viewModel.allIncomes.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val loans by viewModel.allLoans.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("Overview") }
    var showAddIncome by remember { mutableStateOf(false) }
    var showAddExpense by remember { mutableStateOf(false) }
    var showAddLoan by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Financial Management",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Neutral950
                )
                Text(
                    text = "Track income, expenses, loans, and profitability",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showAddIncome = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green500,
                        contentColor = Green50
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Income")
                }
                
                Button(
                    onClick = { showAddExpense = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red500,
                        contentColor = Red50
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Remove,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Expense")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Overview", "Loans", "P&L").forEach { tab ->
                TabButton(
                    text = tab,
                    isSelected = selectedTab == tab,
                    onClick = { selectedTab = tab }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Tab Content
        when (selectedTab) {
            "Overview" -> OverviewTab(incomes = incomes, expenses = expenses)
            "Loans" -> LoansTab(
                loans = loans,
                onAddLoan = { showAddLoan = true },
                onDeleteLoan = { viewModel.deleteLoan(it) }
            )
            "P&L" -> ProfitLossTab(incomes = incomes, expenses = expenses)
        }
    }
    
    // Dialogs
    if (showAddIncome) {
        AddIncomeDialog(
            onDismiss = { showAddIncome = false },
            onSave = { income ->
                viewModel.addIncome(income)
                showAddIncome = false
            }
        )
    }
    
    if (showAddExpense) {
        AddExpenseDialog(
            onDismiss = { showAddExpense = false },
            onSave = { expense ->
                viewModel.addExpense(expense)
                showAddExpense = false
            }
        )
    }
    
    if (showAddLoan) {
        AddLoanDialog(
            onDismiss = { showAddLoan = false },
            onSave = { loan ->
                viewModel.addLoan(loan)
                showAddLoan = false
            }
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Green800.copy(alpha = 0.2f) else Color.Transparent
    val textColor = if (isSelected) Green300 else Neutral600
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun OverviewTab(
    incomes: List<Income>,
    expenses: List<Expense>
) {
    val totalIncome = incomes.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val netProfit = totalIncome - totalExpenses
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Income
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                border = BorderStroke(1.dp, Green800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = Green400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Income",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "TZS ${String.format("%,.0f", totalIncome)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Green400
                    )
                }
            }
            
            // Total Expenses
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                border = BorderStroke(1.dp, Red800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TrendingDown,
                        contentDescription = null,
                        tint = Red400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "TZS ${String.format("%,.0f", totalExpenses)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Red400
                    )
                }
            }
            
            // Net Profit
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                border = BorderStroke(1.dp, if (netProfit >= 0) Green800 else Red800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (netProfit >= 0) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                        contentDescription = null,
                        tint = if (netProfit >= 0) Green400 else Red400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Net Profit",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "TZS ${String.format("%,.0f", netProfit)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = if (netProfit >= 0) Green400 else Red400
                    )
                }
            }
        }
        
        // Recent Transactions
        Text(
            text = "RECENT TRANSACTIONS",
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600
        )
        
        val allTransactions = (incomes.map { it to "income" } + expenses.map { it to "expense" })
            .sortedByDescending { 
                when (it.first) {
                    is Income -> (it.first as Income).date
                    is Expense -> (it.first as Expense).date
                    else -> LocalDate.fromEpochDays(0)
                }
            }
            .take(10)
        
        if (allTransactions.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.Receipt,
                title = "No Transactions",
                description = "Start recording income and expenses to track your finances."
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                border = BorderStroke(1.dp, Neutral200),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column {
                    allTransactions.forEach { (transaction, type) ->
                        TransactionRow(transaction = transaction, type = type)
                        HorizontalDivider(color = Neutral100, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: Any, type: String) {
    val amount = when (transaction) {
        is Income -> transaction.amount
        is Expense -> transaction.amount
        else -> 0.0
    }
    val category = when (transaction) {
        is Income -> transaction.category
        is Expense -> transaction.category
        else -> ""
    }
    val description = when (transaction) {
        is Income -> transaction.description
        is Expense -> transaction.description
        else -> ""
    }
    val date = when (transaction) {
        is Income -> transaction.date
        is Expense -> transaction.date
        else -> LocalDate.fromEpochDays(0)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = description ?: category,
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral950
            )
            Text(
                text = category,
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600
            )
        }
        
        Text(
            text = date.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = Neutral600,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "${if (type == "income") "+" else "-"} TZS ${String.format("%,.0f", amount)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily,
                fontWeight = FontWeight.Medium
            ),
            color = if (type == "income") Green400 else Red400,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun LoansTab(
    loans: List<Loan>,
    onAddLoan: () -> Unit,
    onDeleteLoan: (Loan) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LOANS",
                style = MaterialTheme.typography.labelSmall,
                color = Neutral600
            )
            
            Button(
                onClick = onAddLoan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = Green50
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Loan")
            }
        }
        
        if (loans.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.AccountBalance,
                title = "No Loans Recorded",
                description = "Track loans and repayment schedules here."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(loans) { loan ->
                    LoanCard(loan = loan, onDelete = { onDeleteLoan(loan) })
                }
            }
        }
    }
}

@Composable
private fun LoanCard(loan: Loan, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = loan.lenderName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Neutral950
                    )
                    Text(
                        text = loan.notes ?: "General",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = Neutral600
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "TZS ${String.format("%,.0f", loan.amount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Neutral950
                    )
                }
                
                Column {
                    Text(
                        text = "Interest Rate",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "${loan.interestRate ?: 0.0}%",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Neutral950
                    )
                }
                
                Column {
                    Text(
                        text = "Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "TZS ${String.format("%,.0f", loan.balance)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = if (loan.balance > 0) Amber400 else Green400
                    )
                }
            }
            
            // Status
            StatusChip(status = loan.status)
        }
    }
}

@Composable
private fun ProfitLossTab(
    incomes: List<Income>,
    expenses: List<Expense>
) {
    // Group by enterprise/category
    val incomeByCategory = incomes.groupBy { it.category }
    val expenseByCategory = expenses.groupBy { it.category }
    val allCategories = (incomeByCategory.keys + expenseByCategory.keys).distinct()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ENTERPRISE P&L",
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600
        )
        
        if (allCategories.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.Analytics,
                title = "No Data Available",
                description = "Record income and expenses to see enterprise profitability."
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                border = BorderStroke(1.dp, Neutral200),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Neutral100.copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ENTERPRISE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "INCOME",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "EXPENSES",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "P&L",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Table Rows
                    allCategories.forEach { category ->
                        val categoryIncome = incomeByCategory[category]?.sumOf { it.amount } ?: 0.0
                        val categoryExpense = expenseByCategory[category]?.sumOf { it.amount } ?: 0.0
                        val profit = categoryIncome - categoryExpense
                        
                        PLRow(
                            category = category,
                            income = categoryIncome,
                            expense = categoryExpense,
                            profit = profit
                        )
                        HorizontalDivider(color = Neutral100, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PLRow(
    category: String,
    income: Double,
    expense: Double,
    profit: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral950,
            modifier = Modifier.weight(2f)
        )
        
        Text(
            text = "TZS ${String.format("%,.0f", income)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Green400,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "TZS ${String.format("%,.0f", expense)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Red400,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "TZS ${String.format("%,.0f", profit)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily,
                fontWeight = FontWeight.Medium
            ),
            color = if (profit >= 0) Green400 else Red400,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "active" -> Pair(Amber800.copy(alpha = 0.3f), Amber300)
        "paid" -> Pair(Green800.copy(alpha = 0.3f), Green300)
        else -> Pair(Neutral800.copy(alpha = 0.3f), Neutral300)
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Neutral300
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral800,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AddIncomeDialog(
    onDismiss: () -> Unit,
    onSave: (Income) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Milk") }
    var description by remember { mutableStateOf("") }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Income",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (TZS)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Milk", "Cheese", "Livestock", "Crops", "Other")) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Income(
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            category = category,
                            description = description.ifBlank { "" },
                            date = today
                        )
                    )
                },
                enabled = amount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Neutral600)
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Feed") }
    var description by remember { mutableStateOf("") }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Expense",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (TZS)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Red500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Feed", "Labour", "Medicine", "Equipment", "Utilities", "Other")) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Red800.copy(alpha = 0.3f),
                                selectedLabelColor = Red300
                            )
                        )
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Red500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Expense(
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            category = category,
                            description = description.ifBlank { "" },
                            date = today
                        )
                    )
                },
                enabled = amount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Red500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Neutral600)
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AddLoanDialog(
    onDismiss: () -> Unit,
    onSave: (Loan) -> Unit
) {
    var lenderName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Loan",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = lenderName,
                    onValueChange = { lenderName = it },
                    label = { Text("Lender Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (TZS)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = { Text("Interest Rate (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val loanAmount = amount.toDoubleOrNull() ?: 0.0
                    val rate = interestRate.toDoubleOrNull()
                    onSave(
                        Loan(
                            lenderName = lenderName,
                            amount = loanAmount,
                            disbursementDate = today,
                            interestRate = rate,
                            balance = loanAmount,
                            notes = notes.ifBlank { null }
                        )
                    )
                },
                enabled = lenderName.isNotBlank() && amount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Neutral600)
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}
