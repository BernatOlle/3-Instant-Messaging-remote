package webSocketService;

import apiREST.Cons;
import com.google.gson.Gson;
import util.Message;
import util.Topic;
import util.Subscription_close;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import subscriber.Subscriber;
import util.Subscription_request;

@ClientEndpoint
public class WebSocketClient {

  static Map<Topic, Subscriber> subscriberMap;
  static Session session;

  public static void newInstance() {
    subscriberMap = new HashMap<Topic, Subscriber>();
    try {
      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      session = container.connectToServer(WebSocketClient.class,
        URI.create(Cons.SERVER_WEBSOCKET));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void close() {
    try {
      session.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static synchronized void addSubscriber(Topic topic, Subscriber subscriber) {
    try {
        if (!subscriberMap.containsKey(topic)) {
            // Create a subscription request with Type.ADD
            Subscription_request request = new Subscription_request(topic, Subscription_request.Type.ADD);
            String jsonRequest = new Gson().toJson(request);
            session.getBasicRemote().sendText(jsonRequest); // Send the JSON request to the server
            
            subscriberMap.put(topic, subscriber);
            System.out.println("Sent ADD subscription request for topic: " + topic.name);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
  }

  public static synchronized void removeSubscriber(Topic topic) {
    try {
        if (subscriberMap.containsKey(topic)) {           
            // Create a subscription request with Type.REMOVE
            Subscription_request request = new Subscription_request(topic, Subscription_request.Type.REMOVE);
            String jsonRequest = new Gson().toJson(request);
            session.getBasicRemote().sendText(jsonRequest); // Send the JSON request to the server

            subscriberMap.remove(topic);
            System.out.println("Sent REMOVE subscription request for topic: " + topic.name);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
  }

  // Handle incoming WebSocket messages
  @OnMessage
  public void onMessage(String json) {
    try { 
      // Intentamos deserializar el mensaje como un Subscription_close 
      Gson gson = new Gson(); 
      Subscription_close subs_close = gson.fromJson(json, Subscription_close.class); 
       
      // Si existe una causa de cierre de suscripción 
      if (subs_close != null && subs_close.cause != null) { 
        // Procesamos el cierre de la suscripción (sin métodos adicionales) 
        if (subs_close.cause == Subscription_close.Cause.PUBLISHER) { 
            Message message = gson.fromJson(json, Message.class); 
            Subscriber subscriber = subscriberMap.get(message.topic);
            subscriber.onClose(subs_close);
        } else if (subs_close.cause == Subscription_close.Cause.SUBSCRIBER) { 
          System.out.println("The subscriber closed the subscription for topic: " + subs_close.topic.name); 
        } 
        subscriberMap.remove(subs_close.topic); // Eliminamos el suscriptor del mapa 
      } else { 
        // Si no es un Subscription_close, procesamos un mensaje ordinario 
        Message message = gson.fromJson(json, Message.class); 
        if (message == null || message.content == null || message.content.trim().isEmpty()) {
            System.out.println("Error: Cannot publish an empty message.");
        } else { 
          // Enviamos el mensaje a los suscriptores correspondientes 
          Subscriber subscriber = subscriberMap.get(message.topic); 
          if (subscriber != null) { 
            subscriber.onMessage(message); 
          } else { 
            System.out.println("No subscriber found for topic: " + message.topic.name); 
          } 
        } 
      } 
    } catch (Exception e) { 
      e.printStackTrace(); 
    } 
  } 
}
