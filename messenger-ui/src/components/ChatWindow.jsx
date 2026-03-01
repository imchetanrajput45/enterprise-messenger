import { useRef, useEffect } from 'react';
import { useChat } from '../context/ChatContext';
import ChatHeader from './ChatHeader';
import MessageInput from './MessageInput';
import { BsChatDotsFill } from 'react-icons/bs';
import { BiCheck, BiCheckDouble } from 'react-icons/bi';
import { getDisplayNameSync } from '../services/userCache';

export default function ChatWindow() {
  const { selectedConversation, messages } = useChat();
  const messagesEndRef = useRef(null);
  const currentUserId = localStorage.getItem('userId');

  const currentMessages = selectedConversation
    ? messages[selectedConversation.id] || []
    : [];

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [currentMessages]);

  if (!selectedConversation) {
    return (
      <div className="chat-main">
        <div className="chat-empty">
          <BsChatDotsFill />
          <h2>Enterprise Messenger</h2>
          <p>Send and receive messages. Select a conversation to start chatting.</p>
        </div>
      </div>
    );
  }

  // Group messages by date
  const groupedMessages = groupByDate(currentMessages);
  const isGroup = selectedConversation.type !== 'DIRECT';

  return (
    <div className="chat-main">
      <ChatHeader conversation={selectedConversation} />
      <div className="message-area">
        {groupedMessages.map((group, gi) => (
          <div key={gi}>
            <div className="date-separator">
              <span>{group.label}</span>
            </div>
            {group.messages.map((msg) => {
              const isSent = msg.senderId === currentUserId;
              const status = msg.localStatus || 'SENT';
              return (
                <div key={msg.id} className={`message-row ${isSent ? 'sent' : 'received'}`}>
                  <div className="message-bubble">
                    {!isSent && isGroup && (
                      <div style={{ color: 'var(--text-secondary)', fontSize: '13px', marginBottom: '4px', fontWeight: '500' }}>
                        {getDisplayNameSync(msg.senderId)}
                      </div>
                    )}
                    <span className="message-text">{msg.content}</span>
                    <span className="message-meta">
                      <span className="message-time">
                        {new Date(msg.createdAt).toLocaleTimeString([], {
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </span>
                      {isSent && (
                        <span className={`message-ticks ${status === 'READ' ? 'read' : ''}`}>
                          {status === 'SENT' ? <BiCheck /> : <BiCheckDouble />}
                        </span>
                      )}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>
      <MessageInput conversationId={selectedConversation.id} />
    </div>
  );
}

function groupByDate(messages) {
  const groups = [];
  let currentDate = '';
  
  messages.forEach((msg) => {
    const d = new Date(msg.createdAt);
    const dateStr = d.toDateString();
    const now = new Date();
    
    let label;
    if (dateStr === now.toDateString()) {
      label = 'Today';
    } else {
      const yesterday = new Date(now);
      yesterday.setDate(yesterday.getDate() - 1);
      if (dateStr === yesterday.toDateString()) {
        label = 'Yesterday';
      } else {
        label = d.toLocaleDateString([], { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });
      }
    }

    if (dateStr !== currentDate) {
      currentDate = dateStr;
      groups.push({ label, messages: [msg] });
    } else {
      groups[groups.length - 1].messages.push(msg);
    }
  });

  return groups;
}
