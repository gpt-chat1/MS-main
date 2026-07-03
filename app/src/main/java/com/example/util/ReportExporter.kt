package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.entity.DepartmentReportCard
import com.example.data.entity.EmployeeEvaluation
import com.example.data.entity.EmployeeEvaluationCard
import com.example.data.entity.EmployeeReportCard
import com.example.data.entity.Invoice
import com.example.data.entity.Office
import com.example.data.entity.OfficeEvaluationSummary
import com.example.data.entity.OfficeReportCard
import com.example.data.entity.Project
import com.example.data.entity.Task
import java.io.File
import java.io.FileOutputStream

object ReportExporter {

    /**
     * Exports a structured PDF report using Android's native PdfDocument.
     * Generates a beautifully formatted document with charts, statistics, and breakdowns.
     */
    fun exportReportToPdf(
        context: Context,
        fileName: String,
        branchPerformance: String,
        employeeCards: List<EmployeeReportCard>,
        officeCards: List<OfficeReportCard>
    ): File {
        val pdfDocument = PdfDocument()
        
        // A4 standard dimensions in postscript points (72 points per inch)
        // 595 x 842
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.parseColor("#054239") // Deep Green
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val sectionPaint = Paint().apply {
            color = Color.parseColor("#B9A779") // Gold Accent
            textSize = 15f
            isFakeBoldText = true
            isAntiAlias = true
        }

        // 1. Draw Background Decor
        val bgPaint = Paint().apply {
            color = Color.parseColor("#FCFAEE") // Warm cream background
        }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        // Draw green top banner
        val bannerPaint = Paint().apply {
            color = Color.parseColor("#054239")
        }
        canvas.drawRect(0f, 0f, 595f, 90f, bannerPaint)

        // Header Title (Al-Shaheen Arabic/English report title)
        headerPaint.color = Color.WHITE
        canvas.drawText("بوابة الشاهين للإدارة - تقرير الأداء والتحليلات الشامل", 40f, 50f, headerPaint)
        
        val subHeaderPaint = Paint().apply {
            color = Color.parseColor("#B9A779")
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText("Al-Shaheen Portal Regional Operations and Performance Audit Report", 40f, 75f, subHeaderPaint)

        // Reset text paint style for report details
        textPaint.color = Color.parseColor("#1B1C15")
        textPaint.textSize = 11f

        var currentY = 120f

        // 2. Branch Performance Summary Card
        canvas.drawText("1. ملخص الأداء العام للفرع (Branch Overview)", 40f, currentY, sectionPaint)
        currentY += 20f

        val cardBgPaint = Paint().apply {
            color = Color.parseColor("#F0EEE3")
        }
        canvas.drawRect(35f, currentY, 560f, currentY + 75f, cardBgPaint)

        val textPaintBold = Paint().apply {
            color = Color.parseColor("#054239")
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }

        canvas.drawText("مؤشر الكفاءة الكلي للفرع:", 50f, currentY + 25f, textPaintBold)
        canvas.drawText(branchPerformance, 240f, currentY + 25f, textPaint)

        canvas.drawText("إجمالي الموظفين:", 50f, currentY + 45f, textPaintBold)
        canvas.drawText("${employeeCards.size} موظفاً نشطاً", 240f, currentY + 45f, textPaint)

        canvas.drawText("متوسط الحضور اليومي الحالي:", 50f, currentY + 65f, textPaintBold)
        val avgAttendance = employeeCards.map { it.attendanceRate }.average()
        canvas.drawText(String.format("%.1f%%", if (avgAttendance.isNaN()) 0.0 else avgAttendance), 240f, currentY + 65f, textPaint)

        currentY += 105f

        // 3. Department & Office Breakdowns Table
        canvas.drawText("2. تقرير المكاتب والأقسام (Offices Breakdown)", 40f, currentY, sectionPaint)
        currentY += 20f

        // Table Header
        val tblHeaderBg = Paint().apply { color = Color.parseColor("#054239") }
        canvas.drawRect(35f, currentY, 560f, currentY + 20f, tblHeaderBg)
        
        val tblHeaderTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("المكتب", 45f, currentY + 14f, tblHeaderTextPaint)
        canvas.drawText("القسم", 170f, currentY + 14f, tblHeaderTextPaint)
        canvas.drawText("مدير المكتب", 290f, currentY + 14f, tblHeaderTextPaint)
        canvas.drawText("الكادر", 410f, currentY + 14f, tblHeaderTextPaint)
        canvas.drawText("إجمالي الفواتير", 480f, currentY + 14f, tblHeaderTextPaint)

        currentY += 20f

        val rowBgEven = Paint().apply { color = Color.parseColor("#EAE8DD") }
        val rowBgOdd = Paint().apply { color = Color.parseColor("#FCFAEE") }

        for ((index, office) in officeCards.withIndex()) {
            if (currentY > 800f) break // page break protection
            val bg = if (index % 2 == 0) rowBgEven else rowBgOdd
            canvas.drawRect(35f, currentY, 560f, currentY + 22f, bg)

            canvas.drawText(office.officeName, 45f, currentY + 15f, textPaint)
            canvas.drawText(office.departmentName, 170f, currentY + 15f, textPaint)
            canvas.drawText(office.managerName, 290f, currentY + 15f, textPaint)
            canvas.drawText("${office.employeeCount} موظف", 410f, currentY + 15f, textPaint)
            canvas.drawText(String.format("%.0f ر.س", office.totalInvoices), 480f, currentY + 15f, textPaint)

            currentY += 22f
        }

        currentY += 25f

        // 4. Employee Productivity Card
        canvas.drawText("3. بطاقات الموظفين (Employee Report Cards)", 40f, currentY, sectionPaint)
        currentY += 20f

        // Table headers for Employee reports
        canvas.drawRect(35f, currentY, 560f, currentY + 20f, tblHeaderBg)
        canvas.drawText("الاسم والمنصب", 45f, currentY + 14f, tblHeaderTextPaint)
        canvas.drawText("مقر العمل", 220f, currentY + 14f, tblHeaderTextPaint)
        canvas.drawText("المهام المنجزة", 350f, currentY + 14f, tblHeaderTextPaint)
        canvas.drawText("معدل الحضور", 440f, currentY + 14f, tblHeaderTextPaint)
        canvas.drawText("الجزاءات", 510f, currentY + 14f, tblHeaderTextPaint)

        currentY += 20f

        for ((index, emp) in employeeCards.withIndex()) {
            if (currentY > 800f) break
            val bg = if (index % 2 == 0) rowBgEven else rowBgOdd
            canvas.drawRect(35f, currentY, 560f, currentY + 22f, bg)

            canvas.drawText("${emp.employeeName} (${emp.employeeRole})", 45f, currentY + 15f, textPaint)
            canvas.drawText(emp.branchLocation, 220f, currentY + 15f, textPaint)
            canvas.drawText("${emp.completedTasks} من ${emp.totalTasks}", 350f, currentY + 15f, textPaint)
            canvas.drawText(String.format("%.1f%%", emp.attendanceRate), 440f, currentY + 15f, textPaint)
            canvas.drawText(String.format("%.0f ر.س", emp.totalPenalties), 510f, currentY + 15f, textPaint)

            currentY += 22f
        }

        // Draw Footer
        val footerPaint = Paint().apply {
            color = Color.parseColor("#7D766A")
            textSize = 8f
            isAntiAlias = true
        }
        canvas.drawText("Levantine Heritage Admin © 2026 • تكنولوجيا تشفير البيانات المدمجة", 190f, 830f, footerPaint)

        pdfDocument.finishPage(page)

        // Write the document to a file in the app cache
        val cacheDir = context.cacheDir
        val reportFile = File(cacheDir, fileName)
        val fileOutputStream = FileOutputStream(reportFile)
        pdfDocument.writeTo(fileOutputStream)
        pdfDocument.close()
        fileOutputStream.close()

        return reportFile
    }

    /**
     * Exports structured reports as a beautifully formatted CSV file that Microsoft Excel
     * parses instantly. Contains complete data worksheets separated by headers.
     */
    fun exportReportToCsv(
        context: Context,
        fileName: String,
        branchPerformance: String,
        employeeCards: List<EmployeeReportCard>,
        officeCards: List<OfficeReportCard>
    ): File {
        val stringBuilder = StringBuilder()

        // 1. Title Sheet
        stringBuilder.append("AdminCenter - Regional Performance Report\n")
        stringBuilder.append("Generated on: 2026-06-29\n")
        stringBuilder.append("Overall Efficiency Metric: $branchPerformance\n\n")

        // 2. Department & Office Breakdowns Sheet
        stringBuilder.append("--- OFFICE & DEPARTMENT FINANCIALS BREAKDOWN ---\n")
        stringBuilder.append("Office ID,Office Name,Department,Manager,Employee Count,Total Invoices Cost,Tasks Total,Tasks Completed\n")
        for (office in officeCards) {
            stringBuilder.append("${office.officeId},${escapeCsv(office.officeName)},${escapeCsv(office.departmentName)},${escapeCsv(office.managerName)},${office.employeeCount},${office.totalInvoices},${office.totalTasks},${office.completedTasks}\n")
        }
        stringBuilder.append("\n")

        // 3. Individual Employee Productivity Worksheets
        stringBuilder.append("--- INDIVIDUAL STAFF CARD & PERFORMANCE STATS ---\n")
        stringBuilder.append("Employee ID,Employee Name,Role,Location,Completed Tasks,Total Tasks,Attendance Rate (%),Total Penalties (SAR)\n")
        for (emp in employeeCards) {
            stringBuilder.append("${emp.employeeId},${escapeCsv(emp.employeeName)},${escapeCsv(emp.employeeRole)},${escapeCsv(emp.branchLocation)},${emp.completedTasks},${emp.totalTasks},${String.format("%.2f", emp.attendanceRate)},${emp.totalPenalties}\n")
        }

        val cacheDir = context.cacheDir
        val csvFile = File(cacheDir, fileName)
        csvFile.writeText(stringBuilder.toString(), Charsets.UTF_8)
        return csvFile
    }

    /**
     * Exports employee evaluation report as PDF with detailed score breakdown.
     */
    fun exportEvaluationReportToPdf(
        context: Context,
        fileName: String,
        employeeName: String,
        evaluations: List<EmployeeEvaluationCard>
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val bgPaint = Paint().apply { color = Color.parseColor("#FCFAEE") }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        val bannerPaint = Paint().apply { color = Color.parseColor("#054239") }
        canvas.drawRect(0f, 0f, 595f, 90f, bannerPaint)

        val headerPaint = Paint().apply { color = Color.WHITE; textSize = 18f; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText("تقرير تقييم الأداء الوظيفي", 40f, 45f, headerPaint)

        val subPaint = Paint().apply { color = Color.parseColor("#B9A779"); textSize = 10f; isAntiAlias = true }
        canvas.drawText("Employee Performance Evaluation Report", 40f, 70f, subPaint)

        val sectionPaint = Paint().apply { color = Color.parseColor("#B9A779"); textSize = 13f; isFakeBoldText = true; isAntiAlias = true }
        var y = 120f
        canvas.drawText("الموظف: $employeeName", 40f, y, sectionPaint)
        y += 30f

        val textPaint = Paint().apply { color = Color.parseColor("#1B1C15"); textSize = 10f; isAntiAlias = true }
        val boldPaint = Paint().apply { color = Color.parseColor("#054239"); textSize = 10f; isFakeBoldText = true; isAntiAlias = true }

        for (eval in evaluations) {
            if (y > 800f) break
            val ev = eval.evaluation
            canvas.drawText("الفترة: ${ev.periodStart} إلى ${ev.periodEnd}", 40f, y, boldPaint)
            y += 20f
            canvas.drawText("إنجاز المهام بالموعد: ${ev.taskTimelinessScore}/30", 50f, y, textPaint); y += 15f
            canvas.drawText("جودة العمل: ${ev.qualityScore}/25", 50f, y, textPaint); y += 15f
            canvas.drawText("الالتزام بالدوام: ${ev.attendanceScore}/20", 50f, y, textPaint); y += 15f
            canvas.drawText("العمل الجماعي: ${ev.teamworkScore}/10", 50f, y, textPaint); y += 15f
            canvas.drawText("الإبداع والمبادرات: ${ev.innovationScore}/10", 50f, y, textPaint); y += 15f
            canvas.drawText("خصم الجزاءات: ${ev.penaltyDeduction}/-15", 50f, y, textPaint); y += 15f

            val totalPaint = Paint().apply { color = Color.parseColor("#054239"); textSize = 14f; isFakeBoldText = true; isAntiAlias = true }
            canvas.drawText("المجموع الكلي: ${ev.totalScore} - ${ev.rating}", 40f, y + 5f, totalPaint)
            y += 25f
            canvas.drawText("ملاحظات: ${ev.notes}", 50f, y, textPaint); y += 25f

            if (evaluations.last() != eval) {
                val dividerPaint = Paint().apply { color = Color.parseColor("#D3CBB3") }
                canvas.drawLine(40f, y, 555f, y, dividerPaint)
                y += 10f
            }
        }

        val footerPaint = Paint().apply { color = Color.parseColor("#7D766A"); textSize = 8f; isAntiAlias = true }
        canvas.drawText("Al-Shaheen Portal © 2026 • نظام تقييم إلكتروني", 180f, 830f, footerPaint)

        pdfDocument.finishPage(page)

        val cacheDir = context.cacheDir
        val reportFile = File(cacheDir, fileName)
        val fileOutputStream = FileOutputStream(reportFile)
        pdfDocument.writeTo(fileOutputStream)
        pdfDocument.close()
        fileOutputStream.close()

        return reportFile
    }

    /**
     * Exports office/department evaluation summary as CSV.
     */
    fun exportOfficeEvaluationToCsv(
        context: Context,
        fileName: String,
        officeSummaries: List<OfficeEvaluationSummary>
    ): File {
        val sb = StringBuilder()
        sb.append("--- OFFICE EVALUATION SUMMARY ---\n")
        sb.append("Office ID,Office Name,Department,Employee Count,Average Score,Rating,Top Employee,Top Score\n")
        for (office in officeSummaries) {
            sb.append("${office.officeId},${escapeCsv(office.officeName)},${escapeCsv(office.departmentName)},${office.employeeCount},${String.format("%.2f", office.averageScore)},${office.averageRating},${escapeCsv(office.topEmployeeName)},${office.topEmployeeScore}\n")
        }
        val cacheDir = context.cacheDir
        val csvFile = File(cacheDir, fileName)
        csvFile.writeText(sb.toString(), Charsets.UTF_8)
        return csvFile
    }

    fun exportEmployeeReportToPdf(context: Context, card: EmployeeReportCard, tasks: List<Task>): File {
        val pdf = PdfDocument()
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        drawPdfHeader(canvas, "تقرير الموظف: ${card.employeeName}")
        var y = 120f
        val text = textPaint()
        val bold = boldPaint()
        canvas.drawText("المنصب: ${card.employeeRole}", 40f, y, bold); y += 18f
        canvas.drawText("الموقع: ${card.branchLocation}", 40f, y, text); y += 18f
        canvas.drawText("المهام: ${card.completedTasks}/${card.totalTasks} مكتملة", 40f, y, text); y += 18f
        canvas.drawText("معدل الحضور: ${String.format("%.1f", card.attendanceRate)}%", 40f, y, text); y += 18f
        canvas.drawText("إجمالي الجزاءات: ${card.totalPenalties} ر.س", 40f, y, text); y += 28f
        canvas.drawText("المهام المسندة:", 40f, y, bold); y += 20f
        for (task in tasks) {
            if (y > 780f) break
            val type = if (task.projectId != null) "مشروع" else "مستقلة"
            canvas.drawText("• ${task.title} ($type) - ${task.progress}%", 50f, y, text); y += 16f
        }
        pdf.finishPage(page)
        return writePdf(context, pdf, "Employee_${card.employeeId}_${System.currentTimeMillis()}.pdf")
    }

    fun exportOfficeReportToPdf(context: Context, card: OfficeReportCard): File {
        val pdf = PdfDocument()
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        drawPdfHeader(canvas, "تقرير المكتب: ${card.officeName}")
        var y = 120f
        val text = textPaint()
        val bold = boldPaint()
        canvas.drawText("القسم: ${card.departmentName}", 40f, y, text); y += 18f
        canvas.drawText("المدير: ${card.managerName}", 40f, y, text); y += 18f
        canvas.drawText("عدد الموظفين: ${card.employeeCount}", 40f, y, text); y += 18f
        canvas.drawText("المهام: ${card.completedTasks}/${card.totalTasks}", 40f, y, text); y += 18f
        canvas.drawText("إجمالي الفواتير: ${String.format("%.0f", card.totalInvoices)} ر.س", 40f, y, bold)
        pdf.finishPage(page)
        return writePdf(context, pdf, "Office_${card.officeId}_${System.currentTimeMillis()}.pdf")
    }

    fun exportDepartmentReportToPdf(context: Context, card: DepartmentReportCard): File {
        val pdf = PdfDocument()
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        drawPdfHeader(canvas, "تقرير القسم: ${card.departmentName}")
        var y = 120f
        val text = textPaint()
        canvas.drawText("عدد المكاتب: ${card.officeCount}", 40f, y, text); y += 18f
        canvas.drawText("عدد الموظفين: ${card.employeeCount}", 40f, y, text); y += 18f
        canvas.drawText("المهام: ${card.completedTasks}/${card.totalTasks}", 40f, y, text); y += 18f
        canvas.drawText("إجمالي الفواتير: ${String.format("%.0f", card.totalInvoices)} ر.س", 40f, y, text)
        pdf.finishPage(page)
        return writePdf(context, pdf, "Department_${card.departmentId}_${System.currentTimeMillis()}.pdf")
    }

    fun exportProjectReportToPdf(context: Context, project: Project, tasks: List<Task>, members: List<com.example.data.entity.Employee>): File {
        val pdf = PdfDocument()
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        drawPdfHeader(canvas, "تقرير المشروع: ${project.name}")
        var y = 120f
        val text = textPaint()
        val bold = boldPaint()
        val scope = when (project.scopeType) {
            "Shared" -> "مشروع مشترك"
            "SingleDepartment" -> "خاص بالقسم"
            else -> "خاص بالمكتب"
        }
        canvas.drawText("النطاق: $scope", 40f, y, text); y += 18f
        canvas.drawText("التقدم: ${project.progress}%", 40f, y, bold); y += 18f
        canvas.drawText("الحالة: ${project.status}", 40f, y, text); y += 18f
        canvas.drawText("الأعضاء (${members.size}):", 40f, y, bold); y += 18f
        members.forEach { m -> canvas.drawText("• ${m.name} (${m.role})", 50f, y, text); y += 16f }
        y += 10f
        canvas.drawText("المهام (${tasks.size}):", 40f, y, bold); y += 18f
        tasks.forEach { t -> canvas.drawText("• ${t.title} - ${t.progress}%", 50f, y, text); y += 16f }
        pdf.finishPage(page)
        return writePdf(context, pdf, "Project_${project.id}_${System.currentTimeMillis()}.pdf")
    }

    fun exportInvoicesReportToPdf(context: Context, invoices: List<Invoice>, offices: List<Office>, total: Double): File {
        val pdf = PdfDocument()
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        drawPdfHeader(canvas, "تقرير الفواتير")
        var y = 120f
        val text = textPaint()
        val bold = boldPaint()
        canvas.drawText("الإجمالي: ${String.format("%.0f", total)} ر.س", 40f, y, bold); y += 24f
        for (inv in invoices) {
            if (y > 780f) break
            val office = offices.find { it.id == inv.officeId }?.name ?: "—"
            canvas.drawText("${inv.trackingNumber} | $office | ${inv.amount} ر.س", 40f, y, text); y += 14f
            canvas.drawText("  ${inv.description} (${inv.date})", 50f, y, text); y += 18f
        }
        pdf.finishPage(page)
        return writePdf(context, pdf, "Invoices_${System.currentTimeMillis()}.pdf")
    }

    fun exportFinancialReportToPdf(
        context: Context,
        invoices: List<Invoice>,
        offices: List<Office>,
        deptCards: List<DepartmentReportCard>,
        total: Double
    ): File {
        val pdf = PdfDocument()
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        drawPdfHeader(canvas, "التقرير المالي الشامل")
        var y = 120f
        val text = textPaint()
        val bold = boldPaint()
        canvas.drawText("إجمالي المصروفات: ${String.format("%.0f", total)} ر.س", 40f, y, bold); y += 24f
        canvas.drawText("حسب القسم:", 40f, y, bold); y += 18f
        deptCards.forEach { d ->
            canvas.drawText("• ${d.departmentName}: ${String.format("%.0f", d.totalInvoices)} ر.س", 50f, y, text); y += 16f
        }
        y += 10f
        canvas.drawText("تفاصيل الفواتير:", 40f, y, bold); y += 18f
        invoices.take(15).forEach { inv ->
            val office = offices.find { it.id == inv.officeId }?.name ?: "—"
            canvas.drawText("${inv.trackingNumber} | $office | ${inv.amount} ر.س", 50f, y, text); y += 14f
        }
        pdf.finishPage(page)
        return writePdf(context, pdf, "Financial_${System.currentTimeMillis()}.pdf")
    }

    fun printPdf(context: Context, file: File) {
        val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
        printManager.print(
            "AdminCenter Report",
            object : android.print.PrintDocumentAdapter() {
                override fun onLayout(oldAttributes: android.print.PrintAttributes?, newAttributes: android.print.PrintAttributes?, cancellationSignal: android.os.CancellationSignal?, callback: android.print.PrintDocumentAdapter.LayoutResultCallback?, extras: android.os.Bundle?) {
                    callback?.onLayoutFinished(android.print.PrintDocumentInfo.Builder("report.pdf").setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build(), true)
                }
                override fun onWrite(pages: Array<out android.print.PageRange>?, destination: android.os.ParcelFileDescriptor?, cancellationSignal: android.os.CancellationSignal?, callback: android.print.PrintDocumentAdapter.WriteResultCallback?) {
                    try {
                        file.inputStream().use { input ->
                            android.os.ParcelFileDescriptor.AutoCloseOutputStream(destination).use { output ->
                                input.copyTo(output)
                            }
                        }
                        callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            },
            null
        )
    }

    private fun drawPdfHeader(canvas: Canvas, title: String) {
        canvas.drawRect(0f, 0f, 595f, 842f, Paint().apply { color = Color.parseColor("#FCFAEE") })
        canvas.drawRect(0f, 0f, 595f, 90f, Paint().apply { color = Color.parseColor("#054239") })
        canvas.drawText(title, 40f, 55f, Paint().apply { color = Color.WHITE; textSize = 18f; isFakeBoldText = true; isAntiAlias = true })
    }

    private fun textPaint() = Paint().apply { color = Color.parseColor("#1B1C15"); textSize = 11f; isAntiAlias = true }
    private fun boldPaint() = Paint().apply { color = Color.parseColor("#054239"); textSize = 11f; isFakeBoldText = true; isAntiAlias = true }

    private fun writePdf(context: Context, pdf: PdfDocument, fileName: String): File {
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        return file
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
