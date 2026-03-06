package eu.espcaa.boardingpassscanner.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class Screen(
    val name: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val content: @Composable (innerPadding: PaddingValues) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(innerPadding: PaddingValues = PaddingValues(0.dp)) {

    val screens = listOf(
        Screen("Home", Icons.Filled.Home, Icons.Outlined.Home) { HomeContent(it) },
        Screen("Map", Icons.Filled.Map, Icons.Outlined.Map) { MapContent(it) },
        Screen(
            "Profile",
            Icons.Filled.AccountCircle,
            Icons.Outlined.AccountCircle
        ) { ProfileContent(it) }
    )

    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var searchHistory by remember { mutableStateOf(listOf("idk gng", "chinese airlines", "paris")) }

    var currentScreen by remember { mutableStateOf(screens[0]) }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            bottomBar = {
                if (!isSearchExpanded) {
                    HomeBottomBar(
                        screens,
                        currentScreen,
                        onScreenSelected = { selectedScreen ->
                            currentScreen = selectedScreen
                        },
                    )
                }
            },
            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { },
                        modifier = Modifier.size(80.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        ) { scaffoldPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                currentScreen.content(scaffoldPadding)
            }
        }

        if (currentScreen.name == "Home") {
            SearchBarOverlay(
                query = query,
                onQueryChange = { query = it },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                searchHistory = searchHistory
            )
        }

    }
}


@Composable
fun HomeContent(innerPadding: PaddingValues = PaddingValues(0.dp)) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(top = 80.dp)
    ) {
        Text("Your flights")
    }
}

@Composable
fun MapContent(innerPadding: PaddingValues = PaddingValues(0.dp)) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        Text("Map of your flights")
    }
}

@Composable
fun ProfileContent(innerPadding: PaddingValues = PaddingValues(0.dp)) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        Text("Your profile")
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarOverlay(
    query: String,
    onQueryChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    searchHistory: List<String> = listOf("idk gng", "chinese airlines", "paris")
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (expanded) 0.dp else 16.dp)
    ) {
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter),
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = { onExpandedChange(false) },
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    placeholder = { Text("Search in your flights...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear Search",
                                modifier = Modifier.clickable { onQueryChange("") }
                            )
                        }
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = onExpandedChange
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(searchHistory) { suggestion ->
                    ListItem(
                        headlineContent = { Text(suggestion) },
                        modifier = Modifier.clickable {
                            onQueryChange(suggestion)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun HomeBottomBar(
    screens: List<Screen>,
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit = {}
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth()
    ) {
        screens.forEachIndexed { _, screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (currentScreen.name == screen.name) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.name
                    )
                },
                label = { Text(screen.name) },
                selected = currentScreen.name == screen.name,
                onClick = {
                    onScreenSelected(screen)
                }
            )
        }

    }
}