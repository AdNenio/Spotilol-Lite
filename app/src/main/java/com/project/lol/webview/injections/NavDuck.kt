package com.project.lol.webview.injections

object NavDuck {
    const val CONTENT = """
        (function(){
            if(window.__splDuckInit) return;
            window.__splDuckInit = true;
            var LEVEL = 0.2, ducked = false, els = [];
            var P = HTMLMediaElement.prototype;
            var vd = Object.getOwnPropertyDescriptor(P, 'volume');
            if(!vd || !vd.get || !vd.set || !vd.configurable) return;

            function track(e){
                if(els.indexOf(e) < 0){
                    els.push(e);
                    if(els.length > 32) els.shift();
                }
            }
            function base(e){
                return (typeof e.__splVol === 'number') ? e.__splVol : vd.get.call(e);
            }
            function apply(e){
                try{
                    var b = base(e);
                    e.__splVol = b;
                    vd.set.call(e, ducked ? b * LEVEL : b);
                }catch(x){}
            }

            Object.defineProperty(P, 'volume', {
                configurable: true,
                enumerable: vd.enumerable,
                get: function(){ return base(this); },
                set: function(v){
                    if(!(v >= 0 && v <= 1)){ vd.set.call(this, v); return; }
                    this.__splVol = v;
                    track(this);
                    vd.set.call(this, ducked ? v * LEVEL : v);
                }
            });

            var origPlay = P.play;
            P.play = function(){
                try{ track(this); if(ducked) apply(this); }catch(x){}
                return origPlay.apply(this, arguments);
            };

            window.__splDuck = function(on){
                ducked = !!on;
                var q = document.querySelectorAll('audio,video');
                for(var i = 0; i < q.length; i++) track(q[i]);
                for(var j = 0; j < els.length; j++) apply(els[j]);
            };
        })();
    """
}
