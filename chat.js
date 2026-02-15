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
textarea.style.height=Math.min(textarea.scrollHeight,120)+'px';
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
<div class="msg-time">${timeStr}</div>`;

row.addEventListener('click',(e)=>{
if(e.target.closest('.msg-edit-bar'))return;
if(row.classList.contains('editing'))return;
showMsgContextMenu(msg.id,msg.text,msg.sender,e);
});

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

// ===== WHATSAPP-STYLE CONTEXT MENU =====
function showMsgContextMenu(id,text,sender,e){
closeMsgContextMenu();
const overlay=document.createElement('div');
overlay.className='msg-ctx-overlay';
overlay.id='msgCtxOverlay';
overlay.addEventListener('click',closeMsgContextMenu);

const menu=document.createElement('div');
menu.className='msg-ctx-menu';

const bubbleRect=e.currentTarget.querySelector('.msg-bubble').getBoundingClientRect();
let top=bubbleRect.top-10;
let left=sender==='me'?bubbleRect.right-170:bubbleRect.left;
if(top+200>window.innerHeight)top=bubbleRect.top-180;
if(top<10)top=10;
if(left<10)left=10;
if(left+170>window.innerWidth)left=window.innerWidth-180;

menu.style.top=top+'px';
menu.style.left=left+'px';

menu.innerHTML=`
<button class="msg-ctx-item" data-action="copy">
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
Copy
</button>
<button class="msg-ctx-item" data-action="edit">
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
Edit
</button>
<div class="msg-ctx-sep"></div>
<button class="msg-ctx-item delete-item" data-action="delete">
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
Delete
</button>`;

menu.querySelectorAll('.msg-ctx-item').forEach(btn=>{
btn.addEventListener('click',(ev)=>{
ev.stopPropagation();
const action=btn.dataset.action;
closeMsgContextMenu();
if(action==='copy'){
navigator.clipboard.writeText(text).then(()=>showToast('Copied!'));
}else if(action==='edit'){
startEditMessage(id,text);
}else if(action==='delete'){
deleteMessage(id);
showToast('Message deleted');
}
});
});

document.body.appendChild(overlay);
document.body.appendChild(menu);
}

function closeMsgContextMenu(){
const overlay=document.getElementById('msgCtxOverlay');
if(overlay)overlay.remove();
document.querySelectorAll('.msg-ctx-menu').forEach(m=>m.remove());
}

// ===== EDIT MESSAGE =====
function startEditMessage(id,text){
const row=document.querySelector(`.msg-row[data-id="${id}"]`);
if(!row)return;
row.classList.add('editing');
const bubble=row.querySelector('.msg-bubble');
const originalHtml=bubble.innerHTML;
const timeEl=row.querySelector('.msg-time');
if(timeEl)timeEl.style.display='none';

bubble.innerHTML=`
<textarea class="msg-edit-input" id="editInput_${id}">${text}</textarea>
<div class="msg-edit-bar">
<button class="msg-edit-cancel" onclick="cancelEdit(${id})">Cancel</button>
<button class="msg-edit-save" onclick="saveEdit(${id})">Save</button>
</div>`;

const input=document.getElementById('editInput_'+id);
if(input){
input.focus();
input.style.height='auto';
input.style.height=Math.min(input.scrollHeight,80)+'px';
input.addEventListener('input',()=>{
input.style.height='auto';
input.style.height=Math.min(input.scrollHeight,80)+'px';
});
}
}

function saveEdit(id){
const input=document.getElementById('editInput_'+id);
if(!input)return;
const newText=input.value.trim();
if(!newText){showToast('Message cannot be empty');return;}
const idx=messages.findIndex(m=>m.id===id);
if(idx!==-1){
messages[idx].text=newText;
renderMessages();
saveChat();
showToast('Message edited');
}
}

function cancelEdit(id){
renderMessages();
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

const convo=buildConversation(insertAt);

try{
const text=await callProviders(convo.messages);
const replies=parseReplies(text);
if(replies.length===0) throw new Error('Empty');
hideTyping();
showReplies(replies,insertAt,convo.nextSender);
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
const isHinglishLang=lang==='hinglish';
const isHindiLang=lang==='hindi';
const langNote=isHinglishLang?`
IMPORTANT — WRITE IN HINGLISH (Roman script Hindi-English mix). This is how REAL young Indians text on WhatsApp/Instagram:

HINGLISH RULES:
- Mix Hindi + English naturally: "arre yaar sun na, tu toh full cute hai ngl"
- Use SHORT Hindi fillers: yaar, arre, accha, haan, na, re, oye, chal, sun, bol, matlab, bas, bhai
- Use Hindi texting shortcuts: nhi (nahi), kya, kyu, kab, thk h, pta nhi, koi ni, sahi hai, chl, btao, krna, ruk, haan bolo
- Drop vowels like real texts: "achha" = "acha", "theek" = "thk", "nahi" = "nhi"
- Add Hindi reactions: "kya baat hai!", "mast", "zabardast", "bakwas", "pagal hai kya", "ekdum", "kamaal"
- Use Bollywood casually: "sharma ji ka beta vibes", "picture abhi baaki hai", "full filmy"
- Mix English slang + Hindi: "bro sun", "yaar ngl", "lowkey accha tha", "fr yaar", "literally pagal ho gaye"
- Regional flavor OK: "oye hoye" (punjabi), "bindaas" (mumbai), "bawaal" (delhi)
- Emojis Indians use: 😂 🤣 🔥 💀 👀 😏 🙈 ❤️ 😭

BAD (AI/formal): "Main aapko bahut pasand karta hoon, aap bahut sundar hain."
GOOD (Real text): "arre yaar tu toh ekdum mast hai 🔥 kya karti hai itna cute hokar"
BAD: "Mujhe aapke saath samay bitana bahut achha lagta hai."
GOOD: "tere saath time spend krna >>> baaki sab bakwas"
BAD: "Kya aap mere saath dinner par chalenge?"
GOOD: "chal na dinner pe chalte hain, bohot bore ho rha hun akele"
BAD: "Aapki smile bahut sundar hai."
GOOD: "teri smile dekh ke dimag kharab ho jata hai yaar 😭"

EVERY reply MUST be in Hinglish. NO pure English. NO Devanagari. NO formal Hindi.
`:isHindiLang?`
IMPORTANT — WRITE IN HINDI (Roman script / Romanized Hindi). Like real Indians text:
- Use Roman script (English letters), NOT Devanagari
- Keep it casual: "sun na, kya kar rahi hai?"
- Use shortcuts: nhi, thk h, pta nhi, koi ni, acha, chl
- Sound like real WhatsApp chat, not textbook Hindi
- Drop formality: NO aap/aapko, use tu/tujhe/tere
- Add fillers: yaar, arre, accha, na, re, haan
BAD: "Kya aap mujhe bata sakti hain?"
GOOD: "arre bata na yaar"
EVERY reply MUST be in Roman Hindi. NO Devanagari. NO pure English.
`:lang&&lang!=='english'?`\nIMPORTANT: Write ALL replies in ${lang}.`:'';

let chatLog=slice.map(m=>`${m.sender==='me'?'Me':theirName}: ${m.text}`).join('\n');

const lastSender=slice.length>0?slice[slice.length-1].sender:'them';

const nextSender=lastSender==='me'?'them':'me';

const styleDesc={
smooth:'smooth and charming',funny:'funny and witty',romantic:'romantic and heartfelt',bold:'bold and confident',
savage:'savage and brutally honest',sweet:'sweet and wholesome',spicy:'flirty and suggestive with sexual tension',
toxic:'toxic and possessive but attractive',drunk:'like drunk texting — messy, unhinged, chaotic',
villain:'dark, mysterious villain energy',desi:'desi/Bollywood style with Hindi-English mix',
flirtyaf:'extremely flirty and aggressive',nerdy:'nerdy with clever references',poetic:'poetic and lyrical'
};
const styleNote=styleDesc[style]||style;

const sysPrompt=`You generate text message replies that read EXACTLY like a real human typed them in a chat app.${isHinglishLang?' You are a YOUNG INDIAN person texting on WhatsApp. You naturally mix Hindi and English.':isHindiLang?' You are a YOUNG INDIAN person texting in casual Roman Hindi.':''}

RULES — non-negotiable:
1. TYPE LIKE A REAL PERSON: mostly lowercase, skip periods sometimes, use "..." for pauses.${isHinglishLang?' Mix Hindi+English naturally. Use: yaar, arre, accha, chal, sun, bhai, ngl, fr, lowkey. Drop vowels (nhi, thk, acha, pta nhi). Write like WhatsApp not textbook.':isHindiLang?' Write in Roman Hindi like real Indian texting. Use shortcuts: nhi, kya, kyu, thk h, acha, chl.':' Use "haha" "lol" "ngl" "tbh" "fr" "lowkey" "yk" "bruh" naturally.'} imperfect grammar OK.
2. KEEP IT SHORT: 5-20 words per reply. real texts are short, not essays.
3. SOUND HUMAN, NOT AI:${isHinglishLang?`
   BAD: "Mujhe aapke saath waqt bitana accha lagta hai."
   GOOD: "tere saath time spend krna >>> baaki sab 🔥"
   BAD: "Aapki smile bahut sundar hai."
   GOOD: "teri smile dekh ke dimag kharab ho jata h yaar 😭"
   BAD: "Main aapko bahut miss karta hoon."
   GOOD: "yaar bohot miss kr rha hun tujhe ngl"
   BAD: "Kya aap mere saath coffee peene chalenge?"
   GOOD: "chal na coffee pe chalte h, bore ho rha hun"
   BAD: "Bahut achha laga aapko jaankar."
   GOOD: "yaar tujhse baat krke maza aa gya fr fr"`:isHindiLang?`
   BAD: "Kya aap mujhe bata sakti hain?"
   GOOD: "arre bata na"
   BAD: "Mujhe bahut khushi hogi agar aap aayen."
   GOOD: "aa na yaar, maza aayega"`:'\n   BAD: "I must say, your smile is truly captivating."\n   GOOD: "ok but your smile is actually so cute wtf"\n   BAD: "Thank you for the compliment! You\'re quite attractive yourself."\n   GOOD: "haha stoppp you\'re making me blush"\n   BAD: "I would love to spend time with you!"\n   GOOD: "wait fr?? im down lol when"'}
4. READ THE FULL CONVERSATION and reply in context. Reference what was said. Don't be random.
5. Output ONLY numbered lines (1. 2. 3. etc). Nothing else.${isHinglishLang?'\n6. EVERY reply MUST be in Hinglish (Hindi+English mix in Roman script). NO pure English. NO Devanagari. If you write pure English you have FAILED.':isHindiLang?'\n6. EVERY reply MUST be in Roman Hindi. NO Devanagari. NO pure English.':''}`;

const whoReplies=nextSender==='me'?'I ("Me")':theirName+' ("them")';
const whoSpokeLast=lastSender==='me'?'I':'they';

const userPrompt=`Here's the chat conversation so far:
---
${chatLog}
---

Generate 5 replies that ${whoReplies} would send next. Read the FULL conversation and reply naturally to what ${whoSpokeLast} just said.

Vibe: ${style} — ${styleNote} (${intDesc}).${window._scenarioContext?'\nScenario context: '+window._scenarioContext:''}
${langNote}

These must sound like real texts from a real person chatting. Short, casual, natural. React to what was actually said — use their words, tease, be specific to THIS conversation. ONLY numbered replies (1. 2. 3. 4. 5.)`;

return {
messages:[{role:'system',content:sysPrompt},{role:'user',content:userPrompt}],
nextSender
};
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

function showReplies(replies,insertAt,sender){
const replySender=sender||'me';
const panel=document.getElementById('aiReplies');
const list=document.getElementById('aiRepliesList');
list.innerHTML='';

const headerTitle=panel.querySelector('.ai-replies-title');
if(headerTitle) headerTitle.innerHTML=`<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg> Pick a reply ${replySender==='them'?'(as them)':'(as you)'}`;

replies.forEach((r,i)=>{
const btn=document.createElement('button');
btn.className='ai-reply-option';
btn.innerHTML=`<span class="reply-num">${i+1}</span><span class="reply-text">${escapeHtml(r)}</span>`;
btn.addEventListener('click',()=>{
if(insertAt!=null){
const msg={id:Date.now(),sender:replySender,text:r,time:new Date()};
messages.splice(insertAt,0,msg);
}else{
messages.push({id:Date.now(),sender:replySender,text:r,time:new Date()});
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

NEXT MOVE: [one sentence — what should they do/say next]

SUGGESTED REPLIES:
1. [a smooth, confident reply they could send right now]
2. [a funny/playful reply option]
3. [a bold/flirty reply option]
4. [a sweet/caring reply option]
5. [a witty/clever reply option]

The 5 suggested replies must be actual messages "Me" can send next. Keep them short (1-2 sentences max), natural, and match the conversation's tone and context. Make each one feel distinct in style.${lang==='hinglish'?' Write ALL suggested replies in Hinglish (Hindi+English mix, Roman script) like how real young Indians text on WhatsApp. Use yaar/arre/accha/chal naturally. NO pure English. NO formal Hindi. NO Devanagari.':lang==='hindi'?' Write ALL suggested replies in casual Roman Hindi.':''}`;

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

const suggestedMatch=text.match(/SUGGESTED REPLIES:\s*([\s\S]+?)$/i);
if(suggestedMatch){
const replies=suggestedMatch[1].trim().split('\n').map(l=>l.replace(/^\d+[.)\-:\s]+/,'').replace(/^"/,'').replace(/"$/,'').trim()).filter(l=>l.length>3);
if(replies.length>0){
html+=`<div class="coach-section"><div class="coach-section-title">💬 Suggested Replies</div><div class="coach-replies">`;
replies.forEach((r,i)=>{
const vibes=['smooth','playful','bold','sweet','witty'];
const emojis=['😎','😄','🔥','💕','🧠'];
html+=`<button class="coach-reply-btn" data-reply="${r.replace(/"/g,'&quot;')}" onclick="useCoachReply(this)"><span class="coach-reply-vibe">${emojis[i]||'💬'} ${vibes[i]||'reply'}</span><span class="coach-reply-text">${r.replace(/</g,'&lt;')}</span></button>`;
});
html+=`</div></div>`;
}
}

if(!html) html=`<div class="coach-vibe">${text.replace(/</g,'&lt;').replace(/\n/g,'<br>')}</div>`;

body.innerHTML=html;
}

function closeCoach(){
document.getElementById('coachPanel').style.display='none';
}

function useCoachReply(btn){
const reply=btn.dataset.reply;
if(!reply) return;
messages.push({id:Date.now(),sender:'me',text:reply,time:new Date()});
renderMessages();
saveChat();
scrollToBottom();
closeCoach();
showToast('Reply added!');
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
// SCREENSHOT UPLOAD → OCR → AI PARSE → AUTO IMPORT TO CHAT
// ============================================================

let lastSSImgUrl=null;

async function handleSSUpload(e){
const file=e.target.files?.[0];
if(!file) return;
e.target.value='';

if(!file.type.startsWith('image/')){showToast('Please upload an image');return;}
if(file.size>10*1024*1024){showToast('Image too large (max 10MB)');return;}
if(ssAnalyzing){showToast('Already processing...');return;}

ssAnalyzing=true;
const ssBtn=document.getElementById('ssBtn');
ssBtn.classList.add('loading');

const panel=document.getElementById('ssPanel');
const body=document.getElementById('ssPanelBody');
panel.style.display='flex';

const imgUrl=URL.createObjectURL(file);
lastSSImgUrl=imgUrl;

body.innerHTML=`
<img class="ss-preview" src="${imgUrl}" alt="Screenshot">
<div class="ss-step active"><div class="ss-step-icon"><div class="spinner"></div></div><div class="ss-step-text">Reading text from screenshot...</div></div>
<div class="ss-step"><div class="ss-step-icon">2</div><div class="ss-step-text">AI parsing conversation...</div></div>
<div class="ss-step"><div class="ss-step-icon">3</div><div class="ss-step-text">Import to chat</div></div>`;

try{
const ocrText=await runOCR(file);
if(!ocrText||ocrText.trim().length<5) throw new Error('Could not read any text from screenshot');

const steps=body.querySelectorAll('.ss-step');
steps[0].classList.remove('active');steps[0].classList.add('done');
steps[0].querySelector('.ss-step-icon').innerHTML='✓';
steps[1].classList.add('active');
steps[1].querySelector('.ss-step-icon').innerHTML='<div class="spinner"></div>';

const parsed=await aiParseConversation(ocrText);
if(!parsed||parsed.length===0) throw new Error('Could not identify messages');

steps[1].classList.remove('active');steps[1].classList.add('done');
steps[1].querySelector('.ss-step-icon').innerHTML='✓';
steps[2].classList.add('active');
steps[2].classList.remove('active');steps[2].classList.add('done');
steps[2].querySelector('.ss-step-icon').innerHTML='✓';

renderSSPreview(imgUrl,ocrText,parsed);

}catch(err){
console.error('Screenshot error:',err);
body.innerHTML=`
<img class="ss-preview" src="${imgUrl}" alt="Screenshot">
<div class="ss-error">
<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--red)" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
<p>${err.message||'Failed to read screenshot'}</p>
<span>Try a clearer screenshot with visible text</span>
</div>
<div class="ss-btn-row">
<button class="ss-btn-action" onclick="document.getElementById('ssUploadInput').click()">Try Another Screenshot</button>
</div>`;
}

ssAnalyzing=false;
ssBtn.classList.remove('loading');
}

async function runOCR(file){
showToast('Reading screenshot...');
if(typeof Tesseract==='undefined'){
throw new Error('OCR library still loading — wait a moment and try again');
}
try{
const worker=await Tesseract.createWorker('eng');
const {data:{text}}=await worker.recognize(file);
await worker.terminate();
console.log('[OCR] Raw text:',text);
if(!text||text.trim().length<3) throw new Error('No text found');
return text.trim();
}catch(err){
console.error('[OCR] Failed:',err);
throw new Error('Could not read text from image — try a clearer screenshot');
}
}

async function aiParseConversation(rawText){
const sysPrompt=`You parse raw OCR text extracted from a chat screenshot into a structured conversation.

Given messy OCR text, figure out which lines are messages and who sent them.
- "them" = the other person (usually left-side messages in screenshots)
- "me" = the user / phone owner (usually right-side / colored messages)

Rules:
- Use context clues: timestamps, names, message groupings
- Skip UI elements like "delivered", "seen", timestamps, dates, app headers
- Each message should be short (as it would appear in a chat bubble)
- If you can't determine sender, alternate between them/me
- Output ONLY valid JSON array, nothing else

Output format (raw JSON, no markdown):
[{"sender":"them","text":"hey whats up"},{"sender":"me","text":"not much hbu"},{"sender":"them","text":"wanna hang out"}]`;

const msgs=[
{role:'system',content:sysPrompt},
{role:'user',content:`Parse this OCR text from a chat screenshot into a conversation:\n\n${rawText}\n\nOutput ONLY the JSON array.`}
];

const text=await callProviders(msgs);
const clean=text.replace(/```json\n?/g,'').replace(/```\n?/g,'').trim();

const startIdx=clean.indexOf('[');
const endIdx=clean.lastIndexOf(']');
if(startIdx===-1||endIdx===-1) throw new Error('AI could not parse messages');

const json=JSON.parse(clean.substring(startIdx,endIdx+1));
if(!Array.isArray(json)||json.length===0) throw new Error('No messages found');

return json.filter(m=>m.text&&m.text.trim().length>0).map(m=>({
sender:m.sender==='me'?'me':'them',
text:m.text.trim()
}));
}

function renderSSPreview(imgUrl,rawText,parsed){
const body=document.getElementById('ssPanelBody');

const previewMsgs=parsed.map((m,i)=>`
<div class="ss-msg-preview ${m.sender}">
<div class="ss-msg-sender-toggle">
<button class="ss-toggle-btn ${m.sender==='them'?'active':''}" onclick="toggleSSMsgSender(${i},'them',this)">Them</button>
<button class="ss-toggle-btn ${m.sender==='me'?'active':''}" onclick="toggleSSMsgSender(${i},'me',this)">Me</button>
</div>
<div class="ss-msg-text">${m.text.replace(/</g,'&lt;')}</div>
<button class="ss-msg-delete" onclick="deleteSSMsg(${i},this)" title="Remove">×</button>
</div>`).join('');

body.innerHTML=`
<img class="ss-preview" src="${imgUrl}" alt="Screenshot">

<div class="ss-section">
<div class="ss-section-title">💬 ${parsed.length} Messages Found</div>
<p class="ss-section-sub">Tap Them/Me to fix who sent each message</p>
<div class="ss-messages-preview" id="ssMsgList">${previewMsgs}</div>
</div>

<div class="ss-btn-row">
<button class="ss-btn-action" onclick="document.getElementById('ssUploadInput').click()">
<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
Re-upload
</button>
<button class="ss-btn-action primary" onclick="importSSToChat()">
<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M19 12l-7 7-7-7"/></svg>
Import ${parsed.length} Messages
</button>
</div>`;

window._ssParsed=parsed;
}

function toggleSSMsgSender(idx,sender,btn){
if(!window._ssParsed) return;
window._ssParsed[idx].sender=sender;
const row=btn.closest('.ss-msg-preview');
row.className=`ss-msg-preview ${sender}`;
row.querySelectorAll('.ss-toggle-btn').forEach(b=>b.classList.remove('active'));
btn.classList.add('active');
}

function deleteSSMsg(idx,btn){
if(!window._ssParsed) return;
window._ssParsed.splice(idx,1);
const row=btn.closest('.ss-msg-preview');
row.style.height=row.offsetHeight+'px';
requestAnimationFrame(()=>{
row.style.height='0px';row.style.opacity='0';row.style.margin='0';row.style.padding='0';row.style.overflow='hidden';
});
setTimeout(()=>row.remove(),200);
const importBtn=document.querySelector('.ss-btn-action.primary');
if(importBtn) importBtn.textContent=`Import ${window._ssParsed.length} Messages`;
}

function importSSToChat(){
const parsed=window._ssParsed;
if(!parsed||parsed.length===0){showToast('No messages to import');return;}

parsed.forEach(m=>{
const msg={id:Date.now()+Math.random()*1000,sender:m.sender,text:m.text,time:new Date()};
messages.push(msg);
});

renderMessages();
saveChat();
scrollToBottom();
hideHint();
closeSSPanel();

showToast(`${parsed.length} messages imported! 🎉`);
window._ssParsed=null;
}

function closeSSPanel(){
document.getElementById('ssPanel').style.display='none';
}

// ===== FEATURE: VIBE METER =====
let vibeValue=20;
function updateVibeMeter(){
if(messages.length<2){vibeValue=20;renderVibe();return;}
const last5=messages.slice(-6);
let score=40;
last5.forEach(m=>{
const t=m.text.toLowerCase();
if(/haha|lol|😂|🤣|😄|❤|🥰|😍|😘|💕|🔥|cute|love|miss|beautiful|amazing|❤️/.test(t))score+=10;
if(/lmao|omg|stoppp|you're|ur so|i like|wanna|date|hang out|come over/.test(t))score+=8;
if(/ok|k|sure|idk|whatever|fine|cool|bye|nah|no|busy|maybe/.test(t)&&t.length<10)score-=8;
if(/😏|😈|💀|👀|😜|🤭/.test(t))score+=5;
if(m.sender==='them'&&t.includes('?'))score+=4;
if(t.length>50)score+=3;
if(t.length<5)score-=3;
});
const theirMsgs=last5.filter(m=>m.sender==='them').length;
const myMsgs=last5.filter(m=>m.sender==='me').length;
if(theirMsgs>myMsgs)score+=5;
if(myMsgs>theirMsgs+2)score-=5;
vibeValue=Math.max(5,Math.min(100,score));
renderVibe();
}
function renderVibe(){
const fill=document.getElementById('vibeFill');
const label=document.getElementById('vibeLabel');
const scoreEl=document.getElementById('vibeScore');
if(!fill)return;
fill.style.width=vibeValue+'%';
scoreEl.textContent=vibeValue;
let txt,color;
if(vibeValue>=80){txt='🔥 On Fire';color='#ef4444';}
else if(vibeValue>=60){txt='😏 Flirty';color='#ec4899';}
else if(vibeValue>=40){txt='😊 Warm';color='#f59e0b';}
else if(vibeValue>=25){txt='😐 Neutral';color='#9898b0';}
else{txt='🥶 Cold';color='#3b82f6';}
label.textContent=txt;label.style.color=color;scoreEl.style.color=color;
}

// ===== FEATURE: PERSONA SIMULATOR =====
let persona='normal';
function initPersona(){
document.querySelectorAll('.persona-chip').forEach(c=>{
c.addEventListener('click',()=>{
document.querySelectorAll('.persona-chip').forEach(x=>x.classList.remove('active'));
c.classList.add('active');
persona=c.dataset.persona;
showToast(`Persona: ${c.textContent.trim()}`);
});
});
}

function getPersonaPrompt(){
const personas={
normal:'',
shy:'They are shy and introverted — short replies, lots of "haha", nervous energy, takes time to open up, uses "..." often.',
confident:'They are super confident and direct — knows what they want, assertive, smooth talker, never seems nervous.',
sarcastic:'They are very sarcastic — dry humor, witty comebacks, never gives a straight answer, always teasing.',
dry:'They are a dry texter — minimal effort replies, "k", "lol", "nice", rarely asks questions. Hard to read.',
flirty:'They are extremely flirty — drops hints constantly, playful, uses lots of emojis, always escalating.',
hardtoget:'They play hard to get — interested but won\'t show it easily, makes you work for it, hot and cold.',
clingy:'They are clingy — double texts, gets worried if you don\'t reply fast, very affectionate, needs reassurance.',
intellectual:'They are intellectual — uses proper grammar, references books/philosophy, deeper conversations, thoughtful responses.'
};
return personas[persona]||'';
}

// ===== FEATURE: AI AUTOPILOT =====
let autopilotOn=false;
function initAutopilot(){
const btn=document.getElementById('autopilotBtn');
if(!btn)return;
btn.addEventListener('click',()=>{
autopilotOn=!autopilotOn;
btn.textContent=autopilotOn?'ON':'OFF';
btn.classList.toggle('on',autopilotOn);
showToast(autopilotOn?'🤖 Autopilot ON — AI plays them':'Autopilot OFF');
if(autopilotOn){
document.querySelectorAll('.sender-btn').forEach(x=>x.classList.remove('active'));
document.getElementById('senderMe').classList.add('active');
currentSender='me';
document.getElementById('msgInput').placeholder='Type your message (AI will reply as them)...';
}
});
}

async function autopilotReply(){
if(!autopilotOn||generating)return;
generating=true;
showTyping();
const theirName=document.getElementById('theirName').value.trim()||'them';
const chatLog=messages.map(m=>`${m.sender==='me'?'Me':theirName}: ${m.text}`).join('\n');
const personaNote=getPersonaPrompt();
const hinglishAutoNote=lang==='hinglish'?`\nLANGUAGE: Reply in HINGLISH — casual Hindi+English mix in Roman script like a real young Indian on WhatsApp.\nExamples of how to text: "haan bolo", "acha thik hai", "kya kar rha hai", "pagal hai kya 😂", "chal theek h", "arre sun na", "bohot bore ho rha hun"\nNO formal Hindi. NO pure English. NO Devanagari. Mix naturally like: "arre yaar kya hua", "haha pagal", "accha sun", "tu toh 🔥 h"`:lang==='hindi'?'\nLANGUAGE: Reply in casual Roman Hindi. Use shortcuts: nhi, acha, thk h, kya, chl. NO Devanagari. NO pure English.':'';
const sysPrompt=`You are simulating "${theirName}" in a text conversation. You ARE them, not an AI. Reply as a REAL person would text.
${personaNote?'PERSONALITY: '+personaNote:''}${hinglishAutoNote}
RULES:
1. TYPE LIKE A REAL HUMAN: lowercase, casual.${lang==='hinglish'?' Use Hindi fillers (yaar, arre, accha, na, haan) + English naturally mixed.':' Use "lol" "haha" "ngl" "tbh" naturally.'}
2. KEEP IT SHORT: 3-15 words. Real texts are brief.
3. STAY IN CHARACTER based on the personality described.
4. React naturally to what "Me" just said. Be specific to the conversation.
5. Output ONLY the reply message. Nothing else. No quotes, no prefix.`;
try{
const text=await callProviders([
{role:'system',content:sysPrompt},
{role:'user',content:`Conversation:\n---\n${chatLog}\n---\nReply as ${theirName}:`}
]);
hideTyping();
const reply=text.replace(/^["']|["']$/g,'').replace(/^(them|they|her|him|sarah|[a-z]+):\s*/i,'').trim();
if(reply.length>2){
messages.push({id:Date.now(),sender:'them',text:reply,time:new Date()});
renderMessages();saveChat();scrollToBottom();
updateVibeMeter();
updateGoalProgress();
generateQuickReplies();
}
}catch(e){hideTyping();showToast('AI reply failed');}
generating=false;
}

// ===== FEATURE: MESSAGE REWRITE =====
async function openRewrite(msgId){
const msg=messages.find(m=>m.id===msgId);
if(!msg)return;
const popup=document.getElementById('rewritePopup');
const orig=document.getElementById('rewriteOriginal');
const opts=document.getElementById('rewriteOptions');
orig.textContent=msg.text;
opts.innerHTML='<div style="text-align:center;padding:16px;color:var(--text3);font-size:.85rem">Generating rewrites...</div>';
popup.style.display='flex';
try{
const theirName=document.getElementById('theirName').value.trim()||'them';
const chatLog=messages.map(m=>`${m.sender==='me'?'Me':theirName}: ${m.text}`).join('\n');
const msgs=[{role:'system',content:`Rewrite the user's message in 5 different styles. Keep the same intent but make each version better. Format as JSON array: [{"style":"Smoother","text":"rewritten msg"},{"style":"Funnier","text":"msg"},{"style":"Bolder","text":"msg"},{"style":"Sweeter","text":"msg"},{"style":"Wittier","text":"msg"}]. Keep each rewrite SHORT (under 20 words), natural, and texting-style.`},{role:'user',content:`Conversation context:\n${chatLog}\n\nRewrite this message:\n"${msg.text}"`}];
const res=await callProviders(msgs);
const arr=JSON.parse(res.match(/\[[\s\S]*\]/)?.[0]||'[]');
if(arr.length){
opts.innerHTML=arr.map(r=>`<button class="rewrite-option" data-text="${(r.text||'').replace(/"/g,'&quot;')}"><span class="rewrite-option-label">${r.style||'Option'}</span><span class="rewrite-option-text">${r.text||''}</span></button>`).join('');
opts.querySelectorAll('.rewrite-option').forEach(btn=>{
btn.addEventListener('click',()=>{
const newText=btn.dataset.text;
const idx=messages.findIndex(m=>m.id===msgId);
if(idx!==-1){messages[idx].text=newText;renderMessages();saveChat();scrollToBottom();}
popup.style.display='none';
showToast('Message rewritten!');
});
});
}else{opts.innerHTML='<div style="text-align:center;padding:16px;color:var(--red)">Failed — try again</div>';}
}catch(e){opts.innerHTML='<div style="text-align:center;padding:16px;color:var(--red)">Rewrite failed</div>';}
}

// ===== FEATURE: REPLY TIMING ADVISOR =====
let timingPanel=null;
async function showTimingAdvice(){
if(messages.length<2){showToast('Need messages first');return;}
const existing=document.querySelector('.timing-panel');
if(existing){existing.remove();return;}
const last=messages[messages.length-1];
if(last.sender==='me'){showToast('Wait for their message first');return;}
const panel=document.createElement('div');
panel.className='timing-panel';
panel.innerHTML=`<div class="timing-panel-inner"><span class="timing-icon">⏱️</span><div class="timing-info"><div class="timing-advice">Analyzing...</div><div class="timing-reason">Reading conversation patterns</div></div><button class="timing-close" onclick="this.closest('.timing-panel').remove()"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg></button></div>`;
document.body.appendChild(panel);
try{
const theirName=document.getElementById('theirName').value.trim()||'them';
const chatLog=messages.slice(-6).map(m=>`${m.sender==='me'?'Me':theirName}: ${m.text}`).join('\n');
const msgs=[{role:'system',content:'You are a texting timing expert. Analyze the conversation and advise when to reply. Return ONLY JSON: {"timing":"Reply in 5-10 minutes","reason":"Short explanation why","emoji":"⏰"}'},{role:'user',content:`When should I reply?\n${chatLog}`}];
const res=await callProviders(msgs);
const json=JSON.parse(res.match(/\{[\s\S]*?\}/)?.[0]||'{}');
const advice=panel.querySelector('.timing-advice');
const reason=panel.querySelector('.timing-reason');
const icon=panel.querySelector('.timing-icon');
if(advice){advice.textContent=json.timing||'Reply when ready';}
if(reason){reason.textContent=json.reason||'';}
if(icon){icon.textContent=json.emoji||'⏱️';}
}catch(e){
const advice=panel.querySelector('.timing-advice');
if(advice)advice.textContent='Could not analyze timing';
}
setTimeout(()=>{if(panel.parentNode)panel.remove();},15000);
}

// ===== FEATURE: CONVERSATION GOALS =====
let currentGoal='none';
let goalProgress=0;
function initGoals(){
document.querySelectorAll('.goal-chip').forEach(c=>{
c.addEventListener('click',()=>{
document.querySelectorAll('.goal-chip').forEach(x=>x.classList.remove('active'));
c.classList.add('active');
currentGoal=c.dataset.goal;
goalProgress=0;
const tracker=document.getElementById('goalTracker');
if(currentGoal==='none'){
tracker.style.display='none';
}else{
tracker.style.display='block';
const goalNames={getnumber:'📱 Get Their Number',askout:'💕 Ask Them Out',keepflirty:'🔥 Keep It Flirty',deepen:'💎 Deepen Connection',friendzone:'🏃 Escape Friend Zone'};
document.getElementById('goalText').textContent=goalNames[currentGoal]||'Goal';
updateGoalUI();
document.querySelector('.chat-container').style.top='118px';
}
showToast(`Goal: ${c.textContent.trim()}`);
});
});
document.getElementById('closeGoal')?.addEventListener('click',()=>{
currentGoal='none';goalProgress=0;
document.getElementById('goalTracker').style.display='none';
document.querySelector('.chat-container').style.top='90px';
document.querySelectorAll('.goal-chip').forEach(x=>x.classList.remove('active'));
});
}
function updateGoalUI(){
document.getElementById('goalFill').style.width=goalProgress+'%';
document.getElementById('goalPct').textContent=goalProgress+'%';
}
function updateGoalProgress(){
if(currentGoal==='none'||messages.length<2)return;
const last3=messages.slice(-3).map(m=>m.text.toLowerCase()).join(' ');
let bump=0;
switch(currentGoal){
case 'getnumber':
if(/number|digits|phone|text me|call me|whatsapp|snap/.test(last3))bump=30;
else if(/\d{7,}/.test(last3))bump=50;
else bump=3;break;
case 'askout':
if(/date|meet|hang out|grab|coffee|dinner|drinks|this weekend|tomorrow|friday|saturday/.test(last3))bump=25;
else if(/yes|i'd love|i'm down|sounds good|when|where/.test(last3))bump=35;
else bump=3;break;
case 'keepflirty':
if(/😏|😘|🔥|😈|💕|cute|hot|sexy|flirt|tease|blush/.test(last3))bump=8;
else bump=2;break;
case 'deepen':
if(/feel|think|believe|dream|fear|hope|life|family|passion|vulnerable|real|honest|deep/.test(last3))bump=10;
else bump=2;break;
case 'friendzone':
if(/friend|bro|buddy|pal|like a brother|like a sister/.test(last3))bump=-5;
else if(/date|more than|feelings|attracted|special|different/.test(last3))bump=15;
else bump=2;break;
}
goalProgress=Math.max(0,Math.min(100,goalProgress+bump));
updateGoalUI();
if(goalProgress>=100)showToast('🎉 Goal achieved!');
}

// ===== FEATURE: QUICK REPLIES =====
let quickRepliesBar=null;
async function generateQuickReplies(){
if(messages.length===0)return;
const last=messages[messages.length-1];
if(last.sender==='me')return;
removeQuickReplies();
const bar=document.createElement('div');
bar.className='quick-replies-bar';
bar.id='quickRepliesBar';
bar.innerHTML='<span class="quick-reply-loading">⚡ Loading quick replies...</span>';
document.body.appendChild(bar);
try{
const theirName=document.getElementById('theirName').value.trim()||'them';
const chatLog=messages.slice(-4).map(m=>`${m.sender==='me'?'Me':theirName}: ${m.text}`).join('\n');
const qrLangNote=lang==='hinglish'?' Write in Hinglish (Hindi+English mix, Roman script) like real Indian WhatsApp texts. Example: ["arre sach mein? 😂","chal na yaar","tu toh full mast hai"]. Use yaar/arre/accha/bhai naturally. NO pure English. NO formal Hindi.':lang==='hindi'?' Write in casual Roman Hindi like real texts. Use shortcuts.':'';
const msgs=[{role:'system',content:`Generate 3 ultra-short quick reply options (3-8 words each) for this chat. These should be casual, natural texts.${qrLangNote} Return ONLY a JSON array: ["reply1","reply2","reply3"]. Make each distinct in tone: 1 smooth, 1 funny, 1 bold.`},{role:'user',content:chatLog}];
const res=await callProviders(msgs);
const arr=JSON.parse(res.match(/\[[\s\S]*?\]/)?.[0]||'[]');
if(arr.length&&document.getElementById('quickRepliesBar')){
bar.innerHTML=arr.map(r=>`<button class="quick-reply-chip">${r.replace(/</g,'&lt;')}</button>`).join('');
bar.querySelectorAll('.quick-reply-chip').forEach(btn=>{
btn.addEventListener('click',()=>{
messages.push({id:Date.now(),sender:'me',text:btn.textContent,time:new Date()});
renderMessages();saveChat();scrollToBottom();
removeQuickReplies();updateVibeMeter();updateGoalProgress();
if(autopilotOn)setTimeout(autopilotReply,800);
});
});
}
}catch(e){removeQuickReplies();}
}
function removeQuickReplies(){
const el=document.getElementById('quickRepliesBar');
if(el)el.remove();
}

// ===== FEATURE: MULTI-CHAT MANAGER =====
let currentChatId='default';
let allChats={};
function initMultiChat(){
loadAllChats();
document.getElementById('chatSwitcherBtn')?.addEventListener('click',openChatSwitcher);
document.getElementById('closeChatSwitcher')?.addEventListener('click',closeChatSwitcher);
document.getElementById('newChatBtn')?.addEventListener('click',createNewChat);
}
function loadAllChats(){
const stored=localStorage.getItem('rizzMultiChats');
if(stored){try{allChats=JSON.parse(stored);}catch(e){allChats={};}}
if(!allChats.default){allChats.default={name:'Chat Simulator',messages:[],avatar:'S'};}
const savedId=localStorage.getItem('rizzCurrentChatId')||'default';
currentChatId=allChats[savedId]?savedId:'default';
messages=allChats[currentChatId].messages||[];
const n=allChats[currentChatId].name||'Chat Simulator';
document.getElementById('navName').textContent=n;
document.getElementById('navAvatar').textContent=n.charAt(0).toUpperCase();
}
function saveAllChats(){
allChats[currentChatId].messages=messages;
allChats[currentChatId].name=document.getElementById('navName').textContent||'Chat';
localStorage.setItem('rizzMultiChats',JSON.stringify(allChats));
localStorage.setItem('rizzCurrentChatId',currentChatId);
}
function openChatSwitcher(){
const list=document.getElementById('chatSwitcherList');
list.innerHTML='';
Object.keys(allChats).forEach(id=>{
const c=allChats[id];
const lastMsg=c.messages?.length?c.messages[c.messages.length-1].text:'No messages yet';
const isActive=id===currentChatId;
const div=document.createElement('div');
div.className=`chat-item${isActive?' active':''}`;
div.innerHTML=`<div class="chat-item-avatar">${(c.name||'C').charAt(0).toUpperCase()}</div><div class="chat-item-info"><div class="chat-item-name">${c.name||'Chat'}</div><div class="chat-item-preview">${lastMsg.length>40?lastMsg.slice(0,40)+'...':lastMsg}</div></div><div class="chat-item-meta"><span class="chat-item-count">${c.messages?.length||0} msgs</span>${id!=='default'?`<button class="chat-item-del" data-id="${id}">🗑️</button>`:''}</div>`;
div.addEventListener('click',(e)=>{
if(e.target.closest('.chat-item-del'))return;
switchToChat(id);
closeChatSwitcher();
});
list.appendChild(div);
});
list.querySelectorAll('.chat-item-del').forEach(btn=>{
btn.addEventListener('click',(e)=>{
e.stopPropagation();
const id=btn.dataset.id;
delete allChats[id];
if(currentChatId===id){switchToChat('default');}
saveAllChats();
openChatSwitcher();
showToast('Chat deleted');
});
});
document.getElementById('chatSwitcherOverlay').style.display='flex';
}
function closeChatSwitcher(){
document.getElementById('chatSwitcherOverlay').style.display='none';
}
function switchToChat(id){
saveAllChats();
currentChatId=id;
const c=allChats[id];
messages=c.messages||[];
const n=c.name||'Chat Simulator';
document.getElementById('navName').textContent=n;
document.getElementById('navAvatar').textContent=n.charAt(0).toUpperCase();
document.getElementById('theirName').value=n==='Chat Simulator'?'':n;
renderMessages();
if(messages.length===0){const hint=document.querySelector('.chat-hint');if(hint)hint.style.display='';}
scrollToBottom();
localStorage.setItem('rizzCurrentChatId',id);
updateVibeMeter();
showToast(`Switched to ${n}`);
}
function createNewChat(){
const id='chat_'+Date.now();
const name=prompt('Chat name (e.g., Sarah - Tinder):')||'New Chat';
allChats[id]={name,messages:[],avatar:name.charAt(0)};
saveAllChats();
switchToChat(id);
closeChatSwitcher();
}

// ===== FEATURE: SENTIMENT TIMELINE =====
function drawSentimentTimeline(){
const panel=document.getElementById('sentimentPanel');
const canvas=document.getElementById('sentimentCanvas');
if(!canvas||messages.length<2){
if(panel.style.display!=='none')showToast('Need more messages for timeline');
return;
}
panel.style.display='block';
const ctx=canvas.getContext('2d');
const W=canvas.width=canvas.offsetWidth*2;
const H=canvas.height=240;
ctx.clearRect(0,0,W,H);
const points=[];
messages.forEach((m,i)=>{
let score=50;
const t=m.text.toLowerCase();
if(/❤|🥰|😍|😘|💕|love|miss|beautiful|amazing|cute|sweet/.test(t))score=85;
else if(/haha|lol|😂|🤣|funny|😄|lmao/.test(t))score=75;
else if(/😏|🔥|😈|flirt|hot|damn|wow/.test(t))score=80;
else if(/sure|ok|k|fine|whatever|idk|busy|bye|nah/.test(t)&&t.length<12)score=25;
else if(/\?/.test(t)&&t.length>10)score=60;
else if(t.length>30)score=55;
else if(t.length<5)score=30;
else score=50;
points.push({x:(i/(messages.length-1))*W,y:H-((score/100)*H*0.8)-H*0.1,score,sender:m.sender});
});
const grad=ctx.createLinearGradient(0,0,0,H);
grad.addColorStop(0,'rgba(34,197,94,0.15)');
grad.addColorStop(0.5,'rgba(168,85,247,0.05)');
grad.addColorStop(1,'rgba(239,68,68,0.15)');
ctx.fillStyle=grad;ctx.fillRect(0,0,W,H);
[0.25,0.5,0.75].forEach(p=>{
ctx.beginPath();ctx.strokeStyle='rgba(255,255,255,0.05)';ctx.lineWidth=1;
ctx.moveTo(0,H*p);ctx.lineTo(W,H*p);ctx.stroke();
});
if(points.length>1){
ctx.beginPath();ctx.strokeStyle='rgba(168,85,247,0.6)';ctx.lineWidth=3;ctx.lineJoin='round';ctx.lineCap='round';
ctx.moveTo(points[0].x,points[0].y);
for(let i=1;i<points.length;i++){
const xc=(points[i-1].x+points[i].x)/2;
const yc=(points[i-1].y+points[i].y)/2;
ctx.quadraticCurveTo(points[i-1].x,points[i-1].y,xc,yc);
}
ctx.lineTo(points[points.length-1].x,points[points.length-1].y);
ctx.stroke();
const fillGrad=ctx.createLinearGradient(0,0,0,H);
fillGrad.addColorStop(0,'rgba(168,85,247,0.2)');
fillGrad.addColorStop(1,'rgba(168,85,247,0)');
ctx.lineTo(points[points.length-1].x,H);ctx.lineTo(points[0].x,H);ctx.closePath();
ctx.fillStyle=fillGrad;ctx.fill();
}
points.forEach(p=>{
const c=p.score>=70?'#22c55e':p.score>=45?'#f59e0b':'#ef4444';
ctx.beginPath();ctx.arc(p.x,p.y,5,0,Math.PI*2);ctx.fillStyle=c;ctx.fill();
ctx.beginPath();ctx.arc(p.x,p.y,8,0,Math.PI*2);ctx.fillStyle=c.replace(')',',0.2)').replace('rgb','rgba');ctx.fill();
});
ctx.font='bold 18px Inter,sans-serif';ctx.textAlign='left';
ctx.fillStyle='rgba(34,197,94,0.5)';ctx.fillText('🔥 Hot',8,24);
ctx.fillStyle='rgba(239,68,68,0.5)';ctx.fillText('🥶 Cold',8,H-8);
}

// ===== HOOK INTO EXISTING FUNCTIONS =====
const _origAddMessageDirect=addMessageDirect;
addMessageDirect=function(sender,text){
_origAddMessageDirect(sender,text);
updateVibeMeter();
updateGoalProgress();
if(sender==='me'&&autopilotOn){setTimeout(autopilotReply,800);}
if(sender==='them'){generateQuickReplies();}
else{removeQuickReplies();}
saveAllChats();
};

const _origRenderMessages=renderMessages;
renderMessages=function(){
_origRenderMessages();
const container=document.getElementById('chatMessages');
container.querySelectorAll('.msg-row.me').forEach(row=>{
const id=parseInt(row.dataset.id);
const actions=row.querySelector('.msg-actions');
if(actions&&!actions.querySelector('.rewrite')){
const btn=document.createElement('button');
btn.className='msg-action-btn rewrite';
btn.textContent='Rewrite';
btn.onclick=()=>openRewrite(id);
actions.insertBefore(btn,actions.querySelector('.del'));
}
});
};

const _origSaveChat=saveChat;
saveChat=function(){
_origSaveChat();
saveAllChats();
};

// ===== INIT ALL NEW FEATURES =====
function initNewFeatures(){
initPersona();
initAutopilot();
initGoals();
initMultiChat();
updateVibeMeter();
document.getElementById('closeSentiment')?.addEventListener('click',()=>{
document.getElementById('sentimentPanel').style.display='none';
});
document.getElementById('sentimentBtn')?.addEventListener('click',()=>{
drawSentimentTimeline();
const settingsPanel=document.getElementById('settingsPanel');
if(settingsPanel)settingsPanel.classList.remove('open');
});
document.getElementById('closeRewrite')?.addEventListener('click',()=>{
document.getElementById('rewritePopup').style.display='none';
});
document.getElementById('quickReplyBtn')?.addEventListener('click',generateQuickReplies);
document.getElementById('timingBtn')?.addEventListener('click',showTimingAdvice);
renderMessages();
}

if(document.readyState==='loading'){
document.addEventListener('DOMContentLoaded',()=>setTimeout(initNewFeatures,100));
}else{setTimeout(initNewFeatures,100);}

// ===== CONVERSATION SCENARIOS =====
const SCENARIOS=[
{emoji:'💬',title:'First DM to Crush',desc:'They posted a story and you want to slide in smooth.',messages:[{sender:'them',text:'*posted a sunset photo on their story*'}],context:'You\'re DMing your crush for the first time after they posted a story.'},
{emoji:'🔥',title:'Bumble/Tinder Match',desc:'You just matched! Open with something memorable.',messages:[{sender:'them',text:'We matched! 👋'}],context:'You just matched on a dating app and need the perfect opener.'},
{emoji:'📱',title:'Getting Their Number',desc:'You\'ve been chatting and want to take it offline.',messages:[{sender:'them',text:'haha you\'re actually funny'},{sender:'me',text:'I try my best 😎'},{sender:'them',text:'so what do you do for fun?'}],context:'You\'ve been chatting for a bit and want to smoothly ask for their number.'},
{emoji:'🌹',title:'After First Date',desc:'The date went well — follow up perfectly.',messages:[{sender:'them',text:'I had a really good time tonight 😊'},{sender:'me',text:'Me too! The food was amazing'},{sender:'them',text:'we should definitely do this again'}],context:'First date went great, you\'re texting after getting home.'},
{emoji:'😴',title:'Late Night Texts',desc:'It\'s 11 PM and you want to keep them up talking.',messages:[{sender:'them',text:'can\'t sleep 😩'},{sender:'me',text:'same here, what are you up to?'},{sender:'them',text:'just scrolling through my phone thinking'}],context:'Late night conversation, intimate and flirty vibes.'},
{emoji:'❄️',title:'Reviving a Dead Chat',desc:'They stopped replying — win them back.',messages:[{sender:'me',text:'hey how are you?'},{sender:'them',text:'good hbu'},{sender:'me',text:'doing well! seen any good movies lately?'},{sender:'them',text:'not really'}],context:'The conversation is dying with dry replies. Need to revive it.'},
{emoji:'😘',title:'Flirty Good Morning',desc:'Start their day with butterflies.',messages:[],context:'You want to send the perfect good morning text to someone you\'re interested in.'},
{emoji:'🎭',title:'Playing Hard to Get',desc:'They\'re being mysterious — match their energy.',messages:[{sender:'them',text:'maybe I\'ll tell you... maybe I won\'t 😏'},{sender:'me',text:'oh so we\'re playing games now?'},{sender:'them',text:'who said anything about games? 😇'}],context:'They\'re being playfully mysterious and hard to get.'},
{emoji:'💔',title:'Ex Texted Back',desc:'Your ex reached out — handle it with class.',messages:[{sender:'them',text:'hey... can we talk?'}],context:'Your ex texted you out of nowhere. Respond with confidence.'},
{emoji:'🤝',title:'Friend to More',desc:'You want to shift from friendship to something more.',messages:[{sender:'them',text:'you\'re literally my best friend'},{sender:'me',text:'haha yeah you\'re pretty cool too'},{sender:'them',text:'what would I do without you'}],context:'You\'re in the friend zone and want to hint at deeper feelings.'},
{emoji:'🎉',title:'Party Invite',desc:'Invite them to hang out without being too forward.',messages:[{sender:'them',text:'this weekend is gonna be so boring'},{sender:'me',text:'why what\'s up?'},{sender:'them',text:'literally nothing to do'}],context:'They\'re bored this weekend and you want to invite them somewhere.'},
{emoji:'📸',title:'Replied to Your Story',desc:'They reacted to your photo/story — capitalize on it.',messages:[{sender:'them',text:'🔥🔥🔥'},{sender:'them',text:'where is this??'}],context:'They replied to your Instagram/Snapchat story with fire emojis.'}
];

function initScenarios(){
const grid=document.getElementById('scenarioGrid');
if(!grid)return;
grid.innerHTML=SCENARIOS.map((s,i)=>`
<div class="scenario-card" data-idx="${i}">
<div class="scenario-card-emoji">${s.emoji}</div>
<div class="scenario-card-title">${s.title}</div>
<div class="scenario-card-desc">${s.desc}</div>
</div>`).join('');
grid.querySelectorAll('.scenario-card').forEach(card=>{
card.addEventListener('click',()=>{
const idx=parseInt(card.dataset.idx);
loadScenario(SCENARIOS[idx]);
closeScenarioOverlay();
});
});
document.getElementById('scenarioBtn')?.addEventListener('click',openScenarioOverlay);
document.getElementById('closeScenario')?.addEventListener('click',closeScenarioOverlay);
}

function openScenarioOverlay(){
document.getElementById('scenarioOverlay').style.display='flex';
}
function closeScenarioOverlay(){
document.getElementById('scenarioOverlay').style.display='none';
}
function loadScenario(scenario){
messages=[];
scenario.messages.forEach(m=>{
messages.push({id:Date.now()+Math.random()*1000,sender:m.sender,text:m.text,time:new Date()});
});
if(scenario.title){
const name=scenario.title;
document.getElementById('navName').textContent=name;
document.getElementById('navAvatar').textContent=scenario.emoji;
}
renderMessages();
saveChat();
scrollToBottom();
hideHint();
showToast(`Scenario loaded: ${scenario.title}`);
window._scenarioContext=scenario.context;
}

// patch generateReply to use scenario context
const _origBuildSysPrompt=typeof buildSystemPrompt==='function'?buildSystemPrompt:null;

// add scenario init
const _origDOMLoaded=document.readyState;
if(document.readyState==='loading'){
document.addEventListener('DOMContentLoaded',initScenarios);
}else{
initScenarios();
}
