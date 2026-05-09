package dev.veeso.biangbianghanzi.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.veeso.biangbianghanzi.R
import dev.veeso.biangbianghanzi.services.AppSettingsRepository
import dev.veeso.biangbianghanzi.services.CANTONESE
import dev.veeso.biangbianghanzi.services.SIMPLIFIED_CHINESE
import dev.veeso.biangbianghanzi.ui.AppDesign
import dev.veeso.biangbianghanzi.ui.components.SectionView
import dev.veeso.biangbianghanzi.ui.screens.textmode.TextModeViewModel
import java.util.Locale


@Composable
fun TextModeView(
    viewModel: TextModeViewModel = viewModel(),
    settingsRepo: AppSettingsRepository = AppSettingsRepository(LocalContext.current),
) {
    val inputText by viewModel.inputText.collectAsState()
    val pinyinText by viewModel.pinyinText.collectAsState()
    val translatedText by viewModel.translatedText.collectAsState()
    val chineseType by settingsRepo.chineseType.collectAsState(initial = SIMPLIFIED_CHINESE)
    val context = LocalContext.current
    val clipboard = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    val isCantonese = chineseType == CANTONESE

    LaunchedEffect(isCantonese) { viewModel.setMode(context, isCantonese) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = AppDesign.horizontalPadding,
                    vertical = 5.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(AppDesign.sectionSpacing),
            horizontalAlignment = Alignment.Start,
        ) {
            TopBar()

            SectionView(
                title = "Hanzi",
                actionLabel = "Paste",
                actionIcon = Icons.Default.ContentPaste,
                onActionClick = {
                    val clip = clipboard.primaryClip
                    if (clip != null && clip.itemCount > 0) {
                        viewModel.onInputChanged(
                            clip.getItemAt(0).coerceToText(context).toString(),
                        )
                    }
                },
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = viewModel::onInputChanged,
                    textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                )
            }

            SectionView(
                title = if (isCantonese) "Jyutping" else "Pinyin",
                actionLabel = "Copy",
                actionIcon = Icons.Default.ContentCopy,
                onActionClick = { viewModel.copyToClipboard(context, pinyinText) },
            ) {
                OutlinedTextField(
                    value = pinyinText,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                )
            }

            if (!isCantonese) {
                SectionView(
                    title = "Translation",
                    actionLabel = "Copy",
                    actionIcon = Icons.Default.ContentCopy,
                    onActionClick = { viewModel.copyToClipboard(context, translatedText) },
                ) {
                    OutlinedTextField(
                        value = translatedText,
                        onValueChange = {},
                        readOnly = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(onClick = { viewModel.translate(Locale.getDefault().language) }) {
                        Icon(Icons.Default.Translate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Translate")
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(AppDesign.cornerRadius)),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "BiangBiang Hanzi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "Convert Hanzi to Pinyin",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 4.dp),
        )
    }
}
