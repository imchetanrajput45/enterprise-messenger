import { useState } from 'react';
import { useChat } from '../context/ChatContext';
import { BiArrowBack, BiSearch } from 'react-icons/bi';

export default function NewChatModal({ onClose }) {
  const [userId, setUserId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { createConversation, selectConversation } = useChat();

  const handleCreate = async () => {
    if (!userId.trim()) return;
    setLoading(true);
    setError('');
    try {
      const conv = await createConversation({
        type: 'DIRECT',
        memberIds: [userId.trim()],
      });
      selectConversation(conv);
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create conversation');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <button className="icon-btn" onClick={onClose}>
            <BiArrowBack />
          </button>
          <h2>New Chat</h2>
        </div>
        <div className="modal-body">
          <div className="modal-search">
            <div className="search-input-wrapper">
              <BiSearch />
              <input
                type="text"
                placeholder="Enter User ID to start a conversation"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleCreate()}
              />
            </div>
          </div>

          {error && (
            <div style={{ padding: '8px 20px' }}>
              <div className="error-message">{error}</div>
            </div>
          )}

          <div style={{ padding: '12px 20px' }}>
            <button
              className="btn-primary"
              onClick={handleCreate}
              disabled={loading || !userId.trim()}
            >
              {loading ? 'Creating...' : 'Start Conversation'}
            </button>
          </div>

          <div style={{ padding: '16px 20px', color: 'var(--text-secondary)', fontSize: '13px' }}>
            <p><strong>How to find a User ID:</strong></p>
            <p style={{ marginTop: '6px' }}>
              When a user registers, their User ID is returned in the response. 
              For testing, you can get User IDs from the registration/login API responses.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
