package com.dergoogler.mmrl.ui.screens.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.ext.isPackageInstalled
import com.dergoogler.mmrl.ext.navigateSingleTopTo
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.takeFalse
import com.dergoogler.mmrl.model.local.FeaturedManager
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.dialog.ConfirmData
import com.dergoogler.mmrl.ui.component.dialog.rememberConfirm
import com.dergoogler.mmrl.ui.component.listItem.dsl.List
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.ButtonItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.RadioDialogItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Icon
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.navigation.graphs.SettingsScreen
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.WebUIXPackageName
import com.jakewharton.processphoenix.ProcessPhoenix

@Composable
fun SettingsScreen() {
    val userPreferences = LocalUserPreferences.current
    val viewModel = LocalSettings.current
    val browser = LocalUriHandler.current
    val context = LocalContext.current
    val confirm = rememberConfirm(context)

    SettingsScaffold(
        allowNavigateBack = false,
        title = R.string.page_settings
    ) {
        List {
            NavButton(
                route = SettingsScreen.Appearance.route,
                icon = R.drawable.color_swatch,
                title = R.string.settings_appearance,
                desc = R.string.settings_appearance_desc
            )

            NavButton(
                route = SettingsScreen.Security.route,
                icon = R.drawable.shield,
                title = R.string.settings_security,
                desc = R.string.settings_security_desc
            )

            NavButton(
                route = SettingsScreen.Updates.route,
                icon = R.drawable.refresh,
                title = R.string.settings_updates,
                desc = R.string.settings_updates_desc
            )

            NavButton(
                route = SettingsScreen.Modules.route,
                icon = R.drawable.stack_middle,
                title = R.string.settings_modules,
                desc = R.string.settings_modules_desc
            )

            NavButton(
                route = SettingsScreen.Terminal.route,
                icon = R.drawable.terminal_2_outlined,
                title = R.string.settings_terminal,
                desc = R.string.settings_terminal_desc
            )

            NavButton(
                route = SettingsScreen.Other.route,
                icon = R.drawable.tool,
                title = R.string.settings_other,
                desc = R.string.settings_other_desc
            )

            LinkButton(
                uri = Const.RESOURCES_URL,
                icon = R.drawable.file_3d,
                title = R.string.settings_resources,
                desc = R.string.settings_resources_desc
            )
            val manager =
                FeaturedManager.managers.find { userPreferences.workingMode == it.workingMode }

            manager.nullable { mng ->
                RadioDialogItem(
                    selection = mng.workingMode,
                    options = FeaturedManager.managers.map { it.toRadioDialogItem() },
                    onConfirm = {
                        confirm(
                            ConfirmData(
                                title = context.getString(R.string.change_platform),
                                description = context.getString(R.string.working_mode_change_dialog_desc),
                                closeText = context.getString(R.string.keep),
                                onClose = {},
                                confirmText = context.getString(R.string.apply),
                                onConfirm = {
                                    viewModel.setWorkingMode(it.value)
                                    ProcessPhoenix.triggerRebirth(context)
                                }
                            )
                        )
                    },
                ) {
                    Icon(painter = painterResource(mng.icon))
                    Title(R.string.platform)
                    Description(mng.name)
                }
            }

            NavButton(
                route = SettingsScreen.Changelog.route,
                icon = R.drawable.files,
                title = R.string.settings_changelog,
                desc = R.string.settings_changelo_desc
            )

            NavButton(
                route = SettingsScreen.Blacklist.route,
                icon = R.drawable.file_shredder,
                title = R.string.settings_blacklist,
                desc = R.string.settings_blacklist_desc
            )

            NavButton(
                route = SettingsScreen.LogViewer.route,
                icon = R.drawable.logs,
                title = R.string.settings_log_viewer,
            )

            NavButton(
                route = SettingsScreen.Developer.route,
                icon = R.drawable.bug,
                title = R.string.settings_developer,
                desc = R.string.settings_developer_desc
            )

            LinkButton(
                uri = Const.PRIVACY_POLICY_URL,
                icon = R.drawable.spy,
                title = R.string.settings_privacy_policy,
            )

            LinkButton(
                uri = Const.TERMS_OF_SERVICE_URL,
                icon = R.drawable.files,
                title = R.string.settings_terms_of_service,
            )
        }
    }
}

@Composable
internal fun ListScope.NavButton(
    route: String,
    @DrawableRes icon: Int? = null,
    @StringRes title: Int,
    @StringRes desc: Int? = null,
) {
    val navController = LocalNavController.current

    ButtonItem(
        onClick = {
            navController.navigateSingleTopTo(route)
        },
        content = {
            icon.nullable {
                Icon(
                    painter = painterResource(it)
                )
            }
            Title(title)
            desc.nullable {
                Description(it)
            }
        }
    )
}

@Composable
internal fun ListScope.LinkButton(
    uri: String,
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes desc: Int? = null,
) {
    val browser = LocalUriHandler.current

    ButtonItem(
        onClick = {
            browser.openUri(uri)
        },
        content = {
            Icon(
                painter = painterResource(icon)
            )
            Title(title)
            desc.nullable {
                Description(it)
            }
            Icon(
                slot = ListItemSlot.End,
                size = 12.dp,
                painter = painterResource(R.drawable.external_link)
            )
        }
    )
}