package io.github.nnkwrik.kirinrpc.registry.listener;

import io.github.nnkwrik.kirinrpc.registry.model.RegisterMeta;

/**
 * @author nnkwrik
 * @date 19/05/24 19:54
 */
public interface NotifyListener {
    void notify(RegisterMeta registerMeta, long sequenceNum, NotifyEvent event);

    enum NotifyEvent {
        CHILD_ADDED,
        CHILD_REMOVED
    }
}
