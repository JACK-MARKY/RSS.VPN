package com.v2ray.ang.ui.main

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.AppDropdownMenuItems
import com.v2ray.ang.ui.compose.colorPing
import com.v2ray.ang.ui.compose.colorPingRed

private enum class SimpleDestination { Home, Servers, Settings }

private enum class SimpleAddAction(@StringRes val labelRes: Int, val action: MainAction) {
    Clipboard(R.string.menu_item_import_config_clipboard, MainAction.ImportClipboard),
    QRCode(R.string.menu_item_import_config_qrcode, MainAction.ImportQRcode),
    Subscription(R.string.title_sub_update, MainAction.UpdateSubscriptions),
}

/**
 * The RSS.VPN simple mode: a home screen, a plain server picker and a small settings page.
 * The full v2rayNG interface only appears when "Advanced settings" is switched on.
 */
@Composable
internal fun SimpleModeScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val serverName by mainViewModel.selectedServerName.collectAsStateWithLifecycle()
    val statusText = mainViewModel.formatStatus(uiState.status)
    var destinationName by rememberSaveable { mutableStateOf(SimpleDestination.Home.name) }
    val destination = SimpleDestination.entries.firstOrNull { it.name == destinationName }
        ?: SimpleDestination.Home
    val goHome = { destinationName = SimpleDestination.Home.name }

    BackHandler(enabled = destination != SimpleDestination.Home) { goHome() }

    when (destination) {
        SimpleDestination.Home -> HomeScreen(
            isRunning = uiState.isRunning,
            isConnecting = uiState.isConnecting,
            serverName = serverName,
            statusText = statusText,
            onToggle = {
                if (!uiState.isRunning && uiState.selectedGuid.isNullOrEmpty()) {
                    // Nothing to connect to yet: let the user pick or add a server first.
                    destinationName = SimpleDestination.Servers.name
                } else {
                    onAction(MainAction.ToggleService)
                }
            },
            onTestConnection = { onAction(MainAction.TestCurrentServer) },
            onOpenServers = { destinationName = SimpleDestination.Servers.name },
            onOpenSettings = { destinationName = SimpleDestination.Settings.name },
        )

        SimpleDestination.Servers -> SimpleServersScreen(
            mainViewModel = mainViewModel,
            uiState = uiState,
            onAction = onAction,
            onBack = goHome,
        )

        SimpleDestination.Settings -> SimpleSettingsScreen(
            advancedMode = uiState.advancedMode,
            onAdvancedModeChange = { onAction(MainAction.SetAdvancedMode(it)) },
            onBack = goHome,
        )
    }
}

@Composable
private fun SimpleScaffold(
    title: String,
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back_24dp),
                        contentDescription = stringResource(R.string.acc_back)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )
                actions()
            }
            content()
        }
    }
}

@Composable
private fun SimpleServersScreen(
    mainViewModel: MainViewModel,
    uiState: MainUiState,
    onAction: (MainAction) -> Unit,
    onBack: () -> Unit,
) {
    val groupId = uiState.selectedGroupId
    val groupFlow = remember(mainViewModel, groupId) { mainViewModel.serverGroupState(groupId) }
    val groupState by groupFlow.collectAsStateWithLifecycle()
    var showAddMenu by remember { mutableStateOf(false) }

    SimpleScaffold(
        title = stringResource(R.string.simple_servers_title),
        onBack = onBack,
        actions = {
            Box {
                IconButton(onClick = { showAddMenu = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_24dp),
                        contentDescription = stringResource(R.string.acc_add)
                    )
                }
                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    AppDropdownMenuItems(
                        items = SimpleAddAction.entries,
                        labelRes = { it.labelRes },
                        onSelected = {
                            showAddMenu = false
                            onAction(it.action)
                        }
                    )
                }
            }
        }
    ) {
        if (uiState.groups.size > 1) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.groups.forEach { group ->
                    val selected = group.id == groupId
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.onBackground
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .selectable(
                                selected = selected,
                                role = Role.Tab,
                                onClick = { onAction(MainAction.SelectGroup(group.id)) }
                            )
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = group.remarks,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) MaterialTheme.colorScheme.background
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (groupState.rows.isEmpty()) {
                Text(
                    text = stringResource(R.string.simple_servers_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(groupState.rows, key = { it.guid }) { row ->
                        SimpleServerRow(
                            row = row,
                            selected = row.guid == uiState.selectedGuid,
                            onClick = {
                                onAction(MainAction.SelectServer(row.guid))
                                onBack()
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onAction(MainAction.TestRealAllServers) },
            enabled = groupState.rows.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape
        ) {
            Text(
                text = stringResource(R.string.simple_servers_test_all),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SimpleServerRow(row: ServerRowUiModel, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val delayText = when {
        row.testDelayMillis > 0L -> stringResource(R.string.server_test_delay_value, row.testDelayMillis)
        row.testDelayMillis < 0L -> stringResource(R.string.simple_delay_timeout)
        else -> ""
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = 1.5.dp,
                color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                shape = shape
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .then(
                    if (selected) Modifier.background(MaterialTheme.colorScheme.tertiary)
                    else Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.remarks.ifBlank { row.typeDescription },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = row.typeDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        if (delayText.isNotEmpty()) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = delayText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (row.testDelayMillis < 0L) colorPingRed else colorPing
            )
        }
    }
}

@Composable
private fun SimpleSettingsScreen(
    advancedMode: Boolean,
    onAdvancedModeChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    SimpleScaffold(
        title = stringResource(R.string.title_settings),
        onBack = onBack
    ) {
        val shape = RoundedCornerShape(16.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .toggleable(
                    value = advancedMode,
                    role = Role.Switch,
                    onValueChange = onAdvancedModeChange
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.simple_advanced_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.simple_advanced_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            // The row is the single toggle target, so the switch itself is display only.
            Switch(checked = advancedMode, onCheckedChange = null)
        }
    }
}
