package ucne.edu.notablelists.presentation.notes_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ucne.edu.notablelists.presentation.Notes.list.*
import ucne.edu.notablelists.presentation.users.UserEvent
import ucne.edu.notablelists.presentation.users.UserState
import ucne.edu.notablelists.presentation.users.UserViewModel
import ucne.edu.notablelists.ui.theme.NotableListsTheme

@Composable
fun NotesListRoute(
    viewModel: NotesListViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    onNavigateToDetail: (String?) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val userState by userViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.navigateToDetail) {
        state.navigateToDetail.forEach { noteId ->
            onNavigateToDetail(noteId)
            viewModel.onEvent(NotesListEvent.OnNavigationHandled)
        }
    }

    NotesListScreen(
        state = state,
        userState = userState,
        onEvent = viewModel::onEvent,
        onUserEvent = userViewModel::onEvent,
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
fun NotesListScreen(
    state: NotesListState,
    userState: UserState,
    onEvent: (NotesListEvent) -> Unit,
    onUserEvent: (UserEvent) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage.forEach { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(NotesListEvent.OnDismissLogoutDialog) },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Seguro quieres cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUserEvent(UserEvent.Logout)
                        onEvent(NotesListEvent.OnDismissLogoutDialog)
                    }
                ) {
                    Text("Cerrar Sesión")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onEvent(NotesListEvent.OnDismissLogoutDialog) }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(NotesListEvent.OnAddNoteClick) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CustomSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { onEvent(NotesListEvent.OnSearchQueryChange(it)) },
                    modifier = Modifier.weight(1f)
                )

                UserAvatarMenu(
                    currentUser = userState.currentUser,
                    onLogoutClick = { onEvent(NotesListEvent.OnShowLogoutDialog) },
                    onLoginClick = onNavigateToLogin
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FilterChipsSection(
                filters = state.filterChips,
                onFilterSelected = { onEvent(NotesListEvent.OnFilterChange(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.loadingStatus) {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    items(state.notes, key = { it.id }) { noteUi ->
                        NoteItemCard(
                            noteUi = noteUi,
                            onClick = { onEvent(NotesListEvent.OnNoteClick(noteUi.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserAvatarMenu(
    currentUser: String,
    onLogoutClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isLoggedIn = currentUser.isNotBlank()

    Box {
        IconButton(
            onClick = { expanded = true },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (isLoggedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = if (isLoggedIn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            if (isLoggedIn) {
                Text(
                    text = currentUser.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (isLoggedIn) {
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = "Hola, $currentUser",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    },
                    onClick = { },
                    enabled = false
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Cerrar Sesión") },
                    onClick = {
                        onLogoutClick()
                        expanded = false
                    }
                )
            } else {
                DropdownMenuItem(
                    text = { Text("Iniciar Sesión") },
                    onClick = {
                        onLoginClick()
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CustomSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(50),
        placeholder = { Text("Buscar notas...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun FilterChipsSection(
    filters: List<FilterUiItem>,
    onFilterSelected: (NoteFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { item ->
            val containerColor = if (item.isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            val contentColor = if (item.isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            val borderColor = if (item.isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant

            SuggestionChip(
                onClick = { onFilterSelected(item.filter) },
                label = { Text(item.label) },
                shape = RoundedCornerShape(12.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = containerColor,
                    labelColor = contentColor
                ),
                border = BorderStroke(1.dp, borderColor)
            )
        }
    }
}

@Composable
fun NoteItemCard(
    noteUi: NoteUiItem,
    onClick: () -> Unit
) {
    val (containerColor, contentColor) = noteUi.style.getColors()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = noteUi.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (noteUi.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = noteUi.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.9f),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                noteUi.priorityChips.forEach { priority ->
                    val (_, pContent) = priority.style.getColors()
                    MetaDataChip(
                        text = priority.label,
                        icon = Icons.Default.Flag,
                        contentColor = pContent,
                        containerColor = pContent.copy(alpha = 0.1f)
                    )
                }

                noteUi.tags.forEach { tag ->
                    val (_, tContent) = tag.style.getColors()
                    MetaDataChip(
                        text = tag.label,
                        icon = Icons.Default.Label,
                        contentColor = contentColor,
                        containerColor = contentColor.copy(alpha = 0.1f)
                    )
                }

                noteUi.reminder?.let { reminder ->
                    MetaDataChip(
                        text = reminder,
                        icon = Icons.Default.Alarm,
                        contentColor = contentColor,
                        containerColor = contentColor.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MetaDataChip(
    text: String,
    icon: ImageVector,
    contentColor: Color,
    containerColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

@Composable
fun NoteStyle.getColors(): Pair<Color, Color> {
    return when (this) {
        NoteStyle.Secondary -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        NoteStyle.Primary -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        NoteStyle.Error -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewNotesListRoute() {
    NotableListsTheme {
        NotesListScreen(
            state = NotesListState(
                notes = listOf(
                    NoteUiItem(
                        id = "1",
                        title = "Meeting Notes",
                        description = "Discussed project timeline and deliverables with the team.",
                        style = NoteStyle.Secondary,
                        priorityChips = listOf(PriorityUiItem("Media", NoteStyle.Primary)),
                        tags = listOf(TagUiItem("work", NoteStyle.Secondary))
                    ),
                    NoteUiItem(
                        id = "2",
                        title = "Shopping List",
                        description = "Milk, Eggs, Bread, Fruits, Vegetables",
                        style = NoteStyle.Primary,
                        priorityChips = emptyList(),
                        tags = listOf(TagUiItem("personal", NoteStyle.Primary))
                    )
                ),
                filterChips = NoteFilter.entries.map { filter ->
                    FilterUiItem(
                        filter = filter,
                        label = filter.label,
                        isSelected = filter == NoteFilter.DATE
                    )
                },
                searchQuery = ""
            ),
            userState = UserState(currentUser = "john_doe"),
            onEvent = {},
            onUserEvent = {},
            onNavigateToLogin = {}
        )
    }
}