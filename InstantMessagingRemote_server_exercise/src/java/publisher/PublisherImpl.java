package publisher;

import util.Subscription_close;
import util.Message;
import util.Topic;
import java.util.ArrayList;
import java.util.List;
import subscriber.Subscriber;
import subscriber.SubscriberImpl;
import javax.websocket.Session;

public class PublisherImpl implements Publisher {

  private List<Subscriber> subscriberSet;
  private int numPublishers;
  private Topic topic;

  public PublisherImpl(Topic topic) {
    subscriberSet = new ArrayList<Subscriber>();
    numPublishers = 1;
    this.topic = topic;
  }

  @Override
  public void incPublishers() {
    numPublishers++;
  }

  @Override
  public int decPublishers() {
    if (numPublishers > 0) {
        numPublishers--;
    }
    return numPublishers;  }

  @Override
  public void attachSubscriber(Subscriber subscriber) {
    if (!subscriberSet.contains(subscriber)) {
        subscriberSet.add(subscriber);
    }  
  }

  @Override
  public boolean detachSubscriber(Subscriber subscriber) {
      subscriber.onClose(new Subscription_close(this.topic,Subscription_close.Cause.SUBSCRIBER));
    return subscriberSet.remove(subscriber);
  }

  @Override
  public void detachAllSubscribers() {
      for (Subscriber subscriber : subscriberSet) {
        subscriber.onClose(new Subscription_close(this.topic,Subscription_close.Cause.PUBLISHER));
    }
    subscriberSet.clear();
  }

  @Override
  public void publish(Message message) {
    for (Subscriber subscriber : subscriberSet) {
        System.out.print(message);
        subscriber.onMessage(message);
    }
  }
  
  public Subscriber subscriber(Session session) {
    for (Subscriber subscriber : subscriberSet) {
      SubscriberImpl subscriberImpl = (SubscriberImpl) subscriber;
      if (subscriberImpl.session == session) {
        return subscriber;
      }
    }
    return null;
  }
}
