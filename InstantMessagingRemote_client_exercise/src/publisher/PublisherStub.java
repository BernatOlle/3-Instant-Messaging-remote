package publisher;

import apiREST.apiREST_Publisher;
import util.Message;
import util.Topic;

public class PublisherStub implements Publisher {
  
  private final Topic topic;
  private final apiREST_Publisher restPublisherAPI;

  // Constructor initializes the topic and API dependency
  public PublisherStub(Topic topic) {
    this.topic = topic;
    this.restPublisherAPI = new apiREST_Publisher(); // Assuming the REST client class is available
  }

  @Override
  public boolean publish(Message message) {
    try {
      // Ensure the message has the correct topic
      if (!topic.equals(message.topic)) {
        throw new IllegalArgumentException("Message topic does not match Publisher's topic.");
      }

      // Use the API client to publish the message to the REST service
      if(!restPublisherAPI.publish(message)){
          return false;
      }

      System.out.println("Message published to topic: " + topic.name);
      return true;
    } catch (Exception e) {
      System.err.println("Failed to publish message: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }
}
