import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Hub
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.clusterview.demo.VelvetTheme
import com.clusterview.demo.Cluster // 👈 Add this line!

@Composable
fun VisualizationMapView(clusters: List<Cluster>, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(VelvetTheme.MidnightNavy)) {

        // 1. DYNAMIC GRID BACKGROUND
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 60.dp.toPx()
            for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
                drawLine(VelvetTheme.SlateBlue.copy(alpha = 0.15f), Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), 1f)
            }
            for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
                drawLine(VelvetTheme.SlateBlue.copy(alpha = 0.15f), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), 1f)
            }
        }

        // 2. HEADER AREA
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(backgroundColor = VelvetTheme.DeepMaroon),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, VelvetTheme.SunsetCoral.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.ArrowBack, "Back", tint = VelvetTheme.SunsetCoral, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("BACK TO DASHBOARD", color = Color.White, style = MaterialTheme.typography.caption, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.width(24.dp))

            Column {
                Text("SYSTEM TOPOLOGY MAP", color = Color.White, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                Text("${clusters.size} Active Clusters Detected", color = VelvetTheme.SunsetCoral, style = MaterialTheme.typography.overline)
            }
        }

        // 3. CLUSTER NODES DISPLAY
        // This spreads the clusters out across the screen
        Box(modifier = Modifier.fillMaxSize()) {
            clusters.forEachIndexed { index, cluster ->
                // Basic math to space them out (Can be replaced with Drag-and-Drop later)
                val xOffset = 100 + (index % 3) * 300
                val yOffset = 150 + (index / 3) * 250

                MapNode(cluster, xOffset.dp, yOffset.dp)
            }
        }

        // BOTTOM CAPTION
        Text(
            "CLUSTER_VIEW // VISUALIZATION_ENGINE_V1.0",
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            color = VelvetTheme.SlateBlue.copy(alpha = 0.4f),
            style = MaterialTheme.typography.overline,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun MapNode(cluster: Cluster, x: Dp, y: Dp) {
    Column(
        modifier = Modifier.offset(x, y),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(VelvetTheme.DeepOcean.copy(alpha = 0.8f), CircleShape)
                .border(2.dp, VelvetTheme.SunsetCoral, CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Inner decorative ring
            Box(Modifier.fillMaxSize().border(1.dp, VelvetTheme.SlateBlue.copy(alpha = 0.3f), CircleShape))

            Icon(Icons.Default.Hub, null, tint = Color.White, modifier = Modifier.size(32.dp))
        }

        Spacer(Modifier.height(12.dp))

        Surface(
            color = VelvetTheme.DeepMaroon.copy(alpha = 0.9f),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, VelvetTheme.SlateBlue)
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(cluster.name.uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(cluster.path, color = VelvetTheme.SoftSand, fontSize = 8.sp, maxLines = 1)
            }
        }
    }
}