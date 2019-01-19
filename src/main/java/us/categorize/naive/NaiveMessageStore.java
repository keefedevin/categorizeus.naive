package us.categorize.naive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

import us.categorize.api.MessageStore;
import us.categorize.api.UserStore;
import us.categorize.model.Attachment;
import us.categorize.model.Message;
import us.categorize.model.MetaMessage;
import us.categorize.model.Tag;
import us.categorize.model.TagQuery;
import us.categorize.model.User;

public class NaiveMessageStore implements MessageStore {

	private Connection connection;
	private UserStore userStore;
	private String fileBase;
	private int defaultPageOn=0, defaultPageSize=10;
		
	public NaiveMessageStore(Connection connection, 
			UserStore userStore, 
			String fileBase) {
		this.connection = connection;
		this.userStore = userStore;
		if(fileBase!=null) {
			if(fileBase.charAt(fileBase.length()-1)!=File.separatorChar) {
				fileBase = fileBase + File.separatorChar;
			}
		}
		this.fileBase = fileBase;
	}
	
	@Override
	public Message createMessage(Message message) {
     	String insert = "insert into messages(body,title,posted_by, replies_to, root_replies_to) values (?,?,?,?,?)";
		try {
			PreparedStatement stmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
			mapMessageUpdate(message, stmt);
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();
			String key = ""+rs.getLong(1);
			message.setId(key);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return message; 	
	}
	
    private void mapMessageUpdate(Message message, PreparedStatement stmt) throws SQLException
    {
    	stmt.setString(1, message.getBody());
		stmt.setString(2, message.getTitle());
		stmt.setLong(3, Long.parseLong(message.getPostedBy()));
		if(message.getRepliesTo()==null) {
			stmt.setNull(4, Types.BIGINT);
		}else {
			stmt.setLong(4, Long.parseLong(message.getRepliesTo()));			
		}
		if(message.getRootRepliesTo()==null) {
			stmt.setNull(5, Types.BIGINT);
		}else {
			stmt.setLong(5, Long.parseLong(message.getRootRepliesTo()));			
		}
    }
	@Override
	public Message[] tagSearch(TagQuery query) {
		
		Tag[] tags = tagsToObjects(query.getTags());
		Long tagIds[] = new Long[tags.length];
		String questions = "";
		for(int i=0; i<tags.length;i++) {
			tagIds[i] = Long.parseLong(tags[i].getId());
			if(i!=0) questions = questions+",";//TODO obviously gnarly but don't optimize yet
			questions = questions+"?";
		}
	
		String tagSearch = "select messages.* from messages, message_tags "
				+ "where messages.replies_to is null and messages.id = message_tags.message_id ";
		String tagClause = "";
		if(tags.length==0) {
			tagSearch = "select messages.* from messages where messages.replies_to is null ";
		}else {
			tagClause = "and tag_id in ("+questions+") ";
		}
		
		String positionClause = "";
		String sort = "desc";
		if("asc".equals(query.getSort())){
			sort = "asc";
		}
		if(query.getBefore()!=null) {
			positionClause = positionClause + "and messages.id<? ";
		}
		if(query.getAfter()!=null) {
			positionClause = positionClause + "and messages.id>? ";
		}
		String groupByClause = "";
		if(tags.length>0) {
			groupByClause = "group by messages.id having count(*) = ?";
		}
		String orderClause = " order by messages.id " + sort + " limit ? ";
		
		tagSearch = tagSearch + tagClause + positionClause + groupByClause + orderClause;
		System.out.println(tagSearch);
		
		try {
			PreparedStatement stmt = connection.prepareStatement(tagSearch);
			//Array arr = stmt.getConnection().createArrayOf("bigint", tagIds);
			//  Hint: No operator matches the given name and argument types. You might need to add explicit type casts.
			//org.postgresql.util.PSQLException: ERROR: operator does not exist: bigint = bigint[]
			int i = 0;
			for(i=0; i<tags.length;i++) {
				stmt.setLong(i+1, Long.parseLong(tags[i].getId()));
			}
			if(query.getBefore()!=null) {
				stmt.setLong(i+1, Long.parseLong(query.getBefore()));//TODO exception
				i++;
			}
			if(query.getAfter()!=null) {
				stmt.setLong(i+1, Long.parseLong(query.getAfter()));
				i++;
			}
			
			if(tags!=null && tags.length>0) {
				stmt.setInt(i+1, tags.length);
				i++;
			}

			stmt.setInt(i+1, query.getCount());//+1 for 1 based index on paramters in prepared statements

			ResultSet rs = stmt.executeQuery();
			List<Message> messages = new LinkedList<>();
			while(rs.next()) {
				messages.add(mapMessageRow(new Message(), rs));
			}
			Message messageArr[] = messages.toArray(new Message[messages.size()]);
			return messageArr;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public Message[] tagSearch(String[] tagStrings, Integer pageOn, Integer pageSize) {
		if(pageOn==null) pageOn = defaultPageOn;
		if(pageSize==null) pageSize = defaultPageSize;
		
		Tag[] tags = tagsToObjects(tagStrings);
		Long tagIds[] = new Long[tags.length];
		String questions = "";
		for(int i=0; i<tags.length;i++) {
			tagIds[i] = Long.parseLong(tags[i].getId());
			if(i!=0) questions = questions+",";//TODO obviously gnarly but don't optimize yet
			questions = questions+"?";
		}
	
		String tagSearch = "select messages.* from messages, message_tags where messages.replies_to is null and messages.id = message_tags.message_id and tag_id in ("+questions+") group by messages.id having count(*) = ?";
		if(tags.length==0) {
			tagSearch = "select messages.* from messages where messages.replies_to is null";
		}
		tagSearch += " order by messages.id desc limit ? offset ?";
		
		try {
			PreparedStatement stmt = connection.prepareStatement(tagSearch);
			//Array arr = stmt.getConnection().createArrayOf("bigint", tagIds);
			//  Hint: No operator matches the given name and argument types. You might need to add explicit type casts.
			//org.postgresql.util.PSQLException: ERROR: operator does not exist: bigint = bigint[]
			int i = 0;
			for(i=0; i<tags.length;i++) {
				stmt.setLong(i+1, Long.parseLong(tags[i].getId()));
			}
			if(i>0) {
				stmt.setInt(i+1, tags.length);
				i++;
			}
			stmt.setInt(i+1, pageSize);//+1 for 1 based index on paramters in prepared statements
			stmt.setInt(i+2, pageOn * pageSize);
			ResultSet rs = stmt.executeQuery();
			List<Message> messages = new LinkedList<>();
			while(rs.next()) {
				messages.add(mapMessageRow(new Message(), rs));
			}
			Message messageArr[] = messages.toArray(new Message[messages.size()]);
			return messageArr;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public MetaMessage[] tagSearchFull(TagQuery query) {
		Message[] messages = tagSearch(query);
		MetaMessage[] fullMessages = new MetaMessage[messages.length];
		for(int i=0; i<messages.length;i++) {
			fullMessages[i] = readMessageMetadata(messages[i]);
		}
		return fullMessages;
	}

	private MetaMessage readMessageMetadata(Message message) {
		MetaMessage meta = new MetaMessage();
		meta.setMessage(message);
		meta.setPostedBy(userStore.getUser(message.getPostedBy()));
		meta.setTags(getTags(message));
		meta.setAttachments(readAttachments(message));
		return meta;
	}
	
	private String[] getTags(Message message) {
		String getTags = "select * from message_tags, tags where message_tags.tag_id = tags.id and message_id = ?";
		try {
			PreparedStatement stmt = connection.prepareStatement(getTags);
			stmt.setLong(1, Long.parseLong(message.getId()));
			List<String> tags = new LinkedList<>();
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				tags.add(rs.getString("tag"));
			}
			return tags.toArray(new String[tags.size()]);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String[] {};		
	}

	@Override
	public Message readMessage(String id) {
        try{
            String findMessage = "select * from messages where id=?";
    		PreparedStatement stmt = connection.prepareStatement(findMessage);
    		stmt.setLong(1, Long.parseLong(id));
    		ResultSet rs = stmt.executeQuery();
    		if(rs.next()){
    		    return mapMessageRow(new Message(), rs);
    		}else{
    		    return null;
    		}
    		/*
    		//continuing to be extremely inefficient, let's do ANOTHER query
    		//curious if doing another query vs doing a join above would lead to more efficient results
    		String findTags = "select * from message_tags, tags where message_tags.tag_id=tags.id AND message_id = ?";
    		PreparedStatement findTagsStmt = connection.prepareStatement(findTags);
    		findTagsStmt.setLong(1, message.getId());
    		rs = findTagsStmt.executeQuery();
    		while(rs.next()){
    			Tag tag = new Tag(rs.getLong("id"), rs.getString("tag"));
    			message.getTags().add(tag);
    		}
    		if(!read(message.getPostedBy())){
    			System.out.println("Message Poster Not Found in Database " + message.getPostedBy());
    		}
    		return true;*/
        }catch(SQLException sqe){
            sqe.printStackTrace();
        }
        return null;
	}
	
    private Message mapMessageRow(Message message, ResultSet rs) throws SQLException {
    	message.setId(rs.getLong("id")+"");
		message.setBody(rs.getString("body"));
		message.setTitle(rs.getString("title"));
		message.setPostedBy(rs.getLong("posted_by")+"");
		message.setRepliesTo(rs.getLong("replies_to")+"");
		message.setRootRepliesTo(rs.getLong("root_replies_to")+"");
		return message;
	}

	@Override
	public MetaMessage[] readMessageThread(String id) {
        try{
            String findMessage = "select * from messages where root_replies_to=?";
    		PreparedStatement stmt = connection.prepareStatement(findMessage);
    		stmt.setLong(1,Long.parseLong(id));
    		ResultSet rs = stmt.executeQuery();
    		List<MetaMessage> thread = new LinkedList<MetaMessage>();
    		while(rs.next()){
    		    thread.add(readMessageMetadata(mapMessageRow(new Message(), rs)));
    		}
    		MetaMessage[] threadArr = thread.toArray(new MetaMessage[thread.size()]);
    		return threadArr;
        }catch(SQLException sqe){
            sqe.printStackTrace();
        }
        return null;
	}

	@Override
	public boolean deleteMessage(String id) {
		String deleteMessage = "delete from messages where message_id = ?";
		String deleteTags = "delete from message_tags where message_id=?";
		try {
			PreparedStatement stmt = connection.prepareStatement(deleteMessage);
			stmt.setLong(1, Long.parseLong(id));
			stmt.executeUpdate();
			stmt = connection.prepareStatement(deleteTags);
			stmt.setLong(1, Long.parseLong(id));
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
	}

	@Override
	public boolean tagMessage(String id, String[] tagStrings, User user) {
		String clearOldTags = "delete from message_tags where message_id=?";
		PreparedStatement stmt;
		try {
			stmt = connection.prepareStatement(clearOldTags);
			stmt.setLong(1, Long.parseLong(id));
			stmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Tag[] tags = tagsToObjects(tagStrings);
		for(Tag t : tags) {
			tagMessage(id, t, user);
		}
		return true;
	}

	@Override
	public boolean addMessageTag(String id, String tag, User user) {
		return tagMessage(id, tagFor(tag), user);
	}
	private boolean tagMessage(String id, Tag tag, User user) {
		String tagStatement = "insert into message_tags(message_id, tag_id, user_id) values (?,?,?)";
		try {
			PreparedStatement stmt = connection.prepareStatement(tagStatement);
			stmt.setLong(1, Long.parseLong(id));
			stmt.setLong(2, Long.parseLong(tag.getId()));
			stmt.setLong(3, Long.parseLong(user.getId()));
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			if(e.getMessage().contains("duplicate key")) {
				return true;
			}
			// TODO Auto-generated catch block, this is particularly important because of unique constraint violations
			e.printStackTrace();
		}

		return false;
	}
	@Override
	public boolean removeMessageTag(String id, String tag, User user) {
		String deleteTag = "delete from message_tags where message_id=? and tag_id=?";
		Tag tagO = tagFor(tag);
		try {
			PreparedStatement stmt = connection.prepareStatement(deleteTag);
			stmt.setLong(1, Long.parseLong(id));
			stmt.setLong(2, Long.parseLong(tagO.getId()));
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
    public Tag tagFor(String tag){
    	Tag tagObj = new Tag();
    	tagObj.setTag(tag);
    	
    	readOrCreateTag(tagObj);
    	return tagObj;
    }
    
    public boolean readTag(Tag tag){
        try{
    		String findTag = "select * from tags where tag=?";
    		PreparedStatement stmt = connection.prepareStatement(findTag);
    		stmt.setString(1, tag.getTag());
    		ResultSet rs = stmt.executeQuery();
    		if(rs.next()){
    			tag.setId(rs.getLong("id")+"");
    			return true;
    		}
        }catch(SQLException sqe){
            sqe.printStackTrace();
        }
        return false;
    }
    public boolean readOrCreateTag(Tag tag){
        if(!readTag(tag)){
            try{
         		PreparedStatement insertStatement = connection.prepareStatement("insert into tags(tag) values(?)", Statement.RETURN_GENERATED_KEYS);
    			insertStatement.setString(1, tag.getTag());
    			insertStatement.executeUpdate();//this is synchronous, right?
    			ResultSet rs = insertStatement.getGeneratedKeys();
    			rs.next();
    			String key = ""+rs.getLong(1);
    			tag.setId(key);
    		    return true;
            }catch(SQLException sqe){
                sqe.printStackTrace();
            }
        }
        return false;
        
    }
	private Tag[] tagsToObjects(String tags[]) {
		Tag tag[] = new Tag[tags.length];
		for(int i=0; i<tags.length;i++) {
			tag[i] = tagFor(tags[i]);
		}
		return tag;
	}

	@Override
	public Attachment createAttachment(Attachment attachment, InputStream inputStream) {
		attachment = writeAttachment(attachment);
		String uploadLocation = fileBase+attachment.getId()+attachment.getExtension();
		writeToFile(inputStream, uploadLocation);
		return attachment;
	}
	@Override
	public boolean associateAttachment(Message message, Attachment attachment) {
		String associateAttachment = "insert into message_attachments(message_id, attachment_id) values (?,?)";
		PreparedStatement stmt;
		try {
			stmt = connection.prepareStatement(associateAttachment);
			//this makes no sense if the ids are not specified
			stmt.setLong(1, Long.parseLong(message.getId()));				
			stmt.setLong(2, Long.parseLong(attachment.getId()));
			stmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	private Attachment writeAttachment(Attachment attachment) {
		String sqlWriteAttachment = "insert into attachments(filename, length, extension) values (?,?,?)";
		try {
			PreparedStatement stmt = connection.prepareStatement(sqlWriteAttachment,Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, attachment.getFilename());
			if(attachment.getLength()!=null) {
				stmt.setLong(2, attachment.getLength());				
			}else {
				stmt.setNull(2, Types.BIGINT);
			}
			stmt.setString(3, attachment.getExtension());
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();
			String key = ""+rs.getLong(1);
			attachment.setId(key);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return attachment;
	}
	@Override
	public Attachment updateAttachment(Attachment attachment) {
		String sqlWriteAttachment = "update attachments set filename=?, length=?, extension=? where id=?";
		try {
			PreparedStatement stmt = connection.prepareStatement(sqlWriteAttachment,Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, attachment.getFilename());
			if(attachment.getLength()!=null) {
				stmt.setLong(2, attachment.getLength());				
			}else {
				stmt.setNull(2, Types.BIGINT);
			}
			stmt.setString(3, attachment.getExtension());
			stmt.setLong(4, Long.parseLong(attachment.getId()));
			stmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return attachment;
	}
	private void writeToFile(InputStream uploadedInputStream,
			String uploadedFileLocation) {

			try {
				OutputStream out = new FileOutputStream(new File(
						uploadedFileLocation));
				int read = 0;
				byte[] bytes = new byte[1024];
				while ((read = uploadedInputStream.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	@Override
	public List<Attachment> readAttachments(Message message) {
		String sqlGetAttachment = "select * from attachments, message_attachments where attachments.id = message_attachments.attachment_id and message_attachments.message_id = ?";
		try {
			PreparedStatement stmt = connection.prepareStatement(sqlGetAttachment);
			stmt.setLong(1, Long.parseLong(message.getId()));
			ResultSet rs = stmt.executeQuery();
			List<Attachment> attachments = new LinkedList<Attachment>();
			while(rs!=null && rs.next()) {
				Attachment attachment = new Attachment();
				mapAttachmentRow(rs, attachment);
				attachments.add(attachment);
			}
			return attachments;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void mapAttachmentRow(ResultSet rs, Attachment attachment) throws SQLException {
		attachment.setFilename(rs.getString("filename"));
		attachment.setLength(rs.getLong("length"));
		attachment.setId(""+rs.getLong("id"));
		attachment.setExtension(rs.getString("extension"));
	}

	public int getDefaultPageOn() {
		return defaultPageOn;
	}

	public void setDefaultPageOn(int defaultPageOn) {
		this.defaultPageOn = defaultPageOn;
	}

	public int getDefaultPageSize() {
		return defaultPageSize;
	}

	public void setDefaultPageSize(int defaultPageSize) {
		this.defaultPageSize = defaultPageSize;
	}

	@Override
	public boolean signAttachment(Attachment attachment, String signature) {
		String signAttachment = "insert into attachment_signatures(attachment_id, signature) values (?,?)";
		try {
			PreparedStatement stmt = connection.prepareStatement(signAttachment);
			stmt.setLong(1, Long.parseLong(attachment.getId()));
			stmt.setString(2, signature);
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public Attachment findSignedAttachment(String signature) {
		String findAttachment = "select * from attachments, attachment_signatures where id = attachment_id and signature = ?";
		try {
			PreparedStatement stmt = connection.prepareStatement(findAttachment);
			stmt.setString(1, signature);
			ResultSet rs = stmt.executeQuery();
			if(rs!=null && rs.next()) {
				Attachment attachment = new Attachment();
				mapAttachmentRow(rs, attachment);
				return attachment;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Attachment[] findAssociatedAttachments(Attachment attachment) {
		String findAssociatedAttachments = "select * from message_attachments where attachment_id = ?";
		try {
			PreparedStatement stmt = connection.prepareStatement(findAssociatedAttachments);
			stmt.setLong(1, Long.parseLong(attachment.getId()));
			List<Long> messageIds = new LinkedList<Long>();
			ResultSet rs = stmt.executeQuery();
			while(rs!=null && rs.next()) {
				messageIds.add(rs.getLong("message_id"));
			}
			if(messageIds.size() > 0 ) {
				Message dummy = new Message();
				dummy.setId(messageIds.get(0)+"");
				List<Attachment> attached = readAttachments(dummy);
				return attached.toArray(new Attachment[attached.size()]);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Attachment[] {attachment};
	}
}
