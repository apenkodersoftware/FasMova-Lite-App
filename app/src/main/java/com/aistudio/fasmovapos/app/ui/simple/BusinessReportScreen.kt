package com.aistudio.fasmovapos.app.ui.simple

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.fasmovapos.app.R
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

@Composable
fun BusinessReportScreen(
  data: ReportInput,
  onExpense: () -> Unit,
  onDebts: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var period by remember { mutableStateOf(ReportPeriod.DAY) }
  val now by produceState(System.currentTimeMillis()) {
    while (true) {
      delay(30_000)
      value = System.currentTimeMillis()
    }
  }
  val window = reportWindow(period, now)
  val report = businessReport(data, window)
  LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    item { Text(stringResource(R.string.lite_report), style = MaterialTheme.typography.headlineSmall) }
    item {
      Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
          ReportPeriod.DAY to R.string.lite_daily,
          ReportPeriod.WEEK to R.string.lite_weekly,
          ReportPeriod.MONTH to R.string.lite_monthly,
          ReportPeriod.YEAR to R.string.lite_yearly,
        ).forEach { (option, label) ->
          FilterChip(
            selected = period == option,
            onClick = { period = option },
            label = { Text(stringResource(label)) },
            modifier = Modifier.testTag("lite_period_${option.name}"),
          )
        }
      }
    }
    item {
      Text(
        DateFormat.getDateInstance().format(Date(window.start)) + " — " + DateFormat.getDateInstance().format(Date(window.end - 1)),
      )
    }
    item { BusinessPanel(report) }
    item { ReceiptsPanel(report) }
    item { Button(onClick = onExpense, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.lite_add_expense)) } }
    item { OutlinedButton(onClick = onDebts, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.lite_debts)) } }
    item { Text(stringResource(R.string.lite_estimate_note), style = MaterialTheme.typography.bodyMedium) }
  }
}

@Composable
fun BusinessPanel(
  report: BusinessReport,
  modifier: Modifier = Modifier,
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      ReportFigure(R.string.lite_sales, report.sales, "lite_report_sales")
      ReportFigure(R.string.lite_expenses, report.expenses, "lite_report_expenses")
      HorizontalDivider()
      ReportFigure(if (report.estimate < 0) R.string.lite_loss else R.string.lite_estimate, report.estimate, "lite_report_estimate")
    }
  }
}

@Composable
fun ReceiptsPanel(
  report: BusinessReport,
  modifier: Modifier = Modifier,
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      ReportFigure(R.string.lite_received, report.received, "lite_report_received")
      ReportFigure(R.string.lite_credit_period, report.credit, "lite_report_credit")
      ReportFigure(R.string.lite_collections, report.collected, "lite_report_collections")
      HorizontalDivider()
      ReportFigure(R.string.lite_outstanding, report.outstanding, "lite_report_outstanding")
      ReportFigure(R.string.lite_net_receipts, report.netReceipts, "lite_report_net")
      Text(stringResource(R.string.lite_receipts_note))
    }
  }
}

@Composable
private fun ReportFigure(
  label: Int,
  amount: Double,
  tag: String,
) {
  Column {
    Text(stringResource(label), style = MaterialTheme.typography.bodyLarge)
    Text(simpleMoney(amount), fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, modifier = Modifier.testTag(tag))
  }
}
