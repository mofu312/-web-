const API = '';

const { createApp, ref, reactive, computed, onMounted } = Vue;

const app = createApp({
    setup() {
        const loggedIn = ref(false);
        const username = ref('');
        const userRole = ref('');
        const isLoginMode = ref(true);
        const msg = ref('');
        const msgType = ref('');

        const loginForm = reactive({ username: '', password: '', role: 'STUDENT' });

        async function doLogin() {
            if (!loginForm.username.trim() || !loginForm.password) {
                msg.value = '用户名和密码不能为空'; msgType.value = 'error'; return;
            }
            const res = await fetch(API + '/api/auth/login', {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: loginForm.username, password: loginForm.password })
            });
            const data = await res.json();
            if (data.success) {
                loggedIn.value = true;
                username.value = data.username;
                userRole.value = data.role;
                msg.value = ''; msgType.value = '';
                if (userRole.value === 'ADMIN') { loadBooks(); loadReaders(); }
                else { loadBooks(); }
            } else {
                msg.value = data.message; msgType.value = 'error';
            }
        }

        async function doRegister() {
            if (!loginForm.username.trim() || !loginForm.password) {
                msg.value = '用户名和密码不能为空'; msgType.value = 'error'; return;
            }
            const res = await fetch(API + '/api/auth/register', {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: loginForm.username, password: loginForm.password, role: loginForm.role })
            });
            const data = await res.json();
            if (data.success) {
                msg.value = '注册成功！请登录'; msgType.value = 'success';
                isLoginMode.value = true;
            } else {
                msg.value = data.message; msgType.value = 'error';
            }
        }

        function logout() { loggedIn.value = false; username.value = ''; userRole.value = ''; }

        // ===== 图书 =====
        const bookList = ref([]);
        const searchKeyword = ref('');
        const editBook = reactive({ isbn: '', title: '', author: '', totalCopies: 1 });

        async function loadBooks() {
            const res = await fetch(API + '/api/books');
            bookList.value = await res.json();
        }
        async function searchBooks() {
            if (!searchKeyword.value.trim()) { loadBooks(); return; }
            const res = await fetch(API + '/api/books/search?keyword=' + encodeURIComponent(searchKeyword.value));
            bookList.value = await res.json();
        }
        function selectBook(b) { Object.assign(editBook, b); }
        async function addBook() {
            if (!editBook.isbn || !editBook.title) { opMsg('请填写完整', true); return; }
            const res = await fetch(API + '/api/books', {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(editBook)
            });
            const d = await res.json(); opMsg(d.message, !d.success);
            loadBooks(); clearEditBook();
        }
        async function updateBook() {
            if (!editBook.isbn) return;
            const res = await fetch(API + '/api/books/' + editBook.isbn, {
                method: 'PUT', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(editBook)
            });
            const d = await res.json(); opMsg(d.message, !d.success);
            loadBooks(); clearEditBook();
        }
        async function deleteBook() {
            if (!editBook.isbn || !confirm('确定删除《' + editBook.title + '》？')) return;
            const res = await fetch(API + '/api/books/' + editBook.isbn, { method: 'DELETE' });
            const d = await res.json(); opMsg(d.message, !d.success);
            loadBooks(); clearEditBook();
        }
        function clearEditBook() { editBook.isbn = ''; editBook.title = ''; editBook.author = ''; editBook.totalCopies = 1; }

        // ===== 读者 =====
        const readerList = ref([]);
        const selReader = ref(null);
        const newReader = reactive({ name: '', id: '', maxBorrow: 5 });

        async function loadReaders() {
            const res = await fetch(API + '/api/readers');
            readerList.value = await res.json();
        }
        async function addReader() {
            if (!newReader.name || !newReader.id) { opMsg('请填写完整', true); return; }
            const res = await fetch(API + '/api/readers', {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(newReader)
            });
            const d = await res.json(); opMsg(d.message, !d.success);
            loadReaders(); newReader.name = ''; newReader.id = ''; newReader.maxBorrow = 5;
        }
        async function deleteReader() {
            if (!selReader.value || !confirm('确定删除读者 ' + selReader.value.name + '？')) return;
            const res = await fetch(API + '/api/readers/' + selReader.value.id, { method: 'DELETE' });
            const d = await res.json(); opMsg(d.message, !d.success);
            loadReaders(); selReader.value = null;
        }

        // ===== 借阅 =====
        const borrowForm = reactive({ isbn: '', readerId: '', days: '7' });
        const returnForm = reactive({ isbn: '', readerId: '' });
        const recordList = ref([]);
        const showOverdue = ref(false);
        const myRecordList = ref([]);
        const showUnreturned = ref(false);

        const filteredRecords = computed(() => {
            if (!showOverdue.value) return recordList.value;
            return recordList.value.filter(r => !r.returned && r.overdue);
        });

        async function loadRecords() {
            const url = showOverdue.value ? API + '/api/records/overdue' : API + '/api/records';
            const res = await fetch(url);
            recordList.value = await res.json();
        }
        async function loadMyRecords() {
            const res = await fetch(API + '/api/records/my/' + username.value);
            const all = await res.json();
            if (showUnreturned.value) myRecordList.value = all.filter(r => !r.returned);
            else myRecordList.value = all;
        }

        async function doBorrow() {
            if (!borrowForm.isbn) { opMsg('请输入书号', true); return; }
            const res = await fetch(API + '/api/borrow', {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    isbn: borrowForm.isbn,
                    readerId: userRole.value === 'STUDENT' ? username.value : borrowForm.readerId,
                    days: parseInt(borrowForm.days)
                })
            });
            const d = await res.json(); opMsg(d.message, !d.success);
            borrowForm.isbn = ''; borrowForm.readerId = '';
            loadBooks();
            if (userRole.value === 'ADMIN') { loadRecords(); loadReaders(); }
            else loadMyRecords();
        }

        async function doReturn() {
            if (!returnForm.isbn) { opMsg('请输入书号', true); return; }
            const res = await fetch(API + '/api/return', {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    isbn: returnForm.isbn,
                    readerId: userRole.value === 'STUDENT' ? username.value : returnForm.readerId
                })
            });
            const d = await res.json(); opMsg(d.message, !d.success);
            returnForm.isbn = ''; returnForm.readerId = '';
            loadBooks();
            if (userRole.value === 'ADMIN') { loadRecords(); loadReaders(); }
            else loadMyRecords();
        }

        const opMsgData = reactive({ text: '', error: false });
        const opMsgType = computed(() => opMsgData.error ? 'error' : 'success');
        function opMsg(text, isError) { opMsgData.text = text; opMsgData.error = isError; setTimeout(() => { opMsgData.text = ''; }, 3000); }

        const adminTab = ref('books');
        const stuTab = ref('search');

        return {
            loggedIn, username, userRole, isLoginMode, msg, msgType, loginForm,
            doLogin, doRegister, logout,
            bookList, searchKeyword, editBook, loadBooks, searchBooks, selectBook, addBook, updateBook, deleteBook,
            readerList, selReader, newReader, loadReaders, addReader, deleteReader,
            borrowForm, returnForm, recordList, showOverdue, myRecordList, showUnreturned,
            filteredRecords, loadRecords, loadMyRecords, doBorrow, doReturn,
            opMsgData, opMsgType, adminTab, stuTab
        };
    }
});

app.mount('#app');
