let socket;
let username;

const chatBox = document.getElementById('chatBox');
const usernameInput = document.getElementById('usernameInput');
const joinBtn = document.getElementById('joinBtn');
const messageInput = document.getElementById('messageInput');
const sendBtn = document.getElementById('sendBtn');
const messageArea = document.getElementById('messageArea');

joinBtn.addEventListener('click', () => {
    username = usernameInput.value.trim();
    if (username) {
        connectToServer();
        document.querySelector('.input-area').style.display = 'none';
        messageArea.style.display = 'flex';
    }
});

sendBtn.addEventListener('click', sendMessage);
messageInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') sendMessage();
});

function connectToServer() {
    socket = new WebSocket('ws://localhost:8080');

    socket.onopen = () => {
        console.log('Connected to server');
        socket.send(username);
    };

    socket.onmessage = (event) => {
        displayMessage(event.data);
    };

    socket.onerror = (error) => {
        console.error('WebSocket error:', error);
        alert('Unable to connect to server. Make sure the Java server is running.');
    };

    socket.onclose = () => {
        console.log('Disconnected from server');
    };
}

function sendMessage() {
    const message = messageInput.value.trim();
    if (message && socket.readyState === WebSocket.OPEN) {
        socket.send(message);
        messageInput.value = '';
    }
}

function displayMessage(message) {
    const messageDiv = document.createElement('div');
    messageDiv.classList.add('message');
    messageDiv.textContent = message;
    chatBox.appendChild(messageDiv);
    chatBox.scrollTop = chatBox.scrollHeight;
}
