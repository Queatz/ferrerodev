package dev.ferrero

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    FerreroTheme {
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        val scrolled by remember { derivedStateOf { scrollState.value > 40 } }

        var offeringsOffset by remember { mutableStateOf(0) }
        var projectsOffset by remember { mutableStateOf(0) }
        var connectOffset by remember { mutableStateOf(0) }

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            CreepingDotsBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                SplashSection()
                OfferingsSection(Modifier.onGloballyPositioned { offeringsOffset = it.positionInParent().y.toInt() })
                ProjectsSection(Modifier.onGloballyPositioned { projectsOffset = it.positionInParent().y.toInt() })
                ConnectSection(Modifier.onGloballyPositioned { connectOffset = it.positionInParent().y.toInt() })
                FooterSection()
            }
            Header(
                solid = scrolled,
                onLinkClick = { section ->
                    coroutineScope.launch {
                        val target = when (section) {
                            "Services" -> offeringsOffset
                            "Work" -> projectsOffset
                            "Contact" -> connectOffset
                            else -> 0
                        }
                        scrollState.animateScrollTo(target)
                    }
                },
                modifier = Modifier
            )
        }
    }
}
