package ir.amirreza.socket_module

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import ir.amirreza.socket_module.ui.theme.Socket_moduleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel by viewModels<HomeViewModel>()
        viewModel.connect()
        setContent {
            val timeState = viewModel.timeState
            val messageState = viewModel.messageState
            val messages by viewModel.messages.collectAsState()
            val times by viewModel.times.collectAsState()
            val text = viewModel.text
            val focusManager = LocalFocusManager.current
            Socket_moduleTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        PartItem(
                            timeState,
                            times,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        PartItem(
                            messageState,
                            messages,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        OutlinedTextField(
                            value = text,
                            onValueChange = { viewModel.text = it },
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(),
                            keyboardActions = KeyboardActions {
                                viewModel.send()
                                focusManager.clearFocus()
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            singleLine = true,
                            placeholder = {
                                Text(text = "Message...")
                            },
                            shape = CardDefaults.shape
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity_Test"
    }
}


@Composable
private fun PartItem(
    state: ResponseState,
    items: List<String>,
    modifier: Modifier
) {
    Card(modifier = modifier.padding(top = 12.dp)) {
        AnimatedContent(
            targetState = state,
            modifier = Modifier.fillMaxSize(), label = "state"
        ) {
            when (it) {
                ResponseState.ERROR -> {
                    CenterAlign {
                        Text(
                            text = "Error",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                ResponseState.LOADING -> {
                    CenterAlign {
                        CircularProgressIndicator(strokeCap = StrokeCap.Round)
                    }
                }

                ResponseState.CONNECTED -> {
                    MessagesList(items)
                }
            }
        }
    }
}

@Composable
fun MessagesList(messages: List<String>) {
    val state = rememberLazyListState()
    LaunchedEffect(key1 = messages) {
        if (state.canScrollForward && state.isScrollInProgress.not()) {
            state.scrollToItem(messages.size)
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth(),
        state = state
    ) {
        items(messages) {
            ListItem(headlineContent = {
                Text(text = it)
            }, colors = ListItemDefaults.colors(Color.Transparent))
        }
    }
}

@Composable
fun CenterAlign(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
        content = content
    )
}