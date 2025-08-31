package com.vladceresna.virtel.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import virtel.composeapp.generated.resources.Res
import virtel.composeapp.generated.resources.Inter_Black
import virtel.composeapp.generated.resources.Inter_BlackItalic
import virtel.composeapp.generated.resources.Inter_Bold
import virtel.composeapp.generated.resources.Inter_BoldItalic
import virtel.composeapp.generated.resources.Inter_ExtraBold
import virtel.composeapp.generated.resources.Inter_ExtraBoldItalic
import virtel.composeapp.generated.resources.Inter_ExtraLight
import virtel.composeapp.generated.resources.Inter_ExtraLightItalic
import virtel.composeapp.generated.resources.Inter_Italic
import virtel.composeapp.generated.resources.Inter_Light
import virtel.composeapp.generated.resources.Inter_LightItalic
import virtel.composeapp.generated.resources.Inter_Medium
import virtel.composeapp.generated.resources.Inter_MediumItalic
import virtel.composeapp.generated.resources.Inter_Regular
import virtel.composeapp.generated.resources.Inter_SemiBold
import virtel.composeapp.generated.resources.Inter_SemiBoldItalic
import virtel.composeapp.generated.resources.Inter_Thin
import virtel.composeapp.generated.resources.Inter_ThinItalic




import virtel.composeapp.generated.resources.InterDisplay_Black
import virtel.composeapp.generated.resources.InterDisplay_BlackItalic
import virtel.composeapp.generated.resources.InterDisplay_Bold
import virtel.composeapp.generated.resources.InterDisplay_BoldItalic
import virtel.composeapp.generated.resources.InterDisplay_ExtraBold
import virtel.composeapp.generated.resources.InterDisplay_ExtraBoldItalic
import virtel.composeapp.generated.resources.InterDisplay_ExtraLight
import virtel.composeapp.generated.resources.InterDisplay_ExtraLightItalic
import virtel.composeapp.generated.resources.InterDisplay_Italic
import virtel.composeapp.generated.resources.InterDisplay_Light
import virtel.composeapp.generated.resources.InterDisplay_LightItalic
import virtel.composeapp.generated.resources.InterDisplay_Medium
import virtel.composeapp.generated.resources.InterDisplay_MediumItalic
import virtel.composeapp.generated.resources.InterDisplay_Regular
import virtel.composeapp.generated.resources.InterDisplay_SemiBold
import virtel.composeapp.generated.resources.InterDisplay_SemiBoldItalic
import virtel.composeapp.generated.resources.InterDisplay_Thin
import virtel.composeapp.generated.resources.InterDisplay_ThinItalic


@Composable
fun getInterFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.Inter_Thin, weight = FontWeight.Thin),
        Font(Res.font.Inter_ThinItalic, weight = FontWeight.Thin, style = FontStyle.Italic),
        Font(Res.font.Inter_ExtraLight, weight = FontWeight.ExtraLight),
        Font(Res.font.Inter_ExtraLightItalic, weight = FontWeight.ExtraLight, style = FontStyle.Italic),
        Font(Res.font.Inter_Light, weight = FontWeight.Light),
        Font(Res.font.Inter_LightItalic, weight = FontWeight.Light, style = FontStyle.Italic),
        Font(Res.font.Inter_Regular, weight = FontWeight.Normal),
        Font(Res.font.Inter_Italic, weight = FontWeight.Normal, style = FontStyle.Italic),
        Font(Res.font.Inter_Medium, weight = FontWeight.Medium),
        Font(Res.font.Inter_MediumItalic, weight = FontWeight.Medium, style = FontStyle.Italic),
        Font(Res.font.Inter_SemiBold, weight = FontWeight.SemiBold),
        Font(Res.font.Inter_SemiBoldItalic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
        Font(Res.font.Inter_Bold, weight = FontWeight.Bold),
        Font(Res.font.Inter_BoldItalic, weight = FontWeight.Bold, style = FontStyle.Italic),
        Font(Res.font.Inter_ExtraBold, weight = FontWeight.ExtraBold),
        Font(Res.font.Inter_ExtraBoldItalic, weight = FontWeight.ExtraBold, style = FontStyle.Italic),
        Font(Res.font.Inter_Black, weight = FontWeight.Black),
        Font(Res.font.Inter_BlackItalic, weight = FontWeight.Black, style = FontStyle.Italic),
    )
}

@Composable
fun getInterDisplayFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.InterDisplay_Thin, weight = FontWeight.Thin),
        Font(Res.font.InterDisplay_ThinItalic, weight = FontWeight.Thin, style = FontStyle.Italic),
        Font(Res.font.InterDisplay_ExtraLight, weight = FontWeight.ExtraLight),
        Font(Res.font.InterDisplay_ExtraLightItalic, weight = FontWeight.ExtraLight, style = FontStyle.Italic),
        Font(Res.font.InterDisplay_Light, weight = FontWeight.Light),
        Font(Res.font.InterDisplay_LightItalic, weight = FontWeight.Light, style = FontStyle.Italic),
        Font(Res.font.InterDisplay_Regular, weight = FontWeight.Normal),
        Font(Res.font.InterDisplay_Italic, weight = FontWeight.Normal, style = FontStyle.Italic),
        Font(Res.font.InterDisplay_Medium, weight = FontWeight.Medium),
        Font(Res.font.InterDisplay_MediumItalic, weight = FontWeight.Medium, style = FontStyle.Italic),
        Font(Res.font.InterDisplay_SemiBold, weight = FontWeight.SemiBold),
        Font(Res.font.InterDisplay_SemiBoldItalic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
        Font(Res.font.InterDisplay_Bold, weight = FontWeight.Bold),
        Font(Res.font.InterDisplay_BoldItalic, weight = FontWeight.Bold, style = FontStyle.Italic),
        Font(Res.font.InterDisplay_ExtraBold, weight = FontWeight.ExtraBold),
        Font(Res.font.InterDisplay_ExtraBoldItalic, weight = FontWeight.ExtraBold, style = FontStyle.Italic),
        Font(Res.font.InterDisplay_Black, weight = FontWeight.Black),
        Font(Res.font.InterDisplay_BlackItalic, weight = FontWeight.Black, style = FontStyle.Italic),
    )
}