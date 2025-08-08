package com.dergoogler.mmrl.ui.screens.modules

import androidx.annotatimport com.dergoogler.mmrl.ui.component.lite.row.VerticalAlignment
import com.dergoogler.mmrl.ui.component.text.TextWithIconDefaults
import com.dergoogler.mmrl.ui.providable.LocalStoredModule
import com.dergoogler.mmrl.utils.launchWebUI
import com.dergoogler.mmrl.utils.toFormattedDateSafelyableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.fadingEdge
import com.dergoogler.mmrl.model.local.State
import com.dergoogler.mmrl.model.local.versionDisplay
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.text.TextWithIcon
import com.dergoogler.mmrl.ui.component.card.Card
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.rememberTrue
import com.dergoogler.mmrl.ext.takeTrue
import com.dergoogler.mmrl.ext.toStyleMarkup
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.config
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.hasWebUI
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.file.SuFile.Companion.toFormattedFileSize
import com.dergoogler.mmrl.platform.model.ModId.Companion.moduleDir
import com.dergoogler.mmrl.ui.component.LabelItemDefaults
import com.dergoogler.mmrl.ui.component.LocalCover
import com.dergoogler.mmrl.ui.component.card.component.Absolute
import com.dergoogler.mmrl.ui.component.card.CardScope
import com.dergoogler.mmrl.ui.component.lite.column.LiteColumn
import com.dergoogler.mmrl.ui.component.lite.row.LiteRow
import com.dergoogler.mmrl.ui.component.lite.row.LiteRowScope
import com.dergoogler.mmrl.ui.component.lite.row.VerticalAlignment
import com.dergoogler.mmrl.ui.component.text.TextWithIconDefaults
import com.dergoogler.mmrl.ui.providable.LocalStoredModule
import com.dergoogler.mmrl.utils.WebUIXPackageName
import com.dergoogler.mmrl.utils.launchWebUI
import com.dergoogler.mmrl.utils.toFormattedDateSafely
import dev.dergoogler.mmrl.compat.core.LocalUriHandler
import kotlinx.coroutines.launch

@Composable
fun ModuleItem(
    progress: Float,
    indeterminate: Boolean = false,
    alpha: Float = 1f,
    decoration: TextDecoration = TextDecoration.None,
    switch: @Composable() (() -> Unit?)? = null,
    indicator: @Composable() (CardScope.() -> Unit?)? = null,
    startTrailingButton: @Composable() (LiteRowScope.() -> Unit)? = null,
    trailingButton: @Composable() (LiteRowScope.() -> Unit),
    isBlacklisted: Boolean = false,
    isProviderAlive: Boolean,
) {
    val userPreferences = LocalUserPreferences.current
    val menu = userPreferences.modulesMenu
    val context = LocalContext.current

    val module = LocalStoredModule.current

    val canWenUIAccessed = remember(isProviderAlive, module) {
        isProviderAlive && module.hasWebUI && module.state != State.REMOVE
    }

    val clicker: (() -> Unit)? = remember(canWenUIAccessed) {
        canWenUIAccessed nullable {
            userPreferences.launchWebUI(context, module.id)
        }
    }

    Card(
        border = isBlacklisted nullable BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.errorContainer
        ),
        onClick = clicker
    ) {
        indicator.nullable {
            Absolute(
                alignment = Alignment.Center,
            ) {
                it()
            }
        }

        LiteColumn(
            modifier = Modifier.relative(),
        ) {
            module.config.cover.nullable(menu.showCover) {
                val file = SuFile(module.id.moduleDir, it)

                file.exists {
                    LocalCover(
                        modifier = Modifier.fadingEdge(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black,
                                ),
                                startY = Float.POSITIVE_INFINITY,
                                endY = 0f
                            ),
                        ),
                        inputStream = it.newInputStream(),
                    )
                }
            }

            LiteRow(
                modifier = Modifier.padding(all = 16.dp),
                verticalAlignment = VerticalAlignment.Center
            ) {
                LiteColumn(
                    modifier = Modifier
                        .alpha(alpha = alpha)
                        .weight(1f),
                    spaceBetweenItem = 2.dp,
                ) {
                    TextWithIcon(
                        text = module.config.name ?: module.name,
                        icon = canWenUIAccessed nullable R.drawable.sandbox,
                        style = TextWithIconDefaults.style.copy(textStyle = MaterialTheme.typography.titleSmall)
                    )

                    Text(
                        text = stringResource(
                            id = R.string.module_version_author,
                            module.versionDisplay, module.author
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = decoration,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (module.lastUpdated != 0L && menu.showUpdatedTime) {
                        Text(
                            text = stringResource(
                                id = R.string.module_update_at,
                                module.lastUpdated.toFormattedDateSafely
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = decoration,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                switch?.invoke()
            }

            val description = if (module.config.description != null) {
                module.config.description!!.toStyleMarkup()
            } else {
                AnnotatedString(module.description)
            }

            Text(
                modifier = Modifier
                    .alpha(alpha = alpha)
                    .padding(horizontal = 16.dp),
                text = description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                textDecoration = decoration,
                color = MaterialTheme.colorScheme.outline
            )

            LiteRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = VerticalAlignment.Center,
                spaceBetweenItem = 8.dp,
            ) {
                userPreferences.developerMode.rememberTrue {
                    LabelItem(
                        text = module.id.id,
                        upperCase = false
                    )
                }

                LabelItem(
                    text = module.size.toFormattedFileSize(),
                    style = LabelItemDefaults.style.copy(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }

            when {
                indeterminate -> LinearProgressIndicator(
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(2.dp)
                        .fillMaxWidth()
                )

                progress != 0f -> LinearProgressIndicator(
                    progress = { progress },
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(1.5.dp)
                        .fillMaxWidth()
                )

                else -> HorizontalDivider(
                    thickness = 1.5.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            LiteRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = VerticalAlignment.Center,
            ) {
                startTrailingButton?.invoke(this)
                Spacer(modifier = Modifier.weight(1f))
                trailingButton.invoke(this)
            }
        }
    }
}

@Composable
fun StateIndicator(
    @DrawableRes icon: Int,
    color: Color = MaterialTheme.colorScheme.outline,
) = Image(
    modifier = Modifier.requiredSize(150.dp),
    painter = painterResource(id = icon),
    contentDescription = null,
    alpha = 0.1f,
    colorFilter = ColorFilter.tint(color)
)
