import { createContext, useContext, useReducer, useEffect, useCallback, useState, useRef } from 'react';
import { messagingApi } from '../services/api';
import { connectWebSocket, disconnectWebSocket, subscribeToConversation, subscribeToTyping, sendTypingIndicator } from '../services/websocket';
import { useAuth } from './AuthContext';
import { prefetchProfiles } from '../services/userCache';

const ChatContext = createContext();

const initialState = {
  conversations: [],
  selectedConversation: null,
  messages: {},          // { [conversationId]: MessageDto[] }
  loading: false,
};

function chatReducer(state, action) {
  switch (action.type) {
    case 'SET_CONVERSATIONS':
      return { ...state, conversations: action.payload, loading: false };
    case 'SELECT_CONVERSATION':
      return { ...state, selectedConversation: action.payload };
    case 'ADD_CONVERSATION':
      return {
        ...state,
        conversations: [action.payload, ...state.conversations.filter(c => c.id !== action.payload.id)],
      };
    case 'SET_MESSAGES':
      return {
        ...state,
        messages: { ...state.messages, [action.payload.conversationId]: action.payload.messages },
      };
    case 'ADD_MESSAGE': {
      const convId = action.payload.conversationId;
      const existing = state.messages[convId] || [];
      // Avoid duplicates
      if (existing.find(m => m.id === action.payload.id)) return state;
      return {
        ...state,
        messages: { ...state.messages, [convId]: [...existing, action.payload] },
        conversations: state.conversations.map(c =>
          c.id === convId ? { ...c, lastMessage: action.payload } : c
        ),
      };
    }
    case 'SET_LOADING':
      return { ...state, loading: action.payload };
    case 'UPDATE_MESSAGE_STATUS': {
      const { conversationId, eventType, timestamp, currentUserId } = action.payload;
      console.log('UPDATE_MESSAGE_STATUS', { conversationId, eventType, currentUserId });
      const existing = state.messages[conversationId];
      if (!existing) return state;

      const eventTime = new Date(timestamp).getTime();
      let updatedCount = 0;

      const updatedMsgs = existing.map(msg => {
        if (msg.senderId === currentUserId) {
          const currentStatus = msg.localStatus || 'SENT';
          let newStatus = currentStatus;
          if (eventType === 'READ') newStatus = 'READ';
          else if (eventType === 'DELIVERED' && currentStatus !== 'READ') newStatus = 'DELIVERED';
          
          if (newStatus !== currentStatus) {
            updatedCount++;
            return { ...msg, localStatus: newStatus };
          }
        }
        return msg;
      });

      console.log('UPDATE_MESSAGE_STATUS result', { updatedCount });
      if (updatedCount === 0) return state;
      return {
        ...state,
        messages: { ...state.messages, [conversationId]: updatedMsgs }
      };
    }
    default:
      return state;
  }
}

export function ChatProvider({ children }) {
  const [state, dispatch] = useReducer(chatReducer, initialState);
  const { token, isAuthenticated, userId } = useAuth();
  
  // Typing state: { [conversationId]: Set<userId> }
  const [typingUsers, setTypingUsers] = useState({});
  const typingTimeouts = useRef({});

  // Handle incoming WebSocket message
  const handleIncomingMessage = useCallback((message) => {
    dispatch({ type: 'ADD_MESSAGE', payload: message });
    // If someone sends a message, clear their typing indicator immediately
    const { conversationId, senderId } = message;
    if (senderId !== userId) {
      setTypingUsers(prev => {
        const current = new Set(prev[conversationId] || []);
        current.delete(senderId);
        return { ...prev, [conversationId]: current };
      });
      
      // Auto mark as delivered
      messagingApi.markAsDelivered(conversationId).catch(() => {});
    }
  }, [userId]);

  // Handle incoming typing events
  const handleTypingEvent = useCallback((event) => {
    const { conversationId, userId: typerId } = event;
    if (typerId === userId) return; // ignore my own

    setTypingUsers(prev => {
      const current = new Set(prev[conversationId] || []);
      current.add(typerId);
      return { ...prev, [conversationId]: current };
    });

    // Clear timeout if exists
    const timeoutKey = `${conversationId}-${typerId}`;
    if (typingTimeouts.current[timeoutKey]) {
      clearTimeout(typingTimeouts.current[timeoutKey]);
    }

    // Set new timeout to remove typing status after 3s
    typingTimeouts.current[timeoutKey] = setTimeout(() => {
      setTypingUsers(prev => {
        const current = new Set(prev[conversationId] || []);
        current.delete(typerId);
        return { ...prev, [conversationId]: current };
      });
    }, 3000);
  }, [userId]);

  // Handle incoming message status updates
  const handleStatusEvent = useCallback((event) => {
    dispatch({
      type: 'UPDATE_MESSAGE_STATUS',
      payload: {
        conversationId: event.conversationId,
        eventType: event.eventType,
        timestamp: event.timestamp,
        currentUserId: userId,
      }
    });
  }, [userId]);

  // Connect WebSocket on auth
  useEffect(() => {
    if (isAuthenticated && token) {
      connectWebSocket(token, handleIncomingMessage);
    }
    return () => disconnectWebSocket();
  }, [isAuthenticated, token, handleIncomingMessage]);

  // Load conversations
  const loadConversations = useCallback(async () => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      const res = await messagingApi.getConversations();
      const convos = res.data.data?.content || [];
      dispatch({ type: 'SET_CONVERSATIONS', payload: convos });
      
      // Subscribe to all conversations for real-time updates
      const otherUserIds = [];
      import('../services/websocket').then(({ subscribeToConversation, subscribeToTyping, subscribeToStatus }) => {
        convos.forEach((c) => {
          subscribeToConversation(c.id, handleIncomingMessage);
          subscribeToTyping(c.id, handleTypingEvent);
          subscribeToStatus(c.id, handleStatusEvent);
          const otherMember = c.members?.find((m) => m.userId !== userId);
          if (otherMember) otherUserIds.push(otherMember.userId);
        });
        prefetchProfiles(otherUserIds);
      });
    } catch (err) {
      console.error('Failed to load conversations:', err);
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  }, [handleIncomingMessage, handleTypingEvent, handleStatusEvent, userId]);

  // Load initial conversations
  useEffect(() => {
    if (isAuthenticated) {
      loadConversations();
    }
  }, [isAuthenticated, loadConversations]);

  const selectConversation = async (conversation) => {
    dispatch({ type: 'SELECT_CONVERSATION', payload: conversation });
    
    // Load message history
    if (!state.messages[conversation.id]) {
      try {
        const res = await messagingApi.getMessages(conversation.id);
        const msgs = res.data.data?.content || [];
        dispatch({
          type: 'SET_MESSAGES',
          payload: { conversationId: conversation.id, messages: msgs.reverse() },
        });
      } catch (err) {
        console.error('Failed to load messages:', err);
      }
    }

    // Subscribe to this conversation's WebSocket topic
    import('../services/websocket').then(({ subscribeToStatus }) => {
      subscribeToConversation(conversation.id, handleIncomingMessage);
      subscribeToTyping(conversation.id, handleTypingEvent);
      subscribeToStatus(conversation.id, handleStatusEvent);
    });
    
    // Mark as read
    try {
      await messagingApi.markAsRead(conversation.id);
    } catch (_) {}
  };

  const sendMessage = async (conversationId, content) => {
    try {
      const res = await messagingApi.sendMessage({ conversationId, content });
      const msg = res.data.data;
      dispatch({ type: 'ADD_MESSAGE', payload: msg });
      return msg;
    } catch (err) {
      console.error('Failed to send message:', err);
      throw err;
    }
  };

  const createConversation = async (data) => {
    const res = await messagingApi.createConversation(data);
    const conv = res.data.data;
    dispatch({ type: 'ADD_CONVERSATION', payload: conv });
    subscribeToConversation(conv.id, handleIncomingMessage);
    subscribeToTyping(conv.id, handleTypingEvent);
    return conv;
  };

  const notifyTyping = (conversationId) => {
    sendTypingIndicator(conversationId);
  };

  return (
    <ChatContext.Provider
      value={{
        ...state,
        typingUsers,
        selectConversation,
        sendMessage,
        createConversation,
        loadConversations,
        notifyTyping,
      }}
    >
      {children}
    </ChatContext.Provider>
  );
}

export const useChat = () => useContext(ChatContext);
