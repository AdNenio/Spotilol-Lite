package com.project.lol.webview.injections

object AutoFeatures {
    const val CONTENT = """
            window.addAutoFeatures = function(){
                if('pBtn' in window && firstPlay && window.autoPlayMode!=='disabled' && window.splIsPlaying()===false) {
                    pBtn.click();
                    firstPlay=false;
                }
                if(afint) clearInterval(afint);
                afint = setInterval(function(){
                    if(window.__splBg || window.__splBusy) return;
                    if(window.closeNpPref) closeNowPlay();
                    var ft = document.querySelector('aside div.encore-bright-accent-set button');
                    var splRemote = (typeof window.__splRemoteActive === 'function') && window.__splRemoteActive();
                    if(ft && window.__splTakeControl && !splRemote) {
                        ft.click();
                        setTimeout(function(){
                            var cb = document.querySelector('aside ul[role=list] li[role=listitem] div[role=button]');
                            if(cb) cb.click();
                        },500);
                    }
                    if(window.autoPlayMode==='permanent' && 'pBtn' in window && !reqPause && !ulFlag && window.splIsPlaying()===false) {
                        pBtn.click();
                    }
                    if(window.autoPlayMode==='onetime' && !window.__splApDone && !window.__splApActive && 'pBtn' in window && !reqPause && window.splIsPlaying()===false) {
                        if(typeof splAutoPlay === 'function') splAutoPlay();
                    }
                },5000);
            };
        
    """
}
