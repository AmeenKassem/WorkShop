window.initNotificationSocket = function (username) {
    if (window.notificationSocket && window.notificationSocket.readyState !== WebSocket.CLOSED) {
        return;
    }

    window.socketManuallyClosed = false; // reset manual flag when re-connecting

    const socket = new WebSocket("ws://localhost:8080/notifications?username=" + username);
    window.notificationSocket = socket;

    socket.onmessage = function (event) {
        const msg = event.data;

        if (window.Vaadin && window.Vaadin.Flow && window.Vaadin.Flow.clients) {
            for (const clientKey in window.Vaadin.Flow.clients) {
                const client = window.Vaadin.Flow.clients[clientKey];
                if (client.$server && client.$server.receiveNotification) {
                    client.$server.receiveNotification(msg);
                }
            }
        }
    };

    socket.onclose = () => {
        console.warn("WebSocket closed.");
        // only auto-reconnect if NOT manually closed
        if (!window.socketManuallyClosed) {
            console.warn("Reconnecting in 5s...");
            setTimeout(() => window.initNotificationSocket(username), 5000);
        }
    };
};

// 👇 Call this when user logs out
window.closeNotificationSocket = function () {
    window.socketManuallyClosed = true;
    if (window.notificationSocket) {
        window.notificationSocket.close();
        window.notificationSocket = null;
    }
};
