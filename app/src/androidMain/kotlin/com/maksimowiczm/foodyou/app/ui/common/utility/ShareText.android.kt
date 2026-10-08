package com.maksimowiczm.foodyou.app.ui.common.utility

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberShareTextAction(): (text: String) -> Unit {
    val context = LocalContext.current

    return remember(context) {
        { text: String ->
            val sendIntent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }

            context.startActivity(Intent.createChooser(sendIntent, null))
        }
    }
}
