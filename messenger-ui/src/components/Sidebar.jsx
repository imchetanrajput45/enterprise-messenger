import { useState } from 'react';
import { useChat } from '../context/ChatContext';
import { BiSearch, BiChat } from 'react-icons/bi';
import { BsThreeDotsVertical } from 'react-icons/bs';
import { useUserProfile, getDisplayNameSync } from '../services/userCache';

function ConversationItem({ conv, active, onClick }) {
  const currentUserId = localStorage.getItem('userId');
  const otherMember = conv.members?.find((m) => m.userId !== currentUserId);
  const profile = useUserProfile(otherMember?.userId);
  
  const name = conv.name || profile?.displayName || 'Chat';

  return (
    <div className={`conversation-item ${active ? 'active' : ''}`} onClick={onClick}>
      <div className="conversation-avatar">
        {name.charAt(0).toUpperCase()}
      </div>
      <div className="conversation-content">
        <div className="conversation-top">
          <span className="conversation-name">{name}</span>
          <span className="conversation-time">
            {conv.lastMessage ? formatTime(conv.lastMessage.createdAt) : ''}
          </span>
        </div>
        <div className="conversation-bottom">
          <span className="conversation-preview">
            {conv.lastMessage?.content || 'No messages yet'}
          </span>
          {conv.unreadCount > 0 && (
            <span className="unread-badge">{conv.unreadCount}</span>
          )}
        </div>
      </div>
    </div>
  );
}

export default function Sidebar({ onProfileClick, onNewChatClick }) {
  const { conversations, selectedConversation, selectConversation, loading } = useChat();
  const [search, setSearch] = useState('');
  const username = localStorage.getItem('username') || '?';

  const filtered = conversations.filter((c) => {
    if (!search) return true;
    const name = getSearchName(c);
    return name.toLowerCase().includes(search.toLowerCase());
  });

  return (
    <div className="sidebar">
      {/* Header */}
      <div className="sidebar-header">
        <div className="user-info">
          <div className="user-avatar" onClick={onProfileClick}>
            {username.charAt(0).toUpperCase()}
          </div>
        </div>
        <div className="sidebar-actions">
          <button className="icon-btn" onClick={onNewChatClick} title="New chat">
            <BiChat />
          </button>
          <button className="icon-btn" title="Menu">
            <BsThreeDotsVertical />
          </button>
        </div>
      </div>

      {/* Search */}
      <div className="sidebar-search">
        <div className="search-input-wrapper">
          <BiSearch />
          <input
            type="text"
            placeholder="Search or start new chat"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      {/* Conversation List */}
      <div className="conversation-list">
        {loading && (
          <div className="loading-spinner">
            <div className="spinner" />
          </div>
        )}
        {!loading && filtered.length === 0 && (
          <div style={{ padding: '40px 20px', textAlign: 'center', color: 'var(--text-secondary)' }}>
            <p>No conversations yet</p>
            <p style={{ fontSize: '13px', marginTop: '8px' }}>Click the chat icon above to start messaging</p>
          </div>
        )}
        {filtered.map((conv) => (
          <ConversationItem
            key={conv.id}
            conv={conv}
            active={selectedConversation?.id === conv.id}
            onClick={() => selectConversation(conv)}
          />
        ))}
      </div>
    </div>
  );
}

function getSearchName(conv) {
  if (conv.name) return conv.name;
  const currentUserId = localStorage.getItem('userId');
  const otherMember = conv.members?.find((m) => m.userId !== currentUserId);
  return getDisplayNameSync(otherMember?.userId);
}

function formatTime(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  const now = new Date();
  const isToday = d.toDateString() === now.toDateString();
  if (isToday) {
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (d.toDateString() === yesterday.toDateString()) return 'Yesterday';
  return d.toLocaleDateString([], { month: 'short', day: 'numeric' });
}
