package us.categorize.naive;

import us.categorize.api.MessageStore;
import us.categorize.model.Message;

public class NaiveMessageStore implements MessageStore {

	@Override
	public Message createMessage(Message message) {
		message.setInternalId("subclass message");
		return message;
	}

	@Override
	public Message[] tagSearch(String[] tags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message readMessage(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message[] readMessageThread(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteMessage(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean tagMessage(String id, String[] tags) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addMessageTag(String id, String tag) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeMessageTag(String id, String tag) {
		// TODO Auto-generated method stub
		return false;
	}

}
