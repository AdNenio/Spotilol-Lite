package com.project.lol.webview.injections

object MediaUpdater {
    const val CONTENT = """
            window.splUpCoverUrl = function(url){
                if(!url) return null;
                return url.replace(/ab67616d0000[0-9a-f]{4}/i, 'ab67616d0000b273');
            };

            window.splPlayingTrackId = function(){
                try{
                    var a = document.querySelector('[data-testid="now-playing-widget"] a[href*="/track/"]');
                    if(a){
                        var m = (a.getAttribute('href')||'').match(/\/track\/([a-zA-Z0-9]{22})/);
                        if(m) return m[1];
                    }
                    if(document.querySelector('a[data-testid="context-item-info-show"]')) return null;
                    return window.splTrackId || window.__curTrackId || null;
                }catch(e){ return null; }
            };

            window.splMergeTrackMeta = function(id, patch){
                if(!id || !patch) return null;
                window.__splTrackMeta = window.__splTrackMeta || {};
                var cur = window.__splTrackMeta[id];
                if(!cur){ cur = { uri: 'spotify:track:' + id }; window.__splTrackMeta[id] = cur; }
                for(var k in patch){ if(patch[k] != null && cur[k] == null) cur[k] = patch[k]; }
                return cur;
            };

            window.splRowMeta = function(id){
                try{
                    var onAlbumPage = location.pathname.indexOf('/album/') !== -1;
                    var rows = document.querySelectorAll('div[data-testid="tracklist-row"]');
                    for(var i=0;i<rows.length;i++){
                        var row = rows[i];
                        if(row.closest && row.closest('.playlistRecommenderContainer, [data-testid="recommended-track"]')) continue;
                        var link = row.querySelector('a[data-testid="internal-track-link"]') || row.querySelector('a[href*="/track/"]');
                        if(!link) continue;
                        if((link.getAttribute('href')||'').indexOf(id) === -1) continue;
                        var out = {};
                        var t = (link.textContent||'').trim();
                        if(t) out.name = t;
                        var arts = [];
                        var as = row.querySelectorAll('a[href*="/artist/"]');
                        for(var j=0;j<as.length;j++){
                            var nm = (as[j].textContent||'').trim();
                            if(nm && arts.indexOf(nm) === -1) arts.push(nm);
                        }
                        if(arts.length) out.artist = arts.join(', ');
                        var al = row.querySelector('a[href*="/album/"]');
                        if(al){ var an = (al.textContent||'').trim(); if(an) out.album = an; }
                        var img = row.querySelector('img');
                        if(img && img.src) out.cover = window.splUpCoverUrl(img.src);
                        if(!out.album && onAlbumPage){
                            var h1 = document.querySelector('main h1');
                            if(h1){ var hn = (h1.textContent||'').trim(); if(hn) out.album = hn; }
                            if(!out.cover){
                                var og = document.querySelector('meta[property="og:image"]');
                                if(og){ var oc = og.getAttribute('content'); if(oc) out.cover = window.splUpCoverUrl(oc); }
                            }
                        }
                        return out;
                    }
                }catch(e){}
                return null;
            };

            window.splEnsureTrackMeta = function(id){
                if(!id) return Promise.resolve();
                window.__splMetaRuns = window.__splMetaRuns || {};
                window.__splTrackMeta = window.__splTrackMeta || {};
                var existing = window.__splTrackMeta[id] || null;
                if(existing && existing.album != null) return Promise.resolve();
                if(window.__splMetaRuns[id]) return window.__splMetaRuns[id];

                var sigOf = function(m){ return m ? [(m.name||''),(m.artist||''),(m.album||''),(m.cover||'')].join('|') : ''; };
                var lastSig = sigOf(existing);

                var promote = function(){
                    if(window.splPlayingTrackId() !== id) return;
                    var m = window.__splTrackMeta && window.__splTrackMeta[id];
                    var s = sigOf(m);
                    if(s === lastSig) return;
                    lastSig = s;
                    window.__curTrackId = id;
                    window.__curTrackUri = 'spotify:track:' + id;
                    if(m && m.name) window.__curTrackName = m.name;
                    if(m && m.artist) window.__curTrackArtist = m.artist;
                    if(m && m.album) window.__curTrackAlbum = m.album;
                    if(m && m.cover) window.__curTrackCover = m.cover;
                    window.lastState = null;
                    if(window.updMedia && typeof track !== 'undefined'){ try{ window.updMedia(); }catch(e){} }
                };

                var attempt = function(n, resolve){
                    try{
                        var rm = window.splRowMeta(id);
                        if(rm) window.splMergeTrackMeta(id, rm);
                    }catch(e){}
                    promote();
                    var m = window.__splTrackMeta && window.__splTrackMeta[id];
                    if((m && m.album != null) || n >= 3){
                        delete window.__splMetaRuns[id];
                        resolve();
                        return;
                    }
                    setTimeout(function(){ attempt(n + 1, resolve); }, [400, 600, 800][n]);
                };

                var p = new Promise(function(resolve){ attempt(0, resolve); });
                window.__splMetaRuns[id] = p;
                return p;
            };

            window.updMedia = function(){
                var id = (typeof window.splPlayingTrackId === 'function')
                    ? window.splPlayingTrackId()
                    : (window.splTrackId || window.__curTrackId || null);
                var m = (id && window.__splTrackMeta && window.__splTrackMeta[id]) || {};
                var album = m.album || ((id != null && window.__curTrackId === id) ? window.__curTrackAlbum : null) || '';

                var restart = (typeof position === 'number' && typeof lastPos === 'number' && position < lastPos - 2000);
                lastPos = position;
                if(restart) lastState = null;

                var currState=track+'|'+artist+'|'+playing+'|'+repmode+'|'+isfav+'|'+shuffle+'|'+album+'|'+duration+'|'+cover;
                if(currState!==lastState) {
                    lastState=currState;
                    var values={artist:artist,track:track,album:album,playing:playing,repeat:repmode,fav:isfav,shuffle:shuffle,duration:duration,position:position,cover:cover};
                    AndBridge.recMediaStatus(JSON.stringify(values));
                } else {
                    AndBridge.recMediaPosition(position);
                }
            };

            window.__splEnsureTrackMetaSafe = function(id){
                try{
                    var p = window.splEnsureTrackMeta(id);
                    if(p && typeof p.catch === 'function'){
                        p.catch(function(e){
                            try{ AndBridge.dbg('e','meta ensure failed: '+(e&&e.message||e)); }catch(_){}
                        });
                    }
                    return p;
                }catch(e){ return null; }
            };

            try {
                if(typeof window.splOnTrackChange === 'function'){
                    window.splOnTrackChange(function(uri, id){
                        if(!id) return;
                        if(window.__curTrackId === id){
                            window.__splEnsureTrackMetaSafe(id);
                            return;
                        }
                        var m = (window.__splTrackMeta && window.__splTrackMeta[id]) || null;
                        window.__curTrackId = id;
                        window.__curTrackUri = uri;
                        window.__curTrackName = m ? m.name : null;
                        window.__curTrackArtist = m ? m.artist : null;
                        window.__curTrackAlbum = m ? m.album : null;
                        window.__curTrackCover = m ? m.cover : null;
                        window.lastState = null;
                        window.__splEnsureTrackMetaSafe(id);
                        if(typeof track !== 'undefined'){ try{ window.updMedia(); }catch(e){} }
                    });
                }
            } catch(e){}
    """
}