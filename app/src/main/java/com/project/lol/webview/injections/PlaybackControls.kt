package com.project.lol.webview.injections

object PlaybackControls {
    const val CONTENT = """
            window.splAbortPause = function() {
                window.__splPauseTok = (window.__splPauseTok || 0) + 1;
                window.__splPauseActive = null;
            };
            window.playFromUri = function(uri, contextUri) {
                try { window.splAbortPause(); } catch(e) {}
                var playContext = contextUri || uri;
                var isLikedSongs = (playContext === 'your_library' || playContext.indexOf('collection') !== -1 || playContext === 'playlists' || playContext === 'spotify:collection:tracks');

                var playOptions = {
                    license: 'tft',
                    skip_to: {},
                    player_options_override: {}
                };

                var commandContext = {
                    uri: playContext,
                    url: 'context://' + playContext,
                    metadata: {}
                };

                var featIdent = playContext.match(/^spotify:([^:]+)/);
                featIdent = featIdent ? featIdent[1] : null;
                if (featIdent == 'user' || isLikedSongs) featIdent = 'your_library';

                if (isLikedSongs) {
                    var tracks = window.likedSongsCache || [];
                    var targetUri = (uri && uri.indexOf(':track:') !== -1) ? uri : ((tracks[0] && tracks[0].id) || uri);
                    var trackList = (tracks || []).map(function(t) { return t.id || t.uri || t; }).filter(Boolean);

                    if (targetUri && targetUri.indexOf(':track:') !== -1 && trackList.indexOf(targetUri) === -1) {
                        trackList.unshift(targetUri);
                    }

                    var collectionUri = window.spotUserId ? 'spotify:user:' + window.spotUserId + ':collection' : 'spotify:collection:tracks';

                    commandContext = {
                        uri: collectionUri,
                        url: 'context://' + collectionUri,
                        metadata: { context_description: window.splLikedName() },
                        pages: [{ page_url: 'context://' + collectionUri, tracks: trackList.map(function(u) { return { uri: u }; }) }]
                    };

                    featIdent = 'collection-tracks';

                    playOptions.skip_to = { track_uri: targetUri };
                } else if (contextUri && contextUri !== uri) {
                    playOptions.skip_to = { track_uri: uri };
                }

                var splTarget = (typeof window.__splRemoteActive === 'function' && window.__splRemoteActive() && window.__splActiveDevId) ? window.__splActiveDevId : window.spotDevId;
                (window.mngFetch || oriFetch)('https://gew4-spclient.spotify.com/connect-state/v1/player/command/from/' + window.spotDevId + '/to/' + splTarget, {
                    method: 'POST',
                    headers: { 'Authorization': window.spotAuthToken, 'Client-Token': window.spotCliToken, 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        command: {
                            context: commandContext,
                            play_origin: {
                                feature_identifier: featIdent || 'your_library',
                                feature_version: featVer,
                                referrer_identifier: 'your_library'
                            },
                            options: playOptions,
                            endpoint: 'play'
                        }
                    })
                });
            };
            window.splLikedName = function() {
                try {
                    var h1 = document.querySelector('main h1');
                    if (h1 && h1.textContent && h1.textContent.trim()) return h1.textContent.trim();
                } catch(e) {}
                try {
                    var lib = window.mediaLib;
                    if (lib && lib.playlists) {
                        for (var i = 0; i < lib.playlists.length; i++) {
                            var p = lib.playlists[i];
                            if ((p.id || '').indexOf('collection') !== -1 && p.name) return p.name;
                        }
                    }
                } catch(e) {}
                return 'Liked Songs';
            };
            window.splIsLikedName = function(n) {
                n = String(n || '').toLowerCase();
                if (!n) return false;
                return /liked songs|titres lik|titres aim|j'aime|canciones que te gustan|me gusta|lieblingstitel|brani che ti piacciono|m[uú]sicas curtidas|gelikete nummers|ulubione utwory|polubione utwory|любимые треки|мне нравится|улюблені треки|お気に入りの曲|いいねした曲|좋아요 표시한 곡|喜欢的歌曲|喜愛的歌曲|喜歡的歌曲|beğenilen şarkılar|gillade l[åa]tar|tykätyt kappaleet|obl[íi]bené skladby|obľúbené skladby|kedvelt dalok|melodii apreciate|αγαπημένα τραγούδια|שירים שאהבת|पसंद किए गए गाने|เพลงที่ถูกใจ|lagu yang disukai|bài hát đã thích|харесани песни/i.test(n);
            };
            window.splEnsurePb=function(){
                var pb=window.pBtn;
                if(pb && document.documentElement.contains(pb)) return pb;
                var all=document.querySelectorAll('aside button[data-testid=control-button-playpause], button[data-testid=control-button-playpause]');
                var first=null;
                for(var i=0;i<all.length;i++){
                    if(first===null) first=all[i];
                    if(all[i].getClientRects().length>0){ first=all[i]; break; }
                }
                if(first){ window.pBtn=first; return first; }
                try{ AndBridge.dbg('e','bt auto-pause: no play button found'); }catch(e){}
                return null;
            };
            window.splPauseOutcome=function(cb){
                var s1=window.splIsPlaying();
                if(s1===false){ cb('paused'); return; }
                setTimeout(function(){
                    var s2=window.splIsPlaying();
                    if(s2===false) cb('paused');
                    else if(s1===true && s2===true) cb('playing');
                    else cb('unknown');
                },400);
            };
            window.splPauseRetry=function(tries,gen,probes){
                tries=tries||0;
                probes=probes||0;
                if(gen==null){
                    if(window.__splPauseActive!=null) return;
                    window.__splPauseTok=(window.__splPauseTok||0)+1;
                    gen=window.__splPauseTok;
                    window.__splPauseActive=gen;
                }
                var finish=function(msg,lvl){
                    if(window.__splPauseActive===gen) window.__splPauseActive=null;
                    if(msg){ try{ AndBridge.dbg(lvl,msg); }catch(e){} }
                };
                if(gen!==window.__splPauseTok){
                    if(window.__splPauseActive===gen) window.__splPauseActive=null;
                    return;
                }
                var cur=(typeof window.splIsPlaying==='function')?window.splIsPlaying():null;
                if(cur===false){ finish('bt auto-pause: confirmed paused','s'); return; }
                if(cur!==true){
                    if(probes>=3){ finish('bt auto-pause: play state unknown, aborting without click','w'); return; }
                    setTimeout(function(){ window.splPauseRetry(tries,gen,probes+1); },400);
                    return;
                }
                var pb=window.splEnsurePb();
                if(!pb){ finish(null); return; }
                try{ window.reqPause=true; window.ulFlag=false; }catch(e){}
                pb.click();
                setTimeout(function(){
                    if(gen!==window.__splPauseTok){
                        if(window.__splPauseActive===gen) window.__splPauseActive=null;
                        return;
                    }
                    window.splPauseOutcome(function(outcome){
                        if(gen!==window.__splPauseTok){
                            if(window.__splPauseActive===gen) window.__splPauseActive=null;
                            return;
                        }
                        if(outcome==='paused'){ finish('bt auto-pause: confirmed paused','s'); return; }
                        if(outcome==='unknown'){ finish('bt auto-pause: play state unknown, not retrying','w'); return; }
                        if(tries>=3){ finish('bt auto-pause: playback continued after '+(tries+1)+' attempts','e'); return; }
                        try{ AndBridge.dbg('w','bt auto-pause: still playing, retry '+(tries+1)); }catch(e){}
                        window.splPauseRetry(tries+1,gen,probes);
                    });
                },900);
            };
            window.actPlayPause = function(play) {
                var cur = (typeof window.splIsPlaying === 'function') ? window.splIsPlaying() : null;
                if (play === false) {
                    window.splPauseRetry(0);
                    return;
                }
                window.splAbortPause();
                var pb = window.splEnsurePb();
                if (!pb) return;
                if (play === null || typeof play === 'undefined' || cur === null) {
                    pb.click();
                } else if (play === true) {
                    if (!cur) pb.click();
                }
            };
            window.actSkipBack = function() {
                var bb = document.querySelector('button[data-testid=control-button-skip-back]');
                if(bb) { window.splAbortPause(); AndBridge.wakeUp(); bb.click(); }
            };
            window.actSkipForward = function() {
                var fb = document.querySelector('button[data-testid=control-button-skip-forward]');
                if(fb) { window.splAbortPause(); AndBridge.wakeUp(); fb.click(); }
            };
            window.splShuffleBtn = function() {
                var b = document.querySelector('button[data-testid="control-button-shuffle"]');
                if(b) return b;
                var sk = document.querySelector('button[data-testid="control-button-skip-back"]');
                if(sk) {
                    var p = sk.previousElementSibling;
                    if(p && p.tagName === 'BUTTON') return p;
                    var f = sk.parentElement ? sk.parentElement.querySelector('button') : null;
                    if(f && f !== sk) return f;
                }
                var bs = document.querySelectorAll('button');
                for(var i=0;i<bs.length;i++){
                    var ic = bs[i].querySelector('svg path');
                    if(ic && (ic.getAttribute('d')||'').indexOf('M13.151.922') === 0 && !/spl-btn/.test(bs[i].className||'')) return bs[i];
                }
                return null;
            };
            window.splShuffleState = function() {
                var b = window.splShuffleBtn();
                if(!b) return 'off';
                if(b.getAttribute('aria-disabled') === 'true') return 'disabled';
                if((b.className||'').indexOf('text-bright-accent') === -1) return 'off';
                return /smart|intelligent|inteligente|intelligente|inteligentny|slim|умный|スマート|스마트|智能|akıllı|älykäs/i.test(b.getAttribute('aria-label')||'') ? 'smart' : 'shuffle';
            };
            window.actToggleShuffle = function() {
                var sb = window.splShuffleBtn();
                if(sb && sb.getAttribute('aria-disabled') !== 'true') {
                    AndBridge.wakeUp();
                    sb.click();
                }
            };
            window.splRepeatBtn = function() {
                var b = document.querySelector('button[data-testid="control-button-repeat"]');
                if(b) return b;
                var fw = document.querySelector('button[data-testid="control-button-skip-forward"]');
                if(fw && fw.nextElementSibling && fw.nextElementSibling.tagName === 'BUTTON') return fw.nextElementSibling;
                var bs = document.querySelectorAll('button');
                for(var i=0;i<bs.length;i++){
                    var ic = bs[i].querySelector('svg path');
                    if(ic && (ic.getAttribute('d')||'').indexOf('M0 4.75') === 0 && !/spl-btn/.test(bs[i].className||'')) return bs[i];
                }
                return null;
            };
            window.actRepeat = function() {
                var rb = window.splRepeatBtn();
                if(rb) {
                    if(repmode=='false') repmode='true';
                    else if(repmode=='true') repmode='mixed';
                    else repmode='false';
                    updMedia();
                    rb.click();
                }
            };
            window.actAddToFav = function() {
                if(window.__splFavBusy) return;
                var fb = document.querySelector('div[data-testid=now-playing-widget]>div:last-child>button');
                if(fb) {
                    if(fb.getAttribute('aria-checked')==='false') {
                        fb.click();
                        isfav=true;
                        updMedia();
                    } else {
                        window.__splFavBusy = true;
                        AndBridge.wakeUp();
                        fb.click();
                        var polls = 0;
                        var rfint = setInterval(function(){
                            polls++;
                            var fr = document.querySelector('#context-menu button[role=menuitemcheckbox][aria-checked=true]');
                            if(fr) {
                                clearInterval(rfint);
                                fr.click();
                                setTimeout(function(){
                                    var sb = document.querySelector('#context-menu button[type=submit]');
                                    if(sb) { sb.click(); isfav=false; updMedia(); }
                                    AndBridge.wakeOff();
                                    window.__splFavBusy = false;
                                },500);
                            } else if (polls > 15) {
                                clearInterval(rfint);
                                AndBridge.wakeOff();
                                window.__splFavBusy = false;
                            }
                        },1000);
                    }
                }
            };
            window.actSeek = function(pos) {
                var rg = document.querySelector('div[data-testid=playback-progressbar] input[type=range]');
                if(rg) { rg.value=pos+1; rg.dispatchEvent(new Event('change',{bubbles:true})); }
            };

    """
}