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
            var GRACE_MS = 600;
            var TRIGGER_MAX_AGE = 2500;
            var DISMISS_BLOCK_MS = 1200;
            var menuObserver = null;
            var menuTarget = null;
            var activeTrigger = null;
            var lastTrigger = null;
            var timer = null;
            var sheetOpen = false;
            var ensureTries = 0;

            var menuMutAt = 0;
            var pressAt = 0;
            var pressInMenu = false;
            var pressDismissed = false;

            function visibleEl(el){
                if (!el || !el.isConnected) return false;
                try {
                    var r = el.getBoundingClientRect();
                    if (r.width < 2 || r.height < 2) return false;
                    var node = el, hops = 0;
                    while (node && node !== document.body && hops++ < 8){
                        var cs = getComputedStyle(node);
                        if (cs.display === 'none' || cs.visibility === 'hidden') return false;
                        if (parseFloat(cs.opacity) < 0.02) return false;
                        node = node.parentElement;
                    }
                    return true;
                } catch(e){ return false; }
            }
            function liveMenuBox(){
                var boxes = document.querySelectorAll('#context-menu');
                var live = null;
                for (var i = 0; i < boxes.length; i++){
                    if (visibleEl(boxes[i])) live = boxes[i];
                }
                if (live) return live;
                return boxes.length ? boxes[boxes.length - 1] : null;
            }
            function visibleMenuCount(){
                var uls = document.querySelectorAll('ul[role="menu"][data-depth="0"]');
                var n = 0;
                for (var i = 0; i < uls.length; i++){
                    if (visibleEl(uls[i])) n++;
                }
                return n;
            }

            function menuRootOf(el){
                if (!el || !el.closest) return null;
                return el.closest('[data-tippy-root], [role="menu"], #context-menu');
            }

            function resolveTrigger(){
                var ul = openMenu();
                if (ul && ul.closest){
                    try{
                        var root = ul.closest('[data-tippy-root]');
                        if (root && root.id){
                            var byId = document.querySelector('[aria-describedby="' + root.id + '"]');
                            if (byId) return byId;
                        }
                    }catch(e){}
                }
                try{
                    var open = document.querySelector('[data-context-menu-open="true"]');
                    if (open) return open;
                }catch(e){}
                if (lastTrigger && menuMutAt > pressAt && (Date.now() - pressAt) < TRIGGER_MAX_AGE){
                    return lastTrigger;
                }
                return null;
            }

            function dismissOutside(){
                var m = openMenu();
                if (!m) { notify(false); return; }
                try{
                    m.dispatchEvent(new KeyboardEvent('keydown', {
                        key:'Escape', code:'Escape', keyCode:27, which:27,
                        bubbles:true, cancelable:true
                    }));
                }catch(e){}
                setTimeout(function(){
                    var still = openMenu();
                    if (still && visibleEl(still)){
                        var root = null;
                        try{ root = still.closest('[data-tippy-root]') || still.parentElement; }catch(e){}
                        if (root && root.parentElement) root.remove();
                    }
                    notify(false);
                }, 180);
            }
            function maybeDismissOutside(target){
                var m = openMenu();
                if (!m || !visibleEl(m)) return;
                var trig = null;
                try{ trig = resolveTrigger(); }catch(e){}
                if (trig && target && trig.contains && trig.contains(target)) return;
                try{
                    if (target && target.closest && target.closest('[data-context-menu-open="true"]')) return;
                }catch(e){}
                pressDismissed = true;
                dismissOutside();
            }

            function pressBeganInOpenMenu(){
                return pressInMenu && pressAt >= menuMutAt;
            }
            function gateBlock(target){
                if (pressDismissed && (Date.now() - pressAt) < DISMISS_BLOCK_MS) return true;
                if (!target || !target.closest) return false;
                var root = menuRootOf(target);
                if (root){
                    if (pressBeganInOpenMenu()) return false;
                    return (Date.now() - menuMutAt) < GRACE_MS;
                }
                try{
                    var tr = target.closest('[data-context-menu-open="true"]');
                    if (tr) return pressAt < menuMutAt && (Date.now() - menuMutAt) < GRACE_MS;
                }catch(e){}
                return false;
            }

            document.addEventListener('pointerdown', function(e){
                if (!e.isTrusted) return;
                pressAt = Date.now();
                pressInMenu = !!menuRootOf(e.target);
                pressDismissed = false;
                if (!pressInMenu) maybeDismissOutside(e.target);
                var t = e.target;
                if (!t || !t.closest) return;
                if (t.closest('[data-tippy-root]')) return;
                lastTrigger = t.closest('button') || t;
            }, true);

            ['mousedown','mouseup','click'].forEach(function(type){
                document.addEventListener(type, function(e){
                    if (!e.isTrusted) return;
                    if (!gateBlock(e.target)) return;
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    if (e.type === 'click') pressDismissed = false;
                }, true);
            });

            document.addEventListener('mouseover', function(e){
                if (!e.isTrusted) return;
                if (pressInMenu) return;
                if ((Date.now() - menuMutAt) > 450) return;
                if (!e.target || !e.target.closest) return;
                if (menuRootOf(e.target)) e.stopPropagation();
            }, true);

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
                    S + ' #context-menu>*{width:100%!important;max-width:100%!important;box-sizing:border-box!important}' +
                    S + ' ul[role="menu"]{width:100%!important;max-width:100%!important;min-width:0!important;max-height:55vh!important;overflow-y:auto!important;overflow-x:hidden!important;touch-action:pan-y!important;border-radius:18px 18px 0 0!important;box-shadow:0 -10px 40px rgba(0,0,0,.65)!important;padding:4px 0 calc(14px + env(safe-area-inset-bottom,0px))!important;scrollbar-width:none!important;overscroll-behavior:contain!important}' +
                    S + ' ul[role="menu"]::-webkit-scrollbar{display:none!important;width:0!important;height:0!important}' +
                    S + ' ul[role="menu"]::before{content:"";display:block;position:sticky;top:0;z-index:2;width:36px;height:4px;border-radius:2px;background:rgba(255,255,255,.28);margin:8px auto 6px}' +
                    S + ' ul[role="menu"]:has(> .' + HEAD_CLS + ')::before{display:none!important}' +
                    S + ' ul[role="menu"] button{padding:12px 16px!important;min-height:48px!important}' +
                    S + ' ul[role="menu"]:has(> .' + HEAD_CLS + '){min-height:55vh!important}' +
                    S + ' [data-testid="user-widget-menu"]{width:100%!important;max-width:100%!important}' +
                    S + ' [data-testid="user-widget-menu"]>*{width:100%!important;max-width:100%!important;box-sizing:border-box!important}' +
                    S + ' [data-testid="user-widget-menu"] ul{width:100%!important;max-width:100%!important}' +
                    S + ' [data-testid="user-widget-menu"] *{max-width:100%!important}' +
                    '.' + HEAD_CLS + '{display:flex;flex-direction:column;padding:0 16px 14px;margin-bottom:4px;border-bottom:1px solid rgba(255,255,255,.1);touch-action:none!important;cursor:grab}' +
                    '.spl-sheet-grab{display:flex;align-items:center;justify-content:center;height:34px;margin:0 -16px 10px;flex-shrink:0;touch-action:none!important}' +
                    '.spl-sheet-grab span{width:44px;height:5px;border-radius:3px;background:rgba(255,255,255,.35)}' +
                    '.spl-sheet-main{display:flex;align-items:center;gap:12px;min-height:56px}' +
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

            function menuEl(){ return liveMenuBox(); }
            function sheetUl(){
                var cm = menuEl();
                if (!cm) return null;
                return cm.querySelector('ul[role="menu"][data-depth="0"]');
            }
            function openMenu(){
                var subs = document.querySelectorAll('ul[role="menu"][data-depth="0"]');
                var live = null;
                for (var i = 0; i < subs.length; i++){
                    if (visibleEl(subs[i])) live = subs[i];
                }
                if (live) return live;
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

            function trackRow(){
                var t = resolveTrigger();
                if (!t || !t.closest) return null;
                try{
                    return t.closest('[data-testid="tracklist-row"]') || t.closest('[role="row"]');
                }catch(e){ return null; }
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
                head.innerHTML =
                    '<div class="spl-sheet-grab"><span></span></div>' +
                    '<div class="spl-sheet-main">' +
                    '<img class="spl-sheet-cover" alt="" aria-hidden="true">' +
                    '<div class="spl-sheet-meta"><div class="spl-sheet-title"></div><div class="spl-sheet-artist"></div></div>' +
                    '<button class="spl-sheet-like" type="button" aria-label="Like"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 20.7 3.9 12.6a5.1 5.1 0 0 1 7.2-7.2l.9.9.9-.9a5.1 5.1 0 0 1 7.2 7.2z"/></svg></button>' +
                    '</div>';
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
                var heads = document.querySelectorAll('.' + HEAD_CLS);
                for (var i = 0; i < heads.length; i++) heads[i].remove();
            }
            function ensure(){
                var ul = sheetUl();
                if (!ul && !openMenu()) { removeHead(); activeTrigger = null; notify(false); ensureTries = 0; return; }
                notify(true);
                activeTrigger = resolveTrigger() || lastTrigger;
                if (!ul) { removeHead(); return; }
                var d = trackInfo();
                if (!d) {
                    removeHead();
                    if (ensureTries < 8) { ensureTries++; schedule(); }
                    return;
                }
                ensureTries = 0;
                var head = ul.querySelector('.' + HEAD_CLS);
                if (head && !head.querySelector('.spl-sheet-like')) { head.remove(); head = null; }
                if (!head) {
                    head = buildHead();
                    ul.insertBefore(head, ul.firstChild);
                } else if (ul.firstElementChild !== head) {
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
                var t = activeTrigger || resolveTrigger() || lastTrigger;
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

                panel.addEventListener('pointerdown', function(e){
                    if (!e.isTrusted) return;
                    if (e.pointerType === 'mouse' && e.button !== 0) return;
                    if (visibleMenuCount() > 1) { pid = null; return; }
                    if (panel.querySelector('form')) { pid = null; return; }
                    var sc = scrollerOf(panel);
                    if (sc && sc.scrollTop > 0) { pid = null; return; }
                    var inHead = !!(e.target.closest && e.target.closest('.' + HEAD_CLS));
                    var inHandle = false;
                    var ul = panel.querySelector('ul[role="menu"][data-depth="0"]');
                    if (ul){
                        var r = ul.getBoundingClientRect();
                        inHandle = e.clientY >= r.top && (e.clientY - r.top) <= 44;
                    }
                    if (sc && !inHead && !inHandle) { pid = null; return; }
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
                        if (Math.abs(d) < 10 && dx < 10) return;
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
                    var cancelled = (e.type === 'pointercancel');
                    pid = null;
                    dragging = false;
                    if (!was) return;
                    try { panel.releasePointerCapture(e.pointerId); } catch(err){}
                    if (cancelled) { settle(false); return; }
                    settle(dy > Math.min(110, (panel.offsetHeight || 1) * 0.28));
                }
                panel.addEventListener('pointerup', end, {capture:true, passive:true});
                panel.addEventListener('pointercancel', end, {capture:true, passive:true});
            }

            function schedule(){
                if (timer) return;
                timer = setTimeout(function run(){
                    timer = null;
                    if (window.__splBg) return;
                    if (window.__splBusy) { timer = setTimeout(run, 250); return; }
                    ensure();
                    watchMenu();
                }, 120);
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
                menuObserver = new MutationObserver(function(){
                    menuMutAt = Date.now();
                    schedule();
                });
                menuObserver.observe(cm, {childList: true, subtree: true});
            }

            window.__splCloseCtxSheet = closeSheet;
            injectStyle();
            if (window.MutationObserver) {
                var bodyObserver = new MutationObserver(function(muts){
                    for (var i = 0; i < muts.length; i++) {
                        var added = muts[i].addedNodes;
                        for (var j = 0; j < added.length; j++) {
                            var n = added[j];
                            if (n.nodeType !== 1) continue;
                            if ((n.matches && n.matches('[data-tippy-root]')) ||
                                (n.querySelector && n.querySelector('ul[role="menu"], #context-menu'))) {
                                menuMutAt = Date.now();
                            }
                        }
                    }
                    schedule();
                });
                bodyObserver.observe(document.body, {childList: true});
            }
            ensure();
            watchMenu();
        })();
    """
}