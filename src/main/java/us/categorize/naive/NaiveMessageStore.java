package us.categorize.naive;

import us.categorize.api.MessageStore;
import us.categorize.model.Message;

public class NaiveMessageStore implements MessageStore {

	@Override
	public Message createMessage(Message message) {
		return message;
	}

	@Override
	public Message[] tagSearch(String[] tags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message readMessage(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message[] readMessageThread(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteMessage(long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean tagMessage(long id, String[] tags) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addMessageTag(long id, String tag) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeMessageTag(long id, String tag) {
		// TODO Auto-generated method stub
		return false;
	}

}
