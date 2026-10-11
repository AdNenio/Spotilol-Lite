package com.project.lol.webview.injections

object SpicyLyrics {
    const val CONTENT = """
        (function(){
            if (window.__spicyLyrics) return;
            window.__spicyLyrics = true;

            var cache = {};

            function enabled(){ return window.__spicyLyricsEnabled !== false; }

            function idFromUri(uri){
                var m = /spotify:track:([A-Za-z0-9]{22})/.exec(uri || '');
                return m ? m[1] : null;
            }

            function trackMeta(){
                try {
                    var t = document.querySelector('[data-testid="context-item-info-title"]');
                    var a = document.querySelector('[data-testid="context-item-info-artist"]');
                    var d = document.querySelector('[data-testid="playback-duration"]');
                    var title = t ? (t.textContent || '').trim() : '';
                    var artist = a ? (a.textContent || '').trim() : '';
                    var dur = 0;
                    if (d) {
                        var parts = (d.textContent || '').trim().split(':');
                        if (parts.length === 2) dur = (parseInt(parts[0], 10) || 0) * 60 + (parseInt(parts[1], 10) || 0);
                    }
                    if (!title) {
                        var dt = (document.title || '').split(' \u2022 ');
                        if (dt.length >= 2) { title = dt[0].trim(); if (!artist) artist = dt[1].trim(); }
                    }
                    return { title: title, artist: artist, duration: dur };
                } catch(e){ return { title:'', artist:'', duration:0 }; }
            }

            function resolve(id){
                if (!enabled() || !id || id.length !== 22) return null;
                if (Object.prototype.hasOwnProperty.call(cache, id)) return cache[id];
                var meta = trackMeta();
                var payload = null;
                try {
                    var raw = AndBridge.spicyLyrics(id, meta.title, meta.artist, meta.duration);
                    if (raw && raw !== 'null') payload = JSON.parse(raw);
                } catch(e){ payload = null; }
                var unsynced = payload && payload.lyrics && payload.lyrics.syncType === 'UNSYNCED';
                if (!(unsynced && !meta.title)) cache[id] = payload;
                return payload;
            }

            function patchMetadata(json){
                if (!json || json.has_lyrics !== undefined) return false;
                var id = idFromUri(json.canonical_uri);
                if (!id) return false;
                if (!resolve(id)) return false;
                json.has_lyrics = true;
                return true;
            }

            function jsonResponse(obj){
                var h = new Headers();
                h.set('content-type', 'application/json');
                return new Response(JSON.stringify(obj), { status: 200, statusText: 'OK', headers: h });
            }

            var prev = window.fetch.bind(window);

            window.fetch = function(input, init){
                if (!enabled()) return prev(input, init);
                var url = typeof input === 'string' ? input : (input && input.url) || '';
                var isMeta = url.indexOf('metadata/4/track/') !== -1;
                var lyr = /color-lyrics\/v2\/track\/([A-Za-z0-9]{22})/.exec(url);
                if (!isMeta && !lyr) return prev(input, init);
                return prev(input, init).then(function(resp){
                    if (isMeta) {
                        if (resp.status !== 200) return resp;
                        return resp.clone().json().then(function(json){
                            if (!patchMetadata(json)) return resp;
                            return jsonResponse(json);
                        }).catch(function(){ return resp; });
                    }
                    if (resp.status !== 404) return resp;
                    var payload = resolve(lyr[1]);
                    if (!payload) return resp;
                    return jsonResponse(payload);
                });
            };

            function nudgeAvailability(){
                try {
                    var btn = document.querySelector('button[data-testid="lyrics-button"]');
                    if (!btn) return false;
                    var keys = Object.keys(btn), fk = null;
                    for (var i = 0; i < keys.length; i++) {
                        if (keys[i].indexOf('__reactFiber$') === 0) { fk = keys[i]; break; }
                    }
                    if (!fk) return false;
                    var node = btn[fk], guard = 0, obs = null;
                    while (node && guard++ < 16) {
                        var hook = node.memoizedState, hg = 0, states = [];
                        while (hook && hg++ < 14) { states.push(hook.memoizedState); hook = hook.next; }
                        for (var j = 0; j < states.length; j++) {
                            var v = states[j];
                            if (v && v.options && v.options.queryKey &&
                                JSON.stringify(v.options.queryKey).indexOf('useLyricsAvailability') !== -1) obs = v;
                        }
                        if (obs) break;
                        node = node.return;
                    }
                    if (!obs || typeof obs.refetch !== 'function') return false;
                    obs.refetch();
                    return true;
                } catch(e) { return false; }
            }

            function armTrackWatch(){
                if (typeof window.splOnTrackChange !== 'function') return;
                window.splOnTrackChange(function(uri, id){
                    if (!enabled() || !id) return;
                    if (!resolve(id)) return;
                    var tries = 0;
                    var iv = setInterval(function(){
                        tries++;
                        var b = document.querySelector('button[data-testid="lyrics-button"]');
                        if (b && !b.disabled) { clearInterval(iv); return; }
                        nudgeAvailability();
                        if (tries >= 8) clearInterval(iv);
                    }, 2500);
                });
            }

            window.spicyLyrics = {
                refresh: function(){ cache = {}; },
                has: function(id){ return !!resolve(id); },
                nudge: nudgeAvailability
            };

            armTrackWatch();
        })();
    """
}
