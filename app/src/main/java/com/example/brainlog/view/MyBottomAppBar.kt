package com.example.brainlog.view


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun CustomNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    selectedColor: Color = Color.White,
    unselectedColor: Color = Color.White.copy(alpha = 0.6f)
) {
    val tint = if (selected) selectedColor else unselectedColor
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp) // Kleinere Icons als im vorigen Bsp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 12.sp
        )
    }
}

@Composable
fun MyBottomAppBar(
    navController: NavHostController,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    arrangement: Arrangement.Horizontal = Arrangement.SpaceAround // Standard: SpaceAround
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 50.dp, end = 50.dp, bottom = 12.dp)
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(25.dp)),
            color = Color(0xFF5B231D)

        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize() // Füllt die Surface aus
                    .padding(horizontal = 8.dp), // Innenabstand in der Row

                horizontalArrangement = arrangement,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Füge die Items direkt in die Row ein
                CustomNavigationItem(
                    selected = currentRoute == "home",
                    onClick = onHomeClick,
                    icon = Icons.Default.Home,
                    label = "Home"
                )

                CustomNavigationItem(
                    selected = currentRoute == "profile",
                    onClick = onProfileClick,
                    icon = Icons.Default.Person,
                    label = "Profile"
                )
            }
        }
    }
}










