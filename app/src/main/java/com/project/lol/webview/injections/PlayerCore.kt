package com.project.lol.webview.injections

object PlayerCore {
    const val CONTENT = """
            var reqPause=false,firstPlay=true,ulFlag=false,ffDone=false,npOpen=false;
            window.__splUnlocked=false;
            window.__splApActive=false;
            window.__splApDone=false;
            var featVer='web-player_'+new Date().toISOString().split('T')[0]+'_'+Date.now()+'_'+Math.floor(Math.random()*0xFFFFFFF).toString(16).padStart(7,'0');
            var lastState=null,lastPos=null,playing=false;
            var pfint=null,afint=null,cssint=null,aaint=null;
            window.__splBusy=false;
            (function(){
                var bt=null;
                function busy(){
                    window.__splBusy=true;
                    if(bt) clearTimeout(bt);
                    bt=setTimeout(function(){ window.__splBusy=false; },180);
                }
                var o={passive:true,capture:true};
                window.addEventListener('scroll',busy,o);
                window.addEventListener('wheel',busy,o);
                window.addEventListener('touchmove',busy,o);
                document.addEventListener('scroll',busy,o);
            })();
            try{
                if(typeof window.__splDbgOn==='undefined') window.__splDbgOn=false;
                var __splDbgFn=AndBridge.dbg.bind(AndBridge);
                AndBridge.dbg=function(l,m){
                    if(!window.__splDbgOn) return;
                    try{ __splDbgFn(l,m); }catch(e){}
                };
            }catch(e){}
            window.opHash=function(name,fb){var m=window.splOpHashes||{};return m[name]||fb;};
            window.splViewH=function(){
                try{if(window.visualViewport&&window.visualViewport.height)return window.visualViewport.height;}catch(e){}
                return document.documentElement.clientHeight||window.innerHeight||0;
            };
            window.splPlayerTop=function(){
                var p=document.getElementById('spotilolPlayerControls');
                if(p){var r=p.getBoundingClientRect();if(r.height>2)return r.top;}
                var o=document.querySelector('aside[data-testid="now-playing-bar"]');
                if(o){var s=getComputedStyle(o);if(s.display!=='none'&&s.visibility!=='hidden'){var r2=o.getBoundingClientRect();if(r2.height>2)return r2.top;}}
                return window.splViewH();
            };
            window.__splFloaters=[];
            setInterval(function(){
                if(window.__splBg||window.__splBusy) return;
                for(var i=0;i<window.__splFloaters.length;i++){
                    try{window.__splFloaters[i]();}catch(e){}
                }
            },250);
            if(typeof window.__splPbVal==='undefined') window.__splPbVal=null;
            window.splPbNode=function(){
                var n=document.querySelector('aside button[data-testid=control-button-playpause]');
                if(n) return n;
                n=document.querySelector('button[data-testid=control-button-playpause]');
                if(n) return n;
                if(window.pBtn) return window.pBtn;
                return null;
            };
            window.splMediaEl=function(){
                var els=document.querySelectorAll('audio,video'),best=null;
                for(var i=0;i<els.length;i++){
                    var e=els[i];
                    if(!e.currentSrc&&!e.src&&!e.srcObject) continue;
                    if(e.paused===false) return e;
                    if(!best&&e.readyState>=1) best=e;
                }
                return best;
            };
            window.splIsPlaying=function(){
                try{
                    var pb=window.splPbNode();
                    if(pb){
                        var ic=pb.querySelector('svg path');
                        var d=ic?(ic.getAttribute('d')||''):'';
                        if(d.indexOf('M2.7 1a.7.7')===0) return true;
                        if(d.indexOf('M3 1.713')===0) return false;
                        var al=pb.getAttribute('aria-label')||'';
                        if(/paus|wstrzymaj|duraklat|пауз|приостанов|一時停止|일시정지|暂停|暫停/i.test(al)) return true;
                        if(/^(play|lecture|spela|spil|afspil)$|reproduc|reproduz|wiedergabe|abspielen|riproduci|afspelen|odtwarzaj|воспроизв|再生|재생|播放|oynat/i.test(al)) return false;
                    }
                    var el=window.splMediaEl();
                    if(el) return el.paused===false;
                    var ms=navigator.mediaSession&&navigator.mediaSession.playbackState;
                    if(ms==='playing') return true;
                    if(ms==='paused') return false;
                    var r=document.querySelector('div[data-testid=playback-progressbar] input[type=range]');
                    if(r){
                        var v=parseFloat(r.value||'0');
                        if(!isNaN(v)){
                            var now=Date.now();
                            if(typeof window.__splPbVal!=='number'){ window.__splPbVal=v; window.__splPbAt=now; }
                            else{
                                if(v!==window.__splPbVal){ window.__splPbVal=v; window.__splPbAt=now; }
                                var dt=now-window.__splPbAt;
                                if(dt<1500) return true;
                                if(dt>3000) return false;
                            }
                        }
                    }
                }catch(e){}
                return null;
            };
            window.splIsPlayingSticky=function(){
                var s=window.splIsPlaying();
                if(s!==null) window.__splLastPlaying=s;
                return window.__splLastPlaying===true;
            };
            window.splQ=function(sel){
                var c=(window.__splQc||(window.__splQc={}))[sel];
                if(!c||!c.isConnected){ c=document.querySelector(sel); window.__splQc[sel]=c; }
                return c;
            };
    """
}