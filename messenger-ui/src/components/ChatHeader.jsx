import { BsThreeDotsVertical } from 'react-icons/bs';
import { useUserProfile, getDisplayNameSync } from '../services/userCache';
import { useChat } from '../context/ChatContext';

export default function ChatHeader({ conversation }) {
  const currentUserId = localStorage.getItem('userId');
  const otherMember = conversation.members?.find((m) => m.userId !== currentUserId);
  const profile = useUserProfile(otherMember?.userId);
  const { typingUsers } = useChat();
  
  const name = conversation.name || profile?.displayName || 'Chat';

  // Check typing status
  const activeTypers = Array.from(typingUsers[conversation.id] || []);
  let statusText = conversation.type === 'DIRECT' ? 'online' : `${conversation.members?.length || 0} members`;
  
  if (activeTypers.length > 0) {
    if (conversation.type === 'DIRECT') {
      statusText = 'typing...';
    } else {
      const typerName = getDisplayNameSync(activeTypers[0]);
      statusText = `${typerName} is typing...`;
    }
  }

  return (
    <div className="chat-header">
      <div className="chat-header-avatar">
        {name.charAt(0).toUpperCase()}
      </div>
      <div className="chat-header-info">
        <h3>{name}</h3>
        <span className={activeTypers.length > 0 ? 'typing-text' : ''}>
          {statusText}
        </span>
      </div>
      <button className="icon-btn">
        <BsThreeDotsVertical />
      </button>
    </div>
  );
}
