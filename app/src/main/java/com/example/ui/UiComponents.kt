package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import com.example.ui.theme.AlShaheenBorderGold
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun todayIsoDate(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

fun isoToDisplay(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        formatter.format(parser.parse(iso)!!)
    } catch (_: Exception) {
        iso
    }
}

fun periodDateRange(period: String): Pair<String, String> {
    val cal = Calendar.getInstance()
    val end = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    val start = when (period) {
        "اليوم" -> end
        "الأسبوع" -> {
            cal.add(Calendar.DAY_OF_MONTH, -7)
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        }
        "الشهر" -> {
            cal.add(Calendar.MONTH, -1)
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        }
        else -> end
    }
    return Pair(start, end)
}

@Composable
fun shaheenTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    disabledTextColor = Color.Black.copy(alpha = 0.5f),
    focusedBorderColor = DeepGreen,
    unfocusedBorderColor = AlShaheenBorderGold,
    cursorColor = DeepGreen,
    focusedLabelColor = DeepGreen,
    unfocusedLabelColor = TextMuted
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShaheenOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp),
    textStyle: TextStyle = TextStyle(color = Color.Black)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = shape,
        textStyle = textStyle,
        colors = shaheenTextFieldColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val display = isoToDisplay(selectedDate)

    ShaheenOutlinedTextField(
        value = display,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showPicker = true },
        placeholder = { Text("اضغط لاختيار التاريخ") }
    )

    if (showPicker) {
        val initialMillis = try {
            if (selectedDate.isNotBlank()) {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate)?.time
            } else null
        } catch (_: Exception) {
            null
        } ?: System.currentTimeMillis()
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        val iso = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.DAY_OF_MONTH)
                        )
                        onDateSelected(iso)
                    }
                    showPicker = false
                }) { Text("تأكيد", color = DeepGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("إلغاء", color = TextMuted) }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
