package us.categorize.naive;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import us.categorize.api.UserStore;
import us.categorize.model.User;

public class NaiveUserStore implements UserStore {

	private Connection connection;
		
	public NaiveUserStore(Connection connection) {
		this.connection = connection;
	}
	
	@Override
	public User getPrincipal(String sessionKey) {
		String findSessionUser = "select * from user_sessions where session_uuid=?";
		try {
			PreparedStatement stmt = connection.prepareStatement(findSessionUser);
			stmt.setString(1,  sessionKey);
			ResultSet rs = stmt.executeQuery();
			if(rs!=null && rs.next()){
				return find(rs.getLong("user_id"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private User find(long id)  {
		String findUser = "select * from users where id=?";
		PreparedStatement stmt;
		try {
			stmt = connection.prepareStatement(findUser);
			stmt.setLong(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs!=null && rs.next()){
				User user = new User();
				user.setId(rs.getLong("id"));
				user.setEmail(rs.getString("email"));
				user.setPasshash(rs.getString("passhash"));
				user.setUsername(rs.getString("username"));
				return user;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean registerUser(User user) {
		String insertUser = "insert into users(username, passhash, email) values (?,?,?)";
		PreparedStatement stmt;
		try {
			stmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, user.getUsername());
			stmt.setString(2, user.getPasshash());
			stmt.setString(3, user.getEmail());
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();
			long newId = rs.getLong(1);
			user.setId(newId);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	

	@Override
	public boolean establishUserSession(User user, String sessionKey) {
		String createUserSession = "insert into user_sessions(session_uuid, user_id) values (?,?)";
		String findUser = "select * from users where username=? and passhash=?";
		try{
			PreparedStatement stmt = connection.prepareStatement(findUser);
			stmt.setString(1, user.getUsername());
			stmt.setString(2, user.getPasshash());
			ResultSet rs = stmt.executeQuery();
			if(!(rs!=null && rs.next())){
				return false;		
			}
			stmt = connection.prepareStatement(createUserSession);
			stmt.setString(1,sessionKey);
			stmt.setLong(2, user.getId());
			stmt.executeUpdate();
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean destroySessionUser(String sessionUUID) {
		String deleteUserSession = "delete from user_sessions where session_uuid = ?";
		try {
			PreparedStatement stmt = connection.prepareStatement(deleteUserSession);
			stmt.setString(1, sessionUUID);
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
