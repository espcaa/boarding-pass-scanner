package eu.espcaa.boardingpassscanner.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import eu.espcaa.boardingpassscanner.R

@OptIn(ExperimentalTextApi::class)
val emphasizedTypography = FontFamily(
    Font(
        resId = R.font.roboto,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),    // Custom weight
            FontVariation.width(150f),   // Custom width (Fancy/Wide)
            FontVariation.slant(-20f)    // Custom slant
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val wideTypography = FontFamily(
    Font(
        resId = R.font.roboto,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),    // Custom weight
            FontVariation.width(200f)    // Extra wide for labels
        )
    )
)


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalTextApi::class)
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    displayLargeEmphasized = TextStyle(
        fontFamily = emphasizedTypography,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 45.sp,
        letterSpacing = (-0.25).sp,
    ),
    labelLargeEmphasized = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    titleLargeEmphasized = TextStyle(
        fontFamily = emphasizedTypography,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleSmallEmphasized = TextStyle(
        fontFamily = FontFamily(
            Font(
                resId = R.font.roboto,
                variationSettings = FontVariation.Settings(
                    FontVariation.weight(300),    // Thin weight
                    FontVariation.width(150f),   // Custom width (Fancy/Wide)
                    FontVariation.slant(-10f)    // Custom slant for a more dynamic look
                )
            )
        ),
        fontWeight = FontWeight.Thin,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)