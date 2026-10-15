package com.project.lol.webview.injections

object NpHeaderFix {
    const val CONTENT = """
        (function(){
            if (window.__splNpHeaderFix) return;
            window.__splNpHeaderFix = true;

            var COMPACT_W = 420;

            var st = document.createElement('style');
            st.id = 'spl-np-header-fix';
            st.textContent = [
                '.spl-nph{width:100%!important;max-width:100vw!important;min-width:0!important;flex-wrap:wrap!important;row-gap:4px!important;align-items:center!important;overflow:visible!important}',
                '.spl-nph > .spl-nph-title{flex:0 1 auto!important;min-width:0!important;max-width:100%!important;overflow:hidden!important}',
                '.spl-nph > .spl-nph-title a{display:block!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important;max-width:100%!important}',
                '.spl-nph-row{display:flex!important;flex-wrap:wrap!important;row-gap:4px!important;align-items:center!important;min-width:0!important;max-width:100%!important;overflow:visible!important}',
                '.spl-nph-row > .spl-nph-spacer{flex:1 1 auto!important;min-width:0!important;overflow:hidden!important}',
                '.spl-nph-row > .spl-nph-grp{display:flex!important;flex:0 0 auto!important;min-width:0!important;overflow:visible!important}',
                '.spl-nph-btn{display:inline-flex!important;visibility:visible!important;flex:0 0 auto!important}',
                '.spl-nph-compact .spl-nph-btn{padding:5px!important;min-width:28px!important;min-height:28px!important}',
                '.spl-nph-compact .spl-nph-grp{column-gap:2px!important}'
            ].join('\n');
            function appendStyle(){
                var t = document.head || document.documentElement;
                if (t && !document.getElementById('spl-np-header-fix')) t.appendChild(st);
            }
            appendStyle();
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', appendStyle);
            }

            var logged = false;
            var row = null;
            var roRow = null;

            function findRow(){
                var grps = document.querySelectorAll('div[role="radiogroup"]');
                for (var i = 0; i < grps.length; i++) {
                    if (grps[i].querySelector('button[role="radio"][value="lyrics"]')) {
                        return grps[i].parentElement || null;
                    }
                }
                return null;
            }

            function measure(){
                if (!row) return;
                var w = row.getBoundingClientRect().width || 0;
                row.classList.toggle('spl-nph-compact', w < COMPACT_W);
            }

            var ro = null;
            try {
                if (typeof ResizeObserver !== 'undefined') {
                    ro = new ResizeObserver(function(entries){
                        var e = entries[entries.length - 1];
                        e.target.classList.toggle('spl-nph-compact', e.contentRect.width < COMPACT_W);
                    });
                }
            } catch (err) {}

            function tag(){
                if (window.__splBg || window.__splBusy) return;

                if (row && !document.contains(row)) {
                    if (ro) ro.disconnect();
                    roRow = null;
                    row = null;
                }
                if (!row) row = findRow();
                if (!row) return;

                var root = row.parentElement;
                if (!root || !root.classList) return;

                root.classList.add('spl-nph');
                row.classList.add('spl-nph-row');

                var kids = row.children;
                for (var i = 0; i < kids.length; i++) {
                    if (kids[i].querySelector('button')) kids[i].classList.add('spl-nph-grp');
                    else kids[i].classList.add('spl-nph-spacer');
                }

                var rkids = root.children;
                for (var j = 0; j < rkids.length; j++) {
                    if (rkids[j] !== row) rkids[j].classList.add('spl-nph-title');
                }

                var btns = row.querySelectorAll('button');
                for (var b = 0; b < btns.length; b++) btns[b].classList.add('spl-nph-btn');

                if (ro) {
                    if (roRow !== row) {
                        ro.disconnect();
                        ro.observe(row);
                        roRow = row;
                    }
                } else {
                    measure();
                }

                if (!logged) {
                    logged = true;
                    try { AndBridge.dbg('s', 'np-header fix active (' + btns.length + ' buttons)'); } catch (e) {}
                }
            }

            var pending = false;
            function schedule(){
                if (pending) return;
                pending = true;
                setTimeout(function(){ pending = false; tag(); }, 150);
            }

            try {
                var obs = new MutationObserver(function(muts){
                    for (var i = 0; i < muts.length; i++) {
                        if (muts[i].addedNodes.length) { schedule(); break; }
                    }
                });
                obs.observe(document.documentElement, { childList: true, subtree: true });
            } catch (e) {}

            setInterval(tag, 2000);
            tag();
        })();
    """
}