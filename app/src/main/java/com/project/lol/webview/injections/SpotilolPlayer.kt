package com.project.lol.webview.injections
/*
 * CREDIT: Spotilol - Custom Player.
 *
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⡀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣾⠙⠻⢶⣄⡀⠀⠀⠀⢀⣤⠶⠛⠛⡇⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⣇⠀⠀⣙⣿⣦⣤⣴⣿⣁⠀⠀⣸⠇⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⣡⣾⣿⣿⣿⣿⣿⣿⣿⣷⣌⠋⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣴⣿⣷⣄⡈⢻⣿⡟⢁⣠⣾⣿⣦⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⣿⣿⣿⣿⠘⣿⠃⣿⣿⣿⣿⡏⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⠀⠈⠛⣰⠿⣆⠛⠁⠀⡀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣼⣿⣦⠀⠘⠛⠋⠀⣴⣿⠁⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣤⣶⣾⣿⣿⣿⣿⡇⠀⠀⠀⢸⣿⣏⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⣠⣶⣿⣿⣿⣿⣿⣿⣿⣿⠿⠿⠀⠀⠀⠾⢿⣿⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⣠⣿⣿⣿⣿⣿⣿⡿⠟⠋⣁⣠⣤⣤⡶⠶⠶⣤⣄⠈⠀⠀⠀⠀⠀⠀
⠀⠀⠀⢰⣿⣿⣮⣉⣉⣉⣤⣴⣶⣿⣿⣋⡥⠄⠀⠀⠀⠀⠉⢻⣄⠀⠀⠀⠀⠀
⠀⠀⠀⠸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣟⣋⣁⣤⣀⣀⣤⣤⣤⣤⣄⣿⡄⠀⠀⠀⠀
⠀⠀⠀⠀⠙⠿⣿⣿⣿⣿⣿⣿⣿⡿⠿⠛⠋⠉⠁⠀⠀⠀⠀⠈⠛⠃⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠉⠉⠉⠉⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
 */


object SpotilolPlayer {
    val CONTENT: String = listOf(PART_1, PART_2).joinToString("")

    private const val PART_1 = """
            window.initSpotilolPlayer=function(){
                if(document.getElementById('spotilolPlayerControls')) return;
                var npb=document.querySelector('aside[data-testid="now-playing-bar"]');
                if(!npb) return;
                npb.style.display='none';

                function splFindShuffle(){
                    return (typeof window.splShuffleBtn === 'function') ? window.splShuffleBtn() : document.querySelector('button[data-testid="control-button-shuffle"]');
                }
                function splFindRepeat(){
                    return (typeof window.splRepeatBtn === 'function') ? window.splRepeatBtn() : document.querySelector('button[data-testid="control-button-repeat"]');
                }
                function splShuffleState(){
                    return (typeof window.splShuffleState === 'function') ? window.splShuffleState() : 'off';
                }
                var splAvBtn=null;
                function splVideoToggle(){
                    if(splAvBtn&&splAvBtn.isConnected) return splAvBtn;
                    var bs=document.querySelectorAll('button');
                    var hit=null;
                    for(var i=0;i<bs.length&&!hit;i++){
                        if(bs[i].closest('#spotilolPlayerControls')) continue;
                        var a=(bs[i].getAttribute('aria-label')||'').trim().toLowerCase();
                        if(a==='switch to audio'||a==='switch to video') hit=bs[i];
                    }
                    for(var j=0;j<bs.length&&!hit;j++){
                        if(bs[j].closest('#spotilolPlayerControls')) continue;
                        var p=bs[j].querySelector('svg path');
                        var d=p?(p.getAttribute('d')||''):'';
                        if(d.indexOf('M4 1h11v11.75')===0||d.indexOf('M1.75 2.5a.25.25 0 0 0-.25.25v10.5')===0) hit=bs[j];
                    }
                    splAvBtn=hit;
                    return hit;
                }
                function splPageVideo(){
                    return document.querySelector('.VideoPlayer__container video');
                }
                var splIconVideo='<svg viewBox="0 0 16 16"><path fill="currentColor" d="M1.75 2.5a.25.25 0 0 0-.25.25v10.5c0 .138.112.25.25.25h12.5a.25.25 0 0 0 .25-.25V2.75a.25.25 0 0 0-.25-.25zM0 2.75C0 1.784.784 1 1.75 1h12.5c.967 0 1.75.784 1.75 1.75v10.5A1.75 1.75 0 0 1 14.25 15H1.75A1.75 1.75 0 0 1 0 13.25z"/><path fill="currentColor" d="m6 5 5.196 3L6 11z"/></svg>';
                var splIconAudio='<svg viewBox="0 0 16 16"><path fill="currentColor" d="M4 1h11v11.75A2.75 2.75 0 1 1 12.25 10h1.25V2.5h-8v10.25A2.75 2.75 0 1 1 2.75 10H4zm0 10.5H2.75A1.25 1.25 0 1 0 4 12.75zm9.5 0h-1.25a1.25 1.25 0 1 0 1.25 1.25z"/></svg>';

                var pl=document.createElement('div');
                pl.id='spotilolPlayerControls';
                pl.innerHTML=''
                    +'<div class="spl-np-head">'
                    +'<button class="spl-btn" id="spl-collapse" aria-label="Collapse player"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M2.793 8.043a1 1 0 0 1 1.414 0L12 15.836l7.793-7.793a1 1 0 1 1 1.414 1.414L12 18.664 2.793 9.457a1 1 0 0 1 0-1.414z"/></svg></button>'
                    +'<div class="spl-np-title">Now playing</div>'
                    +'<span class="spl-np-spacer"></span>'
                    +'</div>'
                    +'<div class="spl-top">'
                    +'<div class="spl-cover"><img id="spl-cover-img" src="" alt=""></div>'
                    +'<div class="spl-info"><div class="spl-track" id="spl-track">No track</div>'
                    +'<div class="spl-artist" id="spl-artist">\u2014</div>'
                    +'<button class="spl-avswitch" id="spl-avtoggle" type="button" aria-label="Switch to video" style="display:none;"><span class="spl-avswitch-i" id="spl-avtoggle-i" data-k="v"><svg viewBox="0 0 16 16"><path fill="currentColor" d="M1.75 2.5a.25.25 0 0 0-.25.25v10.5c0 .138.112.25.25.25h12.5a.25.25 0 0 0 .25-.25V2.75a.25.25 0 0 0-.25-.25zM0 2.75C0 1.784.784 1 1.75 1h12.5c.967 0 1.75.784 1.75 1.75v10.5A1.75 1.75 0 0 1 14.25 15H1.75A1.75 1.75 0 0 1 0 13.25z"/><path fill="currentColor" d="m6 5 5.196 3L6 11z"/></svg></span><span id="spl-avtoggle-l">Switch to video</span></button>'
                    +'</div>'
                    +'<button class="spl-btn spl-btn-sm spl-liked-btn" id="spl-liked" aria-label="Like"><svg viewBox="0 0 16 16"><path fill="currentColor" d="M15.724 4.22A4.313 4.313 0 0 0 12.192.814a4.269 4.269 0 0 0-3.622 1.13.837.837 0 0 1-1.14 0 4.272 4.272 0 0 0-6.38 5.69l5.4 6.06a1.09 1.09 0 0 0 1.504.06l5.397-5.892a4.32 4.32 0 0 0 1.253-3.436z"/></svg></button>'
                    +'<div class="spl-mini-transport">'
                    +'<button class="spl-btn" id="spl-prev-mini" aria-label="Previous"><svg viewBox="0 0 16 16"><path fill="currentColor" d="M3.3 1a.7.7 0 0 1 .7.7v5.15l9.95-5.744a.7.7 0 0 1 1.05.606v12.575a.7.7 0 0 1-1.05.607L4 9.149V14.3a.7.7 0 0 1-.7.7H1.7a.7.7 0 0 1-.7-.7V1.7a.7.7 0 0 1 .7-.7z"/></svg></button>'
                    +'<button class="spl-btn spl-play" id="spl-play-mini" aria-label="Play"><svg viewBox="0 0 16 16"><path fill="currentColor" d="M3 1.713a.7.7 0 0 1 1.05-.607l10.89 6.288a.7.7 0 0 1 0 1.212L4.05 14.894A.7.7 0 0 1 3 14.288z"/></svg></button>'
                    +'<button class="spl-btn" id="spl-next-mini" aria-label="Next"><svg viewBox="0 0 16 16"><path fill="currentColor" d="M12.7 1a.7.7 0 0 0-.7.7v5.15L2.05 1.107A.7.7 0 0 0 1 1.712v12.575a.7.7 0 0 0 1.05.607L12 9.149V14.3a.7.7 0 0 0 .7.7h1.6a.7.7 0 0 0 .7-.7V1.7a.7.7 0 0 0-.7-.7z"/></svg></button>'
                    +'</div>'
                    +'</div>'
                    +'<div class="spl-row2">'
                    +'<div class="spl-actions-left">'
                    +'<button class="spl-btn spl-btn-sm spl-ep-only spl-speed" id="spl-speed" aria-label="Playback speed"><span id="spl-speed-l">1\u00d7</span></button>'
                    +'<button class="spl-btn spl-btn-sm" id="spl-timer" aria-label="Timer"><svg viewBox="0 0 20 20"><path fill="currentColor" d="M16.32 7.1A8 8 0 1 1 9 4.06V2h2v2.06c1.46.18 2.8.76 3.9 1.62l1.46-1.46l1.42 1.42l-1.46 1.45zM10 18a6 6 0 1 0 0-12a6 6 0 0 0 0 12zM7 0h6v2H7V0zm5.12 8.46l1.42 1.42L10 13.4L8.59 12l3.53-3.54z"/></svg></button>'
                    +'<button class="spl-btn spl-btn-sm" id="spl-nptoggle" aria-label="Now Playing"><svg viewBox="0 0 16 17"><rect x="1" y="0.75" width="14" height="15.5" rx="2" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M 6 5 L 6 5.9160156 L 9.6933594 8.5 L 6 11.080078 L 6 12 L 11 8.5 L 6 5 z" stroke="currentColor" stroke-width="1.2"/></svg></button>'
                    +'<button class="spl-btn spl-btn-sm" id="spl-queue" aria-label="Queue"><svg viewBox="0 0 16 16"><path fill="currentColor" d="M15 15H1v-1.5h14zm0-4.5H1V9h14zm-14-7A2.5 2.5 0 0 1 3.5 1h9a2.5 2.5 0 0 1 0 5h-9A2.5 2.5 0 0 1 1 3.5m2.5-1a1 1 0 0 0 0 2h9a1 1 0 1 0 0-2z"/></svg></button>'
                    +'<button class="spl-btn spl-btn-sm" id="spl-connect" aria-label="Connect to a device"><svg viewBox="0 0 16 16"><path fill="currentColor" d="M6 2.75C6 1.784 6.784 1 7.75 1h6.5c.966 0 1.75.784 1.75 1.75v10.5A1.75 1.75 0 0 1 14.25 15h-6.5A1.75 1.75 0 0 1 6 13.25zm1.75-.25a.25.25 0 0 0-.25.25v10.5c0 .138.112.25.25.25h6.5a.25.25 0 0 0 .25-.25V2.75a.25.25 0 0 0-.25-.25zm-6 0a.25.25 0 0 0-.25.25v6.5c0 .138.112.25.25.25H4V11H1.75A1.75 1.75 0 0 1 0 9.25v-6.5C0 1.784.784 1 1.75 1H4v1.5zM4 15H2v-1.5h2z"/><path fill="currentColor" d="M13 10a2 2 0 1 1-4 0 2 2 0 0 1 4 0m-1-5a1 1 0 1 1-2 0 1 1 0 0 1 2 0"/></svg></button>'
                    +'<button class="spl-btn spl-btn-sm spl-vol-btn" id="spl-vol-btn" aria-label="Volume"><svg viewBox="0 0 16 16"><path fill="currentColor" d="M9.741.85a.75.75 0 0 1 .375.65v13a.75.75 0 0 1-1.125.65l-6.925-4a3.64 3.64 0 0 1-1.33-4.967 3.64 3.64 0 0 1 1.33-1.332l6.925-4a.75.75 0 0 1 .75 0zm-6.924 5.3a2.14 2.14 0 0 0 0 3.7l5.8 3.35V2.8zm8.683 4.29V5.56a2.75 2.75 0 0 1 0 4.88"/><path fill="currentColor" d="M11.5 13.614a5.752 5.752 0 0 0 0-11.228v1.55a4.252 4.252 0 0 1 0 8.127z"/></svg></button>'
                    +'</div>'
                    +'</div>'
                    +'<div class="spl-bottom">'
                    +'<span class="spl-time" id="spl-pos">0:00</span>'
                    +'<div class="spl-bar-wrap"><div class="spl-bar" id="spl-bar"><div class="spl-fill" id="spl-fill"></div><div class="spl-handle" id="spl-handle"></div></div></div>'
                    +'<span class="spl-time" id="spl-dur">0:00</span>'
                    +'</div>'
                    +'<div class="spl-edgebar" id="spl-edgebar"><div class="spl-fill" id="spl-fill-edge"></div></div>'
                    +'<div class="spl-transport">'
                    +'<button class="spl-btn spl-btn-sm" id="spl-shuffle" aria-label="Shuffle"><svg viewBox="0 0 16 16"><path fill="currentColor" d="M13.151.922a.75.75 0 1 0-1.06 1.06L13.109 3H11.16a3.75 3.75 0 0 0-2.873 1.34l-6.173 7.356A2.25 2.25 0 0 1 .39 12.5H0V14h.391a3.75 3.75 0 0 0 2.873-1.34l6.173-7.356a2.25 2.25 0 0 1 1.724-.804h1.947l-1.017 1.018a.75.75 0 0 0 1.06 1.06L15.98 3.75zM.391 3.5H0V2h.391c1.109 0 2.16.49 2.873 1.34L4.89 5.277l-.979 1.167-1.796-2.14A2.25 2.25 0 0 0 .39 3.5zm7.758 6.22l.979-1.167 1.35 1.605a2.25 2.25 0 0 0 1.724.804h1.947l-1.017-1.018a.75.75 0 1 1 1.06-1.06l2.829 2.828-2.829 2.828a.75.75 0 1 1-1.06-1.06L13.109 13H11.16a3.75 3.75 0 0 1-2.873-1.34l-1.138-1.94z"/></svg></button>'
                    +'<button class="spl-btn spl-btn-sm spl-ep-only" id="spl-seekb" aria-label="Skip back 15 seconds"><svg viewBox="0 0 16 16"><path fill="none" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" d="M3.2 5.2A5.6 5.6 0 1 1 2.4 8"/><path fill="currentColor" d="M1.6 2.4v3.8h3.8z"/><text x="8.3" y="10.6" text-anchor="middle" font-size="5.6" font-weight="700" fill="currentColor" font-family="sans-serif">15</text></svg></button>'
                    +'<button class="spl-btn" id="spl-prev" aria-label="Previous"><svg viewBox="0 0 16 16"><path fill="currentColor" d="M3.3 1a.7.7 0 0 1 .7.7v5.15l9.95-5.744a.7.7 0 0 1 1.05.606v12.575a.7.7 0 0 1-1.05.607L4 9.149V14.3a.7.7 0 0 1-.7.7H1.7a.7.7 0 0 1-.7-.7V1.7a.7.7 0 0 1 .7-.7z"/></svg></button>'
                    +'<button class="spl-btn spl-play" id="spl-play" aria-label="Play"><svg viewBox="0 0 16 16"><path fill="currentColor" d="M3 1.713a.7.7 0 0 1 1.05-.607l10.89 6.288a.7.7 0 0 1 0 1.212L4.05 14.894A.7.7 0 0 1 3 14.288z"/></svg></button>'
                    +'<button class="spl-btn" id="spl-next" aria-label="Next"><svg viewBox="0 0 16 16"><path fill="currentColor" d="M12.7 1a.7.7 0 0 0-.7.7v5.15L2.05 1.107A.7.7 0 0 0 1 1.712v12.575a.7.7 0 0 0 1.05.607L12 9.149V14.3a.7.7 0 0 0 .7.7h1.6a.7.7 0 0 0 .7-.7V1.7a.7.7 0 0 0-.7-.7z"/></svg></button>'
                    +'<button class="spl-btn spl-btn-sm spl-ep-only" id="spl-seekf" aria-label="Skip forward 15 seconds"><svg viewBox="0 0 16 16"><g transform="matrix(-1 0 0 1 16 0)"><path fill="none" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" d="M3.2 5.2A5.6 5.6 0 1 1 2.4 8"/><path fill="currentColor" d="M1.6 2.4v3.8h3.8z"/></g><text x="7.7" y="10.6" text-anchor="middle" font-size="5.6" font-weight="700" fill="currentColor" font-family="sans-serif">15</text></svg></button>'
                    +'<button class="spl-btn spl-btn-sm" id="spl-repeat" aria-label="Repeat"><svg viewBox="0 0 16 16"><path fill="currentColor" d="M0 4.75A3.75 3.75 0 0 1 3.75 1h8.5A3.75 3.75 0 0 1 16 4.75v5a3.75 3.75 0 0 1-3.75 3.75H9.81l1.018 1.018a.75.75 0 1 1-1.06 1.06L6.939 12.75l2.829-2.828a.75.75 0 1 1 1.06 1.06L9.811 12h2.439a2.25 2.25 0 0 0 2.25-2.25v-5a2.25 2.25 0 0 0-2.25-2.25h-8.5A2.25 2.25 0 0 0 1.5 4.75v5A2.25 2.25 0 0 0 3.75 12H5v1.5H3.75A3.75 3.75 0 0 1 0 9.75z"/></svg></button>'
                    +'</div>';

                document.body.appendChild(pl);
                if(window.__splHideEmpty) pl.classList.add('spl-empty');
                    document.body.appendChild(pl);
                    
                    window.splApplyEmpty=function(){
                        var t=document.getElementById('spl-track');
                        var empty=!t||!t.textContent||t.textContent==='No track';
                        if(window.__splHideEmpty&&empty) pl.classList.add('spl-empty');
                        else pl.classList.remove('spl-empty');
                    };

                    window.splApplyHideTopBar=function(){
                        pl.style.setProperty('--spl-np-head-top', window.__splHideTopBar?'6vh':'0px');
                    };
                    window.splApplyHideTopBar();

                if(!document.getElementById('spl-vol-css')){
                    var sst=document.createElement('style');sst.id='spl-vol-css';
                    sst.textContent='#spotilolPlayerControls .spl-top .spl-liked-btn{margin-left:-8px}#spotilolPlayerControls.spl-mini .spl-top .spl-liked-btn{padding:4px;min-width:30px;min-height:30px}#spotilolPlayerControls.spl-mini .spl-top .spl-liked-btn svg{width:14px;height:14px}#spotilolPlayerControls.spl-empty{opacity:0!important;pointer-events:none!important;transform:translateY(24px)!important}';
                    var t=document.head||document.documentElement;if(t)t.appendChild(sst);
                }

                if(!document.getElementById('spl-full-css')){
                    var fst=document.createElement('style');fst.id='spl-full-css';
                    fst.textContent=[
                        '#spotilolPlayerControls .spl-np-head{display:none}',
                        '#spotilolPlayerControls.spl-fsmode.spl-mini{background:var(--spl-np-mini,rgba(24,24,24,.95))!important;transition:transform .3s cubic-bezier(.2,.8,.2,1),opacity .3s,padding .3s,background-color .4s}',
                        '#spotilolPlayerControls.spl-fsmode.spl-mini .spl-cover img{width:44px!important;height:44px!important;border-radius:6px!important;-webkit-mask-image:none!important;mask-image:none!important}',
                        '#spotilolPlayerControls.spl-fsmode.spl-mini .spl-edgebar{left:10px;right:10px;bottom:4px;height:2px;border-radius:1px;background:rgba(255,255,255,.2)}',
                        '#spotilolPlayerControls.spl-fsmode.spl-mini .spl-edgebar .spl-fill{background:#fff}',
                        '#spotilolPlayerControls.spl-full{position:fixed!important;top:0!important;bottom:0!important;left:0!important;right:0!important;max-width:none!important;margin:0!important;border:none!important;border-radius:0!important;box-shadow:none!important;padding:0 24px 28px!important;overflow:hidden!important;background:linear-gradient(180deg,var(--spl-np-color,#404040) 0%,#121212 78%)!important;font-family:var(--encore-body-font-stack,-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,Helvetica,Arial,sans-serif);animation:splNpIn .32s cubic-bezier(.2,.8,.2,1)}',
                        '@keyframes splNpIn{from{transform:translateY(100%)}to{transform:none}}',
                        '#spotilolPlayerControls.spl-full .spl-np-head{display:flex;align-items:center;justify-content:space-between;order:0;height:64px;flex-shrink:0;margin:var(--spl-np-head-top,0px) -12px 0}',
                        '#spotilolPlayerControls.spl-full .spl-np-head .spl-btn{color:#fff}',
                        '#spotilolPlayerControls.spl-full .spl-np-head svg{width:24px;height:24px}',
                        '#spotilolPlayerControls.spl-full .spl-np-title{font-size:12px;font-weight:700;letter-spacing:.08em;text-transform:uppercase;color:#fff}',
                        '#spotilolPlayerControls.spl-full .spl-np-spacer{width:44px}',
                        '#spotilolPlayerControls.spl-full .spl-top{order:1;flex:1 1 auto;flex-wrap:wrap;align-content:center;row-gap:28px;column-gap:8px;margin:0!important;min-height:0}',
                        '#spotilolPlayerControls.spl-full .spl-cover{flex:0 0 100%;display:flex;justify-content:center}',
                        '#spotilolPlayerControls.spl-full .spl-cover img{width:max(140px,min(calc(100vw - 48px),calc(100vh - 400px)))!important;height:auto!important;aspect-ratio:1/1;border-radius:8px!important;box-shadow:0 12px 40px rgba(0,0,0,.55);-webkit-mask-image:none!important;mask-image:none!important}',
                        '#spotilolPlayerControls.spl-full .spl-track{font-size:22px!important;font-weight:700;line-height:1.25}',
                        '#spotilolPlayerControls.spl-full .spl-artist{font-size:15px!important;color:rgba(255,255,255,.7);margin-top:2px}',
                        '#spotilolPlayerControls.spl-full .spl-top .spl-liked-btn{margin:0 -10px 0 0}',
                        '#spotilolPlayerControls.spl-full .spl-top .spl-liked-btn svg{width:24px;height:24px}',
                        '#spotilolPlayerControls.spl-full .spl-bottom{order:2;flex-wrap:wrap;justify-content:space-between;gap:0;margin:20px 0 6px!important;max-height:none!important}',
                        '#spotilolPlayerControls.spl-full .spl-bar-wrap{flex:0 0 100%;height:20px;order:0}',
                        '#spotilolPlayerControls.spl-full .spl-bar{background:rgba(255,255,255,.25)}',
                        '#spotilolPlayerControls.spl-full .spl-fill{background:#fff}',
                        '#spotilolPlayerControls.spl-full .spl-handle{opacity:1}',
                        '#spotilolPlayerControls.spl-full .spl-time{order:1;font-size:11px;color:rgba(255,255,255,.7);min-width:0;margin-top:2px}',
                        '#spotilolPlayerControls.spl-full .spl-transport{order:3;justify-content:space-between;margin:4px -8px 0;max-height:none!important}',
                        '#spotilolPlayerControls.spl-full .spl-transport .spl-btn{color:#fff}',
                        '#spotilolPlayerControls.spl-full .spl-transport .spl-btn.spl-active{color:var(--spl-accent,#1db954)}',
                        '#spotilolPlayerControls.spl-full .spl-transport .spl-btn-sm svg{width:24px;height:24px}',
                        '#spotilolPlayerControls.spl-full .spl-transport .spl-btn:not(.spl-btn-sm):not(.spl-play) svg{width:32px;height:32px}',
                        '#spotilolPlayerControls.spl-full .spl-transport .spl-play{background:#fff!important;color:#000!important;min-width:68px;min-height:68px;padding:0!important}',
                        '#spotilolPlayerControls.spl-full .spl-transport .spl-play svg{width:26px;height:26px}',
                        '#spotilolPlayerControls.spl-full .spl-row2{order:4;margin:20px -6px 0!important;max-height:none!important}',
                        '#spotilolPlayerControls.spl-full .spl-actions-left{flex:1;justify-content:space-between}',
                        '#spotilolPlayerControls.spl-full .spl-row2 .spl-btn svg{width:20px;height:20px}',
                        '#spotilolPlayerControls.spl-full .spl-edgebar,#spotilolPlayerControls.spl-full .spl-mini-transport{display:none}',
                        '#spotilolPlayerControls .spl-ep-only{display:none!important}',
                        '#spotilolPlayerControls.spl-full.spl-episode .spl-ep-only{display:flex!important}',
                        '#spotilolPlayerControls.spl-full.spl-episode #spl-shuffle,#spotilolPlayerControls.spl-full.spl-episode #spl-repeat{display:none!important}',
                        '#spotilolPlayerControls.spl-full .spl-transport .spl-ep-only svg{width:28px;height:28px}',
                        '#spotilolPlayerControls.spl-full .spl-speed span{font-size:13px;font-weight:700;color:#fff;min-width:28px;text-align:center}',
                        '#spotilolPlayerControls #spl-speed-sheet{display:none}',
                        '#spotilolPlayerControls #spl-canvas,#spotilolPlayerControls #spl-canvas-shade{display:none}',
                        '#spotilolPlayerControls.spl-full.spl-canvas #spl-canvas{display:block;position:absolute;inset:0;width:100%;height:100%;object-fit:cover;z-index:-1;pointer-events:none}',
                        '#spotilolPlayerControls.spl-full.spl-canvas #spl-canvas-shade{display:block;position:absolute;inset:0;z-index:-1;pointer-events:none;background:linear-gradient(180deg,rgba(0,0,0,.45) 0%,rgba(0,0,0,0) 22%,rgba(0,0,0,0) 45%,rgba(0,0,0,.88) 78%)}',
                        '#spotilolPlayerControls.spl-full.spl-canvas .spl-cover{visibility:hidden}',
                        '#spotilolPlayerControls #spl-video{display:none}',
                        '#spotilolPlayerControls.spl-full.spl-video-on #spl-video{display:block;position:absolute;inset:0;z-index:-1;background:#000;pointer-events:none}',
                        '#spotilolPlayerControls #spl-video video{position:static!important;display:block!important;width:100%!important;height:100%!important;object-fit:contain!important;transform:none!important;opacity:1!important;visibility:visible!important}',
                        '#spotilolPlayerControls.spl-video-tall #spl-video video{object-fit:cover!important}',
                        '#spotilolPlayerControls.spl-full.spl-video-on .spl-cover{visibility:hidden}',
                        '#spotilolPlayerControls.spl-full.spl-video-on>*:not(#spl-video){transition:opacity .25s}',
                        '#spotilolPlayerControls.spl-full.spl-video-on .spl-top>*{transition:opacity .25s}',
                        '#spotilolPlayerControls.spl-full.spl-video-on.spl-ctl-hidden>*:not(#spl-video){opacity:0;pointer-events:none}',
                        '#spotilolPlayerControls.spl-full.spl-video-on.spl-ctl-hidden .spl-top>*{opacity:0;pointer-events:none}',
                        '#spotilolPlayerControls.spl-full.spl-video-on #spl-canvas-shade{display:block;position:absolute;inset:0;z-index:-1;pointer-events:none;background:linear-gradient(180deg,rgba(0,0,0,.45) 0%,rgba(0,0,0,0) 22%,rgba(0,0,0,0) 45%,rgba(0,0,0,.88) 78%)}',
                        '#spotilolPlayerControls .spl-canvas-toggle{visibility:hidden}',
                        '#spotilolPlayerControls.spl-full:not(.spl-episode) .spl-canvas-toggle{visibility:visible}',
                        '#spotilolPlayerControls.spl-full:not(.spl-has-canvas) .spl-canvas-toggle{opacity:.4}',
                        '#spotilolPlayerControls.spl-full .spl-canvas-toggle svg{width:22px;height:22px}',
                        '#spotilolPlayerControls.spl-full .spl-np-head .spl-canvas-toggle.spl-active{color:var(--spl-accent,#1db954)}',
                        '#spotilolPlayerControls .spl-avswitch{display:inline-flex;align-items:center;gap:8px;align-self:flex-start;width:fit-content;margin-top:12px;padding:0 14px 0 12px;height:34px;border:0;border-radius:17px;background:rgba(255,255,255,.12);color:#fff;font:600 13px/1 inherit;cursor:pointer;flex-shrink:0;transition:background .15s}',
                        '#spotilolPlayerControls .spl-avswitch:hover{background:rgba(255,255,255,.2)}',
                        '#spotilolPlayerControls .spl-avswitch:active{transform:scale(.97)}',
                        '#spotilolPlayerControls .spl-avswitch .spl-avswitch-i{display:flex;align-items:center;flex-shrink:0}',
                        '#spotilolPlayerControls .spl-avswitch svg{width:16px;height:16px;fill:currentColor}',
                        '#spotilolPlayerControls:not(.spl-full) .spl-avswitch{display:none!important}',
                        'html.spl-hide-ctx #context-menu,html.spl-hide-ctx [data-tippy-root],html.spl-hide-ctx [role="menu"]{opacity:0!important}',
                        '#spotilolPlayerControls.spl-full #spl-speed-sheet.spl-open{display:flex;position:absolute;inset:0;z-index:5;align-items:flex-end;background:rgba(0,0,0,.55)}',
                        '#spotilolPlayerControls .spl-sheet-card{width:100%;box-sizing:border-box;background:#282828;border-radius:16px 16px 0 0;padding:18px 18px 28px}',
                        '#spotilolPlayerControls .spl-sheet-title{font-size:15px;font-weight:700;margin-bottom:14px}',
                        '#spotilolPlayerControls .spl-sheet-opts{display:grid;grid-template-columns:repeat(4,1fr);gap:10px}',
                        '#spotilolPlayerControls .spl-speed-opt{background:rgba(255,255,255,.08);border:0;border-radius:20px;color:#fff;font:600 14px/1 inherit;padding:12px 0;cursor:pointer}',
                        '#spotilolPlayerControls .spl-speed-opt.spl-on{background:var(--spl-accent,#1db954);color:#000}',
                        '@media(orientation:landscape){',
                        '#spotilolPlayerControls.spl-full{display:grid!important;grid-template-columns:auto 1fr;grid-template-rows:56px 1fr auto auto auto;grid-template-areas:"head head" "cover info" "cover seek" "cover transport" "cover actions";column-gap:36px;padding-bottom:16px!important}',
                        '#spotilolPlayerControls.spl-full .spl-np-head{grid-area:head;height:56px}',
                        '#spotilolPlayerControls.spl-full .spl-top{display:contents}',
                        '#spotilolPlayerControls.spl-full .spl-cover{grid-area:cover;align-self:center}',
                        '#spotilolPlayerControls.spl-full .spl-cover img{width:min(calc(100vh - 96px),40vw)!important}',
                        '#spotilolPlayerControls.spl-full .spl-info{grid-area:info;align-self:end;padding-right:48px}',
                        '#spotilolPlayerControls.spl-full .spl-top .spl-liked-btn{grid-area:info;justify-self:end;align-self:end}',
                        '#spotilolPlayerControls.spl-full .spl-bottom{grid-area:seek;margin:12px 0 0!important}',
                        '#spotilolPlayerControls.spl-full .spl-transport{grid-area:transport}',
                        '#spotilolPlayerControls.spl-full .spl-transport .spl-play{min-width:56px;min-height:56px}',
                        '#spotilolPlayerControls.spl-full .spl-row2{grid-area:actions;margin-top:8px!important}',
                        '#spotilolPlayerControls.spl-full.spl-video-on{grid-template-columns:1fr;grid-template-rows:48px 1fr auto auto;grid-template-areas:"head" "info" "seek" "ctl";column-gap:0;padding:8px 28px 10px!important}',
                        '#spotilolPlayerControls.spl-full.spl-video-on .spl-cover{display:none}',
                        '#spotilolPlayerControls.spl-full.spl-video-on .spl-np-head{height:48px}',
                        '#spotilolPlayerControls.spl-full.spl-video-on .spl-bottom{margin:6px 0 0!important}',
                        '#spotilolPlayerControls.spl-full.spl-video-on .spl-transport{grid-area:ctl;justify-self:center;z-index:1}',
                        '#spotilolPlayerControls.spl-full.spl-video-on .spl-row2{grid-area:ctl;align-self:center;margin:0!important}',
                        '#spotilolPlayerControls.spl-full.spl-video-on .spl-actions-left{display:flex!important;width:100%;justify-content:flex-start}',
                        '#spotilolPlayerControls.spl-full.spl-video-on #spl-nptoggle{display:none!important}',
                        '#spotilolPlayerControls.spl-full.spl-video-on #spl-queue{margin-left:auto}',
                        '}'
                    ].join('');
                    var ft=document.head||document.documentElement;if(ft)ft.appendChild(fst);
                }

                document.getElementById('spl-prev').onclick=function(){actSkipBack()};
                document.getElementById('spl-next').onclick=function(){actSkipForward()};
                document.getElementById('spl-play').onclick=function(){var st=window.splIsPlaying();actPlayPause(st===null?null:!st)};
                document.getElementById('spl-prev-mini').onclick=function(){actSkipBack()};
                document.getElementById('spl-next-mini').onclick=function(){actSkipForward()};
                document.getElementById('spl-play-mini').onclick=function(){var st=window.splIsPlaying();actPlayPause(st===null?null:!st)};
                document.getElementById('spl-shuffle').onclick=function(){var sb=splFindShuffle();if(sb&&sb.getAttribute('aria-disabled')!=='true')sb.click()};
                document.getElementById('spl-repeat').onclick=function(){actRepeat()};
                document.getElementById('spl-queue').onclick=function(){splCollapseFull();var qb=document.querySelector('button[data-testid=control-button-queue]');if(qb)qb.click()};
                document.getElementById('spl-vol-btn').onclick=function(){AndBridge.openVolumeDialog()};
                document.getElementById('spl-connect').onclick=function(){var cb=document.querySelector('button[aria-describedby="connect-message-nudge"]');if(cb)cb.click()};
                document.getElementById('spl-avtoggle').onclick=function(){var t=splVideoToggle();if(!t)return;if(/switch to audio/i.test(t.getAttribute('aria-label')||'')){var s=document.getElementById('spl-video');var v=s?s.querySelector('video'):null;if(v&&splVidHome&&splVidHome.isConnected){splVidHome.appendChild(v);}splVidSuppress=Date.now()+1500;}t.click()};
                document.getElementById('spl-nptoggle').onclick=function(){splCollapseFull();clickNP()};
                document.getElementById('spl-collapse').onclick=function(){splSetMini(true)};
                document.getElementById('spl-timer').onclick=function(){AndBridge.openTimerDialog()};
                window.__splPipFillMark=function(n){
                    if(!n) return;
                    try{
                        if(n.__splSavedStyle===undefined){
                            n.__splSavedStyle=n.getAttribute('style');
                            window.__splPipFillList=window.__splPipFillList||[];
                            window.__splPipFillList.push(n);
                        }
                    }catch(e){}
                };
                window.__splPipFillUnblock=function(n){
                    var props=[['transform','none'],['translate','none'],['rotate','none'],['scale','none'],['will-change','auto'],['contain','none'],['container-type','normal'],['filter','none'],['perspective','none'],['backdrop-filter','none'],['content-visibility','visible']];
                    var el=n;
                    while(el&&el!==document.documentElement){
                        try{
                            var cs=window.getComputedStyle(el);
                            for(var i=0;i<props.length;i++){
                                var cur=cs.getPropertyValue(props[i][0]);
                                if(cur&&cur!=='none'&&cur!=='normal'&&cur!=='auto'&&cur!=='visible'){
                                    window.__splPipFillMark(el);
                                    el.style.setProperty(props[i][0],props[i][1],'important');
                                }
                            }
                        }catch(e){}
                        el=el.parentElement;
                    }
                };
                window.__splPipFillApply=function(){
                    try{
                        var c=document.querySelector('.VideoPlayer__container');
                        var v=c?c.querySelector('video'):null;
                        if(!c||!v) return false;
                        var ctl=document.getElementById('spotilolPlayerControls');
                        window.__splPipFillMark(c);window.__splPipFillMark(v);window.__splPipFillMark(ctl);
                        window.__splPipFillUnblock(c);
                        var vv=window.visualViewport;
                        var vw=vv&&vv.width?Math.round(vv.width):(window.innerWidth||0);
                        var vh=vv&&vv.height?Math.round(vv.height):(window.innerHeight||0);
                        if(!(vw>0)||!(vh>0)) return false;
                        var vox=vv&&vv.offsetLeft?Math.round(vv.offsetLeft):0;
                        var voy=vv&&vv.offsetTop?Math.round(vv.offsetTop):0;
                        c.style.setProperty('position','fixed','important');
                        c.style.setProperty('transform','none','important');
                        c.style.setProperty('margin','0','important');
                        c.style.setProperty('padding','0','important');
                        c.style.setProperty('border','0','important');
                        c.style.setProperty('overflow','hidden','important');
                        c.style.setProperty('inset','auto','important');
                        c.style.setProperty('top',voy+'px','important');
                        c.style.setProperty('left',vox+'px','important');
                        c.style.setProperty('width',vw+'px','important');
                        c.style.setProperty('height',vh+'px','important');
                        c.style.setProperty('min-width','0','important');
                        c.style.setProperty('min-height','0','important');
                        c.style.setProperty('max-width','none','important');
                        c.style.setProperty('max-height','none','important');
                        c.style.setProperty('z-index','2147483646','important');
                        c.style.setProperty('background','#000','important');
                        v.style.setProperty('width','100%','important');
                        v.style.setProperty('height','100%','important');
                        v.style.setProperty('object-fit','contain','important');
                        v.style.setProperty('max-width','none','important');
                        v.style.setProperty('max-height','none','important');
                        if(ctl) ctl.style.setProperty('display','none','important');
                        window.__splPipFillTopLayer(c);
                        return true;
                    }catch(e){}
                    return false;
                };
                window.__splPipFillTopLayer=function(c){
                    try{
                        if(window.__splPopoverOk===undefined){
                            window.__splPopoverOk=!!(window.HTMLElement&&HTMLElement.prototype&&('showPopover' in HTMLElement.prototype));
                        }
                        if(!window.__splPopoverOk) return;
                        if(c.matches(':popover-open')) return;
                        if(!c.hasAttribute('popover')) c.setAttribute('popover','manual');
                        try{ c.showPopover(); }catch(e){}
                        if(!c.matches(':popover-open')){
                            try{ c.removeAttribute('popover'); }catch(e){}
                        }
                    }catch(e){}
                };
                window.__splPipFillRestore=function(){
                    var l=window.__splPipFillList||[];
                    for(var i=0;i<l.length;i++){
                        try{
                            var n=l[i];
                            if(n&&n.isConnected){
                                try{ if(n.matches(':popover-open')) n.hidePopover(); }catch(e){}
                                try{ if(n.hasAttribute('popover')) n.removeAttribute('popover'); }catch(e){}
                                if(n.__splSavedStyle==null) n.removeAttribute('style'); else n.setAttribute('style',n.__splSavedStyle);
                            }
                            if(n) delete n.__splSavedStyle;
                        }catch(e){}
                    }
                    window.__splPipFillList=[];
                };
                window.__splPipFillVideo=function(on){
                    try{
                        if(on){
                            if(document.fullscreenElement){ try{ document.exitFullscreen(); }catch(e){} }
                            if(!window.__splFillOn){
                                window.__splFillOn=true;
                                if(!window.__splFillHooked){
                                    window.__splFillHooked=true;
                                    var re=function(){ if(window.__splFillOn) window.__splPipFillApply(); };
                                    window.addEventListener('orientationchange',re);
                                    window.addEventListener('resize',re);
                                    try{
                                        if(window.visualViewport){
                                            window.visualViewport.addEventListener('resize',re);
                                            window.visualViewport.addEventListener('scroll',re);
                                        }
                                    }catch(e){}
                                }
                            }
                            if(!window.__splFillTimer) window.__splFillTimer=setInterval(function(){ if(window.__splFillOn) window.__splPipFillApply(); },700);
                            return window.__splPipFillApply();
                        }
                        if(!window.__splFillOn) return true;
                        window.__splFillOn=false;
                        if(window.__splFillTimer){ clearInterval(window.__splFillTimer); window.__splFillTimer=null; }
                        window.__splPipFillRestore();
                        return true;
                    }catch(e){}
                    return false;
                };
"""

    private const val PART_2 = """                document.getElementById('spl-liked').onclick=function(){actAddToFav()};
                document.getElementById('spl-seekb').onclick=function(){var b=document.querySelector('button[data-testid="control-button-seek-back-15"]');if(b)b.click()};
                document.getElementById('spl-seekf').onclick=function(){var b=document.querySelector('button[data-testid="control-button-seek-forward-15"]');if(b)b.click()};
                function splSpeedBtn(){ return document.querySelector('button[data-testid="control-button-playback-speed"]'); }
                function splSpeedItems(){
                    var seen={};
                    return [].slice.call(document.querySelectorAll('#context-menu [role^="menuitem"], [role="menu"] [role^="menuitem"], [data-tippy-root] button'))
                        .map(function(b){
                            var m=(b.textContent||'').trim().match(/^([\d.,]+)\s*[\u00d7x]$/);
                            if(!m) return null;
                            var r=b.getBoundingClientRect();
                            if(r.width===0||r.height===0) return null;
                            var v=parseFloat(m[1].replace(',','.'));
                            if(seen[v]) return null;
                            seen[v]=1;
                            return {b:b,v:v};
                        })
                        .filter(Boolean).sort(function(a,b){return a.v-b.v;});
                }
                function splHideNativeMenu(on){ document.documentElement.classList.toggle('spl-hide-ctx',!!on); }
                function splCloseSpeedMenu(){
                    document.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape',bubbles:true}));
                    setTimeout(function(){ if(splSpeedItems().length){ var sb=splSpeedBtn(); if(sb) sb.click(); } },120);
                    setTimeout(function(){ splHideNativeMenu(false); },300);
                }
                function splWithSpeedMenu(cb){
                    var sb=splSpeedBtn();
                    if(!sb){ cb(null); return; }
                    var items=splSpeedItems();
                    if(items.length){ cb(items); return; }
                    splHideNativeMenu(true);
                    sb.click();
                    var tries=0;
                    var iv=setInterval(function(){
                        tries++;
                        items=splSpeedItems();
                        if(items.length){ clearInterval(iv); cb(items); return; }
                        if(tries>=15){
                            clearInterval(iv);
                            try{
                                var menu=document.querySelector('#context-menu,[role="menu"],[data-tippy-root]');
                                AndBridge.dbg('w','speed-menu not matched');
                            }catch(e){}
                            splCloseSpeedMenu();
                            cb(null);
                        }
                    },100);
                }
                function splCurSpeed(){
                    var sb=splSpeedBtn();
                    var al=(sb&&sb.getAttribute('aria-label')||'').replace(',','.');
                    return parseFloat((al.match(/([\d.]+)\s*[\u00d7x]/)||[])[1])||1;
                }
                var speedSheet=document.createElement('div');
                speedSheet.id='spl-speed-sheet';
                speedSheet.innerHTML='<div class="spl-sheet-card"><div class="spl-sheet-title">Playback speed</div><div class="spl-sheet-opts" id="spl-speed-opts"></div></div>';
                pl.appendChild(speedSheet);
                function splHideSpeedSheet(){ speedSheet.classList.remove('spl-open'); }
                window.splHideSpeedSheet=splHideSpeedSheet;
                speedSheet.addEventListener('click',function(e){ if(e.target===speedSheet) splHideSpeedSheet(); });
                function splShowSpeedSheet(vals){
                    var cur=splCurSpeed();
                    var box=document.getElementById('spl-speed-opts');
                    box.innerHTML='';
                    vals.forEach(function(v){
                        var b=document.createElement('button');
                        b.className='spl-speed-opt'+(Math.abs(v-cur)<0.001?' spl-on':'');
                        b.textContent=v+'\u00d7';
                        b.onclick=function(){
                            splHideSpeedSheet();
                            if(Math.abs(v-splCurSpeed())<0.001) return;
                            splWithSpeedMenu(function(items){
                                if(!items) return;
                                var it=items.filter(function(x){return Math.abs(x.v-v)<0.001;})[0];
                                if(it){ it.b.click(); setTimeout(function(){ splHideNativeMenu(false); },300); }
                                else splCloseSpeedMenu();
                            });
                        };
                        box.appendChild(b);
                    });
                    speedSheet.classList.add('spl-open');
                }
                var splSpeedVals=null;
                document.getElementById('spl-speed').onclick=function(){
                    if(splSpeedVals){ splShowSpeedSheet(splSpeedVals); return; }
                    splWithSpeedMenu(function(items){
                        if(!items) return;
                        splSpeedVals=items.map(function(x){return x.v;});
                        splCloseSpeedMenu();
                        splShowSpeedSheet(splSpeedVals);
                    });
                };
                var splTrack=document.getElementById('spl-track');
                var splArtist=document.getElementById('spl-artist');
                splTrack.style.cursor='pointer';
                splArtist.style.cursor='pointer';
                splTrack.onclick=function(){
                    if(pl.classList.contains('spl-mini'))return;
                    splCollapseFull();
                    if(typeof closeNowPlay==='function') closeNowPlay();
                    var rl=document.querySelector('a[data-testid=context-item-link]');
                    if(rl){rl.click();}
                };
                splArtist.onclick=function(){
                    if(pl.classList.contains('spl-mini'))return;
                    splCollapseFull();
                    if(typeof closeNowPlay==='function') closeNowPlay();
                    var al=document.querySelector('a[data-testid=context-item-info-artist]');
                    if(!al) al=document.querySelector('a[data-testid=context-item-info-show]');
                    if(al){al.click();}
                };

                var barEl=document.getElementById('spl-bar');
                var edgeBarEl=document.getElementById('spl-edgebar');
                var dragging=false,dragEl=barEl;
                function seekTo(el,e){var r=el.getBoundingClientRect();var pct=Math.max(0,Math.min(1,(e.clientX-r.left)/r.width));var rg=document.querySelector('[data-testid="playback-progressbar"] input[type=range]');var mx=parseInt(rg?rg.getAttribute('max'):0)||1;actSeek(Math.round(pct*mx))}
                function bindSeek(el){el.addEventListener('mousedown',function(e){dragEl=el;dragging=true;seekTo(el,e)});el.addEventListener('touchstart',function(e){dragEl=el;dragging=true;seekTo(el,e.touches[0])},{passive:true});}
                bindSeek(barEl);bindSeek(edgeBarEl);
                document.addEventListener('mousemove',function(e){if(dragging)seekTo(dragEl,e)});
                document.addEventListener('touchmove',function(e){if(dragging)seekTo(dragEl,e.touches[0])},{passive:true});
                document.addEventListener('mouseup',function(){dragging=false});
                document.addEventListener('touchend',function(){dragging=false});

                var splMini=false,splClosing=false;
                var splDrag=null,splSuppressClick=false,splLastDragEnd=0;
                function splApplyMode(m){
                    if(m&&window.splHideSpeedSheet) window.splHideSpeedSheet();
                    var fs=!!window.__splFullPlayer;
                    var wasFull=pl.classList.contains('spl-full');
                    splMini=m;
                    window.splMiniPref=splMini;
                    pl.classList.toggle('spl-fsmode',fs);
                    pl.classList.toggle('spl-mini',splMini);
                    pl.classList.toggle('spl-full',fs&&!splMini);
                    var isFull=fs&&!splMini;
                    if(isFull!==wasFull){ try{ AndBridge.playerExpanded(isFull); }catch(e){} }
                    if(fs){ try{ localStorage.setItem('splFullOpen',isFull?'1':'0'); }catch(e){} }
                }
                function splCollapseFull(){ if(pl.classList.contains('spl-full')) splSetMini(true); }
                window.splRefreshMode=function(){ splApplyMode(window.__splFullPlayer?true:splMini); };
                function splSetMini(m){
                    m=!!m;
                    if(splClosing) return;
                    if(m&&pl.classList.contains('spl-full')){
                        splClosing=true;
                        pl.style.transition='transform .26s cubic-bezier(.4,0,1,1)';
                        pl.style.transform='translateY(100%)';
                        setTimeout(function(){
                            pl.style.transition='none';
                            pl.style.transform='';
                            splApplyMode(true);
                            void pl.offsetHeight;
                            pl.style.transition='';
                            splClosing=false;
                        },260);
                        return;
                    }
                    splApplyMode(m);
                }
                window.splSetMini=splSetMini;
                function splDragStart(x,y){
                    splDrag={sx:x,sy:y,moving:false,dy:0,mini:splMini};
                    pl.style.transition='none';
                }
                function splDragMove(x,y){
                    if(!splDrag)return;
                    var dy=y-splDrag.sy,dx=x-splDrag.sx;
                    if(!splDrag.moving){
                        if(Math.abs(dy)<10||Math.abs(dy)<Math.abs(dx))return;
                        splDrag.moving=true;
                    }
                    splDrag.dy=splDrag.mini?Math.min(0,dy):Math.max(0,dy);
                    pl.style.transform='translateY('+splDrag.dy+'px)';
                    pl.style.opacity=String(Math.max(.7,1-Math.abs(splDrag.dy)/500));
                }
                function splDragEnd(){
                    if(!splDrag)return;
                    var d=splDrag;
                    splDrag=null;
                    var collapse=d.moving&&!d.mini&&d.dy>70&&pl.classList.contains('spl-full');
                    pl.style.transition='';
                    if(!collapse) pl.style.transform='';
                    pl.style.opacity='';
                    if(d.moving){
                        splSuppressClick=true;
                        splLastDragEnd=Date.now();
                        setTimeout(function(){splSuppressClick=false;},100);
                        if(d.mini){if(d.dy<-70)splSetMini(false);}
                        else if(collapse||d.dy>70) splSetMini(true);
                    }
                }
                pl.addEventListener('touchstart',function(e){if(e.target.closest('#spl-bar')||e.target.closest('#spl-edgebar'))return;var t=e.touches[0];splDragStart(t.clientX,t.clientY);},{passive:true});
                pl.addEventListener('touchmove',function(e){if(splDrag&&splDrag.moving)e.preventDefault();if(!splDrag)return;var t=e.touches[0];splDragMove(t.clientX,t.clientY);},{passive:false});
                pl.addEventListener('touchend',function(e){if(splDrag&&splDrag.moving)e.preventDefault();splDragEnd();});
                pl.addEventListener('touchcancel',function(){splDragEnd();});
                pl.addEventListener('mousedown',function(e){if(e.button!==0)return;if(e.target.closest('#spl-bar')||e.target.closest('#spl-edgebar')||e.target.closest('button'))return;splDragStart(e.clientX,e.clientY);});
                document.addEventListener('mousemove',function(e){splDragMove(e.clientX,e.clientY);});
                document.addEventListener('mouseup',function(){splDragEnd();});
                pl.addEventListener('click',function(e){if(splSuppressClick)return;if(Date.now()-splLastDragEnd<400)return;var free=!e.target.closest('button')&&!e.target.closest('#spl-bar')&&!e.target.closest('#spl-edgebar');if(splMini&&free)splSetMini(false);else if(free&&pl.classList.contains('spl-video-on')&&pl.classList.contains('spl-full'))splShowControls(pl.classList.contains('spl-ctl-hidden'));});                    window.splUpdate=function(){ if(window.__splBusy) return;
                        var ci=document.getElementById('spl-cover-img');
                        var tk=document.getElementById('spl-track');
                        var ar=document.getElementById('spl-artist');
                        var fl=document.getElementById('spl-fill');
                        var fe=document.getElementById('spl-fill-edge');
                        var hd=document.getElementById('spl-handle');
                        var ps=document.getElementById('spl-pos');
                        var ds=document.getElementById('spl-dur');
                        var pp=document.getElementById('spl-play');
                        var ppm=document.getElementById('spl-play-mini');
                        var sh=document.getElementById('spl-shuffle');
                        var rp=document.getElementById('spl-repeat');
                        var vl=document.getElementById('spl-vol-btn');
                        var lk=document.getElementById('spl-liked');
                        var tm=document.getElementById('spl-timer');
                        var cn=document.getElementById('spl-connect');
                        var av=document.getElementById('spl-avtoggle');

                        var npb=window.splQ('[data-testid="now-playing-widget"]');
                        var imgEl=npb?npb.querySelector('img[data-testid="cover-art-image"]'):null;
                        if(ci&&imgEl&&imgEl.src&&ci.getAttribute('data-src')!==imgEl.src){
                            var lo=imgEl.src;
                            ci.setAttribute('data-src',lo);
                            if(window.__splFullPlayer){
                                ci.onerror=function(){ ci.onerror=null; if(ci.getAttribute('data-src')===lo) ci.src=lo; };
                                ci.src=splHiRes(imgEl);
                                splTint(lo);
                            } else {
                                ci.src=lo;
                            }
                        }

                        var trackEl=window.splQ('a[data-testid=context-item-link]');
                        if(tk&&trackEl&&trackEl.textContent&&tk.textContent!==trackEl.textContent) tk.textContent=trackEl.textContent;

                        var artistEl=window.splQ('a[data-testid=context-item-info-artist]');
                        if(!artistEl) artistEl=window.splQ('a[data-testid=context-item-info-show]');
                        if(ar&&artistEl&&tk.textContent!=='No track') ar.textContent=artistEl.textContent||'';

                        var rg=window.splQ('[data-testid="playback-progressbar"] input[type=range]');
                        if(pp||ppm){
                            var isPlaying=window.splIsPlayingSticky();
                            var ph=isPlaying
                                ?'<svg viewBox="0 0 16 16"><path fill="currentColor" d="M2.7 1a.7.7 0 0 0-.7.7v12.6a.7.7 0 0 0 .7.7h2.6a.7.7 0 0 0 .7-.7V1.7a.7.7 0 0 0-.7-.7zm8 0a.7.7 0 0 0-.7.7v12.6a.7.7 0 0 0 .7.7h2.6a.7.7 0 0 0 .7-.7V1.7a.7.7 0 0 0-.7-.7z"/></svg>'
                                :'<svg viewBox="0 0 16 16"><path fill="currentColor" d="M3 1.713a.7.7 0 0 1 1.05-.607l10.89 6.288a.7.7 0 0 1 0 1.212L4.05 14.894A.7.7 0 0 1 3 14.288z"/></svg>';
                            if(pp)pp.innerHTML=ph;
                            if(ppm)ppm.innerHTML=ph;
                        }
                        if(sh){
                            var sst=splShuffleState();
                            sh.classList.toggle('spl-active',sst==='shuffle'||sst==='smart');
                            sh.classList.toggle('spl-disabled',sst==='disabled');
                            var isSmart=sst==='smart';
                            var hasSparkle=!!sh.querySelector('.spl-sparkle');
                            if(isSmart&&!hasSparkle){
                                sh.innerHTML='<svg class="spl-sparkle" viewBox="0 0 16 16"><path fill="currentColor" d="M4.502 0a.637.637 0 0 1 .634.58 4.84 4.84 0 0 0 .81 2.184c.515.739 1.297 1.356 2.487 1.486a.637.637 0 0 1 0 1.267c-1.19.13-1.972.747-2.487 1.487a4.8 4.8 0 0 0-.81 2.185.637.637 0 0 1-1.268 0 4.8 4.8 0 0 0-.81-2.185C2.543 6.265 1.76 5.648.57 5.518a.637.637 0 0 1 0-1.268c1.19-.13 1.972-.747 2.487-1.486a4.84 4.84 0 0 0 .81-2.185A.637.637 0 0 1 4.502 0m4.765 11.878c.056.065.126.15.198.236l.33.397.013.015A3 3 0 0 0 12.1 13.59h1.009l-.444.443a.75.75 0 0 0 1.061 1.06l2.254-2.253-2.254-2.254a.75.75 0 0 0-1.06 1.06l.443.444H12.1a1.5 1.5 0 0 1-1.146-.533l-.004-.005-.333-.4-.288-.343-.031-.035-.02-.021-.037-.037-.974 1.16Z"/><path fill="currentColor" d="M12.69 4.196a.75.75 0 0 1 1.06 0l2.254 2.254-2.254 2.254a.75.75 0 0 1-1.06-1.06l.443-.444h-1.008a1.5 1.5 0 0 0-1.15.536l-4.63 5.517c-.344.411-.982 1.021-1.822 1.021v-1.5c.122 0 .371-.124.674-.485l4.63-5.517A3 3 0 0 1 12.125 5.7h1.008l-.443-.443a.75.75 0 0 1 0-1.061"/></svg>';
                            } else if(!isSmart&&hasSparkle){
                                sh.innerHTML='<svg viewBox="0 0 16 16"><path fill="currentColor" d="M13.151.922a.75.75 0 1 0-1.06 1.06L13.109 3H11.16a3.75 3.75 0 0 0-2.873 1.34l-6.173 7.356A2.25 2.25 0 0 1 .39 12.5H0V14h.391a3.75 3.75 0 0 0 2.873-1.34l6.173-7.356a2.25 2.25 0 0 1 1.724-.804h1.947l-1.017 1.018a.75.75 0 0 0 1.06 1.06L15.98 3.75zM.391 3.5H0V2h.391c1.109 0 2.16.49 2.873 1.34L4.89 5.277l-.979 1.167-1.796-2.14A2.25 2.25 0 0 0 .39 3.5zm7.758 6.22l.979-1.167 1.35 1.605a2.25 2.25 0 0 0 1.724.804h1.947l-1.017-1.018a.75.75 0 1 1 1.06-1.06l2.829 2.828-2.829 2.828a.75.75 0 1 1-1.06-1.06L13.109 13H11.16a3.75 3.75 0 0 1-2.873-1.34l-1.138-1.94z"/></svg>';
                            }
                        }
                        if(rp){
                            var rr=splFindRepeat();
                            var rc=rr?rr.getAttribute('aria-checked'):null;
                            var rDisabled=!!(rr&&(rr.disabled||rr.getAttribute('aria-disabled')==='true'));
                            rp.classList.toggle('spl-active',rc==='true'||rc==='mixed');
                            rp.classList.toggle('spl-disabled',rDisabled);
                            rp.classList.toggle('spl-repeat-track',rc==='mixed');
                            if(rc==='mixed'&&!rp.getAttribute('data-rt')){
                                rp.setAttribute('data-rt','1');
                                rp.innerHTML='<svg viewBox="0 0 16 16"><path fill="currentColor" d="M0 4.75A3.75 3.75 0 0 1 3.75 1h.75v1.5h-.75A2.25 2.25 0 0 0 1.5 4.75v5A2.25 2.25 0 0 0 3.75 12H5v1.5H3.75A3.75 3.75 0 0 1 0 9.75zM12.25 2.5a2.25 2.25 0 0 1 2.25 2.25v5A2.25 2.25 0 0 1 12.25 12H9.81l1.018-1.018a.75.75 0 0 0-1.06-1.06L6.939 12.75l2.829 2.828a.75.75 0 1 0 1.06-1.06L9.811 13.5h2.439A3.75 3.75 0 0 0 16 9.75v-5A3.75 3.75 0 0 0 12.25 1h-.75v1.5z"/><path fill="currentColor" d="m8 1.85.77.694H6.095V1.488q1.046-.077 1.507-.385.474-.308.583-.913h1.32V8H8z"/><path fill="currentColor" d="M8.77 2.544 8 1.85v.693z"/></svg>';
                            } else if(rc!=='mixed'&&rp.getAttribute('data-rt')){
                                rp.removeAttribute('data-rt');
                                rp.innerHTML='<svg viewBox="0 0 16 16"><path fill="currentColor" d="M0 4.75A3.75 3.75 0 0 1 3.75 1h8.5A3.75 3.75 0 0 1 16 4.75v5a3.75 3.75 0 0 1-3.75 3.75H9.81l1.018 1.018a.75.75 0 1 1-1.06 1.06L6.939 12.75l2.829-2.828a.75.75 0 1 1 1.06 1.06L9.811 12h2.439a2.25 2.25 0 0 0 2.25-2.25v-5a2.25 2.25 0 0 0-2.25-2.25h-8.5A2.25 2.25 0 0 0 1.5 4.75v5A2.25 2.25 0 0 0 3.75 12H5v1.5H3.75A3.75 3.75 0 0 1 0 9.75z"/></svg>';
                            }
                        }
                        var isEp=!!window.splQ('[data-testid="now-playing-widget"] [data-testid="episode"],[data-testid="now-playing-widget"] [data-testid="context-item-info-show"]');
                        pl.classList.toggle('spl-episode',isEp);
                        var spb=window.splQ('button[data-testid="control-button-playback-speed"]');
                        var spt=document.getElementById('spl-speed-l');
                        if(spt&&spb){
                            var spm=(spb.getAttribute('aria-label')||'').replace(',','.').match(/([\d.]+)\s*[\u00d7x]/);
                            var spv=(spm?spm[1]:'1')+'\u00d7';
                            if(spt.textContent!==spv) spt.textContent=spv;
                        }
                        if(lk){
                            var fb=window.splQ('div[data-testid=now-playing-widget]>div:last-child>button');
                            var liked=fb&&fb.getAttribute('aria-checked')==='true';
                            lk.classList.toggle('spl-active',liked===true);
                            var lkKind=isEp?(liked?'c':'p'):'h';
                            if((lk.getAttribute('data-k')||'h')!==lkKind){
                                lk.setAttribute('data-k',lkKind);
                                lk.setAttribute('aria-label',isEp?'Add to Your Episodes':'Like');
                                lk.innerHTML=lkKind==='p'?'<svg viewBox="0 0 16 16"><path fill="currentColor" d="M1.5 8a6.5 6.5 0 1 1 13 0 6.5 6.5 0 0 1-13 0M8 0a8 8 0 1 0 0 16A8 8 0 0 0 8 0m.75 4.75a.75.75 0 0 0-1.5 0v2.5h-2.5a.75.75 0 0 0 0 1.5h2.5v2.5a.75.75 0 0 0 1.5 0v-2.5h2.5a.75.75 0 0 0 0-1.5h-2.5z"/></svg>'
                                    :lkKind==='c'?'<svg viewBox="0 0 16 16"><path fill="currentColor" d="M0 8a8 8 0 1 1 16 0A8 8 0 0 1 0 8m11.748-1.97a.75.75 0 0 0-1.06-1.06l-4.47 4.47-1.405-1.406a.75.75 0 1 0-1.061 1.06l2.466 2.467 5.53-5.53z"/></svg>'
                                    :'<svg viewBox="0 0 16 16"><path fill="currentColor" d="M15.724 4.22A4.313 4.313 0 0 0 12.192.814a4.269 4.269 0 0 0-3.622 1.13.837.837 0 0 1-1.14 0 4.272 4.272 0 0 0-6.38 5.69l5.4 6.06a1.09 1.09 0 0 0 1.504.06l5.397-5.892a4.32 4.32 0 0 0 1.253-3.436z"/></svg>';
                            }
                        }
                        if(vl){
                            var muted=window.__splSysMuted===true;
                            vl.classList.toggle('spl-active',muted);
                            var hasX=!!vl.querySelector('.spl-mute-x');
                            if(muted&&!hasX){
                                vl.innerHTML='<svg viewBox="0 0 16 16"><path class="spl-mute-x" fill="currentColor" d="M13.86 5.47a.75.75 0 0 0-1.061 0l-1.47 1.47-1.47-1.47A.75.75 0 0 0 8.8 6.53L10.269 8l-1.47 1.47a.75.75 0 1 0 1.06 1.06l1.47-1.47 1.47 1.47a.75.75 0 0 0 1.06-1.06L12.39 8l1.47-1.47a.75.75 0 0 0 0-1.06"/><path fill="currentColor" d="M10.116 1.5A.75.75 0 0 0 8.991.85l-6.925 4a3.64 3.64 0 0 0-1.33 4.967 3.64 3.64 0 0 0 1.33 1.332l6.925 4a.75.75 0 0 0 1.125-.649v-1.906a4.7 4.7 0 0 1-1.5-.694v1.3L2.817 9.852a2.14 2.14 0 0 1-.781-2.92c.187-.324.456-.594.78-.782l5.8-3.35v1.3c.45-.313.956-.55 1.5-.694z"/></svg>';
                            } else if(!muted&&hasX){
                                vl.innerHTML='<svg viewBox="0 0 16 16"><path fill="currentColor" d="M9.741.85a.75.75 0 0 1 .375.65v13a.75.75 0 0 1-1.125.65l-6.925-4a3.64 3.64 0 0 1-1.33-4.967 3.64 3.64 0 0 1 1.33-1.332l6.925-4a.75.75 0 0 1 .75 0zm-6.924 5.3a2.14 2.14 0 0 0 0 3.7l5.8 3.35V2.8zm8.683 4.29V5.56a2.75 2.75 0 0 1 0 4.88"/><path fill="currentColor" d="M11.5 13.614a5.752 5.752 0 0 0 0-11.228v1.55a4.252 4.252 0 0 1 0 8.127z"/></svg>';
                            }
                        }
                        if(tm) tm.classList.toggle('spl-active',typeof sleepTimerActive!=='undefined'&&sleepTimerActive&&sleepTimerActive.value);
                        if(cn){
                            var cbtn=window.splQ('button[aria-describedby="connect-message-nudge"]');
                            if(cbtn){
                                cn.style.display='';
                                cn.classList.toggle('spl-active',cbtn.getAttribute('aria-pressed')==='true');
                            } else {
                                cn.style.display='none';
                            }
                        }
                        if(av){
                            var avb=pl.classList.contains('spl-full')?splVideoToggle():null;
                            if(avb){
                                var toVid=/switch to video/i.test(avb.getAttribute('aria-label')||'');
                                var avl=toVid?'Switch to video':'Switch to audio';
                                av.style.display='';
                                if(av.getAttribute('aria-label')!==avl) av.setAttribute('aria-label',avl);
                                var alt=document.getElementById('spl-avtoggle-l');
                                if(alt&&alt.textContent!==avl) alt.textContent=avl;
                                var aic=document.getElementById('spl-avtoggle-i');
                                var ak=toVid?'v':'a';
                                if(aic&&aic.getAttribute('data-k')!==ak){
                                    aic.setAttribute('data-k',ak);
                                    aic.innerHTML=toVid?splIconVideo:splIconAudio;
                                }
                            } else {
                                av.style.display='none';
                            }
                        }
                        var pbEl=window.splQ('[data-testid="playback-progressbar"] [data-testid="progress-bar"]');
                        if(pbEl){
                            var cs=getComputedStyle(pbEl);
                            var tr=cs.getPropertyValue('--progress-bar-transform');
                            if(tr){
                                var pct=parseFloat(tr)||0;
                                if(fl) fl.style.transform='scaleX('+(pct/100)+')';
                                if(fe) fe.style.transform='scaleX('+(pct/100)+')';
                                if(hd) hd.style.left=pct+'%';
                            }
                        }
                        var posEl=window.splQ('[data-testid="playback-position"]');
                        var durEl=window.splQ('[data-testid="playback-duration"]');
                        if(ps&&posEl) ps.textContent=posEl.textContent;
                        if(ds&&durEl) ds.textContent=durEl.textContent;
                        splApplyEmpty();
                        splCanvasTick();
                        splVideoTick();
                    };
                    function splTint(src){
                        var im=new Image();
                        im.crossOrigin='anonymous';
                        im.onload=function(){
                            try{
                                var c=document.createElement('canvas');c.width=c.height=24;
                                var x=c.getContext('2d');x.drawImage(im,0,0,24,24);
                                var d=x.getImageData(0,0,24,24).data;
                                var r=0,g=0,b=0,w=0;
                                for(var i=0;i<d.length;i+=4){
                                    var mx=Math.max(d[i],d[i+1],d[i+2]),mn=Math.min(d[i],d[i+1],d[i+2]);
                                    var k=(mx-mn)+8;
                                    if(mx<24||mn>235) k=1;
                                    r+=d[i]*k;g+=d[i+1]*k;b+=d[i+2]*k;w+=k;
                                }
                                r/=w;g/=w;b/=w;
                                var top=Math.max(r,g,b,1),s=Math.min(1,150/top);
                                var col=[r*s,g*s,b*s].map(Math.round);
                                pl.style.setProperty('--spl-np-color','rgb('+col.join(',')+')');
                                pl.style.setProperty('--spl-np-mini','rgb('+col.map(function(v){return Math.round(v*.55)}).join(',')+')');
                            }catch(e){}
                        };
                        im.src=src;
                    }
                    function splHiRes(im){
                        var best=im.src,bw=0;
                        (im.getAttribute('srcset')||'').split(',').forEach(function(p){
                            var m=p.trim().split(/\s+/);var w=parseInt(m[1],10)||0;
                            if(m[0]&&w>bw){bw=w;best=m[0];}
                        });
                        if(bw>=600) return best;
                        return best.replace(/ab67616d0000(4851|1e02)/,'ab67616d0000b273')
                                   .replace(/ab6765630000f68d|ab67656300005f1f/,'ab6765630000ba8a');
                    }
                    var splCanvasFor=null,splCanvasUrl=null,splCanvasCache={};
                    var splCanvasOff=false;
                    try{ splCanvasOff=localStorage.getItem('splCanvasOff')==='1'; }catch(e){}
                    var cvEl=document.createElement('video');
                    cvEl.id='spl-canvas';cvEl.muted=true;cvEl.loop=true;cvEl.playsInline=true;
                    cvEl.setAttribute('muted','');cvEl.setAttribute('playsinline','');cvEl.setAttribute('preload','auto');
                    var cvShade=document.createElement('div');cvShade.id='spl-canvas-shade';
                    document.addEventListener('visibilitychange',function(){ if(document.hidden&&!cvEl.paused) cvEl.pause(); });
                    var vSlot=document.createElement('div');vSlot.id='spl-video';
                    pl.insertBefore(cvShade,pl.firstChild);pl.insertBefore(cvEl,pl.firstChild);pl.insertBefore(vSlot,pl.firstChild);
                    var cvToggle=document.createElement('button');
                    cvToggle.className='spl-btn spl-canvas-toggle';cvToggle.id='spl-canvas-toggle';cvToggle.setAttribute('aria-label','Canvas');
                    cvToggle.innerHTML='<svg viewBox="0 0 24 24"><path fill="currentColor" d="M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2zm0 2v12h16V6H4zm5 2.5 6 3.5-6 3.5v-7z"/></svg>';
                    var cvSpacer=pl.querySelector('.spl-np-spacer');
                    if(cvSpacer) cvSpacer.parentNode.replaceChild(cvToggle,cvSpacer);
                    cvToggle.onclick=function(){
                        splCanvasOff=!splCanvasOff;
                        try{ localStorage.setItem('splCanvasOff',splCanvasOff?'1':'0'); }catch(e){}
                        splApplyCanvas();
                        var msg=splCanvasOff?'Canvas off':(splCanvasUrl?'Canvas on':'Canvas on \u2014 this song has no Canvas');
                        try{ AndBridge.deferMessage(msg); }catch(e){}
                    };
                    function clog(m){ try{ AndBridge.dbg('d','canvas '+m); }catch(e){} }
                    function splApplyCanvas(){
                        var has=!!splCanvasUrl;
                        pl.classList.toggle('spl-has-canvas',has);
                        pl.classList.toggle('spl-canvas',has&&!splCanvasOff);
                        cvToggle.classList.toggle('spl-active',!splCanvasOff);
                        if(!has){ if(cvEl.getAttribute('src')){ cvEl.pause(); cvEl.removeAttribute('src'); cvEl.load(); } return; }
                        if(cvEl.getAttribute('src')!==splCanvasUrl){ cvEl.setAttribute('src',splCanvasUrl); }
                    }
                    function splCurTrackId(){
                        var t=document.querySelector('a[data-testid=context-item-link]');
                        var title=t?(t.textContent||'').trim():'';
                        var meta=window.__splTrackMeta||{};
                        for(var k in meta){ if(meta[k]&&meta[k].name===title) return k; }
                        return window.__curTrackId||null;
                    }
                    function splCanvasUrlFrom(text){
                        var m=(text||'').match(/https:\/\/canvaz\.scdn\.co\/[^"\s\u0000-\u001f]+?\.mp4/);
                        return m?m[0].replace(/\\\//g,'/'):null;
                    }
                    async function splFetchCanvas(tid){
                        var uri='spotify:track:'+tid;
                        var f=window.mngFetch||window.oriFetch||window.fetch;
                        try{ if(window.ensureAuthToken) await window.ensureAuthToken(); }catch(e){}
                        var auth=window.spotAuthToken;
                        if(!auth){ clog(tid+' no auth token'); return null; }
                        var hash=window.splOpHashes&&window.splOpHashes.canvas;
                        if(hash){
                            try{
                                window.__splOwnCall=true;
                                var rp=f('https://api-partner.spotify.com/pathfinder/v2/query',{method:'POST',headers:{'Authorization':auth,'Content-Type':'application/json;charset=UTF-8','app-platform':'WebPlayer'},
                                    body:JSON.stringify({variables:{trackUri:uri},operationName:'canvas',extensions:{persistedQuery:{version:1,sha256Hash:hash}}})});
                                window.__splOwnCall=false;
                                var r=await rp;
                                var txt=await r.text();
                                var u=splCanvasUrlFrom(txt);
                                clog(tid+' gql status='+r.status+' url='+(u||'none')+(u?'':' body='+txt.slice(0,160)));
                                if(u) return u;
                            }catch(e){ window.__splOwnCall=false; clog(tid+' gql error '+e); }
                        } else clog(tid+' no canvas gql hash seen; ops='+Object.keys(window.splOpHashes||{}).length);
                        try{
                            var inner='\u000a'+String.fromCharCode(uri.length)+uri;
                            var body='\u000a'+String.fromCharCode(inner.length)+inner;
                            var r2=await f('https://spclient.wg.spotify.com/canvaz-cache/v0/canvases',{method:'POST',headers:{'Authorization':auth,'Content-Type':'application/x-protobuf','Accept':'application/protobuf'},body:body});
                            var t2=await r2.text();
                            var u2=splCanvasUrlFrom(t2);
                            clog(tid+' canvaz status='+r2.status+' url='+(u2||'none')+' len='+t2.length);
                            return u2;
                        }catch(e){ clog(tid+' canvaz error '+e); }
                        return null;
                    }
                    function splCanvasTick(){
                        if(!window.__splFullPlayer||pl.style.display==='none'){ if(!cvEl.paused) cvEl.pause(); return; }
                        var hasVid=!!splPageVideo()||!!document.querySelector('#spl-video video');
                        var tid=(pl.classList.contains('spl-episode')||hasVid)?null:splCurTrackId();
                        if(tid!==splCanvasFor){
                            splCanvasFor=tid;
                            splCanvasUrl=null;
                            if(tid&&(tid in splCanvasCache)) splCanvasUrl=splCanvasCache[tid];
                            else if(tid){
                                splCanvasCache[tid]=null;
                                splFetchCanvas(tid).then(function(u){
                                    splCanvasCache[tid]=u||null;
                                    if(splCanvasFor===tid){ splCanvasUrl=u||null; splApplyCanvas(); }
                                });
                            }
                            splApplyCanvas();
                        }
                        var want=pl.classList.contains('spl-canvas')&&pl.classList.contains('spl-full')&&!!window.splIsPlayingSticky();
                        if(want&&cvEl.paused&&cvEl.getAttribute('src')){
                            if((cvEl.readyState===0||cvEl.error)&&Date.now()-(cvEl.__splReload||0)>3000){ cvEl.__splReload=Date.now(); cvEl.load(); }
                            var pp=cvEl.play(); if(pp&&pp.catch) pp.catch(function(){});
                        }
                        else if(!want&&!cvEl.paused) cvEl.pause();
                    }
                    var splVidHome=null,splVidWide=false,splCtlTimer=0,splVidSuppress=0;
                    function splShowControls(show){
                        clearTimeout(splCtlTimer);
                        pl.classList.toggle('spl-ctl-hidden',!show);
                        if(show&&splVidWide) splCtlTimer=setTimeout(function(){ if(window.splIsPlayingSticky()) pl.classList.add('spl-ctl-hidden'); },4000);
                    }
                    function splVideoTick(){
                        var slot=document.getElementById('spl-video');
                        if(!slot) return;
                        var v=slot.querySelector('video');
                        var avail=splPageVideo();
                        var full=pl.classList.contains('spl-full');
                        if(full&&(pl.classList.contains('spl-episode')||v||avail)){
                            if(!v&&avail&&avail.videoWidth&&Date.now()>splVidSuppress){
                                splVidHome=avail.parentNode; slot.appendChild(avail); v=avail;
                            }
                        } else if(v&&splVidHome&&splVidHome.isConnected){
                            splVidHome.appendChild(v); v=null;
                        }
                        pl.classList.toggle('spl-video-on',!!(v&&v.videoWidth));
                        pl.classList.toggle('spl-video-tall',!!(v&&v.videoHeight>v.videoWidth));
                        var wide=!!(v&&v.videoWidth>v.videoHeight&&full);
                        if(wide!==splVidWide){ splVidWide=wide; try{ AndBridge.wideVideo(wide); }catch(e){} if(wide) splShowControls(true); }
                        if(!v&&pl.classList.contains('spl-ctl-hidden')) splShowControls(true);
                    }
                    function formatTime(ms){
                        var t=Math.floor(ms/1000);
                        return Math.floor(t/60)+':'+(t%60<10?'0':'')+t%60;
                    }

                    var rafLastTime=0;
                    function rafUpdate(timestamp){
                        if(!window.__splBusy&&!window.__splBg&&timestamp-rafLastTime>100){ splUpdate(); rafLastTime=timestamp; }
                        requestAnimationFrame(rafUpdate);
                    }
                    var splWasOpen=false;
                    try{ splWasOpen=localStorage.getItem('splFullOpen')==='1'; }catch(e){}
                    splSetMini(window.__splFullPlayer?!splWasOpen:!!window.splMiniPref);
                    requestAnimationFrame(rafUpdate);
            };
            if(document.readyState==='complete') initSpotilolPlayer();
            else window.addEventListener('load',initSpotilolPlayer);
            setInterval(function(){
                if(window.__splBg||window.__splBusy) return;
                var npb=document.querySelector('aside[data-testid="now-playing-bar"]');
                if(npb&&npb.style.display!=='none') initSpotilolPlayer();
            },3000);
        
    """
}
