package publisher;

import util.Message;

public interface Publisher {
  
    public boolean publish(Message message);
}
