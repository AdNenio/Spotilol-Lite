package com.project.lol.webview.injections

object WebPrefs {
    const val CONTENT = """
        (function(){
            if(window.splSyncWebPrefs) return;
            var busy=false;
            var queued=null;
            window.splSyncWebPrefs=function(canvas,videos,force){
                var want=(canvas?'c1':'c0')+(videos?'v1':'v0');
                var done=null;
                try{ done=localStorage.getItem('splWebPrefs'); }catch(e){}
                if(!force&&done===want) return;
                if(busy){ queued=[canvas,videos]; return; }
                busy=true;
                var back=location.pathname!=='/preferences';
                if(back){
                    history.pushState({splSynthetic:1},'','/preferences');
                    window.dispatchEvent(new PopStateEvent('popstate',{state:null}));
                }
                var tries=0;
                var iv=setInterval(function(){
                    var boxes=[].slice.call(document.querySelectorAll('input[type=checkbox][id^="settings.videos-and-canvas."]'));
                    if(!boxes.length&&++tries<40) return;
                    clearInterval(iv);
                    if(boxes.length){
                        boxes.forEach(function(b){
                            var on=b.id.slice(-7)==='.canvas'?canvas:videos;
                            if(b.checked!==on) b.click();
                        });
                    }
                    try{ localStorage.setItem('splWebPrefs',want); }catch(e){}
                    setTimeout(function(){
                        try{
                            var st=history.state;
                            if(back&&((st&&st.splSynthetic)||location.pathname==='/preferences')) history.back();
                        }catch(e){}
                        busy=false;
                        if(queued){ var q=queued; queued=null; window.splSyncWebPrefs(q[0],q[1],true); }
                    },400);
                },250);
            };
            var waited=0;
            var boot=setInterval(function(){
                if(document.querySelector('[data-testid="user-widget-link"]')){
                    clearInterval(boot);
                    var p=window.__splWebPrefs||[true,true];
                    window.splSyncWebPrefs(p[0],p[1],false);
                } else if(++waited>60) clearInterval(boot);
            },2000);
        })();
    """
}