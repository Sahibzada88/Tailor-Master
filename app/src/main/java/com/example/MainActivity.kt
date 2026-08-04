package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import com.example.data.AppDatabase
import com.example.data.TailorRepository
import com.example.ui.TailorAppMain
import com.example.ui.TailorViewModel
import com.example.ui.theme.MyApplicationTheme
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge support configuration
        enableEdgeToEdge()

        // 1. Initialize local offline Room Database
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TailorRepository(database.tailorDao())

        // 2. Instantiate TailorViewModel via explicit provider factory
        val viewModel: TailorViewModel by viewModels {
            TailorViewModel.Factory(application, repository)
        }

        setContent {
            MyApplicationTheme {
                TailorAppMain(
                    viewModel = viewModel,
                    onSharePdf = { pdfFile ->
                        shareGeneratedReceiptPdf(pdfFile)
                    }
                )
            }
        }
    }

    /**
     * Launch clean implicit intent utilizing secure FileProvider URI
     * to share generated PDF invoices and reports with other apps.
     */
    private fun shareGeneratedReceiptPdf(file: File) {
        try {
            val authority = "${packageName}.provider"
            val uri = FileProvider.getUriForFile(this, authority, file)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share Invoice Receipt via:"))
        } catch (e: Exception) {
            // Soft fail fallback: Show standard Toast
            android.widget.Toast.makeText(this, "File ready: ${file.name}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
}
