package com.dergoogler.mmrl.webui.model

import com.dergoogler.mmrl.webui.asStyleResponse

/**
 * Data class representing insets (top, bottom, left, right) for a view.
 *
 * This class provides methods to generate CSS code that can be injected into a WebView
 * to apply these insets as CSS variables. This is useful for adapting web content
 * to the safe areas of a device screen, considering notches, status bars, and navigation bars.
 *
 * @property top The top inset value in pixels.
 * @property bottom The bottom inset value in pixels.
 * @property left The left inset value in pixels.
 * @property right The right inset value in pixels.
 */
data class Insets(
    val top: Int,
    val bottom: Int,
    val left: Int,
    val right: Int,
) {
    private val css
        get() = buildString {
            appendLine(":root {")
            appendLine("\t--safe-area-inset-top: ${top}px;")
            appendLine("\t--safe-area-inset-right: ${right}px;")
            appendLine("\t--safe-area-inset-bottom: ${bottom}px;")
            appendLine("\t--safe-area-inset-left: ${left}px;")
            appendLine("\t--window-inset-top: var(--safe-area-inset-top, 0px);")
            appendLine("\t--window-inset-bottom: var(--safe-area-inset-bottom, 0px);")
            appendLine("\t--window-inset-left: var(--safe-area-inset-left, 0px);")
            appendLine("\t--window-inset-right: var(--safe-area-inset-right, 0px);")
            appendLine("\t--f7-safe-area-top: var(--window-inset-top, 0px) !important;")
            appendLine("\t--f7-safe-area-bottom: var(--window-inset-bottom, 0px) !important;")
            appendLine("\t--f7-safe-area-left: var(--window-inset-left, 0px) !important;")
            appendLine("\t--f7-safe-area-right: var(--window-inset-right, 0px) !important;")
            append("}")
        }

    val cssInject
        get() = buildString {
            val sdg = css
                .replace(Regex("\t"), "\t\t")
                .replace(Regex("\n\\}"), "\n\t}")

            appendLine("<!-- WebUI X Insets Inject -->")
            appendLine("<style data-internal type=\"text/css\">")
            appendLine("\t$sdg")
            appendLine("</style>")
        }

    val cssResponse get() = css.asStyleResponse()

    companion object {
        val None = Insets(0, 0, 0, 0)
    }
}