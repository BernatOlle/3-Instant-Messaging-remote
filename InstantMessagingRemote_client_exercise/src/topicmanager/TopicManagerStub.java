package topicmanager;

import apiREST.apiREST_TopicManager;
import util.Subscription_check;
import util.Topic;
import util.Topic_check;
import java.util.List;
import publisher.Publisher;
import publisher.PublisherStub;
import subscriber.Subscriber;
import webSocketService.WebSocketClient;

public class TopicManagerStub implements TopicManager {

    public String user;

    public TopicManagerStub(String user) {
        WebSocketClient.newInstance();
        this.user = user;
    }

    public void close() {
        WebSocketClient.close();
    }

    @Override
    public Publisher addPublisherToTopic(Topic topic) {
        if (apiREST_TopicManager.addPublisherToTopic(topic)) {
            return new PublisherStub(topic);
        }
        return null;
    }

    @Override
    public boolean removePublisherFromTopic(Topic topic) {
        return apiREST_TopicManager.removePublisherFromTopic(topic);
    }

    @Override
    public Topic_check isTopic(Topic topic) {
        return apiREST_TopicManager.isTopic(topic);
    }

    @Override
    public List<Topic> topics() {
        return apiREST_TopicManager.topics();
    }

    @Override
    public Subscription_check subscribe(Topic topic, Subscriber subscriber) { //No utilitzar no_subsciption
        try {
            // Check if the topic exists first (assuming you have a method to validate topic existence)
            Topic_check topicCheck = this.isTopic(topic);

            if (topicCheck.isOpen) {
                // If the topic exists, add the subscriber via WebSocket
                WebSocketClient.addSubscriber(topic, subscriber);
                return new Subscription_check(topic, Subscription_check.Result.OKAY);

            } else {
                return new Subscription_check(topic, Subscription_check.Result.NO_TOPIC);

            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Subscription_check(topic, Subscription_check.Result.NO_TOPIC);
        }
    }

    @Override
    public Subscription_check unsubscribe(Topic topic, Subscriber subscriber) {
        try {
            Topic_check topicCheck = this.isTopic(topic);
            if (topicCheck.isOpen) {
                // If the topic exists, add the subscriber via WebSocket
                WebSocketClient.removeSubscriber(topic);  // Remove subscriber via WebSocket
                return new Subscription_check(topic, Subscription_check.Result.OKAY);

            } else {
                return new Subscription_check(topic, Subscription_check.Result.NO_SUBSCRIPTION);

            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Subscription_check(topic, Subscription_check.Result.NO_SUBSCRIPTION);
        }
    }
}
