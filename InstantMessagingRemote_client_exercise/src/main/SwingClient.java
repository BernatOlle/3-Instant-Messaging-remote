package main;

import util.Message;
import util.Subscription_check;
import util.Topic;
import subscriber.SubscriberImpl;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import publisher.Publisher;
import subscriber.Subscriber;
import topicmanager.TopicManager;
import util.Subscription_close;
import util.Topic_check;

public class SwingClient {

  TopicManager topicManager;
  public Map<Topic, Subscriber> my_subscriptions;
  Publisher publisher;
  Topic publisherTopic;                // Currently selected topic for publishing

  JFrame frame;
  JTextArea topic_list_TextArea;
  public JTextArea messages_TextArea;
  public JTextArea info_TextArea;
  public JTextArea my_subscriptions_TextArea;
  JTextArea publisher_TextArea;
  JTextField argument_TextField;

  public SwingClient(TopicManager topicManager) {
    this.topicManager = topicManager;
    my_subscriptions = new HashMap<Topic, Subscriber>();
    publisher = null;
    publisherTopic = null;
  }


public void createAndShowGUI() {

    frame = new JFrame("Publisher/Subscriber Demo");
    frame.setSize(300, 300);
    frame.addWindowListener(new CloseWindowHandler());

    topic_list_TextArea = new JTextArea(5, 10);
    my_subscriptions_TextArea = new JTextArea(5, 10);
    publisher_TextArea = new JTextArea(1, 10);
    argument_TextField = new JTextField(20);

    // Separate TextAreas for Messages and Information
    messages_TextArea = new JTextArea(10, 20);
    messages_TextArea.setEditable(false);
    messages_TextArea.setLineWrap(true);
    messages_TextArea.setWrapStyleWord(true);

    info_TextArea = new JTextArea(10, 20);
    info_TextArea.setEditable(false);
    info_TextArea.setLineWrap(true);
    info_TextArea.setWrapStyleWord(true);

    JButton show_topics_button = new JButton("Show Topics");
    JButton new_publisher_button = new JButton("New Publisher");
    JButton new_subscriber_button = new JButton("New Subscriber");
    JButton to_unsubscribe_button = new JButton("Unsubscribe");
    JButton to_post_an_event_button = new JButton("Post Event");
    JButton forward_message_button = new JButton("Forward Message");
    JButton to_close_the_app = new JButton("Close App");
    JButton clear_info_button = new JButton("Clear Info"); // Clear Information
    JButton clear_messages_button = new JButton("Clear Messages"); // Clear Messages

    show_topics_button.addActionListener(new showTopicsHandler());
    new_publisher_button.addActionListener(new newPublisherHandler());
    new_subscriber_button.addActionListener(new newSubscriberHandler());
    to_unsubscribe_button.addActionListener(new UnsubscribeHandler());
    to_post_an_event_button.addActionListener(new postEventHandler());
    forward_message_button.addActionListener(new ForwardMessageHandler());
    to_close_the_app.addActionListener(new CloseAppHandler());

    clear_info_button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            info_TextArea.setText("");
        }
    });

    clear_messages_button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            messages_TextArea.setText("");
        }
    });

    JPanel buttonsPannel = new JPanel(new FlowLayout());
    buttonsPannel.add(show_topics_button);
    buttonsPannel.add(new_publisher_button);
    buttonsPannel.add(new_subscriber_button);
    buttonsPannel.add(to_unsubscribe_button);
    buttonsPannel.add(to_post_an_event_button);
    buttonsPannel.add(forward_message_button);
    buttonsPannel.add(to_close_the_app);

    JPanel argumentP = new JPanel(new FlowLayout());
    argumentP.add(new JLabel("Write content to set a new_publisher / new_subscriber / unsubscribe / post_event:"));
    argumentP.add(argument_TextField);

    JPanel topicsP = new JPanel();
    topicsP.setLayout(new BoxLayout(topicsP, BoxLayout.PAGE_AXIS));
    topicsP.add(new JLabel("Topics:"));
    topicsP.add(new JScrollPane(topic_list_TextArea));
    topicsP.add(new JLabel("My Subscriptions:"));
    topicsP.add(new JScrollPane(my_subscriptions_TextArea));
    topicsP.add(new JLabel("I'm Publisher of topics:"));
    topicsP.add(publisher_TextArea);
    
    // Information Panel
    JPanel infoPanel = new JPanel();
    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS));
    infoPanel.add(new JLabel("Information:"));
    infoPanel.add(new JScrollPane(info_TextArea));
    infoPanel.add(clear_info_button); // Add Clear Info Button

    // Messages Panel
    JPanel messagesPanel = new JPanel();
    messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.PAGE_AXIS));
    messagesPanel.add(new JLabel("Messages:"));
    messagesPanel.add(new JScrollPane(messages_TextArea));
    messagesPanel.add(clear_messages_button); // Add Clear Messages Button

    // SplitPane for Columns
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, infoPanel, messagesPanel);
    splitPane.setDividerLocation(150); // Initial division point
    splitPane.setResizeWeight(0.3); // Allocate 30% of space to the info panel

    Container mainPanel = frame.getContentPane();
    mainPanel.add(buttonsPannel, BorderLayout.PAGE_START);
    mainPanel.add(splitPane, BorderLayout.CENTER); // Use SplitPane for the center
    mainPanel.add(argumentP, BorderLayout.PAGE_END);
    mainPanel.add(topicsP, BorderLayout.LINE_START);

    frame.pack();
    frame.setVisible(true);
}

  class showTopicsHandler implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        // Obtén la lista de tópicos (sin el cast)
        List<Topic> topicsList = topicManager.topics();  

        // Ahora puedes trabajar con 'topicsList' como una lista
        for (Topic topic : topicsList) {
            System.out.println(topic);
        }

        // Limpiar el área de texto antes de agregar los nuevos datos
        topic_list_TextArea.setText("");
        
        // Añadir los nombres de los tópicos al área de texto
        for (Topic topic : topicsList) {
            topic_list_TextArea.append(topic.name + "\n");
        }
    }
}

  class newPublisherHandler implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        String topicName = argument_TextField.getText().trim();

        if (topicName.isEmpty()) {
            info_TextArea.append("Error: Topic name cannot be empty.\n");
            return;
        }

        if (publisherTopic != null && topicName.equals(publisherTopic.name)){
            info_TextArea.append("You are already publisher of: " + publisherTopic.name + "\n");      
        } else{
            topicManager.removePublisherFromTopic(publisherTopic);
            publisherTopic = new Topic(topicName);

            // Create a publisher for the topic
            publisher = topicManager.addPublisherToTopic(publisherTopic);
            publisher_TextArea.setText(publisherTopic.name);
            info_TextArea.append("New publisher discussing about " + publisherTopic.name + "\n");            
        }
    }
}

  class newSubscriberHandler implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        String topicName = argument_TextField.getText().trim();
        Topic topic = new Topic(topicName);
        Subscriber subscriber = new SubscriberImpl(SwingClient.this);
        Subscription_check result = topicManager.subscribe(topic, subscriber);
        System.out.print(my_subscriptions);
        if (result.result == Subscription_check.Result.OKAY) {
            if (my_subscriptions.containsKey(topic)) {
                info_TextArea.append("Already subcribed to topic: " + topicName + "\n");
               
            }else{
                
                 my_subscriptions.put(topic, subscriber); 
                my_subscriptions_TextArea.setText("");
                for (Topic t : my_subscriptions.keySet()) {
                    my_subscriptions_TextArea.append(t.name + "\n");
                }
                info_TextArea.append("Successfully subscribed to topic: " + topicName + "\n");
            }
        } else if (result.result == Subscription_check.Result.NO_TOPIC) {
            info_TextArea.append("Error: Topic '" + topicName + "' does not exist.\n");
        } else if (result.result == Subscription_check.Result.NO_SUBSCRIPTION){
            info_TextArea.append("Server Error: Not able to complete Subscription.\n");
        }
      
    }
  }

  class UnsubscribeHandler implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      
      String topicName = argument_TextField.getText().trim();
      Topic topic = new Topic(topicName);
       Subscriber subscriber = my_subscriptions.get(topic);
       if (subscriber == null) {
            info_TextArea.append("Error: You are not subscribed to topic '" + topicName + "'.\n");
            return;
       }

        Subscription_check result = topicManager.unsubscribe(topic, subscriber);

        if (result.result == Subscription_check.Result.OKAY) {
            my_subscriptions.remove(topic); 
            Subscription_close subs_close = new Subscription_close(topic,Subscription_close.Cause.SUBSCRIBER);
            subscriber.onClose(subs_close);
            my_subscriptions_TextArea.setText("");
            for (Topic t : my_subscriptions.keySet()) {
                my_subscriptions_TextArea.append(t.name + "\n");
            }

            info_TextArea.append("Successfully unsubscribed from topic: " + topicName + "\n");
        } else if (result.result == Subscription_check.Result.NO_TOPIC) {
            info_TextArea.append("Error: Topic '" + topicName + "' does not exist.\n");
        }
      
    }
  }

  class postEventHandler implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        if (publisherTopic == null) {
            info_TextArea.append("Error: No topic selected for publishing.\n");
            return;
        }

        if (publisher == null) {
            info_TextArea.append("Error: No publisher found for the selected topic.\n");
            return;
        }

        String content = argument_TextField.getText();
        Message message = new Message(publisherTopic, content);
        publisher.publish(message);

        messages_TextArea.append("Message posted to topic '" + publisherTopic.name + "': " + content + "\n");
    }
  }
  
  class ForwardMessageHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
        if (publisherTopic == null) {
            info_TextArea.append("Error: No topic selected for publishing.\n");
            return;
        }

        // Verify that there is a message selected
        String selectedMessage = messages_TextArea.getSelectedText();
        if (selectedMessage == null || selectedMessage.trim().isEmpty()) {
            info_TextArea.append("Error: No message selected for forwarding.\n");
            return;
        }

        if (publisher == null) {
            info_TextArea.append("Error: No publisher found for the selected topic.\n");
            return;
        }

        // Create a message and publish it
        Message message = new Message(publisherTopic, selectedMessage);
        publisher.publish(message);

        messages_TextArea.append("Message forwarded to topic '" + publisherTopic.name + "': " + selectedMessage + "\n");
    }
    }


  class CloseAppHandler implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        
      for (Topic t : my_subscriptions.keySet()) {
                Subscriber subscriber = my_subscriptions.get(t);
                topicManager.unsubscribe(t, subscriber);
                
            }
      topicManager.removePublisherFromTopic(publisherTopic);
      System.out.println("all users closed");
      System.exit(0);
    }
  }

  class CloseWindowHandler implements WindowListener {

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
      
      //...
      
      System.out.println("one user closed");
    }
  }
}