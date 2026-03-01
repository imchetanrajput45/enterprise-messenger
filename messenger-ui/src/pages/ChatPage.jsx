import { useState } from 'react';
import Sidebar from '../components/Sidebar';
import ChatWindow from '../components/ChatWindow';
import ProfilePanel from '../components/ProfilePanel';
import NewChatModal from '../components/NewChatModal';
import { ChatProvider } from '../context/ChatContext';
import { BsChatDotsFill } from 'react-icons/bs';

function ChatPageInner() {
  const [showProfile, setShowProfile] = useState(false);
  const [showNewChat, setShowNewChat] = useState(false);

  return (
    <div className="chat-page">
      <div style={{ position: 'relative' }}>
        {showProfile && <ProfilePanel onClose={() => setShowProfile(false)} />}
        <Sidebar
          onProfileClick={() => setShowProfile(true)}
          onNewChatClick={() => setShowNewChat(true)}
        />
      </div>
      <ChatWindow />
      {showNewChat && <NewChatModal onClose={() => setShowNewChat(false)} />}
    </div>
  );
}

export default function ChatPage() {
  return (
    <ChatProvider>
      <ChatPageInner />
    </ChatProvider>
  );
}
