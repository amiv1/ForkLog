package com.maksimowiczm.foodyou.app.ui.home.master

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.common.compose.utility.LocalClipboardManager
import com.maksimowiczm.foodyou.common.compose.utility.TestTags
import foodyou.app.generated.resources.*
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShareMealsModalBottomSheet(
    text: String,
    onShare: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val label = stringResource(Res.string.action_share)

    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }
    val copy = {
        clipboardManager.copy(label = label, text = text)
        copied = true
    }

    val paddingValues =
        WindowInsets.systemBars
            .only(WindowInsetsSides.Bottom)
            .add(WindowInsets(bottom = 8.dp))
            .asPaddingValues()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag(TestTags.HomeShareSheet),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        contentWindowInsets = { WindowInsets(0) },
    ) {
        Column(
            modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = label, style = MaterialTheme.typography.headlineSmall)

            Surface(
                onClick = copy,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.HomeShareSheetText),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                CompositionLocalProvider(LocalOverscrollFactory provides null) {
                    Text(
                        text = text,
                        modifier =
                            Modifier.heightIn(max = 360.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = copy,
                    modifier = Modifier.weight(1f).testTag(TestTags.HomeShareSheetCopyButton),
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(Res.string.action_copy),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f).testTag(TestTags.HomeShareSheetShareButton),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(Res.string.action_share),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}
