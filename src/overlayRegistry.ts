import * as React from 'react';

type OverlayListener = () => void;

const overlayContent = new Map<string, React.ReactNode>();
const overlayListeners = new Map<string, Set<OverlayListener>>();

function emitOverlayChange(id: string) {
  const listeners = overlayListeners.get(id);
  if (!listeners) {
    return;
  }
  listeners.forEach((listener) => listener());
}

export function setOverlayContent(id: string, content: React.ReactNode) {
  overlayContent.set(id, content);
  emitOverlayChange(id);
}

export function clearOverlayContent(id: string) {
  overlayContent.delete(id);
  emitOverlayChange(id);
}

export function getOverlayContent(id: string) {
  return overlayContent.get(id) ?? null;
}

export function subscribeToOverlay(id: string, listener: OverlayListener) {
  const listeners = overlayListeners.get(id) ?? new Set<OverlayListener>();
  listeners.add(listener);
  overlayListeners.set(id, listeners);

  return () => {
    const currentListeners = overlayListeners.get(id);
    if (!currentListeners) {
      return;
    }

    currentListeners.delete(listener);
    if (currentListeners.size === 0) {
      overlayListeners.delete(id);
    }
  };
}
