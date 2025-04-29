package com.example.brainlog.view

import android.graphics.drawable.Icon
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import androidx.compose.material3.NavigationBarItem

@Composable
fun MyBottomAppBar(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    selectionDestination: String = "home"
) {
    NavigationBar(
        containerColor =  Color(0xFF1F1F1F),
        contentColor = Color.White,
    ) {
        BottomNavigationItem(
            selected = selectionDestination == "home",
            onClick = onHomeClick,
            icon = Icons.Default.Home,
            label = "Home"
        )

        BottomNavigationItem(
            selected = selectionDestination == "profile",
            onClick = onSearchClick,
            icon = Icons.Default.Person,
            label = "Profile"
        )

    }


}




@Composable
fun BottomNavigationItem(selected: Boolean, onClick: () -> Unit, icon: ImageVector, label: String) {

    val animatedColor by animateColorAsState(
        targetValue = if (selected) Color.Cyan else Color.White,
        label = "iconColorAnimation"
    )

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = animatedColor,
                modifier = Modifier.size(24.dp)
            )

        }
    )

}


