package io.devopen.subzilla.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.*
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import io.devopen.subzilla.MainActivity
import java.time.LocalDate

// ── Color helpers ─────────────────────────────────────────────────────────────

private fun Long.toGlanceColor(): Color {
    val a = ((this shr 24) and 0xFF).toFloat() / 255f
    val r = ((this shr 16) and 0xFF).toFloat() / 255f
    val g = ((this shr 8) and 0xFF).toFloat() / 255f
    val b = (this and 0xFF).toFloat() / 255f
    return Color(r, g, b, a)
}

// ── Shared composables ────────────────────────────────────────────────────────

@Composable
private fun EmptyState(message: String) {
    Box(contentAlignment = Alignment.Center, modifier = GlanceModifier.fillMaxSize()) {
        Text(message, style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color.Gray)))
    }
}

@Composable
private fun SubRow(sub: WidgetSub, currency: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.fillMaxWidth()) {
        Box(
            modifier = GlanceModifier
                .size(8.dp)
                .cornerRadius(4.dp)
                .background(ColorProvider(sub.color.toGlanceColor()))
        ) {}
        Spacer(modifier = GlanceModifier.width(6.dp))
        Text(
            sub.name,
            style = TextStyle(fontSize = 12.sp),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1
        )
        Text(
            "$currency${String.format("%.2f", sub.amount)}",
            style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color.Gray))
        )
    }
}

// ── Monthly Spend Widget ──────────────────────────────────────────────────────

class MonthlySpendWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = SubsDataReader.read(context)
        provideContent {
            GlanceTheme {
                MonthlySpendContent(data)
            }
        }
    }
}

@Composable
private fun MonthlySpendContent(data: WidgetData?) {
    val modifier = GlanceModifier
        .fillMaxSize()
        .background(GlanceTheme.colors.background)
        .padding(12.dp)
        .clickable(actionStartActivity<MainActivity>())

    if (data == null || data.subs.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            EmptyState("Add a subscription")
        }
        return
    }

    Column(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("This Month", style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color.Gray)))
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            "${data.currency}${String.format("%.2f", data.monthlyTotal)}",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            "${data.subs.size} subscriptions",
            style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color.Gray))
        )
    }
}

class MonthlySpendWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = MonthlySpendWidget()
}

// ── Next Due Widget ───────────────────────────────────────────────────────────

class NextDueWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = SubsDataReader.read(context)
        val today = LocalDate.now()
        val sorted = data?.subs
            ?.mapNotNull { sub ->
                NextDueDateHelper.parseDate(sub.startDate)?.let { start ->
                    val due = NextDueDateHelper.nextDueDate(start, sub.frequency, today)
                    Triple(sub, due, NextDueDateHelper.daysUntil(due, today))
                }
            }
            ?.sortedBy { it.second }
        provideContent {
            GlanceTheme {
                NextDueContent(data, sorted)
            }
        }
    }
}

@Composable
private fun NextDueContent(data: WidgetData?, sorted: List<Triple<WidgetSub, LocalDate, Long>>?) {
    val modifier = GlanceModifier
        .fillMaxSize()
        .background(GlanceTheme.colors.background)
        .padding(12.dp)
        .clickable(actionStartActivity<MainActivity>())

    if (sorted.isNullOrEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            EmptyState("No subscriptions")
        }
        return
    }

    val (first, _, days) = sorted[0]
    Column(modifier = modifier) {
        Text("Next Due", style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color.Gray)))
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(first.name, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold), maxLines = 1)
        Text(
            "${data!!.currency}${String.format("%.2f", first.amount)}",
            style = TextStyle(fontSize = 13.sp, color = ColorProvider(Color.Gray))
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            if (days == 0L) "Due today" else "in ${days}d",
            style = TextStyle(
                fontSize = 11.sp,
                color = ColorProvider(if (days == 0L) Color.Red else Color.Gray)
            )
        )
    }
}

class NextDueWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = NextDueWidget()
}

// ── Upcoming Widget ───────────────────────────────────────────────────────────

class UpcomingWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = SubsDataReader.read(context)
        val today = LocalDate.now()
        val sorted = data?.subs
            ?.mapNotNull { sub ->
                NextDueDateHelper.parseDate(sub.startDate)?.let { start ->
                    Pair(sub, NextDueDateHelper.nextDueDate(start, sub.frequency, today))
                }
            }
            ?.sortedBy { it.second }
        provideContent {
            GlanceTheme {
                UpcomingContent(data, sorted)
            }
        }
    }
}

@Composable
private fun UpcomingContent(data: WidgetData?, sorted: List<Pair<WidgetSub, LocalDate>>?) {
    val modifier = GlanceModifier
        .fillMaxSize()
        .background(GlanceTheme.colors.background)
        .padding(12.dp)
        .clickable(actionStartActivity<MainActivity>())

    if (sorted.isNullOrEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            EmptyState("No subscriptions")
        }
        return
    }

    Column(modifier = modifier, verticalAlignment = Alignment.Top) {
        Text(
            "Upcoming",
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.Gray))
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
        sorted.take(6).forEach { (sub, _) ->
            SubRow(sub = sub, currency = data!!.currency)
            Spacer(modifier = GlanceModifier.height(4.dp))
        }
        Spacer(modifier = GlanceModifier.defaultWeight())
        Text(
            "${data!!.currency}${String.format("%.2f", data.monthlyTotal)} / mo",
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)
        )
    }
}

class UpcomingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = UpcomingWidget()
}
