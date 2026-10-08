package com.maksimowiczm.foodyou.app.ui.common.utility

import androidx.compose.runtime.Composable

/**
 * Returns a platform action that opens the system share sheet with [text] (e.g. Android's
 * `ACTION_SEND` chooser).
 */
@Composable expect fun rememberShareTextAction(): (text: String) -> Unit
