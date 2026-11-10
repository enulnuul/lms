'use strict';
// Eternity Library Management System — frontend only.
// Authentication and authorization must be enforced by the backend.
const $ = id => document.getElementById(id);
const pages = ['home','search','validate','borrow','return','receipt','login','signup','otp','account'];
const AUTH = { register:'/api/auth/register', login:'/api/auth/login', verify:'/api/auth/verify-otp', resend:'/api/auth/resend-otp', me:'/api/auth/me', logout:'/api/auth/logout', csrf:'/api/auth/csrf' };
let user = null, pendingOtp = false, otpIssuedAt = 0, latestReceipt = null;
let booksLoaded = false, searchRequestId = 0, csrfToken = null, csrfHeader = 'X-CSRF-TOKEN';

function node(tag, cls, value) {
  const el = document.createElement(tag);
  if (cls) el.className = cls;
  if (value !== undefined && value !== null) el.textContent = String(value);
  return el;
}
function showError(target, message) { target.replaceChildren(node('div','error-box',message)); }
function showMessage(target, message) {
  target.replaceChildren(node('div','notice',message));
}
function empty(target, message) {
  const e = node('div','empty-state'); e.append(node('span','empty-icon','⌕'),node('p','',message)); target.replaceChildren(e);
}
function details(target, title, pairs) {
  const e = node('div','result-card'); e.append(node('h2','',title));
  for(const [key,val] of pairs) { const r=node('div','info-row'); r.append(node('span','',key),node('span','',val ?? '—')); e.append(r); }
  target.replaceChildren(e);
}
function notify(message) {
  const t=$('toast'); t.textContent=message; t.hidden=false;
  clearTimeout(notify.timer); notify.timer=setTimeout(()=>{t.hidden=true;},3800);
}
function showPage(name) {
  const page=pages.includes(name)?name:'home';
  document.querySelectorAll('.page').forEach(e=>e.classList.toggle('active',e.id==='page-'+page));
  document.querySelectorAll('.nav-link').forEach(e=>e.classList.toggle('active',e.dataset.page===page));
  $('navigation').classList.remove('open'); $('menuBtn').setAttribute('aria-expanded','false');
  if(location.hash!=='#'+page) location.hash=page;
  window.scrollTo(0,0);
  if(page==='search' && !booksLoaded) loadBooks();
  if(page==='account' && !user) showPage('login');
}
document.querySelectorAll('[data-page]').forEach(btn=>btn.addEventListener('click',()=>showPage(btn.dataset.page)));
$('menuBtn').addEventListener('click',()=>{
  const open=$('navigation').classList.toggle('open'); $('menuBtn').setAttribute('aria-expanded',String(open));
});
window.addEventListener('hashchange',()=>showPage(location.hash.slice(1)));

async function api(path, opts={}) {
  const headers={Accept:'application/json',...(opts.body?{'Content-Type':'application/json'}:{}),...(opts.headers||{})};
  if(opts.method && !['GET','HEAD'].includes(opts.method.toUpperCase()) && csrfToken) headers[csrfHeader]=csrfToken;
  let res;
  try { res=await fetch(path,{credentials:'same-origin',...opts,headers}); }
  catch { throw new Error('Cannot connect to the server. Open this website via http://localhost:8080/ and start Spring Boot.'); }
  const type=res.headers.get('content-type')||'';
  let data=null;
  if(type.includes('application/json')) { try { data=await res.json(); } catch { /* ignore */ } }
  if(res.status===401 && user) {
    user=null; updateAuthUI(); showPage('login'); notify('Your session has expired. Please log in again.');
  }
  if(!res.ok) {
    if([404,405].includes(res.status) && path.startsWith('/api/auth/')) throw new Error('Authentication endpoint not available yet. The security backend must be integrated.');
    const msg=data?.message || data?.error || (res.status===403?'Access denied or CSRF token missing.':'Request failed (HTTP '+res.status+').');
    throw new Error(typeof msg==='string'?msg:'Request failed.');
  }
  if(data===null && res.status!==204) throw new Error('Expected JSON from backend; check the API endpoint.');
  return data;
}
async function getCsrf() {
  try {
    const c=await api(AUTH.csrf);
    csrfToken=c?.token || c?.csrfToken || null;
    csrfHeader=c?.headerName || 'X-CSRF-TOKEN';
  } catch { csrfToken=null; }
}
const validBookId = v => /^[A-Za-z0-9][A-Za-z0-9-]{0,19}$/.test(v);
function renderBook(book) {
  const card=node('article','book-card');
  card.append(node('h3','',book.title??'Untitled'),node('p','','Author: '+(book.author??'—')),
    node('p','','ISBN: '+(book.isbn??'—')),node('p','','Book ID: '+(book.bookId??'—')));
  const available=Number(book.availableCopies)>0;
  card.append(node('span','availability'+(available?'':' unavailable'),available?book.availableCopies+' available':'Unavailable'));
  const actions=node('div','book-actions');
  const check=node('button','btn btn-outline','Check ID'); check.type='button';
  check.addEventListener('click',()=>{$('validateId').value=book.bookId||'';showPage('validate');$('validateForm').requestSubmit();});
  const borrow=node('button','btn btn-green','Borrow'); borrow.type='button'; borrow.disabled=!available;
  borrow.addEventListener('click',()=>{$('borrowId').value=book.bookId||'';showPage('borrow');});
  actions.append(check,borrow); card.append(actions); return card;
}
async function loadBooks() {
  const requestId=++searchRequestId;
  const term=$('searchTerm').value.trim(), type=$('searchType').value;
  const result=$('searchResults'), counter=$('resultCount');
  if(term.length>100) {showError(result,'Search term must not exceed 100 characters.');return;}
  const browse=!term;
  $('resultsTitle').textContent=browse?'Available Books':'Search Results'; counter.textContent='Loading...';empty(result,'Loading books...');
  try {
    const path=browse?'/api/books/search':'/api/books/search?'+new URLSearchParams({[type]:term});
    const books=await api(path);
    if(requestId!==searchRequestId)return;
    if(!Array.isArray(books))throw new Error('Unexpected search response.');
    const items=browse?books.filter(b=>Number(b.availableCopies)>0):books;
    counter.textContent=items.length+' book'+(items.length===1?'':'s'); result.replaceChildren();
    if(items.length) items.forEach(b=>result.append(renderBook(b)));
    else empty(result,browse?'No books are currently available.':'No books match your search.');
    booksLoaded=true;
  } catch(e) {
    if(requestId!==searchRequestId)return;
    booksLoaded=false;counter.textContent='Unable to load books';showError(result,e.message);
  }
}
$('searchForm').addEventListener('submit',e=>{e.preventDefault();loadBooks();});
$('validateForm').addEventListener('submit',async e=>{
  e.preventDefault();const id=$('validateId').value.trim(),result=$('validateResult');
  if(!validBookId(id))return showError(result,'Book ID must be 1–20 characters containing letters, numbers or hyphens.');
  try {
    const b=await api('/api/books/'+encodeURIComponent(id)+'/validate');
    details(result,'Validation Result',[['Book ID',b.bookId],['Book Title',b.title],['Valid',b.valid?'Yes':'No'],['Available Copies',b.availableCopies],['Status',b.message]]);
  } catch(err){showError(result,err.message);}
});

function updateAuthUI() {
  const b=document.querySelector('.login-nav');
  b.textContent=user?'My Account':'Login / Sign Up';
  b.dataset.page=user?'account':'login';
  document.querySelectorAll('.transaction-button').forEach(btn=>{btn.disabled=!user;});
  if(user) {
    const name=user.username||user.email||'Library Member';
    $('accountGreeting').textContent='Signed in as '+name;
    const role=user.role||'Member';
    details($('accountDetails'),'Account Details',[['Username',name],['Email',user.email],['Role',role]]);
    $('roleNotice').textContent=['Admin','Librarian'].includes(role)
      ? 'Staff role confirmed by the server. Additional administrative functions require backend support.'
      : 'Member account';
  }
}
async function checkSession() {
  try {
    const response=await api(AUTH.me);
    // Only the backend can establish whether MFA is complete.
    if(response && (response.authenticated===true || (response.userId && response.mfaVerified===true))) {
      user=response.user||response; updateAuthUI();
    } else {user=null;updateAuthUI();}
  } catch {user=null;updateAuthUI();}
}
$('showLoginPassword').addEventListener('change',e=>{$('loginPassword').type=e.target.checked?'text':'password';});
$('signupForm').addEventListener('submit',async e=>{
  e.preventDefault();const target=$('signupMessage');
  const name=$('signupName').value.trim(),email=$('signupEmail').value.trim();
  const password=$('signupPassword').value,confirm=$('signupConfirm').value;
  if(name.length<2 || name.length>100)return showError(target,'Name must contain 2–100 characters.');
  if(!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))return showError(target,'Enter a valid email address.');
  if(password.length<12 || password.length>128)return showError(target,'Password must contain 12–128 characters.');
  if(password!==confirm)return showError(target,'Passwords do not match.');
  try {
    await getCsrf();
    await api(AUTH.register,{method:'POST',body:JSON.stringify({fullName:name,email,password})});
    $('signupPassword').value='';$('signupConfirm').value='';
    showMessage(target,'Account created. Please log in using your email and password.');
    notify('Registration successful.');
  } catch(err){showError(target,err.message);} 
});
$('loginForm').addEventListener('submit',async e=>{
  e.preventDefault();const target=$('loginMessage'),email=$('loginEmail').value.trim(),password=$('loginPassword').value;
  if(!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))return showError(target,'Enter a valid email address.');
  if(!password)return showError(target,'Enter your password.');
  try {
    await getCsrf();
    const r=await api(AUTH.login,{method:'POST',body:JSON.stringify({email,password})});
    $('loginPassword').value='';
    // No session is granted by the frontend after first-factor login.
    if(r?.status==='OTP_REQUIRED' || r?.otpRequired===true) {
      pendingOtp=true;otpIssuedAt=Date.now();$('otpCode').value='';showPage('otp');
      showMessage($('otpMessage'),'A verification code has been requested. The code expires after 5 minutes.');
    } else showError(target,'The server did not confirm that OTP verification is required. Check the authentication API response.');
  } catch(err){showError(target,err.message);}
});
$('otpForm').addEventListener('submit',async e=>{
  e.preventDefault();const target=$('otpMessage'),code=$('otpCode').value.trim();
  if(!/^\d{6}$/.test(code))return showError(target,'Enter a valid six-digit verification code.');
  if(!pendingOtp)return showError(target,'Start by entering your email and password on the Login page.');
  if(Date.now()-otpIssuedAt>5*60*1000)return showError(target,'The OTP may have expired. Please request a new code.');
  try {
    const result=await api(AUTH.verify,{method:'POST',body:JSON.stringify({otp:code})});
    $('otpCode').value='';
    if(result?.authenticated===true || result?.status==='AUTHENTICATED') {
      pendingOtp=false;
      await checkSession();
      if(user){notify('Login successful.');showPage('account');}
      else showError(target,'OTP was accepted, but the server has not confirmed an authenticated session.');
    } else showError(target,'OTP verification was not confirmed by the server.');
  } catch(err){showError(target,err.message);}
});
$('resendOtp').addEventListener('click',async()=>{
  const target=$('otpMessage');if(!pendingOtp)return showError(target,'Please log in first.');
  const btn=$('resendOtp'); btn.disabled=true;
  try {await api(AUTH.resend,{method:'POST',body:JSON.stringify({})});otpIssuedAt=Date.now();showMessage(target,'New code requested. Check your email.');}
  catch(err){showError(target,err.message);}finally{btn.disabled=false;}
});
$('logoutBtn').addEventListener('click',async()=>{
  try {await api(AUTH.logout,{method:'POST',body:JSON.stringify({})});user=null;pendingOtp=false;updateAuthUI();notify('Logged out.');showPage('login');}
  catch(err){showError($('accountDetails'),err.message);}
});
function receiptRows(d){return [['Loan ID',d.loanId],['Book ID',d.bookId],['Borrow Date',d.borrowDate],['Due Date',d.dueDate],['Return Date',d.returnDate||'Not returned'],['Status',d.status],['Days Overdue',d.daysOverdue],['Late Fee','RM '+Number(d.lateFee||0).toFixed(2)]];}
function saveReceipt(title,data){latestReceipt={title,data};details($('receiptResult'),title+' Receipt',receiptRows(data));}
$('borrowForm').addEventListener('submit',async e=>{
  e.preventDefault();const target=$('borrowResult'),bookId=$('borrowId').value.trim();
  if(!user)return showError(target,'Please log in and complete OTP verification.');
  if(!validBookId(bookId))return showError(target,'Enter a valid Book ID.');
  try {
    // No userId is sent. Backend must derive identity from the session.
    const d=await api('/api/loans/borrow',{method:'POST',body:JSON.stringify({bookId})});
    details(target,'Borrowing Confirmed',receiptRows(d));saveReceipt('Borrow',d);booksLoaded=false;notify('Book borrowed.');
  } catch(err){showError(target,err.message);}
});
$('returnForm').addEventListener('submit',async e=>{
  e.preventDefault();const target=$('returnResult'),loanId=Number($('returnLoanId').value);
  if(!user)return showError(target,'Please log in and complete OTP verification.');
  if(!Number.isSafeInteger(loanId)||loanId<=0)return showError(target,'Enter a valid Loan ID.');
  try {
    // Backend must verify ownership and role, never trust the UI.
    const d=await api('/api/loans/return',{method:'POST',body:JSON.stringify({loanId})});
    details(target,'Return Confirmed',receiptRows(d));saveReceipt('Return',d);booksLoaded=false;
    notify(Number(d.lateFee)>0?'Book returned. Late fee: RM '+Number(d.lateFee).toFixed(2):'Book returned successfully.');
  } catch(err){showError(target,err.message);}
});
async function checkBackend(){try{await api('/api/books/search?title=Clean');$('apiStatus').textContent='Backend connected';}catch{$('apiStatus').textContent='Backend unavailable';}}
showPage(location.hash.slice(1)||'home');
updateAuthUI();
checkBackend();
getCsrf().then(checkSession);
