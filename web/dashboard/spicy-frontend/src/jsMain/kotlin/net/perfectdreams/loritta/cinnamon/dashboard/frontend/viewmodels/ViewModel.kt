package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend

/**
 * A custom Android-like [ViewModel] implementation for Jetpack Compose HTML
 */
open class ViewModel(val m: LorittaDashboardFrontend, val scope: CoroutineScope)

@Composable
fun <T : ViewModel> viewModel(block: (scope: CoroutineScope) -> T): T {
    val scope = rememberCoroutineScope()

    return remember {
        mutableStateOf(block.invoke(scope))
    }.value
}