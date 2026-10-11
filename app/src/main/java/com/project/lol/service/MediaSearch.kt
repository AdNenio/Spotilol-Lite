package com.project.lol.service

object MediaSearch {
    fun playSearchScript(query: String?): String {
        if (query.isNullOrBlank()) return "actPlayPause(true);"
        val quoted = org.json.JSONObject.quote(query.take(1024))
        return "if(typeof window.searchMediaItems==='function') window.searchMediaItems($quoted).then(function(items){if(!Array.isArray(items)) return;var item=items.find(function(i){return !i.browsable;});if(item && typeof playFromUri==='function') playFromUri(item.id);}).catch(function(){});"
    }
}
