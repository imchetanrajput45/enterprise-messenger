import { useState, useRef } from 'react';
import { useChat } from '../context/ChatContext';
import { BiSend } from 'react-icons/bi';
import { BsEmojiSmile } from 'react-icons/bs';

export default function MessageInput({ conversationId }) {
  const [text, setText] = useState('');
  const [sending, setSending] = useState(false);
  const { sendMessage, notifyTyping } = useChat();
  const lastTypingTime = useRef(0);

  const handleSend = async () => {
    if (!text.trim() || sending) return;
    setSending(true);
    try {
      await sendMessage(conversationId, text.trim());
      setText('');
    } catch (err) {
      console.error('Send failed:', err);
    } finally {
      setSending(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleTextChange = (e) => {
    setText(e.target.value);
    const now = Date.now();
    if (now - lastTypingTime.current > 2000) {
      notifyTyping(conversationId);
      lastTypingTime.current = now;
    }
  };

  return (
    <div className="message-input-container">
      <button className="icon-btn">
        <BsEmojiSmile />
      </button>
      <div className="message-input-wrapper">
        <input
          id="message-input"
          type="text"
          placeholder="Type a message"
          value={text}
          onChange={handleTextChange}
          onKeyDown={handleKeyDown}
          autoComplete="off"
        />
      </div>
      <button
        id="send-message-btn"
        className="send-btn"
        onClick={handleSend}
        disabled={!text.trim() || sending}
      >
        <BiSend />
      </button>
    </div>
  );
}
