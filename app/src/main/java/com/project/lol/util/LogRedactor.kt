package com.project.lol.util

object LogRedactor {

    private const val MASK = "***"

    private const val HEADER =
        "(?i)\\b(cookie|set-cookie|authorization|proxy-authorization)\\s*[:=]\\s*[^\\r\\n;]+"

    private const val STRONG_KEY =
        "(?i)\\b(sp_dc|sp_key|sp_t|sp_sso|csrf_?token|access_?token|refresh_?token|id_?token" +
            "|auth_?token|bearer_?token|client_?secret|api_?key|po_?token|visitor_?data" +
            "|session_?id|sessionid|password|passwd|pwd|secret_?key)\\b\\s*[=:]\\s*[\"']?" +
            "(?!true\\b|false\\b|null\\b|none\\b|unknown\\b)[^\\s&,;\"'}\\]]{3,}"

    private const val WEAK_KEY =
        "(?i)\\b(token|secret|signature|sig|lsig|session|auth|code)\\s*=\\s*[\"']?" +
            "(?!true\\b|false\\b|null\\b|none\\b|unknown\\b)[^\\s&,;\"'}\\]]{8,}"

    private const val OAUTH =
        "(?i)([?&](?:code|state|id_token|access_token|refresh_token)=)[^&\\s\"'#]{6,}"

    private const val JWT =
        "\\beyJ[A-Za-z0-9_\\-]{6,}\\.[A-Za-z0-9_\\-]{6,}(?:\\.[A-Za-z0-9_\\-]{4,})?"

    private const val EMAIL =
        "\\b([A-Za-z0-9._%+\\-]{1,64})@([A-Za-z0-9\\-]+(?:\\.[A-Za-z0-9\\-]+)*\\.[A-Za-z]{2,})\\b"

    private val combined = Regex(
        listOf(HEADER, STRONG_KEY, WEAK_KEY, OAUTH, JWT, EMAIL).joinToString("|")
    )

    fun redact(input: String): String {
        if (input.isEmpty() || !combined.containsMatchIn(input)) return input
        return combined.replace(input) { match ->
            val groups = match.groupValues
            when {
                groups[1].isNotEmpty() -> "${groups[1]}: $MASK"
                groups[2].isNotEmpty() -> "${groups[2]}=$MASK"
                groups[3].isNotEmpty() -> "${groups[3]}=$MASK"
                groups[4].isNotEmpty() -> "${groups[4]}$MASK"
                groups[5].isNotEmpty() -> "${groups[5].take(1)}$MASK@${groups[6]}"
                else -> MASK
            }
        }
    }
}
