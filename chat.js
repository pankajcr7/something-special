const _g=['gsk_2oDA7','TXmZY4Nbr','S4DZFjWGdy','b3FYnKvfx3','Man4P79WIR','J2xGprXX'];
const GROQ_KEY=_g.join('');
const GEMINI_KEY='';

let messages=[];
let currentSender='them';
let style='smooth';
let intensity='bold';
let lang='hinglish';
let generating=false;
let coaching=false;
let ssAnalyzing=false;

const providers=[
{
name:'Groq',
enabled:()=>!!GROQ_KEY,
call:async(msgs)=>{
const res=await fetch('https://api.groq.com/openai/v1/chat/completions',{
method:'POST',
headers:{'Content-Type':'application/json','Authorization':`Bearer ${GROQ_KEY}`},
body:JSON.stringify({model:'llama-3.3-70b-versatile',messages:msgs,temperature:1.0,max_tokens:1024})
});
if(!res.ok) throw new Error(`Groq ${res.status}`);
const d=await res.json();
return d?.choices?.[0]?.message?.content||'';
}
},
{
name:'Gemini',
enabled:()=>GEMINI_KEY&&GEMINI_KEY.length>5,
call:async(msgs)=>{
const sys=msgs.find(m=>m.role==='system');
const contents=msgs.filter(m=>m.role!=='system').map(m=>({role:m.role==='assistant'?'model':'user',parts:[{text:m.content}]}));
const body={contents,generationConfig:{temperature:1.0,maxOutputTokens:1024}};
if(sys) body.systemInstruction={parts:[{text:sys.content}]};
const res=await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${GEMINI_KEY}`,{
method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)
});
if(!res.ok) throw new Error(`Gemini ${res.status}`);
const d=await res.json();
return d?.candidates?.[0]?.content?.parts?.[0]?.text||'';
}
},
{
name:'Pollinations',
enabled:()=>true,
call:async(msgs)=>{
const res=await fetch('https://text.pollinations.ai/openai/chat/completions',{
method:'POST',headers:{'Content-Type':'application/json'},
body:JSON.stringify({model:'openai-fast',messages:msgs,temperature:1.0,max_tokens:1024})
});
if(!res.ok) throw new Error(`Pollinations ${res.status}`);
const d=await res.json();
return d?.choices?.[0]?.message?.content||'';
}
}
];

document.addEventListener('DOMContentLoaded',()=>{
const settingsToggle=document.getElementById('settingsToggle');
const settingsPanel=document.getElementById('settingsPanel');
settingsToggle.addEventListener('click',()=>settingsPanel.classList.toggle('open'));

document.querySelectorAll('.chip').forEach(c=>c.addEventListener('click',()=>{
document.querySelectorAll('.chip').forEach(x=>x.classList.remove('active'));
c.classList.add('active');
style=c.dataset.style;
}));

document.querySelectorAll('.pill').forEach(p=>p.addEventListener('click',()=>{
document.querySelectorAll('.pill').forEach(x=>x.classList.remove('active'));
p.classList.add('active');
intensity=p.dataset.int;
}));

document.getElementById('chatLang').addEventListener('change',e=>lang=e.target.value);

document.getElementById('theirName').addEventListener('input',e=>{
const name=e.target.value.trim()||'Chat Simulator';
document.getElementById('navName').textContent=name;
document.getElementById('navAvatar').textContent=name.charAt(0).toUpperCase();
});

document.querySelectorAll('.sender-btn').forEach(b=>b.addEventListener('click',()=>{
document.querySelectorAll('.sender-btn').forEach(x=>x.classList.remove('active'));
b.classList.add('active');
currentSender=b.dataset.sender;
const input=document.getElementById('msgInput');
input.placeholder=currentSender==='them'?"Type their message...":"Type your message...";
input.focus();
}));

const textarea=document.getElementById('msgInput');
textarea.addEventListener('input',()=>{
textarea.style.height='auto';
textarea.style.height=Math.min(textarea.scrollHeight,100)+'px';
});
textarea.addEventListener('keydown',e=>{
if(e.key==='Enter'&&!e.shiftKey){e.preventDefault();addMessage();}
});

document.getElementById('sendBtn').addEventListener('click',addMessage);
document.getElementById('aiBtn').addEventListener('click',()=>generateReply());
document.getElementById('closeReplies').addEventListener('click',closeReplies);
document.getElementById('regenBtn').addEventListener('click',()=>generateReply());
document.getElementById('clearChat').addEventListener('click',clearChat);
document.getElementById('coachBtn').addEventListener('click',runCoach);
document.getElementById('closeCoach').addEventListener('click',closeCoach);
document.getElementById('exportChat').addEventListener('click',exportChatAsImage);

const ssInput=document.getElementById('ssUploadInput');
document.getElementById('ssBtn').addEventListener('click',()=>ssInput.click());
const ssHintBtn=document.getElementById('uploadSSHint');
if(ssHintBtn) ssHintBtn.addEventListener('click',()=>ssInput.click());
ssInput.addEventListener('change',handleSSUpload);
document.getElementById('closeSS').addEventListener('click',closeSSPanel);

document.querySelectorAll('.hint-btn').forEach(b=>b.addEventListener('click',()=>{
addMessageDirect(b.dataset.sender,b.dataset.text);
}));

loadChat();
});

function addMessage(){
const input=document.getElementById('msgInput');
const text=input.value.trim();
if(!text) return;
addMessageDirect(currentSender,text);
input.value='';
input.style.height='auto';
input.focus();
}

function addMessageDirect(sender,text){
const msg={id:Date.now(),sender,text,time:new Date()};
messages.push(msg);
renderMessages();
saveChat();
scrollToBottom();
hideHint();
}

function deleteMessage(id){
messages=messages.filter(m=>m.id!==id);
renderMessages();
saveChat();
}

function copyMessage(text){
navigator.clipboard.writeText(text).then(()=>showToast('Copied!'));
}

function renderMessages(){
const container=document.getElementById('chatMessages');
const dateDiv=container.querySelector('.chat-date');
const hintDiv=container.querySelector('.chat-hint');

const existingMsgs=container.querySelectorAll('.msg-row, .generate-marker');
existingMsgs.forEach(el=>el.remove());

messages.forEach((msg,i)=>{
const row=document.createElement('div');
row.className=`msg-row ${msg.sender}`;
row.dataset.id=msg.id;

const timeStr=formatTime(msg.time);
row.innerHTML=`
<div class="msg-bubble">${escapeHtml(msg.text)}</div>
<div class="msg-time">${timeStr}</div>
<div class="msg-actions">
<button class="msg-action-btn" onclick="copyMessage('${escapeJs(msg.text)}')">Copy</button>
<button class="msg-action-btn del" onclick="deleteMessage(${msg.id})">Delete</button>
</div>`;

container.appendChild(row);

if(i<messages.length-1){
const marker=document.createElement('div');
marker.className='generate-marker';
marker.innerHTML=`<button class="gen-here-btn" onclick="generateReply(${i+1})">
<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
Generate reply here</button>`;
container.appendChild(marker);
}
});

if(messages.length>0&&hintDiv) hintDiv.style.display='none';
}

function hideHint(){
const h=document.querySelector('.chat-hint');
if(h) h.style.display='none';
}

function formatTime(d){
const date=new Date(d);
const h=date.getHours();
const m=String(date.getMinutes()).padStart(2,'0');
const ampm=h>=12?'PM':'AM';
return `${h%12||12}:${m} ${ampm}`;
}

function escapeHtml(t){return t.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');}
function escapeJs(t){return t.replace(/\\/g,'\\\\').replace(/'/g,"\\'").replace(/\n/g,'\\n');}

async function generateReply(insertAt){
if(generating) return;
if(messages.length===0){showToast('Add some messages first');return;}

generating=true;
const aiBtn=document.getElementById('aiBtn');
aiBtn.classList.add('loading');

showTyping();

const conversation=buildConversation(insertAt);

try{
const text=await callProviders(conversation);
const replies=parseReplies(text);
if(replies.length===0) throw new Error('Empty');
hideTyping();
showReplies(replies,insertAt);
}catch(err){
console.error('AI error:',err);
hideTyping();
showToast('AI failed, try again');
}

generating=false;
aiBtn.classList.remove('loading');
}

function buildConversation(insertAt){
const slice=insertAt!=null?messages.slice(0,insertAt):messages;
const theirName=document.getElementById('theirName').value.trim()||'them';

const intensityDesc={mild:'mild and subtle',bold:'confident and charming',extreme:'extremely bold and intense'};
const intDesc=intensityDesc[intensity]||'bold';
const langNote=lang==='hinglish'?'\nIMPORTANT: Write ALL replies in Hinglish — casual mix of Hindi and English in Roman script. Like how young Indians actually text. Example: "arre yaar tu toh full cute hai ngl"':lang&&lang!=='english'?`\nIMPORTANT: Write ALL replies in ${lang}.`:'';

let chatLog=slice.map(m=>`${m.sender==='me'?'Me':theirName}: ${m.text}`).join('\n');

const lastSender=slice.length>0?slice[slice.length-1].sender:'them';

const sysPrompt=`You generate text message replies that read EXACTLY like a real human typed them in a chat app.

RULES — non-negotiable:
1. TYPE LIKE A REAL PERSON: mostly lowercase, skip periods sometimes, use "..." for pauses, "haha" "lol" "ngl" "tbh" "fr" "lowkey" "yk" "bruh" naturally. imperfect grammar OK.
2. KEEP IT SHORT: 5-20 words per reply. real texts are short, not essays.
3. SOUND HUMAN, NOT AI:
   BAD: "I must say, your smile is truly captivating."
   GOOD: "ok but your smile is actually so cute wtf"
   BAD: "Thank you for the compliment! You're quite attractive yourself."
   GOOD: "haha stoppp you're making me blush"
   BAD: "I would love to spend time with you!"
   GOOD: "wait fr?? im down lol when"
4. READ THE FULL CONVERSATION and reply in context. Reference what was said. Don't be random.
5. Output ONLY numbered lines (1. 2. 3. etc). Nothing else.`;

const userPrompt=`Here's the chat conversation so far:
---
${chatLog}
---

Generate 5 replies I ("Me") can send next. Read the FULL conversation above and reply naturally to what ${lastSender==='me'?'I':'they'} just said.

Vibe: ${style} (${intDesc}).
${langNote}

These must sound like real texts from a real person chatting. Short, casual, natural. React to what they actually said — use their words, tease them, be specific to THIS conversation. ONLY numbered replies (1. 2. 3. 4. 5.)`;

return [
{role:'system',content:sysPrompt},
{role:'user',content:userPrompt}
];
}

async function callProviders(msgs){
const active=providers.filter(p=>p.enabled());
let lastErr;
for(const p of active){
try{
console.log(`[Chat] Trying ${p.name}...`);
const text=await Promise.race([
p.call(msgs),
new Promise((_,rej)=>setTimeout(()=>rej(new Error('timeout')),15000))
]);
if(!text||text.trim().length<5) throw new Error('empty');
console.log(`[Chat] ✓ ${p.name}`);
return text;
}catch(err){
console.warn(`[Chat] ✗ ${p.name}:`,err.message);
lastErr=err;
}
}
throw lastErr||new Error('All failed');
}

function parseReplies(text){
return text.split('\n')
.map(l=>l.replace(/^\d+[.)\-:\s]+/,'').replace(/^"/,'').replace(/"$/,'').trim())
.filter(l=>l.length>3&&!l.startsWith('#')&&!l.startsWith('*')&&!l.toLowerCase().startsWith('here')&&!l.toLowerCase().startsWith('sure'));
}

function showReplies(replies,insertAt){
const panel=document.getElementById('aiReplies');
const list=document.getElementById('aiRepliesList');
list.innerHTML='';

replies.forEach((r,i)=>{
const btn=document.createElement('button');
btn.className='ai-reply-option';
btn.innerHTML=`<span class="reply-num">${i+1}</span><span class="reply-text">${escapeHtml(r)}</span>`;
btn.addEventListener('click',()=>{
if(insertAt!=null){
const msg={id:Date.now(),sender:'me',text:r,time:new Date()};
messages.splice(insertAt,0,msg);
}else{
messages.push({id:Date.now(),sender:'me',text:r,time:new Date()});
}
renderMessages();
saveChat();
scrollToBottom();
closeReplies();
showToast('Reply added!');
});
list.appendChild(btn);
});

panel.style.display='block';
adjustLayout();
}

function closeReplies(){
document.getElementById('aiReplies').style.display='none';
adjustLayout();
}

function adjustLayout(){
const panel=document.getElementById('aiReplies');
const isOpen=panel.style.display==='block';
const chatContainer=document.getElementById('chatContainer');
chatContainer.style.bottom=isOpen?`${110+panel.offsetHeight}px`:'110px';
}

function showTyping(){
const container=document.getElementById('chatMessages');
const existing=container.querySelector('.typing-indicator');
if(existing) existing.remove();
const el=document.createElement('div');
el.className='typing-indicator';
el.innerHTML='<span></span><span></span><span></span>';
container.appendChild(el);
scrollToBottom();
}

function hideTyping(){
const el=document.querySelector('.typing-indicator');
if(el) el.remove();
}

function scrollToBottom(){
const c=document.getElementById('chatMessages');
requestAnimationFrame(()=>c.scrollTop=c.scrollHeight);
}

function clearChat(){
if(!confirm('Clear all messages?')) return;
messages=[];
localStorage.removeItem('rizzgpt_chat');
const container=document.getElementById('chatMessages');
container.querySelectorAll('.msg-row, .generate-marker').forEach(el=>el.remove());
const hint=container.querySelector('.chat-hint');
if(hint) hint.style.display='';
closeReplies();
closeCoach();
showToast('Chat cleared');
}

function saveChat(){
try{localStorage.setItem('rizzgpt_chat',JSON.stringify(messages));}catch(e){}
}

function loadChat(){
try{
const saved=localStorage.getItem('rizzgpt_chat');
if(saved){
messages=JSON.parse(saved);
if(messages.length>0){
renderMessages();
hideHint();
scrollToBottom();
}
}
}catch(e){}
}

function showToast(text){
const t=document.getElementById('toast');
document.getElementById('toastText').textContent=text;
t.classList.add('show');
setTimeout(()=>t.classList.remove('show'),2000);
}

async function runCoach(){
if(coaching) return;
if(messages.length<2){showToast('Need at least 2 messages');return;}

coaching=true;
const coachBtn=document.getElementById('coachBtn');
coachBtn.classList.add('loading');

const panel=document.getElementById('coachPanel');
const body=document.getElementById('coachBody');
panel.style.display='block';
body.innerHTML='<div class="coach-loading"><div class="spinner"></div>Analyzing conversation...</div>';

const theirName=document.getElementById('theirName').value.trim()||'them';
const chatLog=messages.map(m=>`${m.sender==='me'?'Me':theirName}: ${m.text}`).join('\n');

const sysPrompt=`You're a dating/texting coach. Analyze the chat conversation and give brief, actionable advice. Be real, be specific, be helpful. Don't sugarcoat.

Reply in EXACTLY this format:
VIBE CHECK: [1-2 sentences about how the conversation is going overall]

GREEN FLAGS:
- [positive signal 1]
- [positive signal 2]

RED FLAGS:
- [concerning signal 1]
- [concerning signal 2]
(if none, say "None detected — looking good!")

TIPS:
- [specific actionable tip 1]
- [specific actionable tip 2]
- [specific actionable tip 3]

NEXT MOVE: [one sentence — what should they do/say next]`;

const userPrompt=`Analyze this chat conversation and give me coaching advice:
---
${chatLog}
---`;

try{
const text=await callProviders([
{role:'system',content:sysPrompt},
{role:'user',content:userPrompt}
]);
renderCoachResult(text);
}catch(err){
console.error('Coach error:',err);
body.innerHTML='<p style="color:var(--red);text-align:center;padding:20px">Coach failed — try again</p>';
}

coaching=false;
coachBtn.classList.remove('loading');
}

function renderCoachResult(text){
const body=document.getElementById('coachBody');
let html='';

const vibeMatch=text.match(/VIBE CHECK:\s*(.+?)(?=\n[A-Z]|\n\n|$)/s);
const greenMatch=text.match(/GREEN FLAGS:\s*([\s\S]+?)(?=\nRED|\n\n[A-Z])/);
const redMatch=text.match(/RED FLAGS:\s*([\s\S]+?)(?=\nTIPS|\n\n[A-Z])/);
const tipsMatch=text.match(/TIPS:\s*([\s\S]+?)(?=\nNEXT|\n\n[A-Z])/);
const nextMatch=text.match(/NEXT MOVE:\s*(.+?)(?=\n|$)/s);

if(vibeMatch){
html+=`<div class="coach-section"><div class="coach-section-title">🎯 Vibe Check</div><div class="coach-vibe">${vibeMatch[1].trim().replace(/</g,'&lt;')}</div></div>`;
}

if(greenMatch){
const flags=greenMatch[1].trim().split('\n').map(l=>l.replace(/^-\s*/,'').trim()).filter(l=>l.length>3);
if(flags.length>0){
html+=`<div class="coach-section"><div class="coach-section-title">✅ Green Flags</div><div>${flags.map(f=>`<span class="coach-flag green">✓ ${f.replace(/</g,'&lt;')}</span>`).join('')}</div></div>`;
}
}

if(redMatch){
const flags=redMatch[1].trim().split('\n').map(l=>l.replace(/^-\s*/,'').trim()).filter(l=>l.length>3);
const hasNone=flags.some(f=>f.toLowerCase().includes('none'));
if(flags.length>0&&!hasNone){
html+=`<div class="coach-section"><div class="coach-section-title">⚠️ Red Flags</div><div>${flags.map(f=>`<span class="coach-flag red">✗ ${f.replace(/</g,'&lt;')}</span>`).join('')}</div></div>`;
}else{
html+=`<div class="coach-section"><div class="coach-section-title">⚠️ Red Flags</div><div><span class="coach-flag green">✓ None detected — looking good!</span></div></div>`;
}
}

if(tipsMatch){
const tips=tipsMatch[1].trim().split('\n').map(l=>l.replace(/^-\s*/,'').trim()).filter(l=>l.length>3);
if(tips.length>0){
html+=`<div class="coach-section"><div class="coach-section-title">💡 Tips</div>${tips.map(t=>`<div class="coach-tip"><span class="coach-tip-icon">→</span><span>${t.replace(/</g,'&lt;')}</span></div>`).join('')}</div>`;
}
}

if(nextMatch){
html+=`<div class="coach-section"><div class="coach-section-title">🎯 Next Move</div><div class="coach-vibe" style="border-color:rgba(34,197,94,.2);background:rgba(34,197,94,.06)">${nextMatch[1].trim().replace(/</g,'&lt;')}</div></div>`;
}

if(!html) html=`<div class="coach-vibe">${text.replace(/</g,'&lt;').replace(/\n/g,'<br>')}</div>`;

body.innerHTML=html;
}

function closeCoach(){
document.getElementById('coachPanel').style.display='none';
}

async function exportChatAsImage(){
if(messages.length===0){showToast('No messages to export');return;}

showToast('Generating image...');

const theirName=document.getElementById('theirName').value.trim()||'Chat';
const canvas=document.createElement('canvas');
const ctx=canvas.getContext('2d');
const W=400;
const padding=20;
const bubblePad=10;
const fontSize=14;
const timeSize=10;
const nameSize=12;
const gap=6;

ctx.font=`${fontSize}px Inter, -apple-system, system-ui, sans-serif`;

let totalH=60;
const msgData=[];
messages.forEach(msg=>{
const maxW=W*0.7;
const words=msg.text.split(' ');
let lines=[];let line='';
words.forEach(w=>{
const test=line?line+' '+w:w;
if(ctx.measureText(test).width>maxW-bubblePad*2){lines.push(line);line=w}
else line=test;
});
if(line) lines.push(line);
const bH=lines.length*(fontSize+4)+bubblePad*2;
msgData.push({msg,lines,bH});
totalH+=bH+gap+timeSize+8;
});
totalH+=40;

canvas.width=W;
canvas.height=totalH;

ctx.fillStyle='#0a0a0f';
ctx.fillRect(0,0,W,totalH);

ctx.fillStyle='#111118';
ctx.fillRect(0,0,W,50);
ctx.strokeStyle='rgba(255,255,255,0.06)';
ctx.lineWidth=1;
ctx.beginPath();ctx.moveTo(0,50);ctx.lineTo(W,50);ctx.stroke();

const grad=ctx.createLinearGradient(0,0,30,30);
grad.addColorStop(0,'#7c3aed');grad.addColorStop(1,'#a855f7');
ctx.fillStyle=grad;
ctx.beginPath();ctx.arc(30,25,15,0,Math.PI*2);ctx.fill();
ctx.fillStyle='#fff';ctx.font=`bold ${nameSize}px Inter, sans-serif`;
ctx.textAlign='center';ctx.fillText(theirName.charAt(0).toUpperCase(),30,29);

ctx.textAlign='left';
ctx.fillStyle='#f0f0f5';ctx.font=`bold ${nameSize+2}px Inter, sans-serif`;
ctx.fillText(theirName,52,23);
ctx.fillStyle='#22c55e';ctx.font=`${timeSize}px Inter, sans-serif`;
ctx.fillText('● AI-powered replies',52,38);

let y=66;

ctx.fillStyle='#5a5a72';ctx.font=`${timeSize}px Inter, sans-serif`;
ctx.textAlign='center';
ctx.fillText('Today',W/2,y);
y+=16;
ctx.textAlign='left';

msgData.forEach(({msg,lines,bH})=>{
const isMe=msg.sender==='me';
const bW=Math.max(...lines.map(l=>ctx.measureText(l).width))+bubblePad*2+4;
const x=isMe?W-padding-bW:padding;

if(isMe){
const grd=ctx.createLinearGradient(x,y,x+bW,y+bH);
grd.addColorStop(0,'#7c3aed');grd.addColorStop(1,'#a855f7');
ctx.fillStyle=grd;
}else{
ctx.fillStyle='#1a1a26';
}

const r=14;
ctx.beginPath();
if(isMe){
ctx.moveTo(x+r,y);ctx.lineTo(x+bW-4,y);ctx.quadraticCurveTo(x+bW,y,x+bW,y+4);
ctx.lineTo(x+bW,y+bH-r);ctx.quadraticCurveTo(x+bW,y+bH,x+bW-r,y+bH);
ctx.lineTo(x+r,y+bH);ctx.quadraticCurveTo(x,y+bH,x,y+bH-r);
ctx.lineTo(x,y+r);ctx.quadraticCurveTo(x,y,x+r,y);
}else{
ctx.moveTo(x+4,y);ctx.lineTo(x+bW-r,y);ctx.quadraticCurveTo(x+bW,y,x+bW,y+r);
ctx.lineTo(x+bW,y+bH-r);ctx.quadraticCurveTo(x+bW,y+bH,x+bW-r,y+bH);
ctx.lineTo(x+r,y+bH);ctx.quadraticCurveTo(x,y+bH,x,y+bH-r);
ctx.lineTo(x,y+4);ctx.quadraticCurveTo(x,y,x+4,y);
}
ctx.closePath();ctx.fill();

ctx.fillStyle=isMe?'#ffffff':'#f0f0f5';
ctx.font=`${fontSize}px Inter, -apple-system, system-ui, sans-serif`;
lines.forEach((line,li)=>{
ctx.fillText(line,x+bubblePad,y+bubblePad+fontSize+(li*(fontSize+4)));
});

y+=bH+2;
ctx.fillStyle='#5a5a72';ctx.font=`${timeSize}px Inter, sans-serif`;
const timeStr=formatTime(msg.time);
if(isMe){ctx.textAlign='right';ctx.fillText(timeStr,W-padding,y+timeSize);ctx.textAlign='left';}
else{ctx.fillText(timeStr,padding+6,y+timeSize);}
y+=timeSize+gap+4;
});

ctx.fillStyle='#5a5a72';ctx.font=`${timeSize}px Inter, sans-serif`;
ctx.textAlign='center';
ctx.fillText('Generated by RizzGPT ⚡',W/2,totalH-12);
ctx.textAlign='left';

canvas.toBlob(blob=>{
const url=URL.createObjectURL(blob);
const a=document.createElement('a');
a.href=url;a.download=`rizzgpt-chat-${Date.now()}.png`;
document.body.appendChild(a);a.click();document.body.removeChild(a);
URL.revokeObjectURL(url);
showToast('Chat exported! 📸');
},'image/png');
}

// ============================================================
// SCREENSHOT UPLOAD & ANALYSIS
// ============================================================

async function handleSSUpload(e){
const file=e.target.files?.[0];
if(!file) return;
e.target.value='';

if(!file.type.startsWith('image/')){showToast('Please upload an image');return;}
if(file.size>10*1024*1024){showToast('Image too large (max 10MB)');return;}

if(ssAnalyzing){showToast('Already analyzing...');return;}
ssAnalyzing=true;
lastSSMime=file.type;

const ssBtn=document.getElementById('ssBtn');
ssBtn.classList.add('loading');

const panel=document.getElementById('ssPanel');
const body=document.getElementById('ssPanelBody');
panel.style.display='flex';
body.innerHTML='<div class="ss-loading"><div class="spinner"></div>AI is reading your screenshot...<span style="font-size:.75rem;color:var(--text3);margin-top:4px">This may take a few seconds</span></div>';

try{
const base64=await fileToBase64(file);
lastSSBase64=base64;
const imgPreviewUrl=URL.createObjectURL(file);

body.innerHTML=`<img class="ss-preview" src="${imgPreviewUrl}" alt="Chat screenshot"><div class="ss-loading"><div class="spinner"></div>Reading conversation from screenshot...</div>`;

const extracted=await extractConversation(base64,file.type);
if(!extracted||extracted.trim().length<10) throw new Error('Could not read conversation');

body.innerHTML=`<img class="ss-preview" src="${imgPreviewUrl}" alt="Chat screenshot"><div class="ss-loading"><div class="spinner"></div>Generating coach advice & replies...</div>`;

const [coachResult,replyResult]=await Promise.all([
getSSCoach(extracted),
getSSReplies(extracted)
]);

renderSSResult(imgPreviewUrl,extracted,coachResult,replyResult);

}catch(err){
console.error('Screenshot analysis error:',err);
body.innerHTML=`<div class="ss-loading" style="color:var(--red)"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>Failed to analyze screenshot<span style="font-size:.78rem;color:var(--text3)">${err.message||'Try again with a clearer screenshot'}</span></div>`;
}

ssAnalyzing=false;
ssBtn.classList.remove('loading');
}

function fileToBase64(file){
return new Promise((resolve,reject)=>{
const reader=new FileReader();
reader.onload=()=>resolve(reader.result.split(',')[1]);
reader.onerror=reject;
reader.readAsDataURL(file);
});
}

async function extractConversation(base64,mimeType){
const mime=mimeType||'image/jpeg';
const sysPrompt=`You are a chat screenshot reader. Extract the conversation from the screenshot image.

Rules:
- Read ALL messages visible in the screenshot
- Format each message as: "Person1: message text" or "Person2: message text"
- If you can identify names, use them. Otherwise use "Them" and "Me" (where Me = the user whose phone this is, usually the person with colored/right-aligned bubbles)
- Include ALL messages in order from top to bottom
- Do NOT add any messages that aren't visible
- Do NOT add commentary, just the raw conversation
- If you can't read the screenshot or it's not a chat, say "NOT_A_CHAT"`;

const messages=[
{role:'system',content:sysPrompt},
{role:'user',content:[
{type:'text',text:'Extract the conversation from this chat screenshot. List every message in order.'},
{type:'image_url',image_url:{url:`data:${mime};base64,${base64}`}}
]}
];

const active=providers.filter(p=>p.enabled());
let lastErr;

for(const p of active){
try{
console.log(`[SS] Trying ${p.name} for vision...`);

let text;
if(p.name==='Groq'){
const res=await fetch('https://api.groq.com/openai/v1/chat/completions',{
method:'POST',
headers:{'Content-Type':'application/json','Authorization':`Bearer ${GROQ_KEY}`},
body:JSON.stringify({
model:'llama-3.2-11b-vision-preview',
messages,
temperature:0.3,
max_tokens:1500
})
});
if(!res.ok) throw new Error(`Groq vision ${res.status}`);
const d=await res.json();
text=d?.choices?.[0]?.message?.content||'';
}else if(p.name==='Gemini'){
const contents=[{
role:'user',
parts:[
{text:'Extract the conversation from this chat screenshot. List every message in order.'},
{inline_data:{mime_type:mime,data:base64}}
]
}];
const body={contents,generationConfig:{temperature:0.3,maxOutputTokens:1500}};
body.systemInstruction={parts:[{text:sysPrompt}]};
const res=await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${GEMINI_KEY}`,{
method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)
});
if(!res.ok) throw new Error(`Gemini vision ${res.status}`);
const d=await res.json();
text=d?.candidates?.[0]?.content?.parts?.[0]?.text||'';
}else{
throw new Error('Vision not supported');
}

if(!text||text.includes('NOT_A_CHAT')) throw new Error('Not a chat screenshot');
if(text.trim().length<10) throw new Error('Empty extraction');

console.log(`[SS] ✓ ${p.name} extracted conversation`);
return text.trim();
}catch(err){
console.warn(`[SS] ✗ ${p.name}:`,err.message);
lastErr=err;
}
}
throw lastErr||new Error('Could not read screenshot');
}

async function getSSCoach(conversation){
const sysPrompt=`You're a dating/texting coach. Analyze this chat conversation extracted from a screenshot and give brief advice.

Reply in this format:
VIBE: [1-2 sentence overall vibe of the conversation]
SCORE: [number 1-10 for how well the conversation is going]
TIPS:
- [tip 1]
- [tip 2]
- [tip 3]
FLAGS:
- [green/red flag 1]
- [green/red flag 2]
NEXT: [what they should say/do next — one sentence]`;

const msgs=[
{role:'system',content:sysPrompt},
{role:'user',content:`Analyze this conversation and coach me:\n\n${conversation}`}
];

try{
return await callProviders(msgs);
}catch(err){
return 'Could not generate coaching advice.';
}
}

async function getSSReplies(conversation){
const intensityDesc={mild:'mild and subtle',bold:'confident and charming',extreme:'extremely bold and intense'};
const intDesc=intensityDesc[intensity]||'bold';
const langNote=lang==='hinglish'?'\nIMPORTANT: Write ALL replies in Hinglish — casual mix of Hindi and English in Roman script.':lang&&lang!=='english'?`\nWrite in ${lang}.`:'';

const sysPrompt=`You generate text message replies that read EXACTLY like a real human typed them.

RULES:
1. Mostly lowercase, casual, with natural fillers like "haha" "ngl" "tbh" "lol" "fr"
2. Keep it SHORT: 5-20 words per reply
3. Sound human, NOT AI
4. Reply in context to the last message in the conversation
5. Output ONLY numbered lines (1. 2. 3. etc)`;

const msgs=[
{role:'system',content:sysPrompt},
{role:'user',content:`Here's a conversation from a chat screenshot:\n\n${conversation}\n\nGenerate 5 replies I can send next. Vibe: ${style} (${intDesc}).${langNote}\n\nONLY numbered replies (1. 2. 3. 4. 5.)`}
];

try{
const text=await callProviders(msgs);
return parseReplies(text);
}catch(err){
return [];
}
}

function renderSSResult(imgUrl,conversation,coachText,replies){
const body=document.getElementById('ssPanelBody');

let coachHtml='';
const vibeMatch=coachText.match(/VIBE:\s*(.+?)(?=\n|$)/s);
const scoreMatch=coachText.match(/SCORE:\s*(\d+)/);
const tipsMatch=coachText.match(/TIPS:\s*([\s\S]+?)(?=\nFLAGS|\n\n[A-Z]|$)/);
const flagsMatch=coachText.match(/FLAGS:\s*([\s\S]+?)(?=\nNEXT|\n\n[A-Z]|$)/);
const nextMatch=coachText.match(/NEXT:\s*(.+?)(?=\n|$)/s);

const score=scoreMatch?parseInt(scoreMatch[1]):0;
const scoreColor=score>=7?'var(--green)':score>=4?'var(--yellow)':'var(--red)';

if(vibeMatch){
coachHtml+=`<div class="ss-coach-card"><p><strong style="color:${scoreColor}">Vibe: ${score}/10</strong></p><p>${vibeMatch[1].trim().replace(/</g,'&lt;')}</p></div>`;
}

if(flagsMatch){
const flags=flagsMatch[1].trim().split('\n').map(l=>l.replace(/^-\s*/,'').trim()).filter(l=>l.length>3);
if(flags.length>0){
coachHtml+=`<div style="margin-bottom:10px">${flags.map(f=>{
const isGreen=f.toLowerCase().includes('green')||f.includes('✓')||f.includes('+')||f.toLowerCase().includes('good')||f.toLowerCase().includes('positive');
const isRed=f.toLowerCase().includes('red')||f.includes('✗')||f.includes('-')||f.toLowerCase().includes('bad')||f.toLowerCase().includes('concern')||f.toLowerCase().includes('warning');
const cls=isGreen?'green':isRed?'red':'yellow';
return `<span class="coach-flag ${cls}">${f.replace(/</g,'&lt;')}</span>`;
}).join('')}</div>`;
}
}

if(tipsMatch){
const tips=tipsMatch[1].trim().split('\n').map(l=>l.replace(/^-\s*/,'').trim()).filter(l=>l.length>3);
if(tips.length>0){
coachHtml+=tips.map(t=>`<div class="coach-tip"><span class="coach-tip-icon">💡</span><span>${t.replace(/</g,'&lt;')}</span></div>`).join('');
}
}

if(nextMatch){
coachHtml+=`<div class="ss-coach-card" style="border-color:rgba(34,197,94,.2);background:rgba(34,197,94,.05);margin-top:8px"><p><strong style="color:var(--green)">🎯 Next Move:</strong> ${nextMatch[1].trim().replace(/</g,'&lt;')}</p></div>`;
}

if(!coachHtml) coachHtml=`<div class="ss-coach-card">${coachText.replace(/</g,'&lt;').replace(/\n/g,'<br>')}</div>`;

let repliesHtml='';
if(replies&&replies.length>0){
repliesHtml=replies.map((r,i)=>`<div class="ss-reply-option"><span class="ss-reply-num">${i+1}</span><span class="ss-reply-text">${r.replace(/</g,'&lt;')}</span><div class="ss-reply-actions"><button class="ss-action-btn" onclick="navigator.clipboard.writeText('${r.replace(/'/g,"\\'")}').then(()=>showToast('Copied!'))" title="Copy"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg></button></div></div>`).join('');
}else{
repliesHtml='<p style="color:var(--text3);font-size:.85rem;text-align:center;padding:10px">Could not generate replies</p>';
}

const convFormatted=conversation.replace(/</g,'&lt;').split('\n').map(line=>{
if(line.match(/^(me|Me|ME|I):/i)) return `<span class="ss-sender-me">${line}</span>`;
return `<span class="ss-sender-them">${line}</span>`;
}).join('\n');

body.innerHTML=`
<img class="ss-preview" src="${imgUrl}" alt="Chat screenshot">

<div class="ss-section">
<div class="ss-section-title">💬 Extracted Conversation</div>
<div class="ss-conversation">${convFormatted}</div>
</div>

<div class="ss-section">
<div class="ss-section-title">🧠 Coach Analysis</div>
${coachHtml}
</div>

<div class="ss-section">
<div class="ss-section-title">⚡ Suggested Replies</div>
<div class="ss-replies-section">${repliesHtml}</div>
</div>

<div class="ss-btn-row">
<button class="ss-btn-action" onclick="reanalyzeScreenshot()">
<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M23 4v6h-6"/><path d="M1 20v-6h6"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
Re-analyze
</button>
<button class="ss-btn-action" onclick="document.getElementById('ssUploadInput').click()">
<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
New Screenshot
</button>
</div>
`;
}

function closeSSPanel(){
document.getElementById('ssPanel').style.display='none';
}

let lastSSBase64=null;
let lastSSMime=null;

async function reanalyzeScreenshot(){
if(!lastSSBase64){showToast('No screenshot to re-analyze');return;}
if(ssAnalyzing){showToast('Already analyzing...');return;}
ssAnalyzing=true;

const body=document.getElementById('ssPanelBody');
const imgEl=body.querySelector('.ss-preview');
const imgSrc=imgEl?imgEl.src:'';

body.innerHTML=`${imgSrc?`<img class="ss-preview" src="${imgSrc}" alt="Chat screenshot">`:''}<div class="ss-loading"><div class="spinner"></div>Re-analyzing screenshot...</div>`;

try{
const extracted=await extractConversation(lastSSBase64,lastSSMime);
if(!extracted||extracted.trim().length<10) throw new Error('Could not read');
const [coachResult,replyResult]=await Promise.all([
getSSCoach(extracted),
getSSReplies(extracted)
]);
renderSSResult(imgSrc,extracted,coachResult,replyResult);
}catch(err){
body.innerHTML=`<div class="ss-loading" style="color:var(--red)">Failed to re-analyze. Try uploading again.</div>`;
}
ssAnalyzing=false;
}
