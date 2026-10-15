package com.project.lol.webview.injections

object CollectionDownload {
    const val CONTENT = """
        (function(){
            if (window.__splColDlInit) return;
            window.__splColDlInit = true;
        
            var SVG_SKIP = '<svg viewBox="0 0 16 16" width="24" height="24"><path fill="currentColor" d="M3.3 1a.7.7 0 0 1 .7.7v5.15l9.95-5.744a.7.7 0 0 1 1.05.606v12.575a.7.7 0 0 1-1.05.607L4 9.149V14.3a.7.7 0 0 1-.7.7H1.7a.7.7 0 0 1-.7-.7V1.7a.7.7 0 0 1 .7-.7h1.6z"/></svg>';
            var SVG_X = '<svg viewBox="0 0 16 16" width="24" height="24"><path fill="currentColor" d="M2.47 2.47a.75.75 0 0 1 1.06 0L8 6.94l4.47-4.47a.75.75 0 1 1 1.06 1.06L9.06 8l4.47 4.47a.75.75 0 1 1-1.06 1.06L8 9.06l-4.47 4.47a.75.75 0 0 1-1.06-1.06L6.94 8 2.47 3.53a.75.75 0 0 1 0-1.06z"/></svg>';
        
            var DL_LABELS = ['download','unduh','télécharger','descargar','herunterladen','scaricare','baixar','pobierz','загрузить','ダウンロード','다운로드','下载','下載'];
            var CIRCLE_HEAD = 'M12 3a9 9 0';
            var ARROW_HEAD = 'M12 6.05';
        
            var ROW_SEL = 'div[data-testid="tracklist-row"]';
            var RECO_SEL = '.playlistRecommenderContainer, [data-testid="recommended-track"]';
            var BAR_SEL = 'div[data-testid="action-bar-row"]';
            var COVER_RE = /ab67616d0000[0-9a-f]{4}/i;
        
            function pageType(){
                var p = location.pathname;
                if (p.indexOf('/playlist/') !== -1) return 'playlist';
                if (p.indexOf('/album/') !== -1) return 'album';
                if (p.indexOf('/collection/tracks') !== -1) return 'liked';
                return null;
            }
        
            function inRecommendations(el){
                try { return !!(el.closest && el.closest(RECO_SEL)); } catch(e){ return false; }
            }
        
            function mainGrid(){
                var g = document.querySelector('div[data-testid="playlist-tracklist"]');
                if (g && !inRecommendations(g)) return g;
                var grids = document.querySelectorAll('div[role="grid"][aria-rowcount]');
                for (var i = 0; i < grids.length; i++) {
                    if (inRecommendations(grids[i])) continue;
                    var rc = parseInt(grids[i].getAttribute('aria-rowcount') || '0', 10);
                    if (rc > 0 && grids[i].querySelector(ROW_SEL)) {
                        return grids[i];
                    }
                }
                return null;
            }
        
            function scrollable(el){
                if (!el) return false;
                var oy = getComputedStyle(el).overflowY;
                if (oy !== 'auto' && oy !== 'scroll' && oy !== 'overlay') return false;
                return el.scrollHeight > el.clientHeight + 100;
            }
        
            function findScroller(from){
                var el = from;
                while (el && el !== document.body) {
                    if (scrollable(el)) return el;
                    el = el.parentElement;
                }
                var se = document.scrollingElement;
                return scrollable(se) ? se : null;
            }
        
            function upCover(url){
                if (!url) return '';
                return url.replace(COVER_RE, 'ab67616d0000b273');
            }
        
            function findCover(){
                var c = '';
                try {
                    var og = document.querySelector('meta[property="og:image"]');
                    if (og) c = og.getAttribute('content') || '';
                } catch(e){}
                if (!c) {
                    var els = document.querySelectorAll(
                        'div[data-testid="cover-art-image"],' +
                        'div[data-testid="entity-image"],' +
                        'div[data-testid="entity-image"] img,' +
                        'section[data-testid="album-page"] img,' +
                        'section[data-testid="playlist-page"] img'
                    );
                    for (var i = 0; i < els.length; i++) {
                        if (els[i].src) { c = els[i].src; break; }
                    }
                }
                return upCover(c);
            }
        
            function headerName(fallback){
                var h1 = document.querySelector('main h1');
                var n = (h1 && h1.textContent || '').trim();
                return n || fallback;
            }
        
            function yearInMetaRow(root){
                var divs = root.querySelectorAll('div');
                for (var i = 0; i < divs.length; i++) {
                    var kids = divs[i].children;
                    var hasBullet = false, year = 0;
                    for (var j = 0; j < kids.length; j++) {
                        if (kids[j].children.length !== 0) continue;
                        var t = (kids[j].textContent || '').trim();
                        if (t === '\u2022') hasBullet = true;
                        else if (/^(19|20)[0-9]{2}$/.test(t)) year = parseInt(t, 10);
                    }
                    if (hasBullet && year) return year;
                }
                return 0;
            }
        
            function albumYear(){
                try {
                    var h1 = document.querySelector('main h1');
                    if (!h1) return 0;
                    var node = h1;
                    for (var up = 0; up < 4 && node; up++) {
                        node = node.parentElement;
                        if (!node) break;
                        var y = yearInMetaRow(node);
                        if (y) return y;
                    }
                } catch(e){}
                return 0;
            }
        
            function rowTrackNum(row){
                var cell = row.querySelector('[role="gridcell"][aria-colindex="1"]');
                if (!cell) return 0;
                var sp = cell.querySelector('span');
                var t = sp ? (sp.textContent || '').trim() : (cell.textContent || '').trim();
                return /^[0-9]+$/.test(t) ? parseInt(t, 10) : 0;
            }
        
            var META_CACHE = {};
        
            function b62ToHex(id){
                var A = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
                var n = 0n;
                for (var i = 0; i < id.length; i++) {
                    var v = A.indexOf(id.charAt(i));
                    if (v < 0) return null;
                    n = n * 62n + BigInt(v);
                }
                var h = n.toString(16);
                while (h.length < 32) h = '0' + h;
                return h;
            }
        
            function fetchTrackMeta(id){
                if (META_CACHE[id]) return Promise.resolve(META_CACHE[id]);
                var auth = window.spotAuthToken;
                var hex = b62ToHex(id);
                if (!auth || !hex) return Promise.resolve(null);
                var f = window.oriFetch || window.fetch;
                var url = 'https://spclient.wg.spotify.com/metadata/4/track/' + hex + '?market=from_token';
                var headers = { 'Authorization': auth, 'app-platform': 'WebPlayer', 'accept': 'application/json' };
                if (window.spotCliToken) headers['client-token'] = window.spotCliToken;
                return f(url, { headers: headers })
                    .then(function(r){
                        var ct = r.headers.get('content-type') || '';
                        if (!r.ok || ct.indexOf('json') === -1) return null;
                        return r.json();
                    })
                    .then(function(j){
                        if (!j) return null;
                        var ad = (j.album && j.album.date) ? j.album.date : null;
                        var m = {
                            trackNumber: (typeof j.number === 'number' && j.number > 0) ? j.number : 0,
                            discNumber: (typeof j.disc_number === 'number' && j.disc_number > 0) ? j.disc_number : 0,
                            year: (ad && ad.year) ? ad.year : 0
                        };
                        META_CACHE[id] = m;
                        return m;
                    })
                    .catch(function(){ return null; });
            }
        
            function enrichTracks(tracks, cb){
                var total = tracks.length;
                if (!total) { cb(); return; }
                var idx = 0, done = 0, active = 0, failed = 0, stopped = false;
                var CONC = 4;
                var finish = function(){ if (!stopped) { stopped = true; cb(); } };
                var pump = function(){
                    while (!stopped && active < CONC && idx < total) {
                        let track = tracks[idx++];
                        active++;
                        fetchTrackMeta(track.trackId).then(function(m){
                            if (m) {
                                failed = 0;
                                if (m.trackNumber) track.trackNumber = m.trackNumber;
                                if (m.discNumber) track.discNumber = m.discNumber;
                                if (m.year) track.year = m.year;
                            } else {
                                failed++;
                            }
                        }).catch(function(){ failed++; }).then(function(){
                            active--; done++;
                            if (done % 10 === 0 || done === total) {
                                try { window.splDownloadProgress(0, 'Reading metadata... ' + done + '/' + total); } catch(e){}
                            }
                            if (done >= total || failed >= 5) { finish(); return; }
                            pump();
                        });
                    }
                };
                pump();
            }
        
            function loadAll(albumFallback, pageYear, cb){
                var tl = mainGrid();
                if (!tl) { cb([]); return; }
                var sc = findScroller(tl);
                var top = sc ? sc.scrollTop : 0;
                var target = parseInt(tl.getAttribute('aria-rowcount') || '0', 10) || 0;
                var seen = {}, out = [];
                var step = sc ? Math.max(240, Math.round(sc.clientHeight * 0.5)) : 0;
                var t0 = Date.now();
                var iter = 0, last = -1, stall = 0;
                if (sc) { try { sc.scrollTop = 0; } catch(e){} }
                var finish = function(){
                    clearInterval(iv);
                    if (sc) { try { sc.scrollTop = top; } catch(e){} }
                    setTimeout(function(){ cb(out); }, 300);
                };
                var iv = setInterval(function(){
                    iter++;
                    scrapeInto(albumFallback, pageYear, tl, seen, out);
                    var pending = sc ? (sc.scrollTop < sc.scrollHeight - sc.clientHeight - 2) : false;
                    if (out.length === last) stall++; else stall = 0;
                    last = out.length;
                    if ((target > 0 && out.length >= target - 1) || (!pending && stall >= 3)) { finish(); return; }
                    if (iter >= 1200 || (Date.now() - t0) > 120000) { finish(); return; }
                    if (iter % 8 === 0) {
                        try {
                            if (typeof window.splDownloadProgress === 'function') {
                                window.splDownloadProgress(0, 'Loading tracklist... ' + out.length + (target > 0 ? '/' + (target - 1) : ''));
                            }
                        } catch(e2){}
                    }
                    if (sc) { try { sc.scrollTop = Math.min(sc.scrollTop + step, sc.scrollHeight); } catch(e){} }
                }, 200);
            }
        
            function scrapeInto(albumFallback, pageYear, root, seen, out){
                var rows = root.querySelectorAll(ROW_SEL);
                var checkReco = !!root.querySelector(RECO_SEL);
                for (var i = 0; i < rows.length; i++) {
                    var row = rows[i];
                    if (checkReco && inRecommendations(row)) continue;
                    var link = row.querySelector('a[data-testid="internal-track-link"]');
                    if (!link) continue;
                    var href = link.getAttribute('href') || '';
                    var id = href.split('/track/')[1];
                    if (id) id = id.split('?')[0].split('#')[0];
                    if (!id || seen[id]) continue;
                    seen[id] = true;
                    var title = (link.textContent || '').trim();
                    var arts = [];
                    var links = row.querySelectorAll('a[href*="/artist/"]');
                    for (var j = 0; j < links.length; j++) {
                        var t = (links[j].textContent || '').trim();
                        if (t) arts.push(t);
                    }
                    var al = row.querySelector('a[href*="/album/"]');
                    var album = albumFallback || (al ? (al.textContent || '').trim() : '');
                    var img = row.querySelector('img');
                    var cover = (img && img.src) ? upCover(img.src) : '';
                    var mm = (window.__splTrackMeta && window.__splTrackMeta[id]) || {};
                    out.push({
                        trackId: id,
                        title: title,
                        artist: arts.join(', '),
                        album: album,
                        cover: cover,
                        trackNumber: mm.trackNumber || rowTrackNum(row) || 0,
                        discNumber: mm.discNumber || 0,
                        year: mm.year || pageYear || 0
                    });
                }
            }
        
            function busy(){
                return !!window.__splColBusy;
            }
        
            function onNativeDownload(e){
                e.preventDefault();
                e.stopPropagation();
                if (busy()) return;
                window.__splColBusy = true;
                var btn = e.currentTarget;
                if (btn) btn.classList.add('spl-ab-busy');
                var type = pageType();
                if (!type) { window.__splColBusy = false; if (btn) btn.classList.remove('spl-ab-busy'); return; }
                var name = headerName(type === 'liked' ? window.splLikedName() : 'Collection');
                var albumFallback = (type === 'album') ? name : '';
                try {
                    if (typeof window.splDownloadProgress === 'function') {
                        window.splDownloadProgress(0, 'Loading tracklist...');
                    }
                } catch(e2){}
                loadAll(albumFallback, albumYear(), function(tracks){
                    if (!tracks.length) {
                        window.__splColBusy = false;
                        if (btn) btn.classList.remove('spl-ab-busy');
                        try { AndBridge.deferMessage('No downloadable tracks found'); } catch(e3){}
                        return;
                    }
                    var send = function(){
                        window.__splColBusy = false;
                        if (btn) btn.classList.remove('spl-ab-busy');
                        var payload = { type: type, name: name, cover: findCover(), tracks: tracks };
                        try {
                            AndBridge.downloadCollection(JSON.stringify(payload));
                        } catch(e4) {
                            AndBridge.deferMessage('Download failed');
                        }
                    };
                    enrichTracks(tracks, send);
                });
            }
        
            function isDownloadButton(b){
                if (b.hasAttribute('aria-haspopup')) return false;
                var al = (b.getAttribute('aria-label') || '').trim().toLowerCase();
                if (al && DL_LABELS.indexOf(al) !== -1) return true;
                var paths = b.querySelectorAll('svg path');
                if (paths.length >= 2){
                    var d1 = paths[0].getAttribute('d') || '';
                    var d2 = paths[1].getAttribute('d') || '';
                    if (d1.indexOf(CIRCLE_HEAD) === 0 && d2.indexOf(ARROW_HEAD) === 0) return true;
                }
                return false;
            }
        
            function hijack(){
                if(!pageType()) return;
                if (window.__splBg) return;
                var bars = document.querySelectorAll(BAR_SEL);
                for (var i = 0; i < bars.length; i++) {
                    var btns = bars[i].querySelectorAll('button');
                    for (var j = 0; j < btns.length; j++) {
                        var b = btns[j];
                        if (b.__splDlHijack) continue;
                        if (!isDownloadButton(b)) continue;
                        b.__splDlHijack = true;
                        b.addEventListener('click', onNativeDownload, true);
                    }
                }
            }
        
            var hijackPending = false;
            var obs = new MutationObserver(function(muts){
                if (window.__splBg) return;
                var dirty = false;
                for (var i = 0; i < muts.length && !dirty; i++) {
                    if (muts[i].addedNodes.length > 0) dirty = true;
                }
                if (dirty && !hijackPending) {
                    hijackPending = true;
                    setTimeout(function(){ hijackPending = false; hijack(); }, 150);
                }
            });
            function startObserver(){
                try { obs.observe(document.body, { childList: true, subtree: true }); } catch(e){}
            }
            if (document.body) startObserver();
            else document.addEventListener('DOMContentLoaded', startObserver);
        
            function makeBtn(id, label, svg, handler){
                var b = document.createElement('button');
                b.id = id;
                b.type = 'button';
                b.title = label;
                b.setAttribute('aria-label', label);
                b.innerHTML = svg;
                b.style.display = 'none';
                b.addEventListener('click', function(e){
                    e.stopPropagation();
                    try { handler(); } catch(err){}
                });
                return b;
            }
        
            function ensureButtons(){
                var type = pageType();
                if (!type) return;
                var bar = document.querySelector(BAR_SEL);
                if (!bar) return;
                if (type === 'playlist' && !document.getElementById('spl-dl-skip-btn')) {
                    bar.appendChild(makeBtn('spl-dl-skip-btn', 'Skip current download', SVG_SKIP, function(){
                        AndBridge.skipDownload();
                    }));
                }
                if (!document.getElementById('spl-dl-cancel-btn')) {
                    bar.appendChild(makeBtn('spl-dl-cancel-btn', 'Cancel download', SVG_X, function(){
                        AndBridge.cancelDownload();
                    }));
                }
            }
        
            function syncButtons(){
                var act = !!window.__splDlActive;
                var batch = act && !!window.__splDlBatch;
                var sk = document.getElementById('spl-dl-skip-btn');
                var ca = document.getElementById('spl-dl-cancel-btn');
                if (sk) sk.style.display = batch ? 'inline-flex' : 'none';
                if (ca) ca.style.display = act ? 'inline-flex' : 'none';
            }
        
            var st = document.createElement('style');
            st.id = 'spl-dlall-style';
            st.textContent = [
                '#spl-dl-skip-btn,#spl-dl-cancel-btn{display:inline-flex;align-items:center;justify-content:center;width:48px;height:48px;background:transparent;border:none;border-radius:50%;color:#b3b3b3;cursor:pointer;padding:0;margin:0 0 0 4px;flex-shrink:0;-webkit-tap-highlight-color:transparent;transition:color .2s,transform .1s}',
                '#spl-dl-skip-btn:hover{color:#fff;transform:scale(1.05)}',
                '#spl-dl-cancel-btn:hover{color:#e57373;transform:scale(1.05)}',
                '#spl-dl-skip-btn:active,#spl-dl-cancel-btn:active{transform:scale(.94)}',
                '#spl-dl-skip-btn svg,#spl-dl-cancel-btn svg{width:26px;height:26px;pointer-events:none}',
                'button.spl-ab-busy{pointer-events:none;animation:splDlAllPulse 1.2s ease-in-out infinite}',
                '@keyframes splDlAllPulse{0%,100%{opacity:.5}50%{opacity:1}}'
            ].join('');
            function appendStyle(){
                var t = document.head || document.documentElement;
                if (t && !document.getElementById('spl-dlall-style')) t.appendChild(st);
            }
            try { appendStyle(); } catch(e){}
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', appendStyle);
            }
        
            function tick(){
                if (window.__splBg || window.__splBusy) return;
                hijack();
                ensureButtons();
                syncButtons();
            }
            setInterval(tick, 2000);
            tick();
        })();
    """
}