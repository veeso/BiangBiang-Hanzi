package dev.veeso.biangbianghanzi.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.os.LocaleList
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import dev.veeso.biangbianghanzi.services.AppSettingsRepository
import dev.veeso.biangbianghanzi.ui.screens.textmode.TextModeViewModel
import dev.veeso.biangbianghanzi.R
import java.util.Locale


@Composable
fun TextModeView(
    viewModel: TextModeViewModel = TextModeViewModel(),
    repo: AppSettingsRepository = AppSettingsRepository(LocalContext.current)
) {

    val inputText by viewModel.inputText.collectAsState()
    val pinyinText by viewModel.pinyinText.collectAsState()
    val translatedText by viewModel.translatedText.collectAsState()
    val context = LocalContext.current
    val clipboard = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    val currentLocale: Locale =
        LocaleList.getDefault().get(0) // first is active
    val translationLanguage by repo.translationLanguage.collectAsState(initial = currentLocale.language)

    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.Start
        ) {
            TopBar()
            // HANZI SECTION
            Section(
                title = "Hanzi",
                actionLabel = "Paste",
                onActionClick = {
                    val clip = clipboard.primaryClip
                    if (clip != null && clip.itemCount > 0) {
                        val pastedText = clip.getItemAt(0).coerceToText(context).toString()
                        viewModel.onInputChanged(pastedText)
                    }
                }
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.onInputChanged(it) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // PINYIN SECTION
            Section(
                title = "Pinyin",
                actionLabel = "Copy",
                onActionClick = { viewModel.copyToClipboard(context, pinyinText) }
            ) {
                OutlinedTextField(
                    value = pinyinText,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TRANSLATION SECTION
            Section(
                title = "Translation",
                actionLabel = "Copy",
                onActionClick = { viewModel.copyToClipboard(context, translatedText) }
            ) {
                OutlinedTextField(
                    value = translatedText,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TRANSLATE BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
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

@Composable
fun TopBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo + title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "BiangBiang Hanzi",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Subtitle
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Convert Hanzi to Pinyin",
            fontSize = 20.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}


@Composable
fun Section(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onActionClick) {
                Text(actionLabel)
            }
        }

        content()
    }
}