package com.project.lol.webview.injections

object ContextMenuSheet {
    const val CONTENT = """
        (function(){
            if (window.__splContextMenuSheet) return;
            window.__splContextMenuSheet = true;

            var STYLE_ID = 'spl-ctxsheet-style';
            var HEAD_CLS = 'spl-sheet-head';
            var S = '[data-tippy-root]:has(ul[role="menu"])';
            var LIKE = S + ' ul[role="menu"] button.spl-sheet-like';
            var menuObserver = null;
            var menuTarget = null;
            var activeTrigger = null;
            var lastTrigger = null;
            var timer = null;
            var sheetOpen = false;

            function rememberTrigger(e){
                var t = e.target;
                if (!t || !t.closest) return;
                if (t.closest('[data-tippy-root]')) return;
                lastTrigger = t.closest('button') || t;
            }

            function notify(open){
                if (open === sheetOpen) return;
                sheetOpen = open;
                try { if (window.AndBridge && window.AndBridge.ctxMenuState) window.AndBridge.ctxMenuState(open); } catch(e){}
            }

            function injectStyle(){
                if (document.getElementById(STYLE_ID)) return;
                var s = document.createElement('style');
                s.id = STYLE_ID;
                s.textContent =
                    S + '{position:fixed!important;inset:auto 0 0 0!important;transform:none!important;width:100%!important;max-width:100%!important;pointer-events:auto!important;z-index:2147483647!important;will-change:auto!important}' +
                    S + ' #context-menu{width:100%!important;max-width:100%!important;max-height:none!important;overflow:visible!important;padding:0!important;background:transparent!important;border-radius:0!important;box-shadow:none!important}' +
                    S + ' ul[role="menu"]{width:100%!important;max-width:100%!important;min-width:0!important;max-height:55vh!important;overflow-y:auto!important;overflow-x:hidden!important;border-radius:18px 18px 0 0!important;box-shadow:0 -10px 40px rgba(0,0,0,.65)!important;padding:4px 0 calc(14px + env(safe-area-inset-bottom,0px))!important;scrollbar-width:none!important;overscroll-behavior:contain!important}' +
                    S + ' ul[role="menu"]::-webkit-scrollbar{display:none!important;width:0!important;height:0!important}' +
                    S + ' ul[role="menu"]::before{content:"";display:block;position:sticky;top:0;z-index:2;width:36px;height:4px;border-radius:2px;background:rgba(255,255,255,.28);margin:8px auto 6px}' +
                    S + ' ul[role="menu"] button{padding:12px 16px!important;min-height:48px!important}' +
                    S + ' > ul[role="menu"]{min-height:55vh!important}' +
                    '.' + HEAD_CLS + '{display:flex;align-items:center;gap:12px;padding:0 16px 12px;margin-bottom:4px;border-bottom:1px solid rgba(255,255,255,.1)}' +
                    '.spl-sheet-cover{width:48px;height:48px;border-radius:6px;object-fit:cover;flex-shrink:0;background:rgba(255,255,255,.08)}' +
                    '.spl-sheet-meta{flex:1;min-width:0}' +
                    '.spl-sheet-title{font-size:15px;font-weight:700;color:#fff;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;line-height:1.3}' +
                    '.spl-sheet-artist{font-size:12px;color:rgba(255,255,255,.6);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;line-height:1.3}' +
                    LIKE + '{width:40px!important;height:40px!important;min-height:0!important;max-height:none!important;flex-shrink:0;border:0;background:none;padding:0!important;display:flex;align-items:center;justify-content:center;color:rgba(255,255,255,.7);cursor:pointer}' +
                    LIKE + ' svg{width:24px!important;height:24px!important;flex-shrink:0}' +
                    '.spl-sheet-like svg path{fill:none;stroke:currentColor;stroke-width:1.8;stroke-linejoin:round}' +
                    '.spl-sheet-like.spl-on{color:var(--spl-accent,#1db954)}' +
                    '.spl-sheet-like.spl-on svg path{fill:currentColor;stroke:none}' +
                    '.spl-sheet-like:active{transform:scale(.92)}';
                (document.head || document.documentElement).appendChild(s);
            }

            function menuEl(){ return document.getElementById('context-menu'); }
            function sheetUl(){
                var cm = menuEl();
                if (!cm) return null;
                return cm.querySelector('ul[role="menu"][data-depth="0"]');
            }
            function openMenu(){
                var subs = document.querySelectorAll('ul[role="menu"][data-depth="0"]');
                if (subs.length) return subs[subs.length - 1];
                var all = document.querySelectorAll('ul[role="menu"]');
                return all.length ? all[all.length - 1] : null;
            }
            function scrollerOf(panel){
                if (!panel) return null;
                var ul = panel.querySelector('ul[role="menu"][data-depth="0"]');
                if (ul && ul.scrollHeight > ul.clientHeight + 1) return ul;
                return panel.scrollHeight > panel.clientHeight + 1 ? panel : null;
            }
            function stampSubmenus(){
                var subs = document.querySelectorAll('ul[role="menu"][data-depth="1"]');
                for (var i = 0; i < subs.length; i++) {
                    if (!subs[i].__splSeen) subs[i].__splSeen = Date.now();
                }
            }
            function triggerEl(){
                return document.querySelector('[data-context-menu-open="true"]');
            }
            function trackRow(){
                var t = triggerEl();
                if (!t) return null;
                return t.closest('[data-testid="tracklist-row"]') || t.closest('[role="row"]');
            }
            function slotButton(){
                var row = trackRow();
                return row ? row.querySelector('button[aria-checked]') : null;
            }
            function likeRowButton(){
                var ul = sheetUl();
                if (!ul) return null;
                var items = ul.children;
                for (var i = 0; i < items.length; i++) {
                    if (items[i].querySelector('img[src*="liked-songs"]')) {
                        return items[i].querySelector('button') || items[i];
                    }
                }
                return null;
            }
            function fire(el){
                if (!el) return;
                var r = el.getBoundingClientRect();
                var x = r.left + r.width / 2, y = r.top + r.height / 2;
                function ev(btns){ return {bubbles:true,cancelable:true,composed:true,view:window,clientX:x,clientY:y,button:0,buttons:btns,pointerId:1,pointerType:'mouse',isPrimary:true}; }
                el.dispatchEvent(new PointerEvent('pointerdown', ev(1)));
                el.dispatchEvent(new MouseEvent('mousedown', ev(1)));
                el.dispatchEvent(new PointerEvent('pointerup', ev(0)));
                el.dispatchEvent(new MouseEvent('mouseup', ev(0)));
                el.dispatchEvent(new MouseEvent('click', ev(0)));
            }
            function trackInfo(){
                var row = trackRow();
                if (!row) return null;
                var title = row.querySelector('a[href*="/track/"]');
                if (!title) return null;
                var img = row.querySelector('img');
                var artist = row.querySelector('a[href*="/artist/"]');
                var slot = row.querySelector('button[aria-checked]');
                return {
                    cover: img ? (img.currentSrc || img.src || '') : '',
                    title: (title.textContent || '').trim(),
                    artist: artist ? (artist.textContent || '').trim() : '',
                    liked: slot ? slot.getAttribute('aria-checked') === 'true' : false
                };
            }
            function buildHead(){
                var head = document.createElement('div');
                head.className = HEAD_CLS;
                head.innerHTML = '<img class="spl-sheet-cover" alt="" aria-hidden="true"><div class="spl-sheet-meta"><div class="spl-sheet-title"></div><div class="spl-sheet-artist"></div></div><button class="spl-sheet-like" type="button" aria-label="Like"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 20.7 3.9 12.6a5.1 5.1 0 0 1 7.2-7.2l.9.9.9-.9a5.1 5.1 0 0 1 7.2 7.2z"/></svg></button>';
                head.querySelector('.spl-sheet-like').addEventListener('click', function(ev){
                    ev.preventDefault();
                    ev.stopPropagation();
                    var slot = slotButton();
                    if (slot && slot.getAttribute('aria-checked') !== 'true') {
                        fire(slot);
                    } else {
                        fire(likeRowButton());
                    }
                    setTimeout(ensure, 400);
                });
                return head;
            }
            function removeHead(){
                var head = document.querySelector('.' + HEAD_CLS);
                if (head) head.remove();
            }
            function ensure(){
                var ul = sheetUl();
                if (!ul && !openMenu()) { removeHead(); activeTrigger = null; notify(false); return; }
                notify(true);
                activeTrigger = triggerEl() || lastTrigger;
                if (!ul) { removeHead(); return; }
                var d = trackInfo();
                if (!d) { removeHead(); return; }
                var head = ul.querySelector('.' + HEAD_CLS);
                if (head && !head.querySelector('.spl-sheet-like')) { head.remove(); head = null; }
                if (!head) {
                    head = buildHead();
                    ul.insertBefore(head, ul.firstChild);
                }
                var cov = head.querySelector('.spl-sheet-cover');
                if (cov.getAttribute('src') !== d.cover) cov.setAttribute('src', d.cover);
                var te = head.querySelector('.spl-sheet-title');
                if (te.textContent !== d.title) te.textContent = d.title;
                var ae = head.querySelector('.spl-sheet-artist');
                if (ae.textContent !== d.artist) ae.textContent = d.artist;
                var lk = head.querySelector('.spl-sheet-like');
                var on = lk.classList.contains('spl-on');
                if (d.liked !== on) {
                    if (d.liked) lk.classList.add('spl-on'); else lk.classList.remove('spl-on');
                }
            }

            function closeSheet(){
                var t = activeTrigger || triggerEl() || lastTrigger;
                if (t && document.contains(t)) { fire(t); return; }
                notify(false);
            }
            function attachDrag(panel){
                if (!panel || panel.__splDrag) return;
                panel.__splDrag = true;
                var startY = 0, startX = 0, dy = 0, pid = null, decided = false, dragging = false;

                function clearStyles(){
                    panel.style.transition = '';
                    panel.style.transform = '';
                    panel.style.opacity = '';
                }
                function settle(close){
                    var h = panel.offsetHeight || 1;
                    panel.style.transition = 'transform .24s cubic-bezier(.2,.8,.2,1), opacity .24s';
                    if (close) {
                        panel.style.transform = 'translateY(' + (h + 60) + 'px)';
                        panel.style.opacity = '0';
                        setTimeout(function(){ closeSheet(); clearStyles(); }, 250);
                    } else {
                        panel.style.transform = 'translateY(0px)';
                        panel.style.opacity = '1';
                        setTimeout(clearStyles, 260);
                    }
                }

                panel.addEventListener('click', function(e){
                    var t = e.target;
                    if (!t || !t.closest) return;
                    var sub = t.closest('ul[role="menu"][data-depth="1"]');
                    if (!sub || !sub.__splSeen) return;
                    if (Date.now() - sub.__splSeen > 400) return;
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                }, {capture:true});

                panel.addEventListener('pointerdown', function(e){
                    if (e.pointerType === 'mouse' && e.button !== 0) return;
                    if (document.querySelectorAll('ul[role="menu"]').length > 1) { pid = null; return; }
                    var sc = scrollerOf(panel);
                    if (sc && sc.scrollTop > 0) { pid = null; return; }
                    startY = e.clientY;
                    startX = e.clientX;
                    dy = 0;
                    pid = e.pointerId;
                    decided = false;
                    dragging = false;
                }, {capture:true, passive:true});

                panel.addEventListener('pointermove', function(e){
                    if (pid === null || e.pointerId !== pid) return;
                    var d = e.clientY - startY;
                    var dx = Math.abs(e.clientX - startX);
                    if (!decided) {
                        if (Math.abs(d) < 6 && dx < 6) return;
                        decided = true;
                        if (d <= 0 || dx > Math.abs(d)) { pid = null; return; }
                        var sc2 = scrollerOf(panel);
                        if (sc2 && sc2.scrollTop > 0) { pid = null; return; }
                        dragging = true;
                        panel.style.transition = 'none';
                        try { panel.setPointerCapture(e.pointerId); } catch(err){}
                    }
                    if (!dragging) return;
                    dy = Math.max(0, d);
                    panel.style.transform = 'translateY(' + dy + 'px)';
                    panel.style.opacity = String(Math.max(0.35, 1 - dy / ((panel.offsetHeight || 1) * 1.4)));
                    e.preventDefault();
                }, {capture:true, passive:false});

                function end(e){
                    if (pid === null || (e.pointerId !== undefined && e.pointerId !== pid)) return;
                    var was = dragging;
                    pid = null;
                    dragging = false;
                    if (!was) return;
                    try { panel.releasePointerCapture(e.pointerId); } catch(err){}
                    settle(dy > Math.min(110, (panel.offsetHeight || 1) * 0.28));
                }
                panel.addEventListener('pointerup', end, {capture:true, passive:true});
                panel.addEventListener('pointercancel', end, {capture:true, passive:true});
            }

            function schedule(){
                if (timer) return;
                timer = setTimeout(function(){ timer = null; stampSubmenus(); ensure(); watchMenu(); }, 120);
            }
            function watchMenu(){
                var m = openMenu();
                var cm = menuEl() || (m ? m.closest('[data-tippy-root]') : null);
                if (!cm) {
                    if (menuObserver) { menuObserver.disconnect(); menuObserver = null; menuTarget = null; }
                    return;
                }
                attachDrag(cm);
                if (menuObserver && menuTarget === cm) return;
                if (menuObserver) menuObserver.disconnect();
                menuTarget = cm;
                menuObserver = new MutationObserver(function(){ stampSubmenus(); schedule(); });
                menuObserver.observe(cm, {childList: true, subtree: true});
            }

            window.__splCloseCtxSheet = closeSheet;
            document.addEventListener('pointerdown', rememberTrigger, true);
            injectStyle();
            if (window.MutationObserver) {
                var bodyObserver = new MutationObserver(schedule);
                bodyObserver.observe(document.body, {childList: true});
            }
            ensure();
            watchMenu();
        })();
    """
}
