package com.fxanhkhoa.what_to_eat_android.components.ingredient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

@Composable
fun SearchBar(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH,
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val searchText = localizationManager.getString(R.string.search_ingredients, language)
    val clearSearchText = localizationManager.getString(R.string.clear_search, language)

    Row(
        modifier = modifier
            .padding(10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                onTextChange(it)
            },
            modifier = Modifier.weight(1f),
            placeholder = { Text(searchText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = searchText) },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onTextChange("")
                        }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = clearSearchText)
                    }
                }
            },
            singleLine = true
        )
    }
}
