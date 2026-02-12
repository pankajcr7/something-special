const DB={pickup:{smooth:{mild:["Do you have a map? Because I just got lost in your eyes.","I must be a snowflake, because I've fallen for you.","Are you a parking ticket? Because you've got 'fine' written all over you.","Is your name Google? Because you have everything I've been searching for.","I'd say God bless you, but it looks like he already did.","If you were a vegetable, you'd be a cute-cumber.","Excuse me, but I think you dropped something: my jaw.","Do you believe in love at first sight, or should I walk by again?","Are you a campfire? Because you're hot and I want s'more.","I'm not a photographer, but I can picture us together."],bold:["If beauty were time, you'd be an eternity.","I'm not a genie, but I can make your dreams come true.","You must be tired because you've been running through my mind all day.","Is it hot in here or is it just you?","I'd never play hide and seek with you — someone like you is impossible to find.","Your hand looks heavy. Let me hold it for you.","I was going to say something really sweet about you but when I saw you I became speechless.","You must be made of copper and tellurium because you're Cu-Te.","I'm no mathematician, but I'm pretty good with numbers. How about you give me yours?","I seem to have lost my phone number. Can I have yours?"],extreme:["I'm not sure what's more breathtaking — the stars or your eyes.","Is there an airport nearby or is that just my heart taking off?","If I were to ask God for one thing, it would be to stop making you so perfect.","Do you have a Band-Aid? Because I just scraped my knee falling for you.","They say nothing lasts forever, so will you be my nothing?","You're so beautiful you made me forget my pickup line.","I'd text you good morning every day just to remind you someone's thinking of you.","They should put you on a stamp so everyone can see the beauty of this world.","You look so familiar — oh wait, I think I've seen you in my dreams.","I didn't know what I wanted in my life until I met you."]},funny:{mild:["Are you a bank loan? Because you got my interest.","Are you French? Because Eiffel for you.","Are you a magician? Because whenever I look at you, everyone else disappears.","Do you have a sunburn or are you always this hot?","Is your dad a boxer? Because you're a knockout.","I'm not a hoarder but I really want to keep you forever.","If looks could kill, you'd be a weapon of mass destruction.","Are you a time traveler? Because I can see you in my future.","Do you have GPS? Because I just got lost in your eyes.","I'm not drunk, I'm just intoxicated by you."],bold:["Are you a parking ticket? Because you've got 'fine' written all over you.","Is your name Wi-Fi? Because I'm feeling a connection.","Are you a camera? Because every time I look at you, I smile.","Do you believe in love at first sight, or should I walk by again?","Are you a beaver? Because daaaam.","If you were a triangle you'd be acute one.","Are you a dictionary? Because you add meaning to my life.","Did the sun come out or did you just smile at me?","Are you my appendix? Because I have a funny feeling I should take you out.","I'm studying to become a historian because I'm interested in your past."],extreme:["Are you a volcano? Because I lava you.","I'd give up my morning coffee for you, and that's saying a lot.","Are you a 90-degree angle? Because you're looking right.","If being beautiful was a crime, you'd be on the most wanted list.","You're like my favorite song — I could listen to you on repeat.","I don't have a library card, but do you mind if I check you out?","My love for you is like diarrhea, I just can't hold it in.","If you were a vegetable, you'd be a cutecumber. If you were a fruit, you'd be a fineapple.","Did you just fart? Because you blew me away.","I'm not a photographer, but I can picture you and me together."]}},reply:{smooth:{mild:["Haha that's smooth, but I bet I'm smoother.","Can't stop thinking about what you said... in a good way.","You always know just what to say, don't you?"],bold:["Not gonna lie, that made me smile way too hard.","Careful, you're making me actually catch feelings.","If flirting was a sport, you'd be MVP."],extreme:["I screenshot that message and I'm not even sorry.","Every time you text me, my heart does a little backflip.","Stop being so perfect, it's making my brain malfunction."]},funny:{mild:["Bruh 😂 you're too funny.","Did you Google that one or did you actually come up with it?","LMAO okay that was actually good."],bold:["I just spit out my coffee reading that.","You're like a human comedy show, I'd buy front row tickets.","I dare you to say something that doesn't make me laugh."],extreme:["I literally cannot breathe 💀 STOP.","Okay officially nominating you for funniest person alive.","I showed this to my friend and they said 'marry them immediately.'"]}}};

const toolCfg={
pickup:{title:'Pickup Line Generator',ctxLabel:'Describe the situation or your crush',ctxPlace:'e.g., She loves coffee and has the most beautiful smile...',reply:false},
reply:{title:'Reply Generator',ctxLabel:'Their message to you',ctxPlace:'e.g., "haha you\'re funny" or "what makes you different?"',reply:true},
bio:{title:'Bio Generator',ctxLabel:'About yourself',ctxPlace:'e.g., 22, software dev, gym rat, love midnight drives...',reply:false},
starter:{title:'Conversation Starter',ctxLabel:'About them',ctxPlace:'e.g., She\'s into photography and travel, matched on Hinge...',reply:false},
compliment:{title:'Compliment Generator',ctxLabel:'About them',ctxPlace:'e.g., She has the most contagious laugh and is super smart...',reply:false},
flirt:{title:'🔍 Flirt Detector',ctxLabel:'Paste their message to analyze',ctxPlace:'e.g., "you looked really good today btw 😏"',reply:false,special:'flirt'},
rewrite:{title:'✏️ Tone Rewriter',ctxLabel:'Your message to rewrite',ctxPlace:'e.g., "hey, wanna hang out this weekend?"',reply:false,special:'rewrite'}
};

const SUGGESTIONS={
pickup:[
"She's on Bumble, she's into travel and dogs",
"Cute girl at the gym, always wears headphones",
"She liked my story, we have mutual friends",
"He's into anime and coding, met at a hackathon",
"Instagram DM to someone who posts aesthetic pics",
"College crush who sits next to me in class"
],
reply:[
"haha you're funny",
"You're not that tall are you?",
"I don't usually reply to DMs",
"What makes you different from other guys?",
"We should hang out sometime",
"You seem too good to be true lol"
],
bio:[
"22, software dev who loves gaming and midnight drives",
"Med student, gym rat, can cook better than your mom",
"Introvert who'll become your favorite person",
"Dog mom, coffee addict, looking for my last first date",
"Tall, sarcastic, will make you laugh then steal your fries"
],
starter:[
"She's into photography and travel, matched on Hinge",
"He plays guitar and posts gym selfies",
"We matched but nobody texted first for 2 days",
"Her profile says she loves horror movies and ramen",
"He's a chef, has a cute dog in all his pics"
],
compliment:[
"She's really smart and always makes everyone laugh",
"He helped me with something without me even asking",
"She has the best energy, lights up every room",
"He's always so calm and patient with everyone",
"She's been working really hard on her fitness journey"
],
flirt:[
"you looked really good today btw 😏",
"i can't stop thinking about last night haha",
"we should definitely hang out more often",
"you always make me laugh ngl",
"why are you so cute its annoying",
"i told my friends about you lol"
],
rewrite:[
"hey wanna hang out this weekend?",
"i had a great time with you last night",
"you look really pretty in that pic",
"i miss talking to you",
"do you wanna go on a date?",
"i think about you a lot ngl"
]
};

const SCENARIOS=[
{label:'😶 Left on Read',tool:'reply',ctx:'They left me on read for 2 days after I sent a heartfelt message',style:'smooth'},
{label:'💬 First DM',tool:'pickup',ctx:'Want to send the first DM on Instagram to someone who posts aesthetic pics',style:'smooth'},
{label:'📸 Story Reply',tool:'reply',ctx:'They posted a fire selfie on their story and I want to reply',style:'bold'},
{label:'🍕 Ask Out',tool:'starter',ctx:'Want to ask them on a casual first date, we\'ve been texting for a week',style:'smooth'},
{label:'😅 Awkward Text',tool:'rewrite',ctx:'sorry for the late reply haha i was busy',style:'smooth'},
{label:'💕 Shoot Shot',tool:'pickup',ctx:'Crush from class, never talked before, want to text for the first time',style:'bold'},
{label:'🔥 Keep Convo',tool:'starter',ctx:'The conversation is dying but I want to keep talking to them',style:'funny'},
{label:'👻 Got Ghosted',tool:'reply',ctx:'They ghosted me for a week and suddenly texted "hey stranger"',style:'savage'}
];

let curTool='pickup',curStyle='smooth',genLines=[];
const AI_CONNECTED=true;

let favorites=JSON.parse(localStorage.getItem('rizzgpt_favs')||'[]');

function saveFavs(){localStorage.setItem('rizzgpt_favs',JSON.stringify(favorites));updateFavBadge();}
function updateFavBadge(){const b=document.getElementById('favBadge');if(b)b.textContent=favorites.length;if(b)b.style.display=favorites.length>0?'flex':'none';}
function toggleFav(line){
const idx=favorites.indexOf(line);
if(idx>-1){favorites.splice(idx,1);showToast('Removed from favorites');}
else{favorites.push(line);showToast('Saved to favorites ❤️');spawnConfetti();}
saveFavs();
renderOut(genLines,true);
}
function openFavDrawer(){document.getElementById('favDrawer').classList.add('open');renderFavDrawer();}
function closeFavDrawer(){document.getElementById('favDrawer').classList.remove('open');}
function renderFavDrawer(){
const list=document.getElementById('favList');
if(favorites.length===0){list.innerHTML='<div class="fav-empty"><p>No favorites yet</p><span>Tap ❤️ on any generated line to save it</span></div>';return;}
list.innerHTML=favorites.map((f,i)=>`<div class="fav-item"><p class="fav-text">${f.replace(/</g,'&lt;')}</p><div class="fav-actions"><button class="fav-action-btn" onclick="navigator.clipboard.writeText(favorites[${i}]).then(()=>showToast('Copied!'))"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg></button><button class="fav-action-btn fav-del" onclick="favorites.splice(${i},1);saveFavs();renderFavDrawer()"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg></button></div></div>`).join('');
}

document.addEventListener('DOMContentLoaded',()=>{
const nav=document.getElementById('navbar');
const mbtn=document.getElementById('menuBtn');
const nlinks=document.getElementById('navLinks');
window.addEventListener('scroll',()=>nav.classList.toggle('scrolled',window.scrollY>50));
if(mbtn)mbtn.addEventListener('click',()=>{nlinks.classList.toggle('open');mbtn.classList.toggle('active')});
if(nlinks)nlinks.querySelectorAll('.nav-link').forEach(l=>l.addEventListener('click',()=>{nlinks.classList.remove('open');if(mbtn)mbtn.classList.remove('active')}));

document.querySelectorAll('.tool-pill[data-tool]').forEach(c=>c.addEventListener('click',()=>switchTool(c.dataset.tool)));
document.querySelectorAll('.style-chip').forEach(b=>b.addEventListener('click',()=>{document.querySelectorAll('.style-chip').forEach(x=>x.classList.remove('active'));b.classList.add('active');curStyle=b.dataset.style}));

const sl=document.getElementById('intensitySlider'),labs=document.querySelectorAll('.i-label');
if(sl)sl.addEventListener('input',()=>labs.forEach((l,i)=>l.classList.toggle('active',i===sl.value-1)));

const ci=document.getElementById('contextInput'),cc=document.getElementById('charCount');
if(ci)ci.addEventListener('input',()=>cc.textContent=ci.value.length);

renderScenarios();
countUp();renderSuggestions('pickup');
updateFavBadge();
});

function renderScenarios(){
const container=document.getElementById('scenarioButtons');
if(!container) return;
container.innerHTML=SCENARIOS.map((s,i)=>`<button class="scenario-btn" data-idx="${i}">${s.label}</button>`).join('');
container.querySelectorAll('.scenario-btn').forEach(b=>b.addEventListener('click',()=>{
const s=SCENARIOS[b.dataset.idx];
switchTool(s.tool);
if(s.style){document.querySelectorAll('.style-chip').forEach(x=>x.classList.remove('active'));const chip=document.querySelector(`.style-chip[data-style="${s.style}"]`);if(chip)chip.classList.add('active');curStyle=s.style;}
const input=document.getElementById('contextInput');
if(input){input.value=s.ctx;input.dispatchEvent(new Event('input'));}
document.getElementById('generator').scrollIntoView({behavior:'smooth'});
}));
}

function switchTool(t){
curTool=t;const c=toolCfg[t];
document.querySelectorAll('.tool-pill[data-tool]').forEach(x=>x.classList.remove('active'));
document.querySelectorAll(`[data-tool="${t}"]`).forEach(x=>x.classList.add('active'));
document.getElementById('panelTitleText').textContent=c.title;
document.getElementById('contextLabel').textContent=c.ctxLabel;
document.getElementById('contextInput').placeholder=c.ctxPlace;
document.getElementById('replyGroup').style.display=c.reply?'block':'none';
const styleRow=document.querySelector('.style-row');
const optGrid=document.querySelector('.options-grid');
const intField=document.getElementById('intensitySlider')?.closest('.field');
const togRow=document.querySelector('.toggles-row');
if(c.special==='flirt'){
if(styleRow)styleRow.style.display='none';if(optGrid)optGrid.style.display='none';if(intField)intField.style.display='none';if(togRow)togRow.style.display='none';
}else if(c.special==='rewrite'){
if(styleRow)styleRow.style.display='flex';if(optGrid)optGrid.style.display='none';if(intField)intField.style.display='none';if(togRow)togRow.style.display='none';
}else{
if(styleRow)styleRow.style.display='flex';if(optGrid)optGrid.style.display='grid';if(intField)intField.style.display='block';if(togRow)togRow.style.display='flex';
}
renderSuggestions(t);
document.getElementById('outputContent').innerHTML=`<div class="output-empty"><div class="empty-icon">✨</div><p>Your ${t==='bio'?'bios':t==='flirt'?'analysis':t==='rewrite'?'rewritten text':'lines'} will appear here</p></div>`;
document.getElementById('outputFooter').style.display='none';
document.getElementById('copyAllBtn').style.display='none';
document.getElementById('generator').scrollIntoView({behavior:'smooth'});
}

function renderSuggestions(tool){
const container=document.getElementById('suggestionChips');
if(!container)return;
const chips=SUGGESTIONS[tool]||[];
container.innerHTML=chips.map(s=>`<button class="suggestion-chip" onclick="useSuggestion(this)">${s}</button>`).join('');
}

function useSuggestion(el){
const input=document.getElementById('contextInput');
input.value=el.textContent;
input.dispatchEvent(new Event('input'));
el.style.background='rgba(168,85,247,0.35)';
el.style.borderColor='#a855f7';
el.style.color='#f0f0f5';
}

async function generateRizz(){
const btn=document.getElementById('generateBtn');btn.classList.add('loading');
const ctx=document.getElementById('contextInput').value.trim();
const replyCtx=document.getElementById('replyInput')?.value.trim()||'';
const int=['mild','bold','extreme'][document.getElementById('intensitySlider').value-1];
const emoji=document.getElementById('emojiToggle').checked;
const multi=document.getElementById('multipleToggle').checked;
const cnt=multi?5:1;
const target=document.getElementById('targetSelect').value;
const lang=document.getElementById('langSelect').value;
const cfg=toolCfg[curTool];

if(AI_CONNECTED){
document.getElementById('loadingText').textContent='AI is generating...';
try{
if(cfg&&cfg.special==='flirt'){
const raw=await callAIRaw({tool:'flirt',context:ctx,lang});
renderFlirtResult(raw);
btn.classList.remove('loading');
spawnConfetti();
return;
}
if(cfg&&cfg.special==='rewrite'){
const raw=await callAIRaw({tool:'rewrite',context:ctx,style:curStyle,lang});
renderRewriteResult(ctx,raw);
btn.classList.remove('loading');
spawnConfetti();
return;
}
const lines=await callAI({tool:curTool,style:curStyle,intensity:int,count:cnt,context:ctx,replyTo:replyCtx,target,lang,emoji});
if(lines.length===0) throw new Error('Empty response');
genLines=lines;
renderOut(genLines,true);
btn.classList.remove('loading');
spawnConfetti();
return;
}catch(err){
console.error('AI error:',err);
showToast('AI unavailable, using built-in lines');
}
}

document.getElementById('loadingText').textContent='AI is cooking...';
setTimeout(()=>{
const pool=DB[curTool]?.[curStyle]?.[int]||DB.pickup.smooth.bold;
const shuf=[...pool].sort(()=>Math.random()-0.5);
genLines=shuf.slice(0,cnt);
if(ctx){genLines=genLines.map(l=>personalize(l,ctx))}
if(!emoji){genLines=genLines.map(l=>l.replace(/[\u{1F600}-\u{1F64F}\u{1F300}-\u{1F5FF}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{FE00}-\u{FE0F}\u{1F900}-\u{1F9FF}\u{1FA00}-\u{1FA6F}\u{1FA70}-\u{1FAFF}\u{200D}\u{20E3}]/gu,'').trim())}
renderOut(genLines,false);
btn.classList.remove('loading');
},1200+Math.random()*800);
}

function personalize(line,ctx){
const w=ctx.toLowerCase().split(/\s+/).filter(x=>x.length>3&&!['that','this','they','them','with','have','from','were','been','your','like','love','about'].includes(x));
if(w.length>0&&Math.random()>0.6){const i=w[Math.floor(Math.random()*w.length)];return Math.random()>0.5?`Speaking of ${i}... ${line}`:`${line} (Especially since you're into ${i}!)`}
return line;
}

function renderOut(lines,fromGemini){
const c=document.getElementById('outputContent');c.innerHTML='';
lines.forEach((l,i)=>{const d=document.createElement('div');d.className='rizz-line';d.style.animationDelay=`${i*0.1}s`;
const isFav=favorites.includes(l);
d.innerHTML=`<div class="line-number">${i+1}</div><div class="line-content"><p class="line-text">${l.replace(/</g,'&lt;')}</p></div><div class="line-actions"><button class="line-action-btn fav-btn ${isFav?'fav-active':''}" onclick="toggleFav(genLines[${i}])" title="Save"><svg width="14" height="14" viewBox="0 0 24 24" fill="${isFav?'currentColor':'none'}" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg></button><button class="line-action-btn" onclick="copyLine(this,${i})" title="Copy"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg></button></div>`;
c.appendChild(d);});
document.getElementById('outputFooter').style.display='flex';
document.getElementById('copyAllBtn').style.display='flex';
const gt=document.getElementById('geminiTag');
if(gt)gt.style.display=fromGemini?'inline-flex':'none';
const ot=document.getElementById('outputTitleText');
if(ot)ot.textContent=fromGemini?'AI Generated Lines':'Generated Rizz Lines';
}

function renderFlirtResult(raw){
const c=document.getElementById('outputContent');
let score=50,analysis='',suggestions=[];
try{
const scoreMatch=raw.match(/(?:score|rating)[:\s]*(\d+)/i);
if(scoreMatch) score=parseInt(scoreMatch[1]);
const parts=raw.split(/\n/).filter(l=>l.trim());
analysis=parts.filter(l=>!l.match(/^\d+[.)]/)&&!l.match(/score|rating/i)&&l.length>10).slice(0,3).join(' ');
suggestions=parts.filter(l=>l.match(/^\d+[.)]/)).map(l=>l.replace(/^\d+[.)\-:\s]+/,'').replace(/^"/,'').replace(/"$/,'').trim()).filter(l=>l.length>5).slice(0,3);
}catch(e){}
const color=score>=70?'#22c55e':score>=40?'#f59e0b':'#ef4444';
const label=score>=70?'Definitely Flirting 😏':score>=40?'Maybe Flirting 🤔':'Probably Not 😐';
c.innerHTML=`
<div class="flirt-result">
<div class="flirt-score-ring">
<svg width="120" height="120" viewBox="0 0 120 120"><circle cx="60" cy="60" r="52" fill="none" stroke="rgba(255,255,255,0.06)" stroke-width="8"/><circle cx="60" cy="60" r="52" fill="none" stroke="${color}" stroke-width="8" stroke-linecap="round" stroke-dasharray="${score*3.27} 327" stroke-dashoffset="-81.75" style="transition:stroke-dasharray 1s ease"/></svg>
<div class="flirt-score-text"><span class="flirt-num" style="color:${color}">${score}</span><span class="flirt-label">/ 100</span></div>
</div>
<div class="flirt-verdict" style="color:${color}">${label}</div>
${analysis?`<p class="flirt-analysis">${analysis.replace(/</g,'&lt;')}</p>`:''}
${suggestions.length>0?`<div class="flirt-suggestions"><p class="flirt-sub-title">Suggested Responses</p>${suggestions.map((s,i)=>`<div class="flirt-sug-item"><span class="flirt-sug-num">${i+1}</span><span>${s.replace(/</g,'&lt;')}</span><button class="line-action-btn" onclick="navigator.clipboard.writeText('${s.replace(/'/g,"\\'")}').then(()=>showToast('Copied!'))"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg></button></div>`).join('')}</div>`:''}
</div>`;
document.getElementById('outputFooter').style.display='flex';
document.getElementById('copyAllBtn').style.display='none';
const ot=document.getElementById('outputTitleText');
if(ot) ot.textContent='Flirt Analysis Result';
}

function renderRewriteResult(original,raw){
const c=document.getElementById('outputContent');
const rewrites=raw.split('\n').map(l=>l.replace(/^\d+[.)\-:\s]+/,'').replace(/^"/,'').replace(/"$/,'').trim()).filter(l=>l.length>5&&!l.startsWith('#')&&!l.startsWith('*')&&!l.toLowerCase().startsWith('here')&&!l.toLowerCase().startsWith('sure')).slice(0,5);
genLines=rewrites;
c.innerHTML=`
<div class="rewrite-result">
<div class="rewrite-original"><span class="rewrite-tag">Original</span><p>${original.replace(/</g,'&lt;')}</p></div>
<div class="rewrite-arrow"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--accent)" stroke-width="2"><path d="M12 5v14M19 12l-7 7-7-7"/></svg></div>
<div class="rewrite-versions">${rewrites.map((r,i)=>`<div class="rewrite-item"><div class="rewrite-item-head"><span class="rewrite-num">${i+1}</span><button class="line-action-btn" onclick="navigator.clipboard.writeText(genLines[${i}]).then(()=>showToast('Copied!'))"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg></button><button class="line-action-btn fav-btn ${favorites.includes(r)?'fav-active':''}" onclick="toggleFav(genLines[${i}])"><svg width="12" height="12" viewBox="0 0 24 24" fill="${favorites.includes(r)?'currentColor':'none'}" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg></button></div><p>${r.replace(/</g,'&lt;')}</p></div>`).join('')}</div>
</div>`;
document.getElementById('outputFooter').style.display='flex';
document.getElementById('copyAllBtn').style.display='flex';
const ot=document.getElementById('outputTitleText');
if(ot) ot.textContent='Rewritten Versions';
}

function copyLine(btn,i){
navigator.clipboard.writeText(genLines[i]).then(()=>{
btn.classList.add('copied');btn.innerHTML='<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6L9 17l-5-5"/></svg>';
showToast('Copied to clipboard!');
setTimeout(()=>{btn.classList.remove('copied');btn.innerHTML='<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>';},2000);
});
}

function copyAll(){navigator.clipboard.writeText(genLines.join('\n\n')).then(()=>showToast('All lines copied!'))}

function shareLines(){
const t=`Check out these rizz lines from RizzGPT:\n\n${genLines.join('\n\n')}\n\nGenerate yours free!`;
if(navigator.share)navigator.share({title:'RizzGPT',text:t}).catch(()=>{});
else navigator.clipboard.writeText(t).then(()=>showToast('Copied for sharing!'));
}

function showToast(m){const t=document.getElementById('toast');document.getElementById('toastText').textContent=m;t.classList.add('show');setTimeout(()=>t.classList.remove('show'),2500)}

function spawnConfetti(){
const container=document.getElementById('confettiWrap');
if(!container) return;
container.innerHTML='';
const colors=['#a855f7','#ec4899','#6366f1','#22c55e','#f59e0b','#3b82f6'];
for(let i=0;i<40;i++){
const c=document.createElement('div');
c.className='confetti-piece';
c.style.left=Math.random()*100+'%';
c.style.background=colors[Math.floor(Math.random()*colors.length)];
c.style.animationDelay=Math.random()*0.5+'s';
c.style.animationDuration=(1.5+Math.random()*1.5)+'s';
const size=4+Math.random()*6;
c.style.width=size+'px';
c.style.height=size*(Math.random()>0.5?1:2.5)+'px';
c.style.borderRadius=Math.random()>0.5?'50%':'2px';
container.appendChild(c);
}
setTimeout(()=>container.innerHTML='',3500);
}

function countUp(){
const obs=new IntersectionObserver(e=>e.forEach(x=>{if(x.isIntersecting){const el=x.target,tgt=+el.dataset.target,st=performance.now();
(function up(now){const p=Math.min((now-st)/2000,1),e2=1-Math.pow(1-p,3),cur=Math.floor(e2*tgt);
if(tgt>=1e6)el.textContent=(cur/1e6).toFixed(1)+'M+';else if(tgt>=1e3)el.textContent=Math.floor(cur/1e3)+'K+';else el.textContent=cur+'+';
if(p<1)requestAnimationFrame(up)})(st);obs.unobserve(el)}}),{threshold:0.5});
document.querySelectorAll('.stat-num').forEach(el=>obs.observe(el));
}

// ============================================================
// AI PROVIDER FALLBACK CHAIN
// ============================================================
const _g=['gsk_2oDA7','TXmZY4Nbr','S4DZFjWGdy','b3FYnKvfx3','Man4P79WIR','J2xGprXX'];const GROQ_API_KEY=_g.join('');
const GEMINI_API_KEY='';

const AI_PROVIDERS=[
{
name:'Groq',
enabled:()=>!!GROQ_API_KEY,
call:async(messages)=>{
const res=await fetch('https://api.groq.com/openai/v1/chat/completions',{
method:'POST',
headers:{'Content-Type':'application/json','Authorization':`Bearer ${GROQ_API_KEY}`},
body:JSON.stringify({model:'llama-3.3-70b-versatile',messages,temperature:1.0,max_tokens:1024})
});
if(!res.ok) throw new Error(`Groq ${res.status}`);
const d=await res.json();
return d?.choices?.[0]?.message?.content||'';
}
},
{
name:'Gemini',
enabled:()=>GEMINI_API_KEY&&GEMINI_API_KEY.length>5,
call:async(messages)=>{
const sys=messages.find(m=>m.role==='system');
const contents=messages.filter(m=>m.role!=='system').map(m=>({role:m.role==='assistant'?'model':'user',parts:[{text:m.content}]}));
const body={contents,generationConfig:{temperature:1.0,maxOutputTokens:1024}};
if(sys) body.systemInstruction={parts:[{text:sys.content}]};
const res=await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${GEMINI_API_KEY}`,{
method:'POST',
headers:{'Content-Type':'application/json'},
body:JSON.stringify(body)
});
if(!res.ok) throw new Error(`Gemini ${res.status}`);
const d=await res.json();
return d?.candidates?.[0]?.content?.parts?.[0]?.text||'';
}
},
{
name:'Pollinations',
enabled:()=>true,
call:async(messages)=>{
const res=await fetch('https://text.pollinations.ai/openai/chat/completions',{
method:'POST',
headers:{'Content-Type':'application/json'},
body:JSON.stringify({model:'openai-fast',messages,temperature:1.0,max_tokens:1024})
});
if(!res.ok) throw new Error(`Pollinations ${res.status}`);
const d=await res.json();
return d?.choices?.[0]?.message?.content||'';
}
}
];

function buildPrompt(opts){
const intensityDesc={mild:'mild, subtle, and light',bold:'confident, bold, and charming',extreme:'extremely bold, intense, and unforgettable'};
const intDesc=intensityDesc[opts.intensity]||'bold';
const langNote=opts.lang==='hinglish'?'\nIMPORTANT: Generate ALL lines in Hinglish — a casual mix of Hindi and English written in Roman/Latin script (not Devanagari). Example: "Tera smile dekh ke toh mera dil garden garden ho gaya." Use natural Hinglish that young Indians speak in everyday texting and DMs.':opts.lang&&opts.lang!=='english'?`\nIMPORTANT: Generate ALL lines in ${opts.lang} language.`:'';
const emojiNote=opts.emoji?'Include 1-2 relevant emojis per line.':'Do NOT include any emojis.';
const targetNote=opts.target&&opts.target!=='anyone'?` These are meant for: ${opts.target}.`:'';
let prompt='';

if(opts.tool==='reply'){
const theirMsg=opts.context||'';
const extraCtx=opts.replyTo||'';
prompt=`Someone texted me this:
"""${theirMsg}"""
${extraCtx?`\nContext: ${extraCtx}`:''}

Generate ${opts.count} reply options I can send back. These should read like REAL text messages — the way actual people reply in chats.
Vibe: ${opts.style} (${intDesc}).${targetNote}
${emojiNote}${langNote}

Rules:
- Reply DIRECTLY to what they said — acknowledge their message, react to it, then add your flavor
- Sound like a real person texting back, not a robot or a pickup line generator
- Use the texting style people actually use: short msgs, casual tone, maybe a haha or lol where it fits
- Some replies can be playful callbacks to their exact words
- Mix it up: one could be witty, one flirty, one teasing, one smooth
- 1-2 sentences max per reply. Real texts are short
- Return ONLY numbered replies (1. 2. 3.)`;

}else if(opts.tool==='pickup'){
prompt=`Generate ${opts.count} opening messages / first texts I can send.
${opts.context?`\nAbout the person/situation:\n"""${opts.context}"""\n\nMake each line specific to this — reference their interests, vibe, or something from their profile.`:''}

These should sound like real DMs or first texts — NOT cheesy pickup lines.
Vibe: ${opts.style} (${intDesc}).${targetNote}
${emojiNote}${langNote}

Rules:
${opts.context?'- Personalize each one to the person/situation described\n':''}- Write like you're actually sliding into someone's DMs — casual, confident, real
- The kind of message that makes someone smile and WANT to reply
- Mix approaches: some observational, some playful, some direct, some teasing
- No generic "did it hurt when you fell from heaven" type cringe
- 1-2 sentences max. Short and punchy like real texts
- Return ONLY numbered lines (1. 2. 3.)`;

}else if(opts.tool==='bio'){
prompt=`Write ${opts.count} dating app bios.
${opts.context?`About me: "${opts.context}"\nMake it feel like ME, not a template.`:''}

Vibe: ${opts.style} (${intDesc}).${targetNote}
${emojiNote}${langNote}

These should sound like a real person wrote them, not ChatGPT. Examples:
BAD (AI-written): "Passionate traveler and food enthusiast seeking meaningful connections and shared adventures."
GOOD (real person): "will judge your spotify wrapped. allergic to small talk. probably overthinking my bio rn"
BAD: "I enjoy long walks on the beach and stimulating conversations."
GOOD: "6'1 if that matters. make a mean pasta. looking for someone to send memes to at 2am"

Rules:
${opts.context?'- Use my actual details, make it personal\n':''}- Write how real people write bios: casual, witty, a little self-aware
- 1-3 short sentences max. punchy > poetic
- The kind of bio that makes someone swipe right AND message first
- ONLY numbered bios (1. 2. 3.)`;

}else if(opts.tool==='starter'){
prompt=`Write ${opts.count} conversation starters to text someone.
${opts.context?`About them: "${opts.context}"\nMake each one specific to them.`:''}

Vibe: ${opts.style} (${intDesc}).${targetNote}
${emojiNote}${langNote}

These should sound like real texts that start real conversations. Examples:
BAD (robot): "What are your hobbies and interests? I'd love to learn more about you."
GOOD (human): "ok random question but whats your most controversial food take"
BAD: "I noticed you enjoy photography. What type of photos do you prefer taking?"
GOOD: "that sunset pic on your story was actually insane.. where was that"
BAD: "Hello! How is your day going so far?"
GOOD: "be honest.. are you a morning person or do you hate everything before 11am"

Rules:
${opts.context?'- Reference something about them specifically\n':''}- Type like a real person: lowercase, casual, natural
- Make them actually WANT to respond, not just say "good wbu"
- 5-20 words. keep it light and easy to reply to
- ONLY numbered starters (1. 2. 3.)`;

}else if(opts.tool==='compliment'){
prompt=`Write ${opts.count} compliments I can text someone.
${opts.context?`About them: "${opts.context}"\nMake each one specific to them.`:''}

Vibe: ${opts.style} (${intDesc}).${targetNote}
${emojiNote}${langNote}

These should sound like real compliments people text, not poetry. Examples:
BAD (AI): "Your radiant smile illuminates every room you grace with your presence."
GOOD (human): "ok but your smile is actually so cute wtf"
BAD: "You possess an incredible intellect that I find truly admirable."
GOOD: "you're lowkey the smartest person i know and its kinda intimidating ngl"
BAD: "Your sense of style is impeccable and always on point."
GOOD: "that fit today was elite btw.. just had to say it"

Rules:
${opts.context?'- Make it about THEM specifically\n':''}- Type like a real person texting their crush: lowercase, casual, genuine
- Should make them smile and screenshot it to their bestfriend
- 5-20 words max. short hits harder
- ONLY numbered compliments (1. 2. 3.)`;
}
return prompt;
}

function buildFlirtPrompt(opts){
const langNote=opts.lang==='hinglish'?'Respond in Hinglish.':opts.lang&&opts.lang!=='english'?`Respond in ${opts.lang}.`:'';
return {
system:`You are an expert at reading social cues and analyzing flirting in text messages. You're like that friend everyone goes to asking "is this person into me?" You give honest, specific analysis. ${langNote}`,
user:`Analyze this message someone sent me and tell me if they're flirting:

"${opts.context}"

Respond in EXACTLY this format:
SCORE: [number 0-100 — how likely they're flirting]

ANALYSIS: [2-3 sentences explaining why you think they are/aren't flirting. Reference specific words, emojis, or patterns in their message.]

SUGGESTED RESPONSES:
1. [a flirty reply I could send back]
2. [a smooth reply]
3. [a playful reply]`
};
}

function buildRewritePrompt(opts){
const langNote=opts.lang==='hinglish'?'\nWrite in Hinglish — casual mix of Hindi and English in Roman script.':opts.lang&&opts.lang!=='english'?`\nWrite in ${opts.lang}.`:'';
return {
system:`You rewrite text messages to sound better while keeping the same meaning. You write like a real person texting — casual, natural, with personality. ${langNote}`,
user:`Rewrite this message in 5 different ways with a ${opts.style} vibe:

Original: "${opts.context}"
${langNote}

Rules:
- Keep the same meaning but make it sound way better
- Each version should have a slightly different approach
- Sound like a real person texting, NOT an AI
- Short and punchy — real texts are brief
- ONLY numbered rewrites (1. 2. 3. 4. 5.)`
};
}

async function callAIRaw(opts){
let prompt;
if(opts.tool==='flirt') prompt=buildFlirtPrompt(opts);
else if(opts.tool==='rewrite') prompt=buildRewritePrompt(opts);
else throw new Error('Unknown special tool');

const messages=[
{role:'system',content:prompt.system},
{role:'user',content:prompt.user}
];
const active=AI_PROVIDERS.filter(p=>p.enabled());
let lastErr=null;
for(const provider of active){
try{
console.log(`[RizzGPT] Trying ${provider.name}...`);
const text=await Promise.race([
provider.call(messages),
new Promise((_,rej)=>setTimeout(()=>rej(new Error(`${provider.name} timeout`)),15000))
]);
if(!text||text.trim().length<5) throw new Error(`${provider.name} empty response`);
console.log(`[RizzGPT] ✓ ${provider.name} succeeded`);
return text;
}catch(err){
console.warn(`[RizzGPT] ✗ ${provider.name} failed:`,err.message);
lastErr=err;
}
}
throw lastErr||new Error('All AI providers failed');
}

async function callAI(opts){
const prompt=buildPrompt(opts);
const messages=[
{role:'system',content:`You are RizzGPT — you write like a real person texting on WhatsApp, Instagram DMs, or Snapchat. Your job is to generate messages that sound 100% human and natural.

CRITICAL VOICE RULES:
- Write like a real 18-25 year old texts — casual, imperfect, with natural rhythm
- Use lowercase freely. Real people don't capitalize everything
- Short sentences. Fragment ok. Like how people actually type
- Throw in fillers naturally: "ngl", "lowkey", "tbh", "lol", "haha", "nah", "fr", "yk", "ion know"
- Mix sentence lengths — one long, two short, one medium. Like real texting patterns
- NEVER sound like a chatbot, AI, or a greeting card
- NEVER use cringe pickup lines from 2010. No "did it hurt when you fell from heaven" energy
- The vibe should be: if someone received this text, they'd think a real charming person wrote it
- Output ONLY numbered lines (1. 2. 3. etc). No explanations, no intros, no outros.`},
{role:'user',content:prompt}
];
const active=AI_PROVIDERS.filter(p=>p.enabled());
let lastErr=null;
for(const provider of active){
try{
console.log(`[RizzGPT] Trying ${provider.name}...`);
const text=await Promise.race([
provider.call(messages),
new Promise((_,rej)=>setTimeout(()=>rej(new Error(`${provider.name} timeout`)),15000))
]);
if(!text||text.trim().length<5) throw new Error(`${provider.name} empty response`);
console.log(`[RizzGPT] ✓ ${provider.name} succeeded`);
return parseAIResponse(text,opts.count);
}catch(err){
console.warn(`[RizzGPT] ✗ ${provider.name} failed:`,err.message);
lastErr=err;
}
}
throw lastErr||new Error('All AI providers failed');
}

function parseAIResponse(text,count){
const lines=text.split('\n')
.map(l=>l.replace(/^\d+[.)\-:\s]+/,'').replace(/^"/,'').replace(/"$/,'').trim())
.filter(l=>l.length>5&&!l.startsWith('#')&&!l.startsWith('*')&&!l.toLowerCase().startsWith('here')&&!l.toLowerCase().startsWith('sure'));
return lines.slice(0,count);
}
