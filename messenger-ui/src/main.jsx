import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

const root = document.getElementById('root');

try {
  createRoot(root).render(
    <StrictMode>
      <App />
    </StrictMode>,
  )
} catch (err) {
  console.error('React mount failed:', err);
  root.innerHTML = `<div style="color:red;padding:20px"><h1>App failed to load</h1><pre>${err.message}\n${err.stack}</pre></div>`;
}
