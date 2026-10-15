package com.project.lol.webview.injections

object ScrollbarAutoHide {
    const val CONTENT = """
        (function(){
            if(window.__splSbIdle) return;
            window.__splSbIdle = true;

            var HIDE_DELAY_MS = 2000;
            var LIVE_CLASS = 'splScrollLive';
            var NODE_SEL = '.main-view-container__scroll-node';
            var SB_SEL = NODE_SEL + '>.os-scrollbar';

            var docEl = document.documentElement;
            if(!docEl) return;

            var liveTimer = null;
            var nodeCache = null;
            var CAPTURE = {capture:true, passive:true};

            function featureOn(){
                return docEl.classList.contains('splScroll');
            }

            function mainNode(){
                if(!nodeCache || !nodeCache.isConnected){
                    nodeCache = document.querySelector(NODE_SEL);
                }
                return nodeCache;
            }

            function hideNow(){
                liveTimer = null;
                docEl.classList.remove(LIVE_CLASS);
            }

            function setLive(on){
                if(liveTimer){ clearTimeout(liveTimer); liveTimer = null; }
                if(on){
                    docEl.classList.add(LIVE_CLASS);
                    liveTimer = setTimeout(hideNow, HIDE_DELAY_MS);
                }else{
                    hideNow();
                }
            }

            var st = document.createElement('style');
            st.id = 'spl-sb-idle-style';
            st.textContent =
                'html.splScroll .main-view-container__scroll-node>.os-scrollbar{transition:opacity .45s ease!important}' +
                'html.splScroll.splScroll:not(.splScrollLive) .main-view-container__scroll-node>.os-scrollbar{opacity:0!important;pointer-events:none!important}' +
                'html.splScroll.splScrollLive .main-view-container__scroll-node>.os-scrollbar.os-scrollbar-visible{opacity:1!important;pointer-events:auto!important}';

            function appendStyle(){
                var t = document.head || docEl;
                if(t && !document.getElementById('spl-sb-idle-style')) t.appendChild(st);
            }
            try{ appendStyle(); }catch(e){}
            document.addEventListener('DOMContentLoaded', appendStyle);

            document.addEventListener('scroll', function(e){
                if(!featureOn()) return;
                var t = e.target;
                if(!t || t === document) return;
                var node = mainNode();
                if(node && (node === t || node.contains(t))) setLive(true);
            }, CAPTURE);

            var SB_EVENTS = ['pointerdown','pointermove','touchstart','touchmove','wheel'];
            function onBarActivity(e){
                if(!featureOn()) return;
                if(e.isTrusted === false) return;
                var t = e.target;
                if(t && t.closest && t.closest(SB_SEL)) setLive(true);
            }
            for(var i = 0; i < SB_EVENTS.length; i++){
                document.addEventListener(SB_EVENTS[i], onBarActivity, CAPTURE);
            }

            try{
                var origSb = window.splScrollbar;
                if(typeof origSb === 'function'){
                    window.splScrollbar = function(on){
                        try{ origSb(on); }catch(err){}
                        setLive(!!on);
                    };
                }
            }catch(e){}
        })();
    """
}