package com.shambasmart.presentation.financial

import com.shambasmart.maarifa.MaarifaViewModel
import com.shambasmart.maarifa.ui.*

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.Income
import com.shambasmart.data.local.entity.Expense
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
    val income by viewModel.allIncome.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val totalIncome = income.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val balance = totalIncome - totalExpenses

    Box(modifier = Modifier.fillMaxSize().background(SurfaceBase)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header
            FinancialHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Financial Summary KPI Strip
            FinancialKPIStrip(
                totalIncome = totalIncome,
                totalExpenses = totalExpenses,
                balance = balance
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tabs
            FinancialTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                incomeCount = income.size,
                expenseCount = expenses.size
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tab Content
            when (selectedTab) {
                0 -> IncomeSection(income = income)
                1 -> ExpensesSection(expenses = expenses)
            }
        }
        
        // Floating Action Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Add Expense FAB
            FloatingActionButton(
                onClick = { showExpenseDialog = true },
                containerColor = Red500,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Remove,
                    contentDescription = "Add Expense"
                )
            }
            
            // Add Income FAB
            FloatingActionButton(
                onClick = { showIncomeDialog = true },
                containerColor = Green500,
                contentColor = Green950,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add Income"
                )
            }
        }
    }

    // Add Income Dialog
    if (showIncomeDialog) {
        AddIncomeDialog(
            onDismiss = { showIncomeDialog = false },
            onAdd = { income ->
                viewModel.addIncome(income)
                showIncomeDialog = false
            }
        )
    }

    // Add Expense Dialog
    if (showExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showExpenseDialog = false },
            onAdd = { expense ->
                viewModel.addExpense(expense)
                showExpenseDialog = false
            }
        )
    }
}

@Composable
private fun FinancialHeader() {
    Column {
        Text(
            text = "Financial Management",
            style = MaterialTheme.typography.headlineLarge,
            color = Neutral950
        )
        Text(
            text = "Track income, expenses, and farm profitability",
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600
        )
    }
}

@Composable
private fun FinancialKPIStrip(
    totalIncome: Double,
    totalExpenses: Double,
    balance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FinancialKPIItem(
                icon = Icons.Outlined.TrendingUp,
                label = "TOTAL INCOME",
                value = "TZS ${String.format("%,.0f", totalIncome)}",
                valueColor = Green400,
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = Neutral200
            )
            
            FinancialKPIItem(
                icon = Icons.Outlined.TrendingDown,
                label = "TOTAL EXPENSES",
                value = "TZS ${String.format("%,.0f", totalExpenses)}",
                valueColor = Red400,
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = Neutral200
            )
            
            FinancialKPIItem(
                icon = Icons.Outlined.AccountBalance,
                label = "BALANCE",
                value = "TZS ${String.format("%,.0f", balance)}",
                valueColor = if (balance >= 0) Green400 else Red400,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FinancialKPIItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Neutral600
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily,
                fontWeight = FontWeight.Medium
            ),
            color = valueColor
        )
    }
}

@Composable
private fun FinancialTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    incomeCount: Int,
    expenseCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Income Tab
            val incomeSelected = selectedTab == 0
            Surface(
                modifier = Modifier.weight(1f),
                color = if (incomeSelected) Green800.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onTabSelected(0) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (incomeSelected) Green300 else Neutral600
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Income ($incomeCount)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = if (incomeSelected) Green300 else Neutral600
                    )
                }
            }
            
            // Expenses Tab
            val expenseSelected = selectedTab == 1
            Surface(
                modifier = Modifier.weight(1f),
                color = if (expenseSelected) Red600.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onTabSelected(1) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (expenseSelected) Red300 else Neutral600
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Expenses ($expenseCount)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = if (expenseSelected) Red300 else Neutral600
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeSection(income: List<Income>) {
    if (income.isEmpty()) {
        EmptyFinancialState(
            icon = Icons.Outlined.TrendingUp,
            title = "No income records",
            subtitle = "Start tracking your farm income to monitor profitability"
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(income) { item ->
                IncomeCard(income = item)
            }
        }
    }
}

@Composable
private fun ExpensesSection(expenses: List<Expense>) {
    if (expenses.isEmpty()) {
        EmptyFinancialState(
            icon = Icons.Outlined.TrendingDown,
            title = "No expense records",
            subtitle = "Track your farm expenses to manage costs effectively"
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(expenses) { item ->
                ExpenseCard(expense = item)
            }
        }
    }
}

@Composable
private fun EmptyFinancialState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
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
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    color = Neutral600
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral400
                )
            }
        }
    }
}

@Composable
private fun IncomeCard(income: Income) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Green800.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(income.category, isIncome = true),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Green400
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = income.category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineMedium,
                        color = Neutral950
                    )
                    Text(
                        text = "TZS ${String.format("%,.0f", income.amount)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Green400
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = income.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral800
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = income.date.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral600
                    )
                    income.buyerName?.let {
                        Text(
                            text = "Buyer: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = Neutral600
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseCard(expense: Expense) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Red600.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(expense.category, isIncome = false),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Red400
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = expense.category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineMedium,
                        color = Neutral950
                    )
                    Text(
                        text = "TZS ${String.format("%,.0f", expense.amount)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Red400
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral800
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = expense.date.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral600
                    )
                    expense.supplier?.let {
                        Text(
                            text = "Supplier: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = Neutral600
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(category: String, isIncome: Boolean): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        isIncome -> when (category) {
            "milk" -> Icons.Outlined.WaterDrop
            "cheese" -> Icons.Outlined.LunchDining
            "animals" -> Icons.Outlined.Pets
            "crops" -> Icons.Outlined.Grass
            "manure" -> Icons.Outlined.Eco
            else -> Icons.Outlined.AttachMoney
        }
        else -> when (category) {
            "feed" -> Icons.Outlined.Restaurant
            "labour" -> Icons.Outlined.People
            "vet" -> Icons.Outlined.MedicalServices
            "medicine" -> Icons.Outlined.Healing
            "seeds" -> Icons.Outlined.Eco
            "fertilizer" -> Icons.Outlined.Science
            else -> Icons.Outlined.ShoppingCart
        }
    }
}

@Composable
private fun AddIncomeDialog(
    onDismiss: () -> Unit,
    onAdd: (Income) -> Unit
) {
    var date by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()) }
    var category by remember { mutableStateOf("milk") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var buyerName by remember { mutableStateOf("") }

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
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Category Selector
                Column {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = Neutral600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("milk", "cheese", "animals", "crops").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = {
                                    Text(
                                        text = cat.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SurfaceSunken,
                                    selectedContainerColor = Green800.copy(alpha = 0.3f),
                                    labelColor = Neutral800,
                                    selectedLabelColor = Green300
                                )
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
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
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Buyer Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        Income(
                            date = LocalDate.parse(date),
                            category = category,
                            description = description,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            buyerName = buyerName.ifBlank { null }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = Green950
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Income")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = Neutral600
                )
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (Expense) -> Unit
) {
    var date by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()) }
    var category by remember { mutableStateOf("feed") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }

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
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Red500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Category Selector
                Column {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = Neutral600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("feed", "labour", "vet", "medicine").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = {
                                    Text(
                                        text = cat.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SurfaceSunken,
                                    selectedContainerColor = Red600.copy(alpha = 0.3f),
                                    labelColor = Neutral800,
                                    selectedLabelColor = Red300
                                )
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Red500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (TZS)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Red500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Red500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        Expense(
                            date = LocalDate.parse(date),
                            category = category,
                            description = description,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            supplier = supplier.ifBlank { null }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Red500,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = Neutral600
                )
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}