package com.project.lol.webview.injections

/*
 * CREDIT: Spotilol - Context Menu Download
 *
 * Adds a "Download" item to Spotify's track context menu (the "..."
 * button on a tracklist row, or right-click / long-press on the row).
 *
 * Track resolution (v3): the track is resolved AT INJECTION TIME from
 * the live menu trigger ([data-context-menu-open="true"] -> closest
 * tracklist row), NOT from a click-time capture. A pointerdown capture
 * on tracklist rows and the contextmenu listener remain as fallbacks
 * (TTL 30s) for menus opened without the trigger attribute.
 *
 * Menus that must NEVER receive the item (all structural, none depend
 * on click-time state):
 *   1. The profile / user-widget dropdown (inner user-widget-menu).
 *   2. Curation sheets (Add to playlist): detected via form /
 *      listRow / curation-sheet-list id.
 *   3. This menu with an expanded submenu trigger (Add to playlist /
 *      Share) - scoped to THIS menu only, so stale poppers elsewhere
 *      can never false-positive the check.
 *   4. Menus whose live trigger is not a tracklist row (column header
 *      sort menu, playlist header, user widget, ...).
 *
 * Active-menu detection walks the ancestor chain (display/visibility/
 * opacity) and prefers the NEWEST visible popper, so stale hidden
 * tippy poppers lingering in the DOM can't steal the injection.
 *
 * The item is built by cloning an existing plain menu entry at runtime
 * (hashed Encore classes survive Spotify deploys) and is inserted
 * BEFORE the first <li> - i.e. directly UNDER the track-info sheet
 * head injected by ContextMenuSheet, regardless of injection order.
 *
 * Clicking it hands the payload to AndBridge.downloadTrack, then
 * closes the menu with a synthesized Escape keydown (tippy-root detach
 * as last-resort fallback).
 */

object ContextMenuDownload {
    const val CONTENT = """
        (function(){
            if(window.__splCtxDlInit) return;
            window.__splCtxDlInit = true;
        
            var DL_ICON = 'M8 1a1 1 0 0 1 1 1v6.586l2.293-2.293a1 1 0 1 1 1.414 1.414l-4 4a1 1 0 0 1-1.414 0l-4-4a1 1 0 1 1 1.414-1.414L7 8.586V2a1 1 0 0 1 1-1zM2 13a1 1 0 0 1 1-1h10a1 1 0 1 1 0 2H3a1 1 0 0 1-1-1z';
            var TRACK_TTL = 30000;
        
            window.__splMenuTrack = null;
            window.__splMenuTrackAt = 0;

            function splUpCover(url){
                if(!url) return '';
                return url.replace(/ab67616d0000[0-9a-f]{4}/i, 'ab67616d0000b273');
            }
        
            function splRowOf(el){
                if(!el || !el.closest) return null;
                try{
                    return el.closest('div[data-testid="tracklist-row"]') || el.closest('[role="row"]');
                }catch(e){ return null; }
            }

            function splGrabRowTrack(row){
                if(!row) return null;
                var link = row.querySelector('a[data-testid="internal-track-link"]') ||
                           row.querySelector('a[href*="/track/"]');
                if(!link) return null;
                var m = (link.getAttribute('href')||'').match(/\/track\/([A-Za-z0-9]+)/);
                if(!m || !m[1]) return null;
                var artists = [];
                var arts = row.querySelectorAll('a[href*="/artist/"]');
                for(var i=0;i<arts.length;i++){
                    var nm = (arts[i].textContent||'').trim();
                    if(nm && artists.indexOf(nm) === -1) artists.push(nm);
                }
                var al = row.querySelector('a[href*="/album/"]');
                var img = row.querySelector('img');
                var cell = row.querySelector('[role="gridcell"][aria-colindex="1"]');
                var numText = cell ? (cell.textContent||'').trim() : '';
                var rowNum = /^[0-9]+$/.test(numText) ? parseInt(numText,10) : 0;
                var mm = (window.__splTrackMeta && window.__splTrackMeta[m[1]]) || {};
                return {
                    trackId: m[1],
                    title: (link.textContent||'').trim(),
                    artist: artists.join(', '),
                    album: al ? (al.textContent||'').trim() : '',
                    cover: img ? splUpCover(img.src||'') : '',
                    trackNumber: mm.trackNumber || rowNum || 0,
                    discNumber: mm.discNumber || 0,
                    year: mm.year || 0
                };
            }
        
            function splMenuVisible(el){
                try{
                    var node = el, hops = 0;
                    while(node && node !== document.body && hops++ < 8){
                        var cs = getComputedStyle(node);
                        if(cs.display === 'none' || cs.visibility === 'hidden') return false;
                        if(parseFloat(cs.opacity) < 0.02) return false;
                        node = node.parentElement;
                    }
                    var r = el.getBoundingClientRect();
                    return r.width >= 2 && r.height >= 2;
                }catch(e){ return false; }
            }
        
            function splActiveMenu(){
                var menus = document.querySelectorAll('[data-testid="context-menu"]');
                var found = null;
                for(var i=0;i<menus.length;i++){
                    if(!splMenuVisible(menus[i])) continue;
                    found = menus[i];
                }
                return found;
            }
        
            function splIsUserMenu(root){
                try{
                    return !!(root.querySelector && root.querySelector('[data-testid="user-widget-menu"]'));
                }catch(e){ return false; }
            }

            function splIsCurationSheet(root, ul){
                try{
                    if(ul.id === 'curation-sheet-list') return true;
                    if(root.querySelector('form')) return true;
                    if(ul.querySelector('[data-encore-id="listRow"]')) return true;
                }catch(e){}
                return false;
            }
        
            function splClearTrack(){
                window.__splMenuTrack = null;
                window.__splMenuTrackAt = 0;
            }
        
            function splDoMenuDownload(track){
                if(!track || !track.trackId){
                    try{ AndBridge.deferMessage('Track not ready'); }catch(e){}
                    return;
                }
                try{
                    AndBridge.downloadTrack(JSON.stringify({
                        trackId: track.trackId,
                        title: track.title || '',
                        artist: track.artist || '',
                        album: track.album || '',
                        cover: track.cover || '',
                        trackNumber: track.trackNumber || 0,
                        discNumber: track.discNumber || 0,
                        year: track.year || 0
                    }));
                }catch(e){}
            }
        
            function splCloseMenu(){
                try{
                    var root = splActiveMenu();
                    var ul = root ? root.querySelector('ul[role="menu"]') : null;
                    if(ul){
                        ul.dispatchEvent(new KeyboardEvent('keydown', {
                            key: 'Escape', code: 'Escape',
                            keyCode: 27, which: 27,
                            bubbles: true, cancelable: true
                        }));
                    }
                }catch(e){}
                setTimeout(function(){
                    try{
                        var m = splActiveMenu();
                        if(m){
                            var tr = m.closest('[data-tippy-root]') || m.parentElement;
                            if(tr && tr.parentElement) tr.remove();
                        }
                    }catch(e){}
                }, 250);
            }
        
            function splInjectItem(ul, track){
                var src = null;
                var lis = ul.querySelectorAll('li[role="presentation"]');
                for(var i=0;i<lis.length;i++){
                    var li = lis[i];
                    if(li.classList.contains('spl-menu-dl')) continue;
                    var b = li.querySelector('button[role="menuitem"]');
                    if(!b) continue;
                    if(b.hasAttribute('aria-expanded')) continue;
                    if(li.querySelector('img')) continue;
                    src = li;
                    break;
                }
                if(!src) return false;
        
                var clone = src.cloneNode(true);
                clone.classList.add('spl-menu-dl');
        
                var btn = clone.querySelector('button[role="menuitem"]');
                btn.removeAttribute('id');
                btn.setAttribute('aria-label', 'Download');
        
                var svg = clone.querySelector('svg[data-encore-id="icon"]');
                if(svg){
                    while(svg.firstChild) svg.removeChild(svg.firstChild);
                    var p = document.createElementNS('http://www.w3.org/2000/svg', 'path');
                    p.setAttribute('d', DL_ICON);
                    svg.appendChild(p);
                }
        
                var spans = clone.querySelectorAll('span[data-encore-id], p[data-encore-id]');
                if(spans.length){
                    spans[0].textContent = 'Download';
                } else {
                    var f = clone.querySelector('span');
                    if(f) f.textContent = 'Download';
                }
        
                btn.addEventListener('click', function(e){
                    e.preventDefault();
                    e.stopPropagation();
                    splDoMenuDownload(track);
                    splCloseMenu();
                }, true);
        
                var ref = null;
                var kids = ul.children;
                for(var k=0;k<kids.length;k++){
                    if(kids[k].tagName === 'LI'){ ref = kids[k]; break; }
                }
                if(ref) ul.insertBefore(clone, ref);
                else ul.appendChild(clone);
                return true;
            }
        
            function splLiveTrack(){
                var trig = null;
                try{ trig = document.querySelector('[data-context-menu-open="true"]'); }catch(e){}
                if(!trig) return undefined;
                var row = splRowOf(trig);
                if(!row) return null;
                return splGrabRowTrack(row);
            }
        
            function splTryInject(attempt){
                if(attempt > 30) return;
                if(window.__splBg) return;
                var root = splActiveMenu();
                if(!root){
                    setTimeout(function(){ splTryInject(attempt+1); }, 120);
                    return;
                }
                if(splIsUserMenu(root)){
                    splClearTrack();
                    return;
                }
                var ul = root.querySelector('ul[role="menu"]');
                if(!ul){
                    setTimeout(function(){ splTryInject(attempt+1); }, 120);
                    return;
                }
                if(ul.querySelector('.spl-menu-dl')) return;
                if(splIsCurationSheet(root, ul)) return;
                var subOpen = false;
                try{ subOpen = !!root.querySelector('button[aria-expanded="true"]'); }catch(e){}
                if(subOpen) return;
        
                var track = splLiveTrack();
                if(track === undefined){
                    var t = window.__splMenuTrack;
                    if(t && (Date.now() - window.__splMenuTrackAt) > TRACK_TTL) t = null;
                    if(!t){
                        setTimeout(function(){ splTryInject(attempt+1); }, 120);
                        return;
                    }
                    track = t;
                }
                if(!track) return;
        
                if(!splInjectItem(ul, track)){
                    setTimeout(function(){ splTryInject(attempt+1); }, 120);
                }
            }
        
            document.addEventListener('contextmenu', function(e){
                var row = splRowOf(e.target);
                window.__splMenuTrack = row ? splGrabRowTrack(row) : null;
                window.__splMenuTrackAt = Date.now();
            }, true);
        
            document.addEventListener('pointerdown', function(e){
                if(!e.isTrusted) return;
                var row = splRowOf(e.target);
                if(!row) return;
                var t = splGrabRowTrack(row);
                if(!t) return;
                window.__splMenuTrack = t;
                window.__splMenuTrackAt = Date.now();
            }, true);
        
            document.addEventListener('click', function(e){
                var target = e.target;
                if(!target || !target.closest) return;
                try{
                    if(target.closest('button[data-testid="user-widget-link"], [data-testid="user-widget-menu"]')){
                        splClearTrack();
                        return;
                    }
                }catch(err){}
                try{
                    if(target.closest('[data-testid="context-menu"]') && target.closest('button[aria-expanded]')){
                        splClearTrack();
                        return;
                    }
                }catch(err){}
            }, true);
        
            var obsPending = false;
            var obs = new MutationObserver(function(muts){
                if(window.__splBg) return;
                var hit = false;
                for(var i=0;i<muts.length && !hit;i++){
                    var a = muts[i].addedNodes;
                    for(var j=0;j<a.length;j++){
                        var n = a[j];
                        if(n.nodeType !== 1) continue;
                        if(n.id === 'context-menu' ||
                           n.getAttribute('data-testid') === 'context-menu' ||
                           n.querySelector('[data-testid="context-menu"]')){
                            hit = true;
                            break;
                        }
                    }
                }
                if(hit && !obsPending){
                    obsPending = true;
                    setTimeout(function(){
                        obsPending = false;
                        splTryInject(0);
                    }, 60);
                }
            });
            obs.observe(document.body, { childList: true, subtree: true });
        
            var splLastHover = 0;
            document.addEventListener('mouseover', function(e){
                if(Date.now() - splLastHover < 150) return;
                try{
                    if(e.target && e.target.closest && e.target.closest('[data-testid="context-menu"]')){
                        splLastHover = Date.now();
                        splTryInject(0);
                    }
                }catch(err){}
            }, true);
        })();
    """
}