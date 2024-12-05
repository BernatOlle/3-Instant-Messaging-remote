/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package subscriber;

import util.Subscription_close;
import util.Message;
import util.Topic;
import java.util.Map;
import javax.swing.JTextArea;
import main.SwingClient;
import publisher.Publisher;

/**
 *
 * @author juanluis
 */
public class SubscriberImpl implements Subscriber {

    private JTextArea messages_TextArea;
    private JTextArea my_subscriptions_TextArea;
    private Map<Topic, Subscriber> my_subscriptions;
    public Map<Topic, Publisher> my_publishers; // Store publishers for multiple topics

    public SubscriberImpl(SwingClient clientSwing) {
        this.messages_TextArea = clientSwing.messages_TextArea;
        this.my_subscriptions_TextArea = clientSwing.my_subscriptions_TextArea;
        this.my_subscriptions = clientSwing.my_subscriptions;
        this.my_publishers = clientSwing.my_publishers;
    }

    public void onClose(Subscription_close subs_close) {
        if (subs_close.cause == Subscription_close.Cause.PUBLISHER) {
            messages_TextArea.append("Topic: " + subs_close.topic.name
                    + " has been closed, no publishers left on that topic.\n");
        }
        my_subscriptions.remove(subs_close.topic);
        my_subscriptions_TextArea.setText("");
        for (Topic topic : my_subscriptions.keySet()) {
            my_subscriptions_TextArea.append(topic.name + "\n");
        }
    }

    public void onMessage(Message message) {
        for (Topic t : my_publishers.keySet()) {
            if (!t.name.contentEquals(message.topic.name)) {
                System.out.print(t.name + " "+ message.topic.name);
                messages_TextArea.append(message.topic.name + ": " + message.content + "\n");
            }
        }
    }
}
