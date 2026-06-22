package dev.ferrero

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.absoluteValue

private const val MAX_CONTENT_WIDTH = 1100
private val BRAND_CORNER_RADIUS = 24.dp

/** Subtle, deterministic paper/grain texture drawn behind a surface. */
private fun Modifier.grain(color: Color, density: Int = 1400): Modifier = drawBehind {
    var seed = 0x9E3779B9L
    fun next(): Float {
        seed = (seed * 6364136223846793005L + 1442695040888963407L)
        return ((seed ushr 33).toInt().absoluteValue % 10000) / 10000f
    }
    repeat(density) {
        val x = next() * size.width
        val y = next() * size.height
        drawCircle(color = color, radius = 0.6f, center = Offset(x, y))
    }
}

@Composable
private fun SectionContainer(
    background: Color,
    modifier: Modifier = Modifier,
    content: @Composable (maxWidth: Int) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth().background(background),
        contentAlignment = Alignment.TopCenter,
    ) {
        BoxWithConstraints {
            val width = if (maxWidth.value.toInt() < MAX_CONTENT_WIDTH) maxWidth.value.toInt() else MAX_CONTENT_WIDTH
            Box(modifier = Modifier.widthIn(max = MAX_CONTENT_WIDTH.dp).fillMaxWidth()) {
                Box(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        content(width)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 1. Header
// ----------------------------------------------------------------------------
@Composable
fun Header(
    solid: Boolean,
    onLinkClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (solid) MaterialTheme.colorScheme.background else Color.Transparent)
            .padding(horizontal = 32.dp, vertical = 20.dp),
    ) {
        BoxWithConstraints(Modifier.widthIn(max = MAX_CONTENT_WIDTH.dp).fillMaxWidth().align(Alignment.Center)) {
            val isMobile = maxWidth < 768.dp
            
            Text(
                text = "ferrero.dev",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable { onLinkClick("Home") },
            )
            
            if (isMobile) {
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    IconButton(onClick = { expanded = true }) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(Modifier.size(24.dp, 2.dp).background(MaterialTheme.colorScheme.onBackground))
                            Box(Modifier.size(24.dp, 2.dp).background(MaterialTheme.colorScheme.onBackground))
                            Box(Modifier.size(24.dp, 2.dp).background(MaterialTheme.colorScheme.onBackground))
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Services") },
                            onClick = { onLinkClick("Services"); expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Work") },
                            onClick = { onLinkClick("Work"); expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Contact") },
                            onClick = { onLinkClick("Contact"); expanded = false }
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavLink("Services") { onLinkClick("Services") }
                    NavLink("Work") { onLinkClick("Work") }
                    NavLink("Contact") { onLinkClick("Contact") }
                }
            }
        }
    }
}

@Composable
private fun NavLink(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        fontFamily = FontFamily.SansSerif,
        modifier = Modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable { onClick() }
    )
}

// ----------------------------------------------------------------------------
// 2. Splash
// ----------------------------------------------------------------------------
@Composable
fun SplashSection() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 620.dp)
            .background(MaterialTheme.colorScheme.background)
            .grain(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        val isMobile = maxWidth < 768.dp
        val titleSize = if (isMobile) 44.sp else 72.sp
        val titleLineHeight = if (isMobile) 48.sp else 76.sp

        Box(Modifier.widthIn(max = MAX_CONTENT_WIDTH.dp).fillMaxWidth().padding(horizontal = 32.dp)) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    text = "We build technology",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Black,
                    fontSize = titleSize,
                    lineHeight = titleLineHeight,
                    fontFamily = FontFamily.SansSerif,
                )
                Text(
                    text = "in service of a New Earth.",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = titleSize,
                    lineHeight = titleLineHeight,
                    fontFamily = FontFamily.SansSerif,
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "Conception, reconception, and complete tech solutions \u2014 software & hardware.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = if (isMobile) 18.sp else 20.sp,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.widthIn(max = 640.dp),
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 3. Offerings
// ----------------------------------------------------------------------------
private data class Offering(val index: String, val title: String, val description: String, val tag: String)

private val offerings = listOf(
    Offering(
        "01",
        "New Product Conception",
        "From ideation to MVP, we help you define and build your first product.",
        "SW / HW"
    ),
    Offering(
        "02",
        "Existing Product Reconception",
        "Revitalize your current offerings with modern tech and better UX.",
        "SW / HW"
    ),
    Offering(
        "03",
        "Market Research & Positioning",
        "Data-driven insights to find your competitive edge in the market.",
        "SW / HW"
    ),
    Offering(
        "04",
        "Tech Team Augmentation",
        "Scale your engineering capacity with our experienced developers.",
        "SW"
    ),
    Offering(
        "05",
        "Complete Tech Solutions",
        "End-to-end development and maintenance of your technology stack.",
        "SW"
    ),
)

@Composable
fun OfferingsSection(modifier: Modifier = Modifier) {
    SectionContainer(background = MaterialTheme.colorScheme.background, modifier = modifier) { width ->
        val isMobile = width < 768
        Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 96.dp)) {
            Text(
                text = "What we do",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                fontSize = 48.sp,
                lineHeight = 52.sp,
                fontFamily = FontFamily.SansSerif,
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(40.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "Aligned with New Earth principles",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                )
            }
            Spacer(Modifier.height(48.dp))
            offerings.forEach { offering ->
                OfferingCard(offering, isMobile)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun OfferingCard(offering: Offering, isMobile: Boolean) {
    if (isMobile) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(BRAND_CORNER_RADIUS))
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), RoundedCornerShape(BRAND_CORNER_RADIUS))
                .padding(32.dp),
        ) {
            Text(
                text = offering.index,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                fontFamily = FontFamily.SansSerif,
            )
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = offering.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    fontFamily = FontFamily.SansSerif,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = offering.description,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                )
            }
            Spacer(Modifier.height(16.dp))
            CategoryTag(offering.tag, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(BRAND_CORNER_RADIUS))
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), RoundedCornerShape(BRAND_CORNER_RADIUS))
                .padding(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = offering.index,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.width(72.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = offering.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    fontFamily = FontFamily.SansSerif,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = offering.description,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                )
            }
            CategoryTag(offering.tag, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun CategoryTag(label: String, background: Color, content: Color) {
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(BRAND_CORNER_RADIUS))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            color = content,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
        )
    }
}

// ----------------------------------------------------------------------------
// 4. Past Projects
// ----------------------------------------------------------------------------
private data class Project(
    val name: String,
    val impact: String,
    val tags: List<String>,
    val color: Color,
)

private val projects = listOf(
    Project(
        "Verdant Grid",
        "Cut energy waste 30% across a regional renewable network.",
        listOf("Hardware", "IoT"),
        Color(0xFF24503A),
    ),
    Project(
        "Mycelia",
        "A marketplace connecting regenerative farms to local kitchens.",
        listOf("Software", "Platform"),
        Color(0xFF2E2A22),
    ),
    Project(
        "Atlas Reconceive",
        "Reimagined a legacy logistics suite into a modern toolkit.",
        listOf("Reconception", "SW"),
        Color(0xFF1F3D2B),
    ),
    Project(
        "Pulse Lab",
        "Embedded sensing wearable that augmented a clinical research team.",
        listOf("Hardware", "Augmentation"),
        Color(0xFF3A3A33),
    ),
)

@Composable
fun ProjectsSection(modifier: Modifier = Modifier) {
    var selectedProject by remember { mutableStateOf<Project?>(null) }

    SectionContainer(background = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier) { width ->
        val twoColumns = width > 760
        Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 96.dp)) {
            Text(
                text = "Selected work",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                fontSize = 48.sp,
                lineHeight = 52.sp,
                fontFamily = FontFamily.SansSerif,
            )
            Spacer(Modifier.height(48.dp))
            if (twoColumns) {
                projects.chunked(2).forEach { rowItems ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        rowItems.forEach { project ->
                            Box(Modifier.weight(1f)) {
                                ProjectCard(
                                    project = project,
                                    onClick = { selectedProject = project }
                                )
                            }
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(24.dp))
                }
            } else {
                projects.forEach { project ->
                    ProjectCard(
                        project = project,
                        onClick = { selectedProject = project }
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    selectedProject?.let { project ->
        WorkDialog(
            project = project,
            onDismiss = { selectedProject = null }
        )
    }
}

@Composable
private fun ProjectCard(project: Project, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(BRAND_CORNER_RADIUS))
            .clip(RoundedCornerShape(BRAND_CORNER_RADIUS))
    ) {
        // Edge-to-edge "imagery" placeholder (no stock photos).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(project.color)
                .grain(Color.White.copy(alpha = 0.06f), density = 900),
        )
        Column(Modifier.padding(28.dp)) {
            Text(
                text = project.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                fontFamily = FontFamily.SansSerif,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = project.impact,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                project.tags.forEach { tag ->
                    CategoryTag(tag, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun WorkDialog(
    project: Project,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() } // Click outside to dismiss
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val isMobile = maxWidth < 768.dp
            val dialogWidth = if (isMobile) maxWidth else 600.dp

            Column(
                modifier = Modifier
                    .width(dialogWidth)
                    .clip(RoundedCornerShape(BRAND_CORNER_RADIUS))
                    .background(MaterialTheme.colorScheme.background)
                    .clickable(enabled = false) {} // Prevent clicks on dialog from dismissing
            ) {
                // Header imagery
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isMobile) 200.dp else 300.dp)
                        .background(project.color)
                        .grain(Color.White.copy(alpha = 0.06f), density = 900),
                )

                Column(Modifier.padding(32.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = project.name,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss) {
                            Text(
                                "✕",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        project.tags.forEach { tag ->
                            CategoryTag(tag, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Impact",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = project.impact,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        fontFamily = FontFamily.SansSerif,
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(BRAND_CORNER_RADIUS),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        contentPadding = PaddingValues(horizontal = 36.dp, vertical = 18.dp),
                    ) {
                        Text(
                            text = "Close",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.SansSerif,
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 5. Let's Connect
// ----------------------------------------------------------------------------
@Composable
fun ConnectSection(modifier: Modifier = Modifier) {
    SectionContainer(background = MaterialTheme.colorScheme.background, modifier = modifier) { width ->
        val twoColumns = width > 820
        Box(Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 96.dp)) {
            if (twoColumns) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(64.dp),
                ) {
                    Box(Modifier.weight(1f)) { ConnectPitch() }
                    Box(Modifier.weight(1f)) { ConnectForm() }
                }
            } else {
                Column {
                    ConnectPitch()
                    Spacer(Modifier.height(48.dp))
                    ConnectForm()
                }
            }
        }
    }
}

@Composable
private fun ConnectPitch() {
    Column {
        Text(
            text = "Let's connect",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Black,
            fontSize = 48.sp,
            lineHeight = 52.sp,
            fontFamily = FontFamily.SansSerif,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Have a product to build, rethink, or scale? Tell us where you are and where you want to go \u2014 we'll bring the technology to get you there.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            fontFamily = FontFamily.SansSerif,
        )
    }
}

@Composable
private fun ConnectForm() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var service by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val services = listOf(
        "New Product Conception",
        "Existing Product Reconception",
        "Market Research & Positioning",
        "Tech Team Augmentation",
        "Complete Tech Solutions",
        "Something else",
    )

    Column(Modifier.fillMaxWidth()) {
        BrandField(name, { name = it }, "Name")
        Spacer(Modifier.height(16.dp))
        BrandField(email, { email = it }, "Email")
        Spacer(Modifier.height(16.dp))

        Box(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = service,
                onValueChange = {},
                readOnly = true,
                label = { Text("Service Needed") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(BRAND_CORNER_RADIUS),
                colors = brandFieldColors(),
            )
            // Transparent overlay to capture clicks for the dropdown.
            Box(
                Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                services.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            service = option
                            expanded = false
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        BrandField(message, { message = it }, "Message", minHeight = 140.dp)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { },
            shape = RoundedCornerShape(BRAND_CORNER_RADIUS),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            contentPadding = PaddingValues(horizontal = 36.dp, vertical = 18.dp),
        ) {
            Text(
                text = "Send message",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
            )
        }
    }
}

@Composable
private fun BrandField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minHeight: androidx.compose.ui.unit.Dp = 56.dp,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().heightIn(min = minHeight),
        shape = RoundedCornerShape(BRAND_CORNER_RADIUS),
        colors = brandFieldColors(),
    )
}

@Composable
private fun brandFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.primary,
)

// ----------------------------------------------------------------------------
// 6. Footer
// ----------------------------------------------------------------------------
@Composable
fun FooterSection(modifier: Modifier = Modifier) {
    SectionContainer(background = MaterialTheme.colorScheme.primary, modifier = modifier) { width ->
        val isMobile = width < 600
        
        Box(Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 56.dp)) {
            if (isMobile) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ferrero.dev",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.SansSerif,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Worldwide \u00B7 Based in the USA and Vietnam",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "\u00A9 2026 ferrero.dev. All rights reserved.",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SocialIcon("in")
                        SocialIcon("X")
                        SocialIcon("gh")
                    }
                }
            } else {
                Box(Modifier.fillMaxWidth()) {
                    Column(Modifier.align(Alignment.CenterStart)) {
                        Text(
                            text = "ferrero.dev",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.SansSerif,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Worldwide \u00B7 Based in the USA and Vietnam",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "\u00A9 2026 ferrero.dev. All rights reserved.",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                        )
                    }
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SocialIcon("in")
                        SocialIcon("X")
                        SocialIcon("gh")
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialIcon(label: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
        )
    }
}
