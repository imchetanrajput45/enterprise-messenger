import { Client } from '@stomp/stompjs';

let stompClient = null;
let subscriptions = {};

const getWsUrl = () => {
  const base = import.meta.env.VITE_API_URL || window.location.origin;
  return `${base}/ws`;
};

export const connectWebSocket = (token, onMessageReceived) => {
  if (stompClient?.active) return;

  stompClient = new Client({
    brokerURL: getWsUrl().replace(/^http/, 'ws'),
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => {
      console.log('WebSocket connected');
      // Re-subscribe everything on connect
      const oldSubs = { ...subscriptions };
      const oldTyping = { ...typingSubscriptions };
      const oldStatus = { ...statusSubscriptions };
      
      // Clear local state so subscribe calls aren't ignored
      subscriptions = {};
      Object.keys(typingSubscriptions).forEach(k => delete typingSubscriptions[k]);
      Object.keys(statusSubscriptions).forEach(k => delete statusSubscriptions[k]);

      Object.entries(oldSubs).forEach(([id, val]) => {
        const cb = typeof val === 'function' ? val : val.cb;
        if (cb) subscribeToConversation(id, cb);
      });
      Object.entries(oldTyping).forEach(([id, val]) => {
        const cb = typeof val === 'function' ? val : val.cb;
        if (cb) subscribeToTyping(id, cb);
      });
      Object.entries(oldStatus).forEach(([id, val]) => {
        const cb = typeof val === 'function' ? val : val.cb;
        if (cb) subscribeToStatus(id, cb);
      });
    },
    onDisconnect: () => {
      console.log('WebSocket disconnected');
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame);
    },
  });

  stompClient.activate();
};

export const disconnectWebSocket = () => {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
    subscriptions = {};
    Object.keys(typingSubscriptions).forEach(k => delete typingSubscriptions[k]);
    Object.keys(statusSubscriptions).forEach(k => delete statusSubscriptions[k]);
  }
};

export const subscribeToConversation = (conversationId, onMessage) => {
  if (!onMessage) return;
  
  if (!stompClient?.active) {
    subscriptions[conversationId] = onMessage; // Store callback for connect later
    return;
  }

  // Already subscribed with a valid STOMP subscription
  if (subscriptions[conversationId]?.sub?.id) return;

  const sub = stompClient.subscribe(
    `/topic/conversations/${conversationId}`,
    (message) => {
      const parsed = JSON.parse(message.body);
      onMessage(parsed);
    }
  );

  subscriptions[conversationId] = { sub, cb: onMessage };
};

export const unsubscribeFromConversation = (conversationId) => {
  if (subscriptions[conversationId]?.sub) {
    subscriptions[conversationId].sub.unsubscribe();
  }
  delete subscriptions[conversationId];
};

const typingSubscriptions = {};

export const subscribeToTyping = (conversationId, onTyping) => {
  if (!onTyping) return;

  if (!stompClient?.active) {
    typingSubscriptions[conversationId] = onTyping;
    return;
  }

  if (typeof typingSubscriptions[conversationId] === 'object' && typingSubscriptions[conversationId].id) return;

  const sub = stompClient.subscribe(
    `/topic/conversations/${conversationId}/typing`,
    (message) => {
      onTyping(JSON.parse(message.body));
    }
  );

  typingSubscriptions[conversationId] = { sub, cb: onTyping };
};

export const unsubscribeFromTyping = (conversationId) => {
  if (typingSubscriptions[conversationId]?.sub) {
    typingSubscriptions[conversationId].sub.unsubscribe();
  }
  delete typingSubscriptions[conversationId];
};

export const sendTypingIndicator = (conversationId) => {
  const userId = localStorage.getItem('userId');
  if (stompClient?.active && userId) {
    stompClient.publish({
      destination: '/app/chat.typing',
      headers: { 'X-User-Id': userId },
      body: JSON.stringify({ conversationId }),
    });
  }
};

const statusSubscriptions = {};

export const subscribeToStatus = (conversationId, onStatus) => {
  if (!onStatus) return;

  if (!stompClient?.active) {
    statusSubscriptions[conversationId] = onStatus;
    return;
  }

  if (typeof statusSubscriptions[conversationId] === 'object' && statusSubscriptions[conversationId].id) return;

  const sub = stompClient.subscribe(
    `/topic/conversations/${conversationId}/status`,
    (message) => {
      onStatus(JSON.parse(message.body));
    }
  );

  statusSubscriptions[conversationId] = { sub, cb: onStatus };
};

export const unsubscribeFromStatus = (conversationId) => {
  if (statusSubscriptions[conversationId]?.sub) {
    statusSubscriptions[conversationId].sub.unsubscribe();
  }
  delete statusSubscriptions[conversationId];
};

export const sendWebSocketMessage = (destination, body) => {
  if (stompClient?.active) {
    stompClient.publish({
      destination,
      body: JSON.stringify(body),
    });
  }
};
