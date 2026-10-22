package com.yong.foldabletest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yong.foldabletest.ui.theme.FoldableAPITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoldableAPITheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier
                            .padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    // Remember dialog state for recomposition when fold/unfold device
    var showDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            modifier = Modifier.size(240.dp),
            painter = painterResource(R.drawable.image),
            contentDescription = null,
        )
        Button(
            onClick = { showDialog = true },
        ) {
            Text(text = "Open Dialog")
        }
    }

    if (showDialog) {
        InfoDialog(
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card {
            Column(
                modifier = Modifier
                    .padding(24.dp),
            ) {
                Text(
                    text = "Dialog Title",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Dialog Description",
                )
                TextButton(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp),
                    onClick = onDismiss,
                ) {
                    Text(text = "Close")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    FoldableAPITheme {
        MainScreen()
    }
}
