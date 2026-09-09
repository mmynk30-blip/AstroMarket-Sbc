package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.AstroMarketScreen
import com.example.ui.theme.CosmicDeepNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AstroMarketViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: AstroMarketViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleNotificationIntent(intent)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = CosmicDeepNavy
        ) {
          AstroMarketScreen(viewModel = viewModel)
        }
      }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    handleNotificationIntent(intent)
  }

  private fun handleNotificationIntent(intent: android.content.Intent?) {
    val targetTab = intent?.getIntExtra("target_tab", -1) ?: -1
    if (targetTab >= 0) {
      viewModel.selectTab(targetTab)
    }
  }
}
