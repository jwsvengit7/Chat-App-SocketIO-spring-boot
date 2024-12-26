'use strict';

const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const connectingElement = document.querySelector('.connecting');
const chatArea = document.querySelector('#chat-messages');
const logout = document.querySelector('#logout');

let stompClient = null;
let userId = null;
let password = null;
let selectedTicket = null;
let selectedUser = null;
let selectedUserName = null;

// Connect to WebSocket server
function connect(event) {
    event.preventDefault();

    userId = document.querySelector('#userId').value.trim();
    password = document.querySelector('#password').value.trim();

    if (!userId || !password) {
        alert('Both userId and fullname are required!');
        return;
    }

    // Prepare login request
    const requestData = {
        emailOrPhoneNumber: userId,
        password: password,
    };


    fetch('http://localhost:8111/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' ,

        },
        body: JSON.stringify(requestData),
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to log in');
            }
            return response.json();
        })
        .then((data) => {
            console.log('Login successful');
            console.log(data);
            localStorage.setItem('authToken',data.data.accessToken);

            selectedUser=data.data.userID
            selectedUserName=data.data.username
            console.log(selectedUserName)
            findAndDisplayConnectedUsers(data.data);
            console.log(selectedUser);
        })
        .catch(error => {
            console.error('Error during login:', error);
            alert('Login failed. Please try again.');
        });
}

// Initialize WebSocket connection
function initializeWebSocket(data) {
    const token = localStorage.getItem('authToken');
    // Assuming the JWT token is stored in localStorage
    console.log("socket token")
    console.log(token)
    const socketUrl = 'ws://localhost/chat';
    console.log(`${socketUrl}?userId=${data.senderId}&ticketId=${data.orderId}`)

        stompClient = new StompJs.Client({
            brokerURL: `${socketUrl}?userId=${data.senderId}&ticketId=${data.orderId}`,

            reconnectDelay: 5000,
            debug: str => console.log(str),
        });

        stompClient.onConnect = () => {
            console.log('Connected to WebSocket');
            onConnected(data);
            stompClient.subscribe(`/user/${data.senderId}/queue/messages`, onMessageReceived);
            stompClient.subscribe('/topic/user-status', onUserUpdateReceived);
            stompClient.subscribe(`/user/${data.senderId}/queue/typing`, onUserTyping);


        };


        stompClient.onStompError = frame => {
            console.error('WebSocket error:', frame.headers['message']);
            console.error('Details:', frame.body);
            onError();
        };

        stompClient.activate();
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

}


function onConnected(data) {


    stompClient.publish({
        destination: '/app/addUser',
        body: JSON.stringify({ userId: data.senderId, fullName: selectedUserName, status: 'ONLINE' }),
    });

    document.querySelector('#connected-user-fullname').textContent = data.username;

}

function appendUserElement(user, connectedUsersList,hidden) {


    const listItem = document.createElement('li');
    listItem.classList.add('user-item');
    listItem.classList.add('userX' + user);
    listItem.id = user;
    const userImage = document.createElement('img');
    userImage.src = '../img/user_icon.png';
    userImage.alt = user;
    const usernameSpan = document.createElement('span');
    usernameSpan.textContent = user;
    usernameSpan.classList.add('username');
    const receivedMsgs = document.createElement('span');
    receivedMsgs.textContent = '0';
    receivedMsgs.classList.add('nbr-msg', hidden);

    listItem.append(userImage, usernameSpan, receivedMsgs);

    listItem.addEventListener('click', userItemClick);

    connectedUsersList.appendChild(listItem);

}

function onUserUpdateReceived(payload) {
    const update = JSON.parse(payload.body);
    console.log(update);


    if (update.status === 'ONLINE') {
        document.querySelector(".nbr-msg").style.display = 'block';
    } else if (update.status === 'OFFLINE') {
        document.querySelector(".nbr-msg").style.display = 'none';
    }
}
const typingTimeouts = {};

function onUserTyping(payload) {
    console.log("***********************");
    console.log(payload);
    const update = JSON.parse(payload.body);
    console.log(update);

    const senderId = update.senderId;
    if( senderId !== selectedUser) {
        const existingTypingDiv = document.querySelector(`.message.typing[data-sender="${senderId}"]`);

        if (!existingTypingDiv) {
            const messageContainer = document.createElement('div');
            messageContainer.classList.add('message', senderId === selectedUser ? 'sender' : 'receiver');
            messageContainer.classList.add('typing');

            messageContainer.setAttribute('data-sender', senderId);

            messageContainer.innerHTML = `<p style="font-size:11px">Typing....</p>`;
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
            delete typingTimeouts[senderId];
        }, 1000);
    }
}







// Fetch and display connected users
async function findAndDisplayConnectedUsers(data) {
    try {
        const requestData ={
            ticketType:'DELIVERY',
            orderId:'ee7e892a-a4eb-4a1e-b950-825ff9a9af18',
            message:'Hello',
        }
        await fetch('http://localhost:8111/api/v1/chat-ticket/send-ticket',
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' ,
                    'Authorization':`Bearer ${data.accessToken}`


                },

        body: JSON.stringify(requestData),
    })
.then(response => {
        if (!response.ok) {
            throw new Error('Failed to log in');
        }
        return response.json();
    })
        .then((data) => {
            console.log('Data successful');
            console.log(data);

            selectedTicket=data.orderId
            console.log(data.orderId);
            initializeWebSocket(data);
            chatPage.classList.remove('hidden');
            const connectedUsersList = document.getElementById('connectedUsers');
            connectedUsersList.innerHTML=''
            appendUserElement(data.orderId,connectedUsersList,'hidden')


            // connectedUsers.forEach(user => appendUserElement(user, connectedUsersList));
        })
        .catch(error => {
            console.error('Error during login:', error);
        });


    } catch (error) {
        console.error('Error fetching connected users:', error);
    }
}






// Handle user item click
 async function   userItemClick  (event) {
    console.log("************")
    document.querySelectorAll('.user-item').forEach(item => item.classList.remove('active'));
    const clickedUser = event.currentTarget;
    clickedUser.classList.add('active');
    selectedTicket = clickedUser.getAttribute('id');
    messageForm.classList.remove('hidden');
    await fetchAndDisplayUserChat();
}

function displayMessage(senderId, content) {
    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message', senderId === selectedUser ? 'sender' : 'receiver');
    messageContainer.innerHTML = `<p>${content}</p>`;
    chatArea.appendChild(messageContainer);
    chatArea.scrollTop = chatArea.scrollHeight;
}

async function onMessageReceived(payload) {
    console.log("Message Received:");
    const message = JSON.parse(payload.body);
    console.log("Message Received:", message);
    console.log("Message selectedUser:", selectedUser);
    console.log("Message message senderId:", message.senderId);
    

    if (message.senderId !== selectedUser) {
        updateUnreadMessagesCounter(message.senderId);
        displayMessage(message.senderId, message.content);
    }
    if (message.senderId === selectedUser) {
        displayMessage(message.senderId, message.content);
    }
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
            senderId: selectedUser,
            recieverId: selectedTicket,
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



async function fetchAndDisplayUserChat() {
    try {
        console.log("api calls")
        const response = await fetch(`http://localhost:8888/messages/${selectedUser}/${selectedTicket}`);
        const chats = await response.json();
        console.log(response.data)
        console.log(response)
        chatArea.innerHTML = '';

        chats.forEach(chat => {
            displayMessage(chat.senderId, chat.content);
        });

        chatArea.scrollTop = chatArea.scrollHeight; // Scroll to latest message
    } catch (error) {
        console.error('Error fetching chat history:', error);
    }
}

// Handle logout
function onLogout() {
    stompClient.publish({
        destination: '/app/user.disconnectUser',
        body: JSON.stringify({ userId: selectedUser, fullName: selectedUserName, status: 'OFFLINE' }),
    });
    window.location.reload();
}
function typing(){

    if ( stompClient) {
        const chatMessage = {
            senderId: selectedUser,
            recipientId: selectedTicket,
        };

        stompClient.publish({
            destination: `/app/type`,
            body: JSON.stringify(chatMessage)
        });


    }


}

// Add event listeners
usernameForm.addEventListener('submit', connect, true);
// Make sure `sendMessage` is declared before this code
messageForm.addEventListener('submit', function(event) {
    event.preventDefault();
    sendMessage(event); // Call the `sendMessage` function
}, true);

logout.addEventListener('click', onLogout, true);
window.onbeforeunload = onLogout;
messageInput.addEventListener("keyup",typing,true)

//6916