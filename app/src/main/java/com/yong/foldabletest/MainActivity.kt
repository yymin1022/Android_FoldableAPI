package com.yong.foldabletest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.yong.foldabletest.fold.FoldPosture
import com.yong.foldabletest.fold.boundsIn
import com.yong.foldabletest.fold.foldingFeature
import com.yong.foldabletest.fold.hingeAngleFlow
import com.yong.foldabletest.fold.toFoldPosture
import com.yong.foldabletest.ui.theme.FoldableAPITheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrieve current window layout info
        val windowLayoutInfoFlow = WindowInfoTracker.getOrCreate(this).windowLayoutInfo(this)
        // Retrieve hinge angle, null if the device has no hinge angle sensor
        val hingeAngleFlow = hingeAngleFlow()
        setContent {
            // Window layout info state
            // - can be null, androidx.window is initialized
            val windowLayoutInfo: WindowLayoutInfo? by windowLayoutInfoFlow.collectAsStateWithLifecycle(
                initialValue = null,
            )
            // Hinge angle state
            // - null until the first sensor event, or if the device has no hinge angle sensor
            val hingeAngle: Float? = hingeAngleFlow?.collectAsStateWithLifecycle(
                initialValue = null,
            )?.value

            FoldableAPITheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier
                            .padding(innerPadding),
                        foldingFeature = windowLayoutInfo?.foldingFeature(),
                        hingeAngle = hingeAngle,
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    foldingFeature: FoldingFeature?,
    hingeAngle: Float?,
    modifier: Modifier = Modifier,
) {
    // Remember dialog state for recomposition when fold/unfold device
    var showDialog by rememberSaveable { mutableStateOf(false) }

    val posture = foldingFeature.toFoldPosture()
    var layoutBoundsInWindow by remember { mutableStateOf(Rect.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutBoundsInWindow = it.boundsInWindow() },
    ) {
        when (posture) {
            // Normal UI
            FoldPosture.NoFold, FoldPosture.Flat -> DefaultLayout(
                onOpenDialog = { showDialog = true },
            )

            // Book UI
            FoldPosture.Book -> BookLayout(
                foldBounds = foldingFeature!!.boundsIn(layoutBoundsInWindow),
                onOpenDialog = { showDialog = true },
            )

            // Tabletop UI
            FoldPosture.Tabletop -> TabletopLayout(
                foldBounds = foldingFeature!!.boundsIn(layoutBoundsInWindow),
                onOpenDialog = { showDialog = true },
            )
        }
        Text(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            text = "Posture: $posture\nHinge: ${hingeAngle?.let { "${it.roundToInt()}°" } ?: "N/A"}",
            style = MaterialTheme.typography.labelLarge,
        )
    }

    if (showDialog) {
        InfoDialog(
            foldingFeature = foldingFeature,
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun DefaultLayout(
    onOpenDialog: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PostureImage(
            modifier = Modifier.size(240.dp),
        )
        OpenDialogButton(
            onClick = onOpenDialog,
        )
    }
}

// Image above the fold, button below it, like a laptop's screen and keyboard
@Composable
private fun TabletopLayout(
    foldBounds: Rect,
    onOpenDialog: () -> Unit,
) {
    val density = LocalDensity.current

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { foldBounds.top.coerceAtLeast(0f).toDp() }),
            contentAlignment = Alignment.Center,
        ) {
            PostureImage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            )
        }
        // Keep the hinge area empty, since it can occlude content on some devices
        Spacer(
            modifier = Modifier
                .height(with(density) { foldBounds.height.toDp() }),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            OpenDialogButton(
                onClick = onOpenDialog,
            )
        }
    }
}

// Image on the left page, button on the right page, like an open book
@Composable
private fun BookLayout(
    foldBounds: Rect,
    onOpenDialog: () -> Unit,
) {
    val density = LocalDensity.current

    Row(
        modifier = Modifier.fillMaxSize(),
        // Fold bounds are absolute, so children must not be reordered in RTL
        horizontalArrangement = Arrangement.Absolute.Left,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(density) { foldBounds.left.coerceAtLeast(0f).toDp() }),
            contentAlignment = Alignment.Center,
        ) {
            PostureImage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            )
        }
        // Keep the hinge area empty, since it can occlude content on some devices
        Spacer(
            modifier = Modifier
                .width(with(density) { foldBounds.width.toDp() }),
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            OpenDialogButton(
                onClick = onOpenDialog,
            )
        }
    }
}

@Composable
private fun PostureImage(
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier,
        painter = painterResource(R.drawable.image),
        contentDescription = null,
    )
}

@Composable
private fun OpenDialogButton(
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
    ) {
        Text(text = "Open Dialog")
    }
}

@Composable
fun InfoDialog(
    foldingFeature: FoldingFeature?,
    onDismiss: () -> Unit,
) {
    val posture = foldingFeature.toFoldPosture()
    // A default dialog is centered and would straddle the fold,
    // so take the whole window instead and place the card on one side of the fold
    val avoidFold = posture == FoldPosture.Tabletop || posture == FoldPosture.Book

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = !avoidFold,
            decorFitsSystemWindows = !avoidFold,
        ),
    ) {
        when (posture) {
            FoldPosture.Tabletop, FoldPosture.Book -> FoldAvoidingDialogLayout(
                foldingFeature = foldingFeature!!,
            ) {
                InfoCard(
                    foldingFeature = foldingFeature,
                    onDismiss = onDismiss,
                )
            }
            FoldPosture.NoFold, FoldPosture.Flat -> InfoCard(
                foldingFeature = foldingFeature,
                onDismiss = onDismiss,
            )
        }
    }
}

// Places the dialog content on one side of the fold:
// - Below a horizontal fold (tabletop), where the user's hands are
// - Right of a vertical fold (book), next to the button that opened it
@Composable
private fun FoldAvoidingDialogLayout(
    foldingFeature: FoldingFeature,
    content: @Composable () -> Unit,
) {
    // Fold bounds are reported in the activity window, and this full-size dialog window shares its origin
    var layoutBoundsInWindow by remember { mutableStateOf(Rect.Zero) }
    val foldBounds = foldingFeature.boundsIn(layoutBoundsInWindow)
    val contentPadding = with(LocalDensity.current) {
        when (foldingFeature.orientation) {
            FoldingFeature.Orientation.HORIZONTAL -> PaddingValues(
                top = foldBounds.bottom.coerceAtLeast(0f).toDp(),
            )
            // Fold bounds are absolute, so the padding must not flip in RTL
            else -> PaddingValues.Absolute(
                left = foldBounds.right.coerceAtLeast(0f).toDp(),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutBoundsInWindow = it.boundsInWindow() }
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun InfoCard(
    foldingFeature: FoldingFeature?,
    onDismiss: () -> Unit,
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
                text = foldingFeature.toDescription(),
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

// Raw FoldingFeature values to compare with the derived posture
private fun FoldingFeature?.toDescription(): String = if (this == null) {
    "Posture: ${toFoldPosture()}\nNo FoldingFeature reported"
} else {
    """
    Posture: ${toFoldPosture()}
    State: $state
    Orientation: $orientation
    isSeparating: $isSeparating
    OcclusionType: $occlusionType
    Bounds: $bounds
    """.trimIndent()
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    FoldableAPITheme {
        MainScreen(
            foldingFeature = null,
            hingeAngle = null,
        )
    }
}
