import { useState, useEffect } from 'react';
import { userApi } from './api';

// In-memory cache of user profiles: { [userId]: { displayName, avatarUrl, ... } }
const userProfileCache = {};
const pendingRequests = {};
const subscribers = {}; // { [userId]: Set<Function> }

function notifySubscribers(userId) {
  if (subscribers[userId]) {
    subscribers[userId].forEach(callback => callback(userProfileCache[userId]));
  }
}

/**
 * Get a user's display name by their ID.
 * Returns cached value immediately if available, otherwise fetches from API.
 */
export async function getUserProfile(userId) {
  if (!userId) return null;

  if (userProfileCache[userId]) {
    return userProfileCache[userId];
  }

  if (pendingRequests[userId]) {
    return pendingRequests[userId];
  }

  pendingRequests[userId] = userApi
    .getProfile(userId)
    .then((res) => {
      const profile = res.data?.data || {};
      userProfileCache[userId] = profile;
      delete pendingRequests[userId];
      notifySubscribers(userId);
      return profile;
    })
    .catch(() => {
      delete pendingRequests[userId];
      const fallback = { displayName: userId.substring(0, 8), userId };
      userProfileCache[userId] = fallback;
      notifySubscribers(userId);
      return fallback;
    });

  return pendingRequests[userId];
}

/**
 * Pre-fetch profiles for a list of user IDs.
 */
export function prefetchProfiles(userIds) {
  const unique = [...new Set(userIds)].filter((id) => id && !userProfileCache[id]);
  unique.forEach(id => getUserProfile(id));
}

/**
 * Get display name synchronously (returns cache or fallback ID prefix).
 */
export function getDisplayNameSync(userId) {
  if (!userId) return '?';
  if (userProfileCache[userId]) {
    return userProfileCache[userId].displayName || userId.substring(0, 8);
  }
  return userId.substring(0, 8);
}

/**
 * React Hook to get a user profile reactively.
 */
export function useUserProfile(userId) {
  const [profile, setProfile] = useState(() => {
    if (!userId) return null;
    return userProfileCache[userId] || { displayName: userId.substring(0, 8), userId };
  });

  useEffect(() => {
    if (!userId) return;

    // Set initial or cached
    if (userProfileCache[userId]) {
      setProfile(userProfileCache[userId]);
    } else {
      // Fetch if not in cache
      getUserProfile(userId);
    }

    // Subscribe to changes
    if (!subscribers[userId]) {
      subscribers[userId] = new Set();
    }
    const callback = (newProfile) => setProfile(newProfile);
    subscribers[userId].add(callback);

    return () => {
      if (subscribers[userId]) {
        subscribers[userId].delete(callback);
      }
    };
  }, [userId]);

  return profile;
}
