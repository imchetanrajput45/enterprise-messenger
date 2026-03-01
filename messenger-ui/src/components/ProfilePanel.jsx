import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { userApi } from '../services/api';
import { BiArrowBack, BiLogOut } from 'react-icons/bi';

export default function ProfilePanel({ onClose }) {
  const { logout } = useAuth();
  const [profile, setProfile] = useState(null);
  const [editing, setEditing] = useState(false);
  const [displayName, setDisplayName] = useState('');
  const [bio, setBio] = useState('');
  const username = localStorage.getItem('username') || '?';

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const res = await userApi.getProfile();
      const p = res.data.data;
      setProfile(p);
      setDisplayName(p?.displayName || '');
      setBio(p?.bio || '');
    } catch (_) {
      // Profile may not exist yet
    }
  };

  const handleSave = async () => {
    try {
      await userApi.updateProfile({ displayName, bio });
      setEditing(false);
      loadProfile();
    } catch (err) {
      console.error('Failed to update profile:', err);
    }
  };

  const handleLogout = async () => {
    await logout();
    window.location.href = '/login';
  };

  return (
    <div className="profile-panel">
      <div className="profile-panel-header">
        <button className="icon-btn" onClick={onClose}>
          <BiArrowBack />
        </button>
        <h2>Profile</h2>
      </div>
      <div className="profile-panel-body">
        <div className="profile-avatar-large">
          {username.charAt(0).toUpperCase()}
        </div>

        <div className="profile-field">
          <label>Username</label>
          <p>@{username}</p>
        </div>

        {editing ? (
          <>
            <div className="profile-field">
              <label>Display Name</label>
              <input
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                placeholder="Your display name"
              />
            </div>
            <div className="profile-field">
              <label>About</label>
              <input
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                placeholder="Something about yourself"
              />
            </div>
            <button className="btn-primary" onClick={handleSave} style={{ marginTop: '16px' }}>
              Save
            </button>
          </>
        ) : (
          <>
            <div className="profile-field">
              <label>Display Name</label>
              <p>{profile?.displayName || 'Not set'}</p>
            </div>
            <div className="profile-field">
              <label>About</label>
              <p>{profile?.bio || 'Hey there! I am using Messenger'}</p>
            </div>
            <button
              className="btn-primary"
              onClick={() => setEditing(true)}
              style={{ marginTop: '16px', background: 'var(--bg-tertiary)' }}
            >
              Edit Profile
            </button>
          </>
        )}

        <button className="btn-logout" onClick={handleLogout}>
          <BiLogOut size={20} />
          Log out
        </button>
      </div>
    </div>
  );
}
