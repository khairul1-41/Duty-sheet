package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SalarySheetWithEntries
import com.example.ui.SalaryViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PdfPreviewScreen
import com.example.ui.screens.SheetDetailScreen
import com.example.ui.theme.MyApplicationTheme

sealed interface Screen {
    data object Home : Screen
    data class SheetDetail(val sheetId: Long) : Screen
    data class PdfPreview(val sheetId: Long) : Screen
}

class MainActivity : ComponentActivity() {

    private val viewModel: SalaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SalarySheetApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SalarySheetApp(viewModel: SalaryViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val allSheets by viewModel.allSheets.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            is Screen.Home -> {
                HomeScreen(
                    viewModel = viewModel,
                    onOpenSheet = { sheetId ->
                        viewModel.selectSheet(sheetId)
                        currentScreen = Screen.SheetDetail(sheetId)
                    },
                    onConvertToPdf = { sheetWithEntries ->
                        viewModel.selectSheet(sheetWithEntries.sheet.id)
                        viewModel.generatePdfForCurrentSheet(sheetWithEntries)
                        currentScreen = Screen.PdfPreview(sheetWithEntries.sheet.id)
                    }
                )
            }

            is Screen.SheetDetail -> {
                SheetDetailScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.Home },
                    onConvertToPdf = { sheetWithEntries ->
                        viewModel.generatePdfForCurrentSheet(sheetWithEntries)
                        currentScreen = Screen.PdfPreview(sheetWithEntries.sheet.id)
                    }
                )
            }

            is Screen.PdfPreview -> {
                val sheetData = allSheets.find { it.sheet.id == screen.sheetId }
                if (sheetData != null) {
                    PdfPreviewScreen(
                        viewModel = viewModel,
                        sheetData = sheetData,
                        onBack = { currentScreen = Screen.SheetDetail(screen.sheetId) }
                    )
                } else {
                    HomeScreen(
                        viewModel = viewModel,
                        onOpenSheet = { id ->
                            viewModel.selectSheet(id)
                            currentScreen = Screen.SheetDetail(id)
                        },
                        onConvertToPdf = { item ->
                            viewModel.selectSheet(item.sheet.id)
                            viewModel.generatePdfForCurrentSheet(item)
                            currentScreen = Screen.PdfPreview(item.sheet.id)
                        }
                    )
                }
            }
        }
    }
}
