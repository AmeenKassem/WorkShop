console.log("🚀 Notification JS loaded");

// Make sure the function is properly exposed to the global scope
window.initNotificationSocket = function(username, baseWebSocketUrl) {
    console.log("Initializing notification socket for user: " + username);
    
    // Check if socket already exists and is open
    if (window.notificationSocket && window.notificationSocket.readyState !== WebSocket.CLOSED) {
        console.log("Socket already exists and is open");
        return;
    }
    
    window.socketManuallyClosed = false;
    
    try {
        // Make sure to use the correct WebSocket URL
        const socket = new WebSocket(`${baseWebSocketUrl}/notifications?username=${encodeURIComponent(username)}`);
        window.notificationSocket = socket;
        
        socket.onopen = function() {
            console.log("🟢 WebSocket connection established for user: " + username);
        };
        
        socket.onmessage = function(event) {
            console.log("📩 Received message: ", event.data);
            const msg = event.data;
            
            // Find the NotificationView component and call its method
            const notificationHandler = document.querySelector('notification-handler');
            
            if (notificationHandler && notificationHandler.$server) {
                console.log("Found NotificationView component, calling receiveNotification");
                notificationHandler.$server.receiveNotification(msg);
            } else {
                console.log("NotificationView component not found, checking Vaadin clients");
                
                // Fallback: Look through all Vaadin clients
                if (window.Vaadin && window.Vaadin.Flow && window.Vaadin.Flow.clients) {
                    let clientFound = false;
                    
                    for (const clientKey in window.Vaadin.Flow.clients) {
                        const client = window.Vaadin.Flow.clients[clientKey];
                        console.log("Checking client:", clientKey);
                        
                        // Try to find the component in the registry
                        if (client.getByNodeId) {
                            const elements = document.querySelectorAll('*');
                            for (let i = 0; i < elements.length; i++) {
                                const element = elements[i];
                                if (element.tagName.toLowerCase() === 'notification-handler') {
                                    console.log("Found notification handler element");
                                    if (element.$server && typeof element.$server.receiveNotification === 'function') {
                                        console.log("Calling receiveNotification on found element");
                                        element.$server.receiveNotification(msg);
                                        clientFound = true;
                                        break;
                                    }   
                                }
                            }
                        }
                        
                        // Direct client call as last resort
                        if (!clientFound && client.$server && typeof client.$server.receiveNotification === 'function') {
                            console.log("Calling receiveNotification on client directly");
                            client.$server.receiveNotification(msg);
                            clientFound = true;
                        }
                    }
                    
                    if (!clientFound) {
                        console.error("No valid Vaadin client found with receiveNotification method");
                        fallbackBrowserNotification(msg);
                    }
                } else {
                    console.error("Vaadin Flow clients not found");
                    fallbackBrowserNotification(msg);
                }
            }
        };
        
        socket.onerror = function(error) {
            console.error("⚠️ WebSocket error:", error);
        };
        
        socket.onclose = function(event) {
            console.warn("🔴 WebSocket closed. Code:", event.code, "Reason:", event.reason);
            // Only auto-reconnect if NOT manually closed
            if (!window.socketManuallyClosed) {
                console.warn("Attempting to reconnect in 5 seconds...");
                setTimeout(() => window.initNotificationSocket(username), 5000);
            }
        };
    } catch (error) {
        console.error("Error initializing WebSocket:", error);
    }
};

function fallbackBrowserNotification(msg) {
    // If we can't find the Vaadin client, at least show a browser notification
    if (Notification.permission === "granted") {
        new Notification("New Notification", {
            body: msg,
            icon: "/favicon.ico"
        });
    } else {
        console.log("Fallback: Browser notification not permitted");
    }
}



window.closeNotificationSocket = function() {
    console.log("Manually closing notification socket");
    window.socketManuallyClosed = true;
    if (window.notificationSocket) {
        window.notificationSocket.close();
        window.notificationSocket = null;
    }
};