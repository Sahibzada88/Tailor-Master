package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.Customer
import com.example.data.CustomerMeasurement
import com.example.data.Order
import com.example.data.OrderWithCustomer
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    private val dateFormatter = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault())

    fun generateReceiptPdf(context: Context, customer: Customer, order: Order): File {
        val pdfDocument = PdfDocument()
        // Page specification: A4 size is approx 595 x 842 points
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        // Draw header background block
        paint.color = Color.rgb(18, 114, 110) // Deep Dark Teal (Luxurious Darzi Brand)
        canvas.drawRect(0f, 0f, 595f, 130f, paint)

        // Title
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 28f
        canvas.drawText("TAILOR BOOK", 40f, 60f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Professional Custom Stitching & Diagnostics", 40f, 85f, paint)
        canvas.drawText("Phone: +92-Offline-Darzi", 40f, 105f, paint)

        // Receipt label top-right
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ORDER RECEIPT", 555f, 60f, paint)

        val trkTag = if (order.trackingId.isNotBlank()) order.trackingId else "TRK-${order.id}"
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Date: ${dateFormatter.format(Date(order.orderDate))}", 555f, 82f, paint)
        canvas.drawText("Invoice: #OR-${order.id}", 555f, 98f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TAG / TRACKING: $trkTag", 555f, 115f, paint)

        // Reset text paint
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT

        // Draw section: CUSTOMER DETAILS
        var y = 170f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        paint.color = Color.rgb(18, 114, 110)
        canvas.drawText("CUSTOMER DETAILS", 40f, y, paint)
        
        y += 6f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)

        y += 24f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Name:", 40f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(customer.name, 100f, y, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Phone:", 320f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(customer.phone, 380f, y, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Address:", 40f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(customer.address.ifBlank { "N/A" }, 110f, y, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Gender:", 320f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(customer.gender, 380f, y, paint)

        // Draw section: SHIRT / KAMEEZ MEASUREMENTS
        y += 40f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        paint.color = Color.rgb(18, 114, 110)
        canvas.drawText("STITCHING MEASUREMENTS", 40f, y, paint)

        y += 6f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)

        y += 24f
        paint.color = Color.BLACK
        paint.textSize = 11f
        
        // Draw Kameez items in 2 columns
        canvas.drawText("KAMEEZ / KURTA (Upper Wear)", 40f, y, paint)
        canvas.drawText("SHALWAR / TROUSER (Lower Wear)", 320f, y, paint)

        y += 8f
        paint.color = Color.rgb(240, 240, 240)
        canvas.drawRect(40f, y, 280f, y + 130f, paint)
        canvas.drawRect(320f, y, 555f, y + 130f, paint)

        y += 20f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        // Upper Columns
        canvas.drawText("Length (Lambaie):   ${order.shirtLength}\"", 50f, y, paint)
        canvas.drawText("S. Length:   ${order.trouserLength}\"", 330f, y, paint)

        y += 18f
        canvas.drawText("Shoulder (Teera):    ${order.shoulder}\"", 50f, y, paint)
        canvas.drawText("S. Bottom (Mori):  ${order.trouserBottom}\"", 330f, y, paint)

        y += 18f
        canvas.drawText("Sleeves (Aasteen):   ${order.sleeves}\"", 50f, y, paint)
        canvas.drawText("Aasan (Thigh):     ${order.trouserAsan}\"", 330f, y, paint)

        y += 18f
        canvas.drawText("Chest (Seena):       ${order.chest}\"", 50f, y, paint)

        y += 18f
        canvas.drawText("Waist (Kamar):       ${order.waist}\"", 50f, y, paint)

        y += 18f
        canvas.drawText("Hip (Ghera):         ${order.hip}\"", 50f, y, paint)

        y += 12f
        canvas.drawText("Collar (Neck):       ${order.collar}\"", 50f, y, paint)

        y += 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Order Items:", 40f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("${order.itemType} (${order.clothType.ifBlank { "Unspecified Material" }})", 140f, y, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Order Deadline:", 40f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(dateFormatter.format(Date(order.dueDate)), 160f, y, paint)

        // Draw Ledger table/card
        y += 35f
        paint.color = Color.rgb(245, 245, 245)
        canvas.drawRect(40f, y, 555f, y + 150f, paint)

        // Draw Ledger Border
        paint.color = Color.rgb(18, 114, 110)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(40f, y, 555f, y + 150f, paint)

        // Fill elements
        paint.style = Paint.Style.FILL
        y += 25f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        canvas.drawText("STATEMENT OF ACCOUNT", 60f, y, paint)

        y += 25f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        canvas.drawText("Stitching Charge (Total Cost):", 60f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Rs. ${order.totalAmount}", 530f, y, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Prepaid Advance (Paid):", 60f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Rs. ${order.paidAmount}", 530f, y, paint)

        y += 8f
        paint.color = Color.GRAY
        canvas.drawLine(60f, y, 530f, y, paint)

        y += 22f
        paint.color = Color.rgb(204, 34, 34) // Red for due amount if greater than 0
        val dueAmount = order.totalAmount - order.paidAmount
        if (dueAmount <= 0) {
            paint.color = Color.rgb(18, 114, 110) // Green if cleared
        }
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Balance Remaining (Due Amount):", 60f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Rs. $dueAmount", 530f, y, paint)

        y += 18f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.textSize = 11f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Fabric Details: ${order.orderNotes.ifBlank { "No special cutting instructions" }}", 60f, y, paint)

        // Footer standard brand lines
        y = 750f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)

        y += 20f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Thank you for choosing Tailor Book. Standard fabric shrinks might apply.", 297f, y, paint)
        y += 15f
        canvas.drawText("Stitched details are custom logged in device database. Registered offline.", 297f, y, paint)

        pdfDocument.finishPage(page)

        val docsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val file = File(docsDir, "Receipt_Order_${order.id}.pdf")
        val outStream = FileOutputStream(file)
        pdfDocument.writeTo(outStream)
        outStream.close()
        pdfDocument.close()

        return file
    }

    fun generateCustomerReportPdf(
        context: Context,
        customer: Customer,
        measurement: CustomerMeasurement?,
        orders: List<Order>
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        
        // Brand Header
        paint.color = Color.rgb(18, 114, 110) // Dark Teal
        canvas.drawRect(0f, 0f, 595f, 130f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 28f
        canvas.drawText("TAILOR BOOK", 40f, 60f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Customer Dossier & Fabric Stitch History", 40f, 85f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CUSTOMER PROFILE", 555f, 60f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Exported: ${dateFormatter.format(Date())}", 555f, 85f, paint)

        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT

        // Basic Info
        var y = 165f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        paint.color = Color.rgb(18, 114, 110)
        canvas.drawText("CUSTOMER BIO", 40f, y, paint)
        
        y += 6f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)

        y += 24f
        paint.color = Color.BLACK
        canvas.drawText("Name:", 40f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(customer.name, 100f, y, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Phone Number:", 300f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(customer.phone, 415f, y, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Address:", 40f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(customer.address.ifBlank { "Unprovided" }, 110f, y, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Registered:", 300f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(dateFormatter.format(Date(customer.dateAdded)), 415f, y, paint)

        // Default Measurements Section
        y += 40f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(18, 114, 110)
        paint.textSize = 14f
        canvas.drawText("MASTER SIZE CHEAT SHEET (Standard Silhouette)", 40f, y, paint)

        y += 6f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)

        y += 20f
        if (measurement != null) {
            paint.color = Color.BLACK
            paint.textSize = 11f
            
            val colWidth = 145f
            val startX = 40f
            
            // Draw a neat tabular measurement layout
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Kameez Length:", startX, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${measurement.shirtLength}\"", startX + 105f, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Shoulder:", startX + colWidth, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${measurement.shoulder}\"", startX + colWidth + 65f, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Sleeves:", startX + colWidth * 2, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${measurement.sleeves}\"", startX + colWidth * 2 + 60f, y, paint)

            y += 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Chest:", startX, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${measurement.chest}\"", startX + 65f, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Waist:", startX + colWidth, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${measurement.waist}\"", startX + colWidth + 50f, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Hip / Ghera:", startX + colWidth * 2, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${measurement.hip}\"", startX + colWidth * 2 + 80f, y, paint)

            y += 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Collar / Neck:", startX, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${measurement.collar}\"", startX + 90f, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Shalwar L.:", startX + colWidth, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${measurement.trouserLength}\"", startX + colWidth + 75f, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Bottom (Mori):", startX + colWidth * 2, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${measurement.trouserBottom}\"", startX + colWidth * 2 + 85f, y, paint)

            y += 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Armhole:", startX, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${measurement.armhole}\"", startX + 65f, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Sleeve Mori:", startX + colWidth, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${measurement.sleeveMori}\"", startX + colWidth + 85f, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Aasan Thigh:", startX + colWidth * 2, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${measurement.trouserAsan}\"", startX + colWidth * 2 + 85f, y, paint)

            y += 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Master Profile Notes: ${measurement.notes.ifBlank { "None" }}", startX, y, paint)

        } else {
            paint.color = Color.DKGRAY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 12f
            canvas.drawText("No default profile dimensions saved for this customer yet.", 40f, y, paint)
        }

        // Job order ledger
        y += 45f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(18, 114, 110)
        paint.textSize = 14f
        canvas.drawText("ORDER HISTORY LEDGER", 40f, y, paint)

        y += 6f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)

        y += 25f
        paint.color = Color.BLACK
        paint.textSize = 10f
        
        // Draw Table Headings
        canvas.drawText("Job Description", 40f, y, paint)
        canvas.drawText("Booked", 200f, y, paint)
        canvas.drawText("Deadline", 280f, y, paint)
        canvas.drawText("Status", 370f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Total (Rs)", 480f, y, paint)
        canvas.drawText("Paid (Rs)", 550f, y, paint)

        y += 6f
        paint.color = Color.BLACK
        paint.strokeWidth = 1f
        canvas.drawLine(40f, y, 555f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        // Draw list of orders
        y += 18f
        if (orders.isEmpty()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.color = Color.DKGRAY
            paint.textSize = 11f
            canvas.drawText("No recorded orders for this customer in database.", 40f, y, paint)
        } else {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            for (order in orders.take(15)) { // Limit to 15 records on this page
                canvas.drawText("#OR-${order.id} ${order.itemType}", 40f, y, paint)
                canvas.drawText(dateFormatter.format(Date(order.orderDate)), 200f, y, paint)
                canvas.drawText(dateFormatter.format(Date(order.dueDate)), 280f, y, paint)
                
                // Status color/draw
                paint.color = when(order.status) {
                    "PENDING" -> Color.rgb(204, 102, 0)
                    "IN_PROGRESS" -> Color.rgb(0, 102, 204)
                    "COMPLETED" -> Color.rgb(18, 114, 110)
                    "DELIVERED" -> Color.rgb(50, 150, 50)
                    else -> Color.BLACK
                }
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(order.status, 370f, y, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.BLACK
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("${order.totalAmount}", 480f, y, paint)
                canvas.drawText("${order.paidAmount}", 550f, y, paint)
                paint.textAlign = Paint.Align.LEFT

                y += 20f
                if (y > 720f) {
                    canvas.drawText("... and more orders ...", 40f, y, paint)
                    break
                }
            }
        }

        // Summary Statistics totals
        val totalStitched = orders.size
        val totalSpent = orders.sumOf { it.totalAmount }
        val totalPaid = orders.sumOf { it.paidAmount }
        val grandBalance = totalSpent - totalPaid

        y = 740f
        paint.color = Color.rgb(240, 245, 245)
        canvas.drawRect(40f, y, 555f, y + 55f, paint)

        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        y += 20f
        canvas.drawText("Stitch Summary Code: S-BOOK", 50f, y, paint)
        canvas.drawText("Total Jobs booked: $totalStitched", 240f, y, paint)
        
        paint.color = Color.rgb(18, 114, 110)
        canvas.drawText("Total Booked: Rs. $totalSpent", 390f, y, paint)

        y += 18f
        paint.color = Color.BLACK
        canvas.drawText("Revenue Received: Rs. $totalPaid", 240f, y, paint)
        paint.color = if (grandBalance > 0) Color.rgb(204, 34, 34) else Color.rgb(18, 114, 110)
        canvas.drawText("Grand Balance: Rs. $grandBalance", 390f, y, paint)

        // Clean Footer
        y += 35f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Tailor Book - Master Stitching Register. Compiled completely offline.", 297f, y, paint)

        pdfDocument.finishPage(page)

        val docsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val file = File(docsDir, "Customer_Report_${customer.id}.pdf")
        val outStream = FileOutputStream(file)
        pdfDocument.writeTo(outStream)
        outStream.close()
        pdfDocument.close()

        return file
    }
}
