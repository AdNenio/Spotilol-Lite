package com.project.lol.innertube.models

import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class YouTubeLocale(
    val gl: String,
    val hl: String,
) {
    fun sanitized(): YouTubeLocale {
        val tag = hl.trim().lowercase(Locale.ROOT)
        val base = tag.substringBefore('-')
        val pair = tag.split('-').take(2).joinToString("-")
        return YouTubeLocale(
            gl = gl.trim().uppercase(Locale.ROOT).takeIf { code ->
                code.length == 2 && code.all { it in 'A'..'Z' }
            } ?: DEFAULT_GL,
            hl = when {
                REGIONAL.containsKey(pair) -> REGIONAL.getValue(pair)
                base in LANGUAGES -> base
                else -> DEFAULT_HL
            },
        )
    }

    companion object {
        const val DEFAULT_GL = "US"
        const val DEFAULT_HL = "en"

        private val LANGUAGES = (
            "af,az,id,ms,bs,ca,cs,da,de,et,en,es,eu,fil,fr,gl,hr,zu,is,it,sw,lv,lt,hu,nl,no,nb,uz," +
                "pl,pt,ro,sq,sk,sl,sr,fi,sv,vi,tr,be,bg,ky,kk,mk,mn,ru,uk,el,hy,he,iw,ur,ar,fa,ne,mr," +
                "hi,bn,pa,gu,ta,te,kn,ml,si,th,lo,my,km,ko,zh,ja,tl,in,mo,sh"
            ).split(',').toSet()

        private val REGIONAL = listOf(
            "en-US", "en-GB", "en-AU", "en-IN",
            "es-ES", "es-US", "es-MX", "es-AR", "es-CO", "es-CL", "es-PE", "es-419",
            "pt-BR", "pt-PT",
            "fr-FR", "fr-CA",
            "it-IT", "de-DE", "nl-NL",
            "zh-CN", "zh-TW", "zh-HK", "zh-Hans", "zh-Hant",
            "sr-Latn", "sr-Cyrl",
            "sv-SE",
        ).associateBy { it.lowercase(Locale.ROOT) }
    }
}
