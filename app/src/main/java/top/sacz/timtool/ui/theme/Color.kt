package top.sacz.timtool.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TimToolColorPalette(
    val brandPrimaryColor: Color,
    val textPrimaryColor: Color,
    val textSecondaryColor: Color,
    val statusErrorColor: Color,
    val statusOverlayTextColor: Color,
    val listItemCardColor: Color,
    val listLimitedCardColor: Color,
    val switchCheckedThumbColor: Color,
    val switchUncheckedThumbColor: Color,
    val switchCheckedTrackColor: Color,
    val switchUncheckedTrackColor: Color,
    val dialogContainerColor: Color,
    val searchInputContainerColor: Color,
    val searchHistoryTagTextColor: Color,
    val tagFunctionColor: Color,
    val accentTagColor: Color,
    val tagGroupColor: Color,
    val userVipIdentityColor: Color,
    val userActionButtonDisabledColor: Color
)

internal val TimToolLightColorPalette = TimToolColorPalette(
    brandPrimaryColor = Color(0x72FFFB03),
    textPrimaryColor = Color(0xFF111827),
    textSecondaryColor = Color(0xFF6B7280),
    statusErrorColor = Color(0xFFF05B72),
    statusOverlayTextColor = Color(0xFF111827),
    listItemCardColor = Color(0xA6FFFFFF),
    listLimitedCardColor = Color(0xA6807040),
    switchCheckedThumbColor = Color(0xFFFFFFFF),
    switchUncheckedThumbColor = Color(0xFFAAAAAA),
    switchCheckedTrackColor = Color(0xB5FFFB03),
    switchUncheckedTrackColor = Color(0xB3536671),
    dialogContainerColor = Color(0xFFFDFEFF),
    searchInputContainerColor = Color(0xFFF1F5F9),
    searchHistoryTagTextColor = Color(0xFFFFFFFF),
    tagFunctionColor = Color(0xFF4CAF50),
    accentTagColor = Color(0xFFFF9800),
    tagGroupColor = Color(0xFF9C27B0),
    userVipIdentityColor = Color(0xFFFFC940),
    userActionButtonDisabledColor = Color(0x596B7280)
)

internal val TimToolDarkColorPalette = TimToolColorPalette(
    brandPrimaryColor = Color(0xCC7A6200),
    textPrimaryColor = Color(0xFFF3F4F6),
    textSecondaryColor = Color(0xFF9CA3AF),
    statusErrorColor = Color(0xFFFF8FA3),
    statusOverlayTextColor = Color(0xFFF8FAFC),
    listItemCardColor = Color(0xCC2A2000),
    listLimitedCardColor = Color(0xB3605040),
    switchCheckedThumbColor = Color(0xFFFFFFFF),
    switchUncheckedThumbColor = Color(0xFF8E9AA7),
    switchCheckedTrackColor = Color(0xE97A6200),
    switchUncheckedTrackColor = Color(0xB35B6775),
    dialogContainerColor = Color.Gray,
    searchInputContainerColor = Color(0xFF24313D),
    searchHistoryTagTextColor = Color(0xFF0F172A),
    tagFunctionColor = Color(0xFF4DD381),
    accentTagColor = Color(0xFFFFB347),
    tagGroupColor = Color(0xFFBA68C8),
    userVipIdentityColor = Color(0xFFFFD666),
    userActionButtonDisabledColor = Color(0x599CA3AF)
)

internal val LocalTimToolColorPalette = staticCompositionLocalOf { TimToolLightColorPalette }

val TimToolBrandPrimaryColor: Color
    @Composable get() = LocalTimToolColorPalette.current.brandPrimaryColor

val TimToolSettingTextPrimaryColor: Color
    @Composable get() = LocalTimToolColorPalette.current.textPrimaryColor

val TimToolSettingTextSecondaryColor: Color
    @Composable get() = LocalTimToolColorPalette.current.textSecondaryColor

val TimToolSettingStatusErrorColor: Color
    @Composable get() = LocalTimToolColorPalette.current.statusErrorColor

val TimToolSettingStatusOverlayTextColor: Color
    @Composable get() = LocalTimToolColorPalette.current.statusOverlayTextColor

val TimToolSettingListItemCardColor: Color
    @Composable get() = LocalTimToolColorPalette.current.listItemCardColor

val TimToolSettingListLimitedCardColor: Color
    @Composable get() = LocalTimToolColorPalette.current.listLimitedCardColor

val TimToolSettingListSwitchCheckedThumbColor: Color
    @Composable get() = LocalTimToolColorPalette.current.switchCheckedThumbColor

val TimToolSettingListSwitchUncheckedThumbColor: Color
    @Composable get() = LocalTimToolColorPalette.current.switchUncheckedThumbColor

val TimToolSettingListSwitchCheckedTrackColor: Color
    @Composable get() = LocalTimToolColorPalette.current.switchCheckedTrackColor

val TimToolSettingListSwitchUncheckedTrackColor: Color
    @Composable get() = LocalTimToolColorPalette.current.switchUncheckedTrackColor

val TimToolDialogContainerColor: Color
    @Composable get() = LocalTimToolColorPalette.current.dialogContainerColor

val TimToolSettingSearchInputContainerColor: Color
    @Composable get() = LocalTimToolColorPalette.current.searchInputContainerColor

val TimToolSettingSearchHistoryTagTextColor: Color
    @Composable get() = LocalTimToolColorPalette.current.searchHistoryTagTextColor

val TimToolSettingTagFunctionColor: Color
    @Composable get() = LocalTimToolColorPalette.current.tagFunctionColor

val TimToolAccentTagColor: Color
    @Composable get() = LocalTimToolColorPalette.current.accentTagColor

val TimToolSettingTagGroupColor: Color
    @Composable get() = LocalTimToolColorPalette.current.tagGroupColor

val TimToolUserVipIdentityColor: Color
    @Composable get() = LocalTimToolColorPalette.current.userVipIdentityColor

val TimToolUserActionButtonDisabledColor: Color
    @Composable get() = LocalTimToolColorPalette.current.userActionButtonDisabledColor
