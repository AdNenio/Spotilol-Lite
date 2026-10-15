package com.project.lol.webview.injections

object PlaylistLookupSheet {
    const val CONTENT = """
        (function(){
            if (window.__splPlSheet) return;
            window.__splPlSheet = true;

            var OPEN_MS = 450;
            var CHECK_CONC = 4;
            var TTL = 600000;
            var LIB_TTL = 300000;
            var PAGE_LIMIT = 100;
            var MAX_PAGES = 10;
            var STYLE_ID = 'spl-plsheet-style';
            var FB_HASH = '346811f856fb0b7e4f6c59f8ebea78dd081c6e2fb01b77c954b26259d5fc6763';

            var sheet = null, card = null, listEl = null, filterEl = null, covEl = null, tiEl = null, arEl = null;
            var cur = null;
            var plCache = null, plCacheAt = 0;
            var contains = {};
            var token = 0;
            var prevClose = null;
            var suppress = { btn: null, until: 0 };
            var rowByPid = {};
            var active = 0, inflight = {}, pumpAll = function(){};

            var ICON_HEART = '<svg viewBox="0 0 16 16"><path fill="currentColor" d="M15.724 4.22A4.313 4.313 0 0 0 12.192.814a4.269 4.269 0 0 0-3.622 1.13.837.837 0 0 1-1.14 0 4.272 4.272 0 0 0-6.38 5.69l5.4 6.06a1.09 1.09 0 0 0 1.504.06l5.397-5.892a4.32 4.32 0 0 0 1.253-3.436z"/></svg>';
            var ICON_CHECK = '<svg viewBox="0 0 16 16"><path fill="currentColor" d="M0 8a8 8 0 1 1 16 0A8 8 0 0 1 0 8m11.748-1.97a.75.75 0 0 0-1.06-1.06l-4.47 4.47-1.405-1.406a.75.75 0 1 0-1.061 1.06l2.466 2.467 5.53-5.53z"/></svg>';
            var ICON_X = '<svg viewBox="0 0 16 16"><path fill="currentColor" d="M2.47 2.47a.75.75 0 0 1 1.06 0L8 6.94l4.47-4.47a.75.75 0 1 1 1.06 1.06L9.06 8l4.47 4.47a.75.75 0 1 1-1.06 1.06L8 9.06l-4.47 4.47a.75.75 0 0 1-1.06-1.06L6.94 8 2.47 3.53a.75.75 0 0 1 0-1.06z"/></svg>';
            var ICON_MUSIC = '<svg viewBox="0 0 16 16"><path fill="currentColor" d="M13.426 2.574a2.831 2.831 0 0 0-4.797 1.55l3.247 3.247a2.831 2.831 0 0 0 1.55-4.797M10.5 8.118l-2.619-2.62L4.74 9.075 2.065 12.12a1.287 1.287 0 0 0 1.816 1.816l3.06-2.688 3.56-3.129zM7.12 4.094a4.331 4.331 0 1 1 4.786 4.786l-3.974 3.493-3.06 2.688a2.787 2.787 0 0 1-3.933-3.933l2.676-3.045z"/></svg>';
            var ICON_SEARCH = '<svg viewBox="0 0 24 24"><path fill="currentColor" d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>';

            function el(tag, cls){ var d = document.createElement(tag); if (cls) d.className = cls; return d; }
            function toast(msg){ try { AndBridge.deferMessage(msg); } catch(e){} }

            function css(){
                if (document.getElementById(STYLE_ID)) return;
                var s = document.createElement('style');
                s.id = STYLE_ID;
                s.textContent = [
                    '#spl-plsheet{position:fixed;inset:0;z-index:2147483647;display:flex;align-items:flex-end;justify-content:center;background:rgba(0,0,0,.55);opacity:0;transition:opacity .2s;font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,Helvetica,Arial,sans-serif}',
                    '#spl-plsheet.spl-open{opacity:1}',
                    '#spl-plsheet .spl-pl-card{width:min(560px,100%);max-height:76vh;display:flex;flex-direction:column;background:#181818;border:1px solid rgba(255,255,255,.08);border-bottom:none;border-radius:18px 18px 0 0;box-shadow:0 -12px 40px rgba(0,0,0,.6);transform:translateY(100%);transition:transform .28s cubic-bezier(.2,.8,.2,1);overscroll-behavior:contain}',
                    '#spl-plsheet.spl-open .spl-pl-card{transform:translateY(0)}',
                    '#spl-plsheet .spl-pl-drag{flex:0 0 auto;display:flex;align-items:center;justify-content:center;height:18px;cursor:grab;touch-action:none}',
                    '#spl-plsheet .spl-pl-drag::before{content:"";width:36px;height:4px;border-radius:2px;background:rgba(255,255,255,.28)}',
                    '#spl-plsheet .spl-pl-head{display:flex;align-items:center;gap:12px;padding:2px 16px 12px;border-bottom:1px solid rgba(255,255,255,.09)}',
                    '#spl-plsheet .spl-pl-cover{width:48px;height:48px;border-radius:6px;object-fit:cover;flex-shrink:0;background:#282828}',
                    '#spl-plsheet .spl-pl-meta{flex:1;min-width:0}',
                    '#spl-plsheet .spl-pl-title{font-size:15px;font-weight:700;color:#fff;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}',
                    '#spl-plsheet .spl-pl-artist{font-size:12px;color:rgba(255,255,255,.6);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}',
                    '#spl-plsheet .spl-pl-close{flex-shrink:0;width:36px;height:36px;border:0;border-radius:50%;background:none;color:rgba(255,255,255,.6);display:flex;align-items:center;justify-content:center;cursor:pointer}',
                    '#spl-plsheet .spl-pl-close:hover{color:#fff;background:rgba(255,255,255,.1)}',
                    '#spl-plsheet .spl-pl-close svg{width:18px;height:18px}',
                    '#spl-plsheet .spl-pl-tools{flex:0 0 auto;padding:10px 16px 4px}',
                    '#spl-plsheet .spl-pl-search{display:flex;align-items:center;gap:8px;height:38px;padding:0 12px;border-radius:10px;background:rgba(255,255,255,.07);border:1px solid rgba(255,255,255,.1)}',
                    '#spl-plsheet .spl-pl-search svg{width:14px;height:14px;color:rgba(255,255,255,.5);flex-shrink:0}',
                    '#spl-plsheet .spl-pl-search input{flex:1;min-width:0;background:none;border:none;outline:none;color:#fff;font-size:13px;height:100%}',
                    '#spl-plsheet .spl-pl-search input::placeholder{color:rgba(255,255,255,.4)}',
                    '#spl-plsheet .spl-pl-list{overflow-y:auto;-webkit-overflow-scrolling:touch;padding:6px 8px calc(16px + env(safe-area-inset-bottom,0px))}',
                    '#spl-plsheet .spl-pl-list::-webkit-scrollbar{width:4px}',
                    '#spl-plsheet .spl-pl-list::-webkit-scrollbar-thumb{background:rgba(255,255,255,.2);border-radius:2px}',
                    '#spl-plsheet .spl-pl-hint{padding:0 12px 8px;font-size:11px;color:rgba(255,255,255,.4)}',
                    '#spl-plsheet .spl-pl-row{display:flex;align-items:center;gap:10px;padding:9px 10px;border-radius:10px;cursor:pointer;min-height:52px}',
                    '#spl-plsheet .spl-pl-row:hover{background:rgba(255,255,255,.07)}',
                    '#spl-plsheet .spl-pl-row:active{background:rgba(255,255,255,.12)}',
                    '#spl-plsheet .spl-pl-ico{width:40px;height:40px;border-radius:6px;flex-shrink:0;background:#282828;display:flex;align-items:center;justify-content:center;color:#b3b3b3;overflow:hidden}',
                    '#spl-plsheet .spl-pl-ico img{width:100%;height:100%;object-fit:cover}',
                    '#spl-plsheet .spl-pl-ico svg{width:20px;height:20px}',
                    '#spl-plsheet .spl-pl-name{flex:1;min-width:0;font-size:14px;font-weight:600;color:#fff;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}',
                    '#spl-plsheet .spl-pl-mark{flex-shrink:0;width:26px;height:26px;display:flex;align-items:center;justify-content:center;color:rgba(255,255,255,.35)}',
                    '#spl-plsheet .spl-pl-mark svg{width:18px;height:18px}',
                    '#spl-plsheet .spl-pl-row.spl-in .spl-pl-mark{color:var(--spl-accent,#1DB954)}',
                    '#spl-plsheet .spl-pl-row.spl-busy .spl-pl-mark{opacity:.4}',
                    '#spl-plsheet .spl-pl-empty{padding:26px 16px;text-align:center;font-size:13px;color:rgba(255,255,255,.5)}',
                    '#spotilolPlayerControls .spl-liked-btn.spl-has-pl::after{content:"";position:absolute;top:7px;right:7px;width:5px;height:5px;border-radius:50%;background:var(--spl-accent,#1DB954);box-shadow:0 0 0 2px rgba(0,0,0,.55)}',
                    'div[data-testid=now-playing-widget]>div:last-child>button.spl-has-pl{position:relative}',
                    'div[data-testid=now-playing-widget]>div:last-child>button.spl-has-pl::after{content:"";position:absolute;top:7px;right:7px;width:5px;height:5px;border-radius:50%;background:var(--spl-accent,#1DB954);box-shadow:0 0 0 2px rgba(0,0,0,.55)}'
                ].join('\n');
                (document.head || document.documentElement).appendChild(s);
            }

            function trackNow(){
                var id = (typeof window.splPlayingTrackId === 'function') ? window.splPlayingTrackId() : (window.__curTrackId || null);
                if (!id) return null;
                var m = (window.__splTrackMeta && window.__splTrackMeta[id]) || {};
                var title = m.name || window.__curTrackName || '';
                var artist = m.artist || window.__curTrackArtist || '';
                var cover = m.cover || window.__curTrackCover || '';
                if (!title) { var t = document.querySelector('a[data-testid=context-item-link]'); if (t) title = (t.textContent || '').trim(); }
                if (!artist) { var a = document.querySelector('a[data-testid=context-item-info-artist]'); if (a) artist = (a.textContent || '').trim(); }
                if (!cover) { var im = document.querySelector('img[data-testid=cover-art-image]'); if (im) cover = im.src; }
                return { id: id, uri: 'spotify:track:' + id, title: title || 'Unknown track', artist: artist || '', cover: cover || '' };
            }

            function likedState(){
                var fb = document.querySelector('div[data-testid=now-playing-widget]>div:last-child>button');
                return !!(fb && fb.getAttribute('aria-checked') === 'true');
            }

            function loadPlaylists(force){
                if (!force && plCache && (Date.now() - plCacheAt) < LIB_TTL) return Promise.resolve(plCache);
                var pre = (typeof window.ensureAuthToken === 'function') ? window.ensureAuthToken() : Promise.resolve(true);
                return Promise.resolve(pre).then(function(){
                    if (typeof window.fetchAllLibrary !== 'function' || typeof window.parseLibrary !== 'function') return [];
                    return window.fetchAllLibrary().then(function(items){
                        var lib = window.parseLibrary(items) || {};
                        var pls = (lib.playlists || []).filter(function(p){
                            return p && p.name && p.id && String(p.id).indexOf('spotify:playlist:') === 0;
                        });
                        plCache = pls;
                        plCacheAt = Date.now();
                        return pls;
                    });
                }).catch(function(){ return plCache || []; });
            }

            function keyOf(tid, plid){ return tid + '|' + plid; }
            function cachedIn(tid, plid){
                var e = contains[keyOf(tid, plid)];
                return (e && (Date.now() - e.t) < TTL) ? e.v : undefined;
            }
            function putIn(tid, plid, v){ contains[keyOf(tid, plid)] = { v: !!v, t: Date.now() }; }
            function plId(p){ return String(p.id || '').split(':').pop(); }

            function gqlPage(plUri, offset){
                var f = window.mngFetch || window.oriFetch || window.fetch;
                var hash = FB_HASH;
                try {
                    if (typeof window.getGqlHash === 'function') hash = window.getGqlHash('fetchPlaylist', FB_HASH);
                } catch(e){}
                return f('https://api-partner.spotify.com/pathfinder/v2/query', {
                    method: 'POST',
                    headers: {
                        'Authorization': window.spotAuthToken || '',
                        'Client-Token': window.spotCliToken || '',
                        'Content-Type': 'application/json;charset=UTF-8',
                        'app-platform': 'WebPlayer',
                        'spotify-app-version': '1.2.40.0'
                    },
                    body: JSON.stringify({
                        variables: { uri: plUri, offset: offset, limit: PAGE_LIMIT, enableWatchFeedEntrypoint: false },
                        operationName: 'fetchPlaylist',
                        extensions: { persistedQuery: { version: 1, sha256Hash: hash } }
                    })
                }).then(function(r){
                    if (!r.ok) throw new Error('http ' + r.status);
                    return r.json();
                });
            }

            function checkContains(p, tid, trackUri){
                var pid = plId(p);
                var plUri = String(p.id || '');
                return new Promise(function(resolve, reject){
                    var offset = 0, page = 0;
                    (function next(){
                        page++;
                        gqlPage(plUri, offset).then(function(j){
                            var content = j && j.data && j.data.playlistV2 && j.data.playlistV2.content;
                            var items = (content && content.items) || [];
                            var total = (content && typeof content.totalCount === 'number') ? content.totalCount : -1;
                            for (var i = 0; i < items.length; i++) {
                                var e = items[i];
                                var iv = e.itemV2 || e.item;
                                var d = (iv && iv.data) || e.track || iv || e;
                                var u = (d && (d.uri || d._uri)) || e.uri || '';
                                if (u === trackUri) { putIn(tid, pid, true); resolve(true); return; }
                            }
                            offset += items.length;
                            if (!items.length || items.length < PAGE_LIMIT || (total >= 0 && offset >= total) || page >= MAX_PAGES) {
                                putIn(tid, pid, false);
                                resolve(false);
                                return;
                            }
                            next();
                        }).catch(reject);
                    })();
                });
            }

            function navTo(path){
                if (!path) return;
                try {
                    history.pushState({}, '', path);
                    window.dispatchEvent(new PopStateEvent('popstate', { state: null }));
                } catch(e){
                    window.location.href = path;
                }
            }

            function setBadge(on){
                var b1 = document.getElementById('spl-liked');
                if (b1) b1.classList.toggle('spl-has-pl', !!on);
                var b2 = document.querySelector('div[data-testid=now-playing-widget]>div:last-child>button');
                if (b2) b2.classList.toggle('spl-has-pl', !!on);
            }

            function updateBadge(){
                if (!cur || !plCache) { setBadge(false); return; }
                for (var i = 0; i < plCache.length; i++) {
                    if (cachedIn(cur.id, plId(plCache[i])) === true) { setBadge(true); return; }
                }
                setBadge(false);
            }

            function buildSheet(){
                if (sheet) return;
                css();
                sheet = el('div'); sheet.id = 'spl-plsheet';
                card = el('div', 'spl-pl-card');

                var drag = el('div', 'spl-pl-drag');
                card.appendChild(drag);
                attachDrag(drag);

                var head = el('div', 'spl-pl-head');
                covEl = document.createElement('img'); covEl.className = 'spl-pl-cover'; covEl.alt = '';
                var meta = el('div', 'spl-pl-meta');
                tiEl = el('div', 'spl-pl-title');
                arEl = el('div', 'spl-pl-artist');
                var x = el('button', 'spl-pl-close'); x.type = 'button'; x.setAttribute('aria-label','Close'); x.innerHTML = ICON_X;
                x.addEventListener('click', function(e){ e.stopPropagation(); closeSheet(); });
                head.appendChild(covEl); head.appendChild(meta); meta.appendChild(tiEl); meta.appendChild(arEl); head.appendChild(x);
                card.appendChild(head);

                var tools = el('div', 'spl-pl-tools');
                var search = el('div', 'spl-pl-search');
                search.innerHTML = ICON_SEARCH;
                filterEl = document.createElement('input');
                filterEl.type = 'text'; filterEl.placeholder = 'Filter playlists';
                filterEl.setAttribute('spellcheck','false'); filterEl.autocomplete = 'off';
                var fT = 0;
                filterEl.addEventListener('input', function(){
                    clearTimeout(fT);
                    fT = setTimeout(function(){
                        if (!cur) return;
                        renderList();
                        startChecks();
                    }, 120);
                });
                search.appendChild(filterEl);
                tools.appendChild(search);
                card.appendChild(tools);

                listEl = el('div', 'spl-pl-list');
                card.appendChild(listEl);

                sheet.appendChild(card);
                sheet.addEventListener('click', function(e){ if (e.target === sheet) closeSheet(); });
                document.body.appendChild(sheet);
                sheet.style.display = 'none';
            }

            function attachDrag(handle){
                var sy = 0, dy = 0, drag = false;
                handle.addEventListener('pointerdown', function(e){
                    drag = true; sy = e.clientY; dy = 0;
                    card.style.transition = 'none';
                    try { handle.setPointerCapture(e.pointerId); } catch(err){}
                    e.preventDefault();
                });
                handle.addEventListener('pointermove', function(e){
                    if (!drag) return;
                    dy = Math.max(0, e.clientY - sy);
                    card.style.transform = 'translateY(' + dy + 'px)';
                    sheet.style.background = 'rgba(0,0,0,' + Math.max(.2, .55 - dy / 800) + ')';
                });
                function end(){
                    if (!drag) return;
                    drag = false;
                    card.style.transition = '';
                    sheet.style.background = '';
                    if (dy > 90) closeSheet();
                    else card.style.transform = '';
                    dy = 0;
                }
                handle.addEventListener('pointerup', end);
                handle.addEventListener('pointercancel', end);
            }

            function openSheet(){
                cur = trackNow();
                if (!cur || !cur.id) { toast('Track not ready'); return; }
                if (!window.spotAuthToken) {
                    if (typeof window.ensureAuthToken === 'function') {
                        window.ensureAuthToken().then(function(ok){
                            if (ok || window.spotAuthToken) openSheet();
                            else toast('Log in to Spotify first');
                        });
                        return;
                    }
                    toast('Log in to Spotify first');
                    return;
                }
                token++;
                buildSheet();
                if (covEl.getAttribute('src') !== cur.cover) { covEl.src = cur.cover; covEl.style.visibility = ''; }
                covEl.onerror = function(){ covEl.style.visibility = 'hidden'; };
                tiEl.textContent = cur.title;
                arEl.textContent = cur.artist;
                filterEl.value = '';
                sheet.style.display = 'flex';
                requestAnimationFrame(function(){ requestAnimationFrame(function(){ sheet.classList.add('spl-open'); }); });
                renderList();
                try { AndBridge.ctxMenuState(true); } catch(e){}
                prevClose = window.__splCloseCtxSheet;
                window.__splCloseCtxSheet = closeSheet;
                loadPlaylists(false).then(function(){
                    if (!cur) return;
                    renderList();
                    startChecks();
                });
            }

            function closeSheet(){
                if (!sheet) return;
                sheet.classList.remove('spl-open');
                try { AndBridge.ctxMenuState(false); } catch(e){}
                if (window.__splCloseCtxSheet === closeSheet) window.__splCloseCtxSheet = prevClose;
                prevClose = null;
                cur = null;
                token++;
                setTimeout(function(){
                    if (sheet && !sheet.classList.contains('spl-open')) sheet.style.display = 'none';
                    if (card) card.style.transform = '';
                }, 320);
            }
            window.__splClosePlSheet = closeSheet;

            function emptyRow(t){ var d = el('div','spl-pl-empty'); d.textContent = t; return d; }

            function renderList(){
                if (!listEl) return;
                listEl.innerHTML = '';
                rowByPid = {};
                var pls = plCache || [];
                if (!pls.length) { listEl.appendChild(emptyRow(plCache ? 'No playlists found' : 'Loading playlists…')); return; }

                var raw = (filterEl.value || '').trim();
                var q = raw.toLowerCase();
                var shown = q ? pls.filter(function(p){ return (p.name || '').toLowerCase().indexOf(q) !== -1; }) : pls;

                var frag = document.createDocumentFragment();
                frag.appendChild(likedRow());
                var hint = el('div','spl-pl-hint');
                hint.textContent = 'A check means the track is already in the playlist · tap to open it';
                frag.appendChild(hint);

                if (!shown.length) { frag.appendChild(emptyRow('No playlists match "' + raw + '"')); listEl.appendChild(frag); return; }
                for (var i = 0; i < shown.length; i++) {
                    var row = plRow(shown[i]);
                    rowByPid[row.__splPid] = row;
                    frag.appendChild(row);
                }
                listEl.appendChild(frag);
            }

            function likedRow(){
                var r = el('div','spl-pl-row');
                var ico = el('div','spl-pl-ico'); ico.innerHTML = ICON_HEART; ico.style.color = 'var(--spl-accent,#1DB954)';
                var nm = el('div','spl-pl-name'); nm.textContent = (typeof window.splLikedName === 'function') ? window.splLikedName() : 'Liked Songs';
                var mk = el('div','spl-pl-mark');
                r.appendChild(ico); r.appendChild(nm); r.appendChild(mk);
                function paint(){
                    var on = likedState();
                    mk.innerHTML = on ? ICON_CHECK : '';
                    mk.style.color = on ? 'var(--spl-accent,#1DB954)' : '';
                }
                paint();
                r.addEventListener('click', function(e){
                    e.stopPropagation();
                    if (typeof window.actAddToFav === 'function') window.actAddToFav();
                    mk.innerHTML = '';
                    setTimeout(paint, 900);
                });
                return r;
            }

            function plRow(p){
                var pid = plId(p);
                var r = el('div','spl-pl-row');
                var ico = el('div','spl-pl-ico');
                if (p.image) { var im = document.createElement('img'); im.src = p.image; im.alt = ''; ico.appendChild(im); }
                else ico.innerHTML = ICON_MUSIC;
                var nm = el('div','spl-pl-name'); nm.textContent = p.name;
                var mk = el('div','spl-pl-mark');
                r.appendChild(ico); r.appendChild(nm); r.appendChild(mk);
                r.__splPid = pid;
                r.__splTid = cur ? cur.id : null;

                function paint(v){
                    if (v === undefined) { r.classList.remove('spl-in'); r.classList.add('spl-busy'); mk.innerHTML = ''; }
                    else { r.classList.remove('spl-busy'); r.classList.toggle('spl-in', !!v); mk.innerHTML = v ? ICON_CHECK : ''; }
                }
                r.__splPaint = paint;
                paint(cur ? cachedIn(cur.id, pid) : undefined);

                r.addEventListener('click', function(e){
                    e.stopPropagation();
                    if (!pid) return;
                    closeSheet();
                    navTo('/playlist/' + pid);
                });
                return r;
            }

            function startChecks(){
                if (!cur || !plCache) return;
                var my = token, tid = cur.id, turi = cur.uri;
                var work = [];
                for (var i = 0; i < plCache.length; i++) {
                    var p = plCache[i], pid = plId(p);
                    if (cachedIn(tid, pid) === undefined && rowByPid[pid] && !inflight[keyOf(tid, pid)]) work.push(p);
                }
                var idx = 0;
                pumpAll = function(){
                    if (my !== token) return;
                    while (active < CHECK_CONC && idx < work.length) {
                        (function(p){
                            var pid = plId(p), key = keyOf(tid, pid);
                            inflight[key] = true;
                            active++;
                            checkContains(p, tid, turi).then(function(v){
                                var row = rowByPid[pid];
                                if (row && row.isConnected && row.__splTid === tid) row.__splPaint(v);
                                updateBadge();
                            }).catch(function(){}).then(function(){
                                delete inflight[key];
                                active--;
                                pumpAll();
                            });
                        })(work[idx++]);
                    }
                };
                pumpAll();
            }

            function wireLongPress(btn){
                if (!btn || btn.__splPlLp) return;
                btn.__splPlLp = true;
                var t = null, fired = false, sx = 0, sy = 0;
                function clear(){ if (t) { clearTimeout(t); t = null; } }
                btn.addEventListener('pointerdown', function(e){
                    if (e.pointerType === 'mouse' && e.button !== 0) return;
                    fired = false; sx = e.clientX; sy = e.clientY;
                    clear();
                    t = setTimeout(function(){
                        t = null; fired = true;
                        suppress = { btn: btn, until: Date.now() + 500 };
                        try { if (navigator.vibrate) navigator.vibrate(12); } catch(err){}
                        openSheet();
                    }, OPEN_MS);
                }, { passive: true });
                btn.addEventListener('pointermove', function(e){
                    if (!t) return;
                    if (Math.abs(e.clientX - sx) > 10 || Math.abs(e.clientY - sy) > 10) clear();
                }, { passive: true });
                btn.addEventListener('pointerup', clear, { passive: true });
                btn.addEventListener('pointercancel', clear, { passive: true });
                btn.addEventListener('contextmenu', function(e){ if (fired || t) e.preventDefault(); }, true);
            }

            document.addEventListener('click', function(e){
                if (Date.now() > suppress.until || !suppress.btn) return;
                if (suppress.btn.contains(e.target)) {
                    e.preventDefault();
                    e.stopPropagation();
                }
            }, true);

            document.addEventListener('keydown', function(e){
                if (e.key === 'Escape' && sheet && sheet.style.display !== 'none') {
                    e.stopPropagation();
                    closeSheet();
                }
            }, true);

            function wireButtons(){
                if (window.__splBg || window.__splBusy) return;
                wireLongPress(document.getElementById('spl-liked'));
                var q = window.splQ || document.querySelector.bind(document);
                wireLongPress(q('div[data-testid=now-playing-widget]>div:last-child>button'));
            }

            try {
                if (typeof window.splOnTrackChange === 'function') {
                    window.splOnTrackChange(function(){ setBadge(false); });
                }
            } catch(e){}

            setInterval(wireButtons, 2000);
            wireButtons();
        })();
    """
}