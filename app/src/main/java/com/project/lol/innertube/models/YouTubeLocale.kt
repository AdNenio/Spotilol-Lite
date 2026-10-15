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
                code.length == 2 && code.all { it in 'A'..'Z' } && code !in UNSUPPORTED_GL
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

        /**
         * ISO-3166-1 alpha-2 country codes that the `youtubei/v1/search` endpoint
         * rejects with HTTP 400 (INVALID_ARGUMENT). Every one of these was probed
         * live against `music.youtube.com/youtubei/v1/search` with the `WEB_REMIX`
         * client: 111 codes returned 200, the 138 listed here returned 400.
         *
         * Only the `search` endpoint enforces this allow-list — `player` and `next`
         * accept any valid 2-letter code. Falling back to [DEFAULT_GL] here is what
         * keeps Spl-DL (offline download resolution) working on devices whose
         * system region is rejected (e.g. a `zh-CN` phone resolving `gl="CN"`).
         */
        private val UNSUPPORTED_GL = setOf(
            "AD", "AF", "AG", "AI", "AL", "AO", "AQ", "AS", "AW", "AX",
            "BB", "BF", "BI", "BJ", "BL", "BM", "BN", "BQ", "BS", "BT",
            "BV", "BW", "BZ", "CC", "CD", "CF", "CG", "CI", "CK", "CM",
            "CN", "CU", "CV", "CW", "CX", "DJ", "DM", "EH", "ER", "ET",
            "FJ", "FK", "FM", "FO", "GA", "GD", "GF", "GG", "GI", "GL",
            "GM", "GN", "GP", "GQ", "GS", "GU", "GW", "GY", "HM", "HT",
            "IM", "IO", "IR", "JE", "KG", "KI", "KM", "KN", "KP", "KY",
            "LC", "LR", "LS", "MC", "MF", "MG", "MH", "ML", "MM", "MN",
            "MO", "MP", "MQ", "MR", "MS", "MU", "MV", "MW", "MZ", "NA",
            "NC", "NE", "NF", "NR", "NU", "PF", "PM", "PN", "PS", "PW",
            "RE", "RW", "SB", "SC", "SD", "SH", "SJ", "SL", "SM", "SO",
            "SR", "SS", "ST", "SX", "SY", "SZ", "TC", "TD", "TF", "TG",
            "TJ", "TK", "TL", "TM", "TO", "TT", "TV", "UM", "UZ", "VA",
            "VC", "VG", "VI", "VU", "WF", "WS", "YT", "ZM",
        )

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
