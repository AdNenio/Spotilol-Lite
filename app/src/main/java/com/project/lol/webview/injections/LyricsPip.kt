package com.project.lol.webview.injections

object LyricsPip {
    const val CONTENT = """
        (function(){
            if(window.__splLyricsPip) return;
            window.__splLyricsPip=true;

            var BTN_ID='spl-lyrics-pip';
            var SVG='<svg viewBox="0 0 16 16" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"><rect x="1.7" y="3.1" width="12.6" height="9.8" rx="1.6"/><rect x="8.1" y="7.7" width="5" height="3.5" rx="0.8" fill="currentColor" stroke="none"/></svg>';

            function lyricsContainer(){
                try{
                    var line=document.querySelector('[data-testid="lyrics-line"]');
                    if(!line) return null;
                    var e=line;
                    while(e&&e!==document.body){
                        var cs=window.getComputedStyle(e);
                        if((cs.overflowY==='scroll'||cs.overflowY==='auto')&&e.scrollHeight>e.clientHeight+10){
                            return e.parentElement||e;
                        }
                        e=e.parentElement;
                    }
                }catch(err){}
                return null;
            }

            window.__splLyricsFillApply=function(){
                try{
                    if(typeof window.__splPipFillMark!=='function') return false;
                    var wrap=lyricsContainer();
                    if(!wrap) return false;
                    window.__splPipFillMark(wrap);
                    window.__splPipFillUnblock(wrap);
                    var vv=window.visualViewport;
                    var vw=vv&&vv.width?Math.round(vv.width):(window.innerWidth||0);
                    var vh=vv&&vv.height?Math.round(vv.height):(window.innerHeight||0);
                    if(!(vw>0)||!(vh>0)) return false;
                    var vox=vv&&vv.offsetLeft?Math.round(vv.offsetLeft):0;
                    var voy=vv&&vv.offsetTop?Math.round(vv.offsetTop):0;
                    wrap.style.setProperty('position','fixed','important');
                    wrap.style.setProperty('transform','none','important');
                    wrap.style.setProperty('margin','0','important');
                    wrap.style.setProperty('padding','0','important');
                    wrap.style.setProperty('border','0','important');
                    wrap.style.setProperty('overflow','hidden','important');
                    wrap.style.setProperty('inset','auto','important');
                    wrap.style.setProperty('top',voy+'px','important');
                    wrap.style.setProperty('left',vox+'px','important');
                    wrap.style.setProperty('width',vw+'px','important');
                    wrap.style.setProperty('height',vh+'px','important');
                    wrap.style.setProperty('min-width','0','important');
                    wrap.style.setProperty('min-height','0','important');
                    wrap.style.setProperty('max-width','none','important');
                    wrap.style.setProperty('max-height','none','important');
                    wrap.style.setProperty('z-index','2147483646','important');
                    wrap.style.setProperty('background','#000','important');
                    window.__splPipFillTopLayer(wrap);
                    return true;
                }catch(err){}
                return false;
            };

            window.__splPipFillLyrics=function(on){
                try{
                    if(on){
                        if(!window.__splLyricsFillOn){
                            window.__splLyricsFillOn=true;
                            if(!window.__splLyricsFillHooked){
                                window.__splLyricsFillHooked=true;
                                var re=function(){ if(window.__splLyricsFillOn) window.__splLyricsFillApply(); };
                                window.addEventListener('orientationchange',re);
                                window.addEventListener('resize',re);
                                try{
                                    if(window.visualViewport){
                                        window.visualViewport.addEventListener('resize',re);
                                        window.visualViewport.addEventListener('scroll',re);
                                    }
                                }catch(err){}
                            }
                        }
                        if(!window.__splLyricsFillTimer){
                            window.__splLyricsFillTimer=setInterval(function(){
                                if(window.__splLyricsFillOn) window.__splLyricsFillApply();
                            },700);
                        }
                        return window.__splLyricsFillApply();
                    }
                    if(!window.__splLyricsFillOn) return true;
                    window.__splLyricsFillOn=false;
                    if(window.__splLyricsFillTimer){ clearInterval(window.__splLyricsFillTimer); window.__splLyricsFillTimer=null; }
                    if(typeof window.__splPipFillRestore==='function') window.__splPipFillRestore();
                    return true;
                }catch(err){}
                return false;
            };

            function lyricsMode(){
                try{
                    var radio=document.querySelector('button[role="radio"][value="lyrics"][aria-checked="true"]');
                    if(!radio) return false;
                    return document.querySelectorAll('[data-testid="lyrics-line"]').length>0;
                }catch(err){ return false; }
            }

            function findAnchor(){
                var btns=document.querySelectorAll('button[aria-label]');
                for(var i=0;i<btns.length;i++){
                    var a=(btns[i].getAttribute('aria-label')||'').trim();
                    if(a==='Minimize Now Playing view') return btns[i];
                }
                var all=document.querySelectorAll('button');
                for(var j=0;j<all.length;j++){
                    var p=all[j].querySelector('svg path');
                    if(p && (p.getAttribute('d')||'').indexOf('M14.53 1.47')===0) return all[j];
                }
                return null;
            }

            function enterPip(){
                try{ AndBridge.enterLyricsPip(); }catch(err){}
            }

            function ensureBtn(){
                try{
                    var cur=document.getElementById(BTN_ID);
                    if(!lyricsMode()){
                        if(cur&&cur.parentNode) cur.parentNode.removeChild(cur);
                        return;
                    }
                    if(cur&&cur.isConnected) return;
                    var anchor=findAnchor();
                    if(!anchor||!anchor.parentNode) return;
                    var btn=document.createElement('button');
                    btn.id=BTN_ID;
                    btn.type='button';
                    btn.className=anchor.className;
                    var encore=anchor.getAttribute('data-encore-id');
                    if(encore) btn.setAttribute('data-encore-id',encore);
                    btn.setAttribute('aria-label','Lyrics picture-in-picture');
                    btn.title='Lyrics PiP';
                    var wrapEl=anchor.firstElementChild;
                    var wrapCls=(wrapEl&&typeof wrapEl.className==='string')?wrapEl.className:'';
                    btn.innerHTML=wrapCls?'<span class="'+wrapCls+'">'+SVG+'</span>':SVG;
                    btn.addEventListener('click',function(ev){
                        ev.preventDefault(); ev.stopPropagation(); ev.stopImmediatePropagation();
                        enterPip();
                    },true);
                    anchor.parentNode.insertBefore(btn,anchor);
                }catch(err){}
            }

            window.__splLyricsPipEnsure=ensureBtn;
            if(window.__splFloaters) window.__splFloaters.push(ensureBtn);
            else setInterval(ensureBtn,800);

            ensureBtn();
        })();
    """
}
