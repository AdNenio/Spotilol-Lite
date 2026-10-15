package com.project.lol.webview.injections

object LyricsEngine {
    const val CONTENT = """
        (function(){
            if (window.__splLyricsEngine) return;
            window.__splLyricsEngine = true;

            var SWEEP_MS = 200;
            var DEBOUNCE_MS = 150;
            var lastSweep = 0;
            var pending = false;

            function rgb(str){
                var m = /rgba?\(([^)]+)\)/.exec(str || '');
                if (!m) return null;
                var p = m[1].split(',');
                return [parseFloat(p[0]) || 0, parseFloat(p[1]) || 0, parseFloat(p[2]) || 0];
            }

            function lum(c){
                if (!c) return 0;
                return (0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]) / 255;
            }

            function stateToken(tokens, isState){
                for (var k = 0; k < tokens.length; k++){
                    if (isState[tokens[k]]) return tokens[k];
                }
                return null;
            }

            function resolve(){
                var lines = document.querySelectorAll('[data-testid="lyrics-line"]');
                if (!lines.length){
                    var stale = document.querySelectorAll('[data-spl-lyr-root]');
                    for (var s = 0; s < stale.length; s++) stale[s].removeAttribute('data-spl-lyr-root');
                    return;
                }

                var total = lines.length;
                var freq = {};
                var per = [];
                var i, j, k;
                for (i = 0; i < total; i++){
                    var tokens = ('' + (lines[i].className || '')).trim().split(/\s+/).filter(Boolean);
                    per.push(tokens);
                    for (j = 0; j < tokens.length; j++) freq[tokens[j]] = (freq[tokens[j]] || 0) + 1;
                }

                var isState = {};
                for (var t in freq){ if (freq[t] < total * 0.8) isState[t] = 1; }

                var groups = {};
                for (i = 0; i < total; i++){
                    var cs = getComputedStyle(lines[i]);
                    var st = stateToken(per[i], isState);
                    var key = st || '__base__';
                    var col = rgb(cs.color);
                    var g = groups[key] || (groups[key] = { n: 0, r: 0, g: 0, b: 0, lum: 0 });
                    g.n++;
                    if (col){ g.r += col[0]; g.g += col[1]; g.b += col[2]; g.lum += lum(col); }
                }

                var keys = Object.keys(groups);
                for (k = 0; k < keys.length; k++){
                    var grp = groups[keys[k]];
                    grp.lum = grp.lum / grp.n;
                }
                keys.sort(function(a, b){ return groups[b].lum - groups[a].lum; });
                var activeKey = keys.length ? keys[0] : null;

                var activeIdx = -1;
                for (i = 0; i < total; i++){
                    if ((stateToken(per[i], isState) || '__base__') === activeKey) activeIdx = i;
                }

                for (i = 0; i < total; i++){
                    var cur = stateToken(per[i], isState) || '__base__';
                    var attr = cur === activeKey ? 'a' : (activeIdx >= 0 ? (i < activeIdx ? 'p' : 'u') : 'u');
                    if (lines[i].getAttribute('data-spl-lyr') !== attr) lines[i].setAttribute('data-spl-lyr', attr);
                }

                var root = null;
                var e = lines[0];
                var hops = 0;
                while (e && e !== document.body && hops++ < 12){
                    var es = getComputedStyle(e);
                    if ((es.overflowY === 'scroll' || es.overflowY === 'auto') && e.scrollHeight > e.clientHeight + 10){
                        root = e;
                        break;
                    }
                    e = e.parentElement;
                }
                if (root && root.getAttribute('data-spl-lyr-root') !== '1') root.setAttribute('data-spl-lyr-root', '1');
                if (root){
                    var bg = getComputedStyle(lines[0]).getPropertyValue('--lyrics-color-background').trim();
                    if (bg && root.__splBg !== bg){
                        root.__splBg = bg;
                        root.style.setProperty('--spl-lyr-bg', bg);
                    }
                }
            }

            function tick(){
                if (window.__splBg || window.__splBusy) return;
                var now = Date.now();
                if (now - lastSweep < SWEEP_MS) return;
                lastSweep = now;
                resolve();
            }

            window.__splLyricsResolve = resolve;
            if (window.__splFloaters) window.__splFloaters.push(tick);
            else setInterval(tick, SWEEP_MS);

            resolve();

            var obs = new MutationObserver(function(){
                if (window.__splBg || window.__splBusy || pending) return;
                pending = true;
                setTimeout(function(){
                    pending = false;
                    if (!window.__splBg && !window.__splBusy) resolve();
                }, DEBOUNCE_MS);
            });
            obs.observe(document.body, { childList: true, subtree: true });

            window.addEventListener('beforeunload', function(){
                try { obs.disconnect(); } catch(e){}
            });
        })();
    """
}
