'use strict';

const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const connectingElement = document.querySelector('.connecting');
const chatArea = document.querySelector('#chat-messages');
const logout = document.querySelector('#logout');

let stompClient = null;
let adminId="4116b6bb-a49d-427b-8004-6bd92fd65b14"
let userId="7"
let connectedUser=null

const socketUrl = `ws://isds-chat-service-8a2254e8ad79.herokuapp.com/chat?userId=${userId}&ticketId=${adminId}`;

// Connect to WebSocket server
initializeWebSocket()


// Initialize WebSocket connection
function initializeWebSocket() {
    const token = localStorage.getItem('authToken');
    // Assuming the JWT token is stored in localStorage
    console.log("socket token")
    console.log(token)
    console.log(`${socketUrl}?userId=${userId}&ticketId=${adminId}`)

    stompClient = new StompJs.Client({
        brokerURL: socketUrl,

        reconnectDelay: 5000,
        debug: str => console.log(str),
    });

    stompClient.onConnect = () => {
        console.log('Connected to WebSocket');

        stompClient.subscribe(`/user/${userId}/queue/messages`, onMessageReceived);
        stompClient.subscribe('/topic/user-status', onUserUpdateReceived);
        stompClient.subscribe(`/user/${userId}/queue/typing`,
            onUserTyping);
        onConnected(adminId);

    };


    stompClient.onStompError = frame => {
        console.error('WebSocket error:', frame.headers['message']);
        console.error('Details:', frame.body);
        onError();
    };

    stompClient.activate();

}


function onConnected(adminId) {


    stompClient.publish({
        destination: '/app/addUser',
        body: JSON.stringify({ userId: adminId, fullName: "ADMIN", status: 'ONLINE' }),
    });

    document.querySelector('#connected-user-fullname').textContent = "ADMIN";

}












function updateUnreadMessagesCounter(senderId) {
    const userElement = document.querySelector(`#${senderId}`);

    const nbrMsg = userElement.querySelector('.nbr-msg');
    const unreadCount = parseInt(nbrMsg.textContent) || 0;
    nbrMsg.textContent = unreadCount + 1;
    nbrMsg.classList.remove('hidden');

}
function sendMessage(event) {
    const messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        const chatMessage = {
            senderId: adminId,
            recieverId: userId,
            content: messageInput.value.trim(),
            timestamp: new Date()
        };

        stompClient.publish({
            destination: `/app/chat`,
            body: JSON.stringify(chatMessage)
        });
        // displayMessage(selectedUser, messageInput.value.trim());
        messageInput.value = '';


    }
    chatArea.scrollTop = chatArea.scrollHeight;
    event.preventDefault();
}

const typingTimeouts = {};
function onUserTyping(payload) {
    console.log("***********************");
    console.log(payload);
    const update = JSON.parse(payload.body);
    console.log(update);

    const senderId = update.senderId;
    if(senderId !== adminId) {

        // Check if a typing div for this sender already exists
        const existingTypingDiv = document.querySelector(`.message.typing[data-sender="${senderId}"]`);

        if (!existingTypingDiv) {
            // Create the typing message if it doesn't already exist
            const messageContainer = document.createElement('div');
            messageContainer.classList.add('message', senderId === adminId ? 'hidden' : 'receiver');


            messageContainer.classList.add('typing');
            messageContainer.setAttribute('data-sender', senderId);

            messageContainer.innerHTML = "<p style='font-size:11px'>Typing....</p>";
            chatArea.appendChild(messageContainer);
            chatArea.scrollTop = chatArea.scrollHeight;
        }

        if (typingTimeouts[senderId]) {
            clearTimeout(typingTimeouts[senderId]);
        }
        typingTimeouts[senderId] = setTimeout(() => {
            const typingDiv = document.querySelector(`.message.typing[data-sender="${senderId}"]`);
            if (typingDiv) {
                typingDiv.remove();
            }
            delete typingTimeouts[senderId]; // Clean up the timeout reference
        }, 1000); // 1 second
    }
}

function typing(){

    if (stompClient) {
        const chatMessage = {
            senderId: adminId,
            recipientId: userId,
        };

        stompClient.publish({
            destination: `/app/type`,
            body: JSON.stringify(chatMessage)
        });



    }


}

// Handle logout
function onLogout() {
    if (stompClient && stompClient.connected) {
        stompClient.publish({
            destination: '/app/user.disconnectUser',
            body: JSON.stringify({ userId: adminId, status: 'OFFLINE' }),
        });
    }
    window.location.reload();
}
async function fetchAndDisplayUserChat() {
    try {
        console.log("Fetching chat history for:", adminId);
        console.log(`https://isds-chat-service-8a2254e8ad79.herokuapp.com/messages/${connectedUser}/${adminId}`)
        const response = await fetch(`https://isds-chat-service-8a2254e8ad79.herokuapp.com/messages/${connectedUser}/${adminId}`);
        const chats = await response.json();

        chatArea.innerHTML = ''; // Clear the chat area

        chats.forEach(chat => {
            displayMessage(chat.senderId, chat.content); // Display each message
        });

        chatArea.scrollTop = chatArea.scrollHeight; // Scroll to the latest message
    } catch (error) {
        console.error('Error fetching chat history:', error);
    }
}
async function userItemClick(event) {
    document.querySelectorAll('.user-item').forEach(item => item.classList.remove('active'));
    const clickedUser = event.currentTarget;
    clickedUser.classList.add('active');

    await fetchAndDisplayUserChat(); // Load the chat history for this user
}

function onUserUpdateReceived(payload) {
    const update = JSON.parse(payload.body);
    console.log(update);
    connectedUser=update.userId;

    const connectedUsersList = document.getElementById('connectedUsers');

    if (update.status === 'ONLINE') {
        console.log("Adding user:", update.userId);
        appendUserElement(update.userId, connectedUsersList);
    } else if (update.status === 'OFFLINE') {
        const userElement = document.getElementById(update.userId);
        if (userElement) userElement.remove();
    }
}
function appendUserElement(user, connectedUsersList) {
    const listItem = document.createElement('li');
    listItem.classList.add('user-item');
    listItem.id = user; // Assign user ID as the element's ID
    listItem.innerHTML = `
        <img src="../img/user_icon.png" alt="${user}" />
        <span class="username">${user}</span>
        <span class="nbr-msg hidden">0</span>
    `;
    listItem.addEventListener('click', userItemClick);
    connectedUsersList.appendChild(listItem);
}
function displayMessage(senderId, content) {
    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message', senderId === adminId ? 'sender' : 'receiver');
    messageContainer.innerHTML = `<p>${content}</p>`;
    chatArea.appendChild(messageContainer);
    chatArea.scrollTop = chatArea.scrollHeight;
}
async function onMessageReceived(payload) {
    console.log("Message Received:");
    const message = JSON.parse(payload.body);

    // Check if the message is for the currently selected user
    if (message.senderId === adminId) {
        displayMessage(message.senderId, message.content);
    } else {
        displayMessage(message.senderId, message.content);
        // updateUnreadMessagesCounter(message.senderId);
    }
}




messageForm.addEventListener('submit', function(event) {
    event.preventDefault();
    sendMessage(event);
}, true);

logout.addEventListener('click', onLogout, true);
window.onbeforeunload = onLogout;

messageInput.addEventListener("keyup",typing,true);