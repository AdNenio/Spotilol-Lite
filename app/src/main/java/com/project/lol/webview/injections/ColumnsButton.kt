package com.project.lol.webview.injections

object ColumnsButton {
    const val CONTENT = """
        (function(){
            if (window.__splColumnsButton) return;
            window.__splColumnsButton = true;

            var BTN_ID = 'spl-cols-btn';
            var STYLE_ID = 'spl-cols-btn-style';

            function trigger(){
                return document.querySelector('div[data-testid="playlist-tracklist"] button[data-skip-in-keyboard-nav]') ||
                    document.querySelector('div[data-testid="track-list"] button[data-skip-in-keyboard-nav]');
            }
            function openMenu(el){
                var r = el.getBoundingClientRect();
                var x = r.left + r.width / 2, y = r.top + r.height / 2;
                function ev(btns){ return {bubbles:true,cancelable:true,composed:true,view:window,clientX:x,clientY:y,button:0,buttons:btns,pointerId:1,pointerType:'mouse',isPrimary:true}; }
                el.dispatchEvent(new PointerEvent('pointerdown', ev(1)));
                el.dispatchEvent(new MouseEvent('mousedown', ev(1)));
                el.dispatchEvent(new PointerEvent('pointerup', ev(0)));
                el.dispatchEvent(new MouseEvent('mouseup', ev(0)));
                el.dispatchEvent(new MouseEvent('click', ev(0)));
            }
            function collectionPage(){
                var p = location.pathname;
                return /^\/(playlist|album)\//.test(p) || p.indexOf('/collection/tracks') === 0;
            }
            function headerBlock(){
                var header = document.querySelector('[data-testid="entity-header"]');
                if (!header) return null;
                var h1 = header.querySelector('h1');
                if (!h1) return header;
                var node = h1;
                while (node && node.parentElement && node.parentElement !== header) {
                    if (/contentSpacing/.test((node.parentElement.className || '').toString())) return node;
                    node = node.parentElement;
                }
                return header;
            }
            function sortAvailable(){
                try {
                    var api = window.splPlaylistSort;
                    return !!(api && api.available && api.available());
                } catch(e){ return false; }
            }
            function setLabel(btn, text){
                var sp = btn.querySelector('span');
                if (sp && sp.textContent !== text) sp.textContent = text;
                if (btn.getAttribute('aria-label') !== text) btn.setAttribute('aria-label', text);
            }
            function injectStyle(){
                if (document.getElementById(STYLE_ID)) return;
                var s = document.createElement('style');
                s.id = STYLE_ID;
                s.textContent =
                    '#spl-cols-btn{display:inline-flex;align-self:flex-start;width:fit-content;align-items:center;gap:6px;margin-top:10px;padding:5px 13px 5px 10px;border-radius:18px;background:rgba(255,255,255,.07);border:1px solid rgba(255,255,255,.14);color:#fff;font-family:inherit;font-size:12px;font-weight:600;line-height:1.4;cursor:pointer;transition:background .15s,border-color .15s}' +
                    '#spl-cols-btn:hover{background:rgba(255,255,255,.15);border-color:rgba(255,255,255,.3)}' +
                    '#spl-cols-btn:active{transform:scale(.97)}' +
                    '#spl-cols-btn svg{width:13px;height:13px;fill:currentColor;flex-shrink:0;opacity:.85}';
                (document.head || document.documentElement).appendChild(s);
            }
            function ensure(){
                var btn = document.getElementById(BTN_ID);
                var block = collectionPage() ? headerBlock() : null;
                if (!block) { if (btn) btn.remove(); return; }
                if (!btn) {
                    injectStyle();
                    btn = document.createElement('button');
                    btn.id = BTN_ID;
                    btn.type = 'button';
                    btn.innerHTML = '<svg viewBox="0 0 16 16" aria-hidden="true"><rect x="1.6" y="2.4" width="3.1" height="11.2" rx="1"/><rect x="6.45" y="2.4" width="3.1" height="11.2" rx="1"/><rect x="11.3" y="2.4" width="3.1" height="11.2" rx="1"/></svg><span></span>';
                    btn.addEventListener('click', function(){
                        var t = trigger();
                        if (t) openMenu(t);
                    });
                }
                if (btn.parentElement !== block) block.appendChild(btn);
                setLabel(btn, sortAvailable() ? 'Sort & columns' : 'Columns');
            }

            ensure();
            setInterval(function(){ if (window.__splBg || window.__splBusy) return; ensure(); }, 1000);
        })();
    """
}
