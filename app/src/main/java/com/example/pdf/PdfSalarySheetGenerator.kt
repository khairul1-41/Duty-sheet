package com.example.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.DutyEntry
import com.example.data.model.SalarySheetWithEntries
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfSalarySheetGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN = 30f

    private val currencyFormat = DecimalFormat("#,##0.##")

    fun generatePdf(context: Context, data: SalarySheetWithEntries): File {
        val document = PdfDocument()

        val entries = data.sortedEntries
        val maxRowsPerPage = 26
        val totalPages = if (entries.isEmpty()) 1 else ((entries.size - 1) / maxRowsPerPage) + 1

        var currentEntryIndex = 0

        for (pageIndex in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val isFirstPage = (pageIndex == 1)
            val isLastPage = (pageIndex == totalPages)

            val pageEntries = mutableListOf<DutyEntry>()
            while (currentEntryIndex < entries.size && pageEntries.size < maxRowsPerPage) {
                pageEntries.add(entries[currentEntryIndex])
                currentEntryIndex++
            }

            drawPageContent(
                canvas = canvas,
                data = data,
                entriesForPage = pageEntries,
                pageNumber = pageIndex,
                totalPages = totalPages,
                isFirstPage = isFirstPage,
                isLastPage = isLastPage
            )

            document.finishPage(page)
        }

        val outputDir = File(context.cacheDir, "generated_pdfs").apply { mkdirs() }
        val sanitizedEmployee = data.sheet.employeeName.replace("\\s+".toRegex(), "_")
        val sanitizedMonth = data.sheet.monthYear.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val pdfFile = File(outputDir, "Salary_Sheet_${sanitizedEmployee}_$sanitizedMonth.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    private fun drawPageContent(
        canvas: Canvas,
        data: SalarySheetWithEntries,
        entriesForPage: List<DutyEntry>,
        pageNumber: Int,
        totalPages: Int,
        isFirstPage: Boolean,
        isLastPage: Boolean
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var yCursor = MARGIN

        // Background
        canvas.drawColor(Color.WHITE)

        // 1. Header Information Box (First Page)
        if (isFirstPage) {
            yCursor = drawHeader(canvas, paint, data, yCursor)
        } else {
            // Header for continuation pages
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10f
            paint.color = Color.DKGRAY
            canvas.drawText("${data.sheet.employeeName} - ${data.sheet.monthYear} (Cont.)", MARGIN, yCursor + 12f, paint)
            yCursor += 25f
        }

        // 2. Data Table
        yCursor = drawTable(canvas, paint, data, entriesForPage, yCursor)

        // 3. Totals and Signatures (Only on Last Page)
        if (isLastPage) {
            yCursor = drawSummaryAndSignatures(canvas, paint, data, yCursor)
        }

        // 4. Footer & Page Numbers
        drawFooter(canvas, paint, pageNumber, totalPages)
    }

    private fun drawHeader(
        canvas: Canvas,
        paint: Paint,
        data: SalarySheetWithEntries,
        startY: Float
    ): Float {
        var currentY = startY

        // Company Name Banner if present
        if (data.sheet.companyName.isNotBlank()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 14f
            paint.color = Color.rgb(0, 100, 115) // Deep cyan
            canvas.drawText(data.sheet.companyName.uppercase(Locale.getDefault()), MARGIN, currentY + 12f, paint)
            currentY += 20f
        }

        // Title box or Header info table
        val headerBoxWidth = PAGE_WIDTH - (MARGIN * 2)
        val headerRowHeight = 20f
        val headerBoxHeight = headerRowHeight * 3

        // Border around header info box
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.rgb(200, 200, 200)
        canvas.drawRect(MARGIN, currentY, MARGIN + headerBoxWidth, currentY + headerBoxHeight, paint)

        // Horizontal dividers
        canvas.drawLine(MARGIN, currentY + headerRowHeight, MARGIN + headerBoxWidth, currentY + headerRowHeight, paint)
        canvas.drawLine(MARGIN, currentY + (headerRowHeight * 2), MARGIN + headerBoxWidth, currentY + (headerRowHeight * 2), paint)

        // Draw Header Labels & Values matching prompt:
        // NAME :KHAIRUL
        // NAME OF MANTH:SEPTEMBER (2026)
        // ACCOUNT :01920235578 (BKASH)
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textSize = 10f

        // Row 1: Name
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NAME :", MARGIN + 8f, currentY + 14f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(data.sheet.employeeName.uppercase(Locale.getDefault()), MARGIN + 55f, currentY + 14f, paint)

        // Row 2: Month
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NAME OF MONTH :", MARGIN + 8f, currentY + headerRowHeight + 14f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(data.sheet.monthYear.uppercase(Locale.getDefault()), MARGIN + 110f, currentY + headerRowHeight + 14f, paint)

        // Row 3: Account
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ACCOUNT :", MARGIN + 8f, currentY + (headerRowHeight * 2) + 14f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(data.sheet.accountDetails.uppercase(Locale.getDefault()), MARGIN + 75f, currentY + (headerRowHeight * 2) + 14f, paint)

        return currentY + headerBoxHeight + 8f
    }

    private fun drawTable(
        canvas: Canvas,
        paint: Paint,
        data: SalarySheetWithEntries,
        entries: List<DutyEntry>,
        startY: Float
    ): Float {
        var currentY = startY
        val tableWidth = PAGE_WIDTH - (MARGIN * 2)

        // Column widths matching format: DATE | ADVANCE | TRAVEL | WORK PLACE | DUTY | OTHER
        // Total width: 535 pt
        val colWidthDate = 70f
        val colWidthAdvance = 65f
        val colWidthTravel = 160f
        val colWidthWorkPlace = 105f
        val colWidthDuty = 85f
        val colWidthOther = 50f

        val colXDate = MARGIN
        val colXAdvance = colXDate + colWidthDate
        val colXTravel = colXAdvance + colWidthAdvance
        val colXWorkPlace = colXTravel + colWidthTravel
        val colXDuty = colXWorkPlace + colWidthWorkPlace
        val colXOther = colXDuty + colWidthDuty

        val headerHeight = 22f
        val rowHeight = 17f

        // Table Header Background (Cyan color matching user's PDF: #00E5FF or #00BCD4)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(0, 229, 255) // Vibrant Cyan
        canvas.drawRect(MARGIN, currentY, MARGIN + tableWidth, currentY + headerHeight, paint)

        // Table Header Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.BLACK
        canvas.drawRect(MARGIN, currentY, MARGIN + tableWidth, currentY + headerHeight, paint)

        // Table Header Text
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        paint.color = Color.BLACK

        drawCenteredText(canvas, paint, "DATE", colXDate, colWidthDate, currentY + 15f)
        drawCenteredText(canvas, paint, "ADVANCE", colXAdvance, colWidthAdvance, currentY + 15f)
        drawCenteredText(canvas, paint, "TRAVEL", colXTravel, colWidthTravel, currentY + 15f)
        drawCenteredText(canvas, paint, "WORK PLACE", colXWorkPlace, colWidthWorkPlace, currentY + 15f)
        drawCenteredText(canvas, paint, "DUTY", colXDuty, colWidthDuty, currentY + 15f)
        drawCenteredText(canvas, paint, "OTHER", colXOther, colWidthOther, currentY + 15f)

        // Vertical lines for header
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        canvas.drawLine(colXAdvance, currentY, colXAdvance, currentY + headerHeight, paint)
        canvas.drawLine(colXTravel, currentY, colXTravel, currentY + headerHeight, paint)
        canvas.drawLine(colXWorkPlace, currentY, colXWorkPlace, currentY + headerHeight, paint)
        canvas.drawLine(colXDuty, currentY, colXDuty, currentY + headerHeight, paint)
        canvas.drawLine(colXOther, currentY, colXOther, currentY + headerHeight, paint)

        currentY += headerHeight

        // Draw Rows
        val totalRowsToDraw = maxOf(entries.size, 18) // ensure minimum rows visually like template

        for (i in 0 until totalRowsToDraw) {
            val entry = if (i < entries.size) entries[i] else null
            val rowY = currentY + (i * rowHeight)

            // Row background (subtle zebra or pure white)
            paint.style = Paint.Style.FILL
            paint.color = if (entry != null && (entry.dutyType.equals("stay Home", true) || entry.dutyType.equals("Off", true))) {
                Color.rgb(250, 245, 245) // soft highlight for off/stay home
            } else {
                Color.WHITE
            }
            canvas.drawRect(MARGIN, rowY, MARGIN + tableWidth, rowY + rowHeight, paint)

            // Row borders
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.6f
            paint.color = Color.rgb(70, 70, 70)
            canvas.drawRect(MARGIN, rowY, MARGIN + tableWidth, rowY + rowHeight, paint)

            // Vertical column dividers
            canvas.drawLine(colXAdvance, rowY, colXAdvance, rowY + rowHeight, paint)
            canvas.drawLine(colXTravel, rowY, colXTravel, rowY + rowHeight, paint)
            canvas.drawLine(colXWorkPlace, rowY, colXWorkPlace, rowY + rowHeight, paint)
            canvas.drawLine(colXDuty, rowY, colXDuty, rowY + rowHeight, paint)
            canvas.drawLine(colXOther, rowY, colXOther, rowY + rowHeight, paint)

            // Draw Entry Values
            if (entry != null) {
                paint.style = Paint.Style.FILL
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.BLACK

                // DATE
                drawCenteredText(canvas, paint, entry.dateStr, colXDate, colWidthDate, rowY + 12f)

                // ADVANCE
                if (entry.advanceAmount > 0) {
                    val advanceText = currencyFormat.format(entry.advanceAmount)
                    drawCenteredText(canvas, paint, advanceText, colXAdvance, colWidthAdvance, rowY + 12f)
                }

                // TRAVEL
                if (entry.travelDescription.isNotBlank()) {
                    paint.textAlign = Paint.Align.LEFT
                    val clippedTravel = clipTextToWidth(paint, entry.travelDescription, colWidthTravel - 8f)
                    canvas.drawText(clippedTravel, colXTravel + 4f, rowY + 12f, paint)
                }

                // WORK PLACE
                if (entry.workPlace.isNotBlank()) {
                    drawCenteredText(canvas, paint, entry.workPlace, colXWorkPlace, colWidthWorkPlace, rowY + 12f)
                }

                // DUTY
                if (entry.dutyType.isNotBlank()) {
                    if (entry.dutyType.contains("Night", ignoreCase = true)) {
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                    drawCenteredText(canvas, paint, entry.dutyType, colXDuty, colWidthDuty, rowY + 12f)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }

                // OTHER
                if (entry.otherNote.isNotBlank()) {
                    paint.textAlign = Paint.Align.LEFT
                    val clippedOther = clipTextToWidth(paint, entry.otherNote, colWidthOther - 6f)
                    canvas.drawText(clippedOther, colXOther + 3f, rowY + 12f, paint)
                }
            }
        }

        return currentY + (totalRowsToDraw * rowHeight)
    }

    private fun drawSummaryAndSignatures(
        canvas: Canvas,
        paint: Paint,
        data: SalarySheetWithEntries,
        startY: Float
    ): Float {
        var currentY = startY + 10f
        val tableWidth = PAGE_WIDTH - (MARGIN * 2)

        // Summary Card Box
        val summaryBoxHeight = 85f
        val summaryRect = RectF(MARGIN, currentY, MARGIN + tableWidth, currentY + summaryBoxHeight)

        // Summary Background
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(240, 248, 250) // Very light teal
        canvas.drawRoundRect(summaryRect, 4f, 4f, paint)

        // Summary Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.rgb(0, 150, 165)
        canvas.drawRoundRect(summaryRect, 4f, 4f, paint)

        // Summary Content: 2 Columns of Details
        val col1X = MARGIN + 12f
        val col2X = MARGIN + (tableWidth / 2f) + 10f

        paint.style = Paint.Style.FILL
        paint.textSize = 8.5f
        paint.color = Color.DKGRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        // Column 1: Duty days & Basic Salary
        canvas.drawText("Working Days: ${data.workingDaysCount} days (Duties: ${currencyFormat.format(data.totalDutyUnits)})", col1X, currentY + 16f, paint)
        canvas.drawText("Duty Rate: ${currencyFormat.format(data.sheet.dailyRate)} ${data.sheet.currency} / duty", col1X, currentY + 32f, paint)
        canvas.drawText("Duty Earnings: ${currencyFormat.format(data.dutyEarnings)} ${data.sheet.currency}", col1X, currentY + 48f, paint)
        canvas.drawText("Travel Reimbursement: ${currencyFormat.format(data.totalTravel)} ${data.sheet.currency}", col1X, currentY + 64f, paint)

        // Column 2: Deductions & Net Payable
        canvas.drawText("Gross Total: ${currencyFormat.format(data.grossEarnings)} ${data.sheet.currency}", col2X, currentY + 16f, paint)
        canvas.drawText("Advance Deducted: -${currencyFormat.format(data.totalAdvance)} ${data.sheet.currency}", col2X, currentY + 32f, paint)

        if (data.sheet.otherDeductions > 0) {
            canvas.drawText("Other Deductions: -${currencyFormat.format(data.sheet.otherDeductions)} ${data.sheet.currency}", col2X, currentY + 48f, paint)
        }

        // NET PAYABLE SALARY Highlight Banner
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(0, 121, 107) // Deep Teal
        val netBanner = RectF(col2X - 5f, currentY + 54f, MARGIN + tableWidth - 10f, currentY + 76f)
        canvas.drawRoundRect(netBanner, 3f, 3f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("NET PAYABLE: ${currencyFormat.format(data.netPayable)} ${data.sheet.currency}", col2X + 4f, currentY + 69f, paint)

        currentY += summaryBoxHeight + 35f

        // Signature Lines
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        paint.color = Color.DKGRAY

        val sigWidth = 110f
        val sig1X = MARGIN + 20f
        val sig2X = MARGIN + (tableWidth / 2f) - (sigWidth / 2f)
        val sig3X = MARGIN + tableWidth - sigWidth - 20f

        canvas.drawLine(sig1X, currentY, sig1X + sigWidth, currentY, paint)
        canvas.drawLine(sig2X, currentY, sig2X + sigWidth, currentY, paint)
        canvas.drawLine(sig3X, currentY, sig3X + sigWidth, currentY, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.BLACK

        drawCenteredText(canvas, paint, "Employee Signature", sig1X, sigWidth, currentY + 12f)
        drawCenteredText(canvas, paint, "Prepared By", sig2X, sigWidth, currentY + 12f)
        drawCenteredText(canvas, paint, "Authorized / Accounts", sig3X, sigWidth, currentY + 12f)

        return currentY + 30f
    }

    private fun drawFooter(canvas: Canvas, paint: Paint, pageNumber: Int, totalPages: Int) {
        val y = PAGE_HEIGHT - 18f
        paint.style = Paint.Style.FILL
        paint.textSize = 7.5f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Generated via Salary Sheet Maker • $timestamp", MARGIN, y, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page $pageNumber of $totalPages", PAGE_WIDTH - MARGIN, y, paint)
    }

    private fun drawCenteredText(
        canvas: Canvas,
        paint: Paint,
        text: String,
        colX: Float,
        colWidth: Float,
        y: Float
    ) {
        paint.textAlign = Paint.Align.CENTER
        val centerX = colX + (colWidth / 2f)
        val clipped = clipTextToWidth(paint, text, colWidth - 4f)
        canvas.drawText(clipped, centerX, y, paint)
    }

    private fun clipTextToWidth(paint: Paint, text: String, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var clipped = text
        while (clipped.isNotEmpty() && paint.measureText("$clipped…") > maxWidth) {
            clipped = clipped.dropLast(1)
        }
        return "$clipped…"
    }
}
