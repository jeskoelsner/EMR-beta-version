package org.zlwima.emurgency.backend;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import org.zlwima.emurgency.backend.model.User;

public class Backend {
	protected static final Logger logger = Logger.getLogger( Backend.class.getName() );

	MongoOperations MONGO;

	private static Backend INSTANCE = null;

	public static Backend getInstance() {
		if( INSTANCE == null ) {
			INSTANCE = new Backend();
			logger.info( "creating Backend.class as Singleton..." );
		}
		return INSTANCE;
	}

	protected Backend() {
		super();
		try {
			MONGO = new MongoTemplate( new Mongo( "127.0.0.1", 27017 ), "yourdb" );
			logger.info( "creating mongoTemplate..." );
		} catch( UnknownHostException ex ) {
			logger.info( ex.getMessage() );
		} catch( MongoException ex ) {
			logger.info( ex.getMessage() );
		}
	}

	public void deleteUserList() {
		MONGO.dropCollection( User.class );
	}

	public List<User> getAllUsers() {
		return MONGO.findAll( User.class );
	}

	public List<User> getUsersRegisteredOnGCM() {
		Query query = new Query( Criteria.where( Shared.USER_REGISTRATIONID ).ne( "" ) );
		return MONGO.find( query, User.class );
	}
	
	public List<User> getLogedInUsers() {
		Query query = new Query( Criteria.where( Shared.USER_LOGINSTATUS ).is( true ) );
		List<User> users = MONGO.find( query, User.class );
		for( User u : users ) {
			logger.log( Level.INFO, "--> logedInUser: {0}",	new Object[] { u.getEmail() } );
		}
		return MONGO.find( query, User.class );
	}	

	public List<User> getUsersByField( String findByField, String findByValue ) {
		Query query = new Query( Criteria.where( findByField ).is( findByValue ) );
		return MONGO.find( query, User.class );
	}

	public void updateSingleUserField( String findByField, String findByValue, String updateField, Object updateValue ) {
		logger.log( Level.INFO, "updateSingleUserField... findByField: {0} , findByValue: {1} , updateField: {2} , updateValue: {3}",
				new Object[] { findByField, findByValue, updateField, updateValue } );
		Query query = new Query( Criteria.where( findByField ).is( findByValue ) );
		Update update = new Update().set( updateField, updateValue );
		MONGO.updateFirst( query, update, User.class );
	}

	public void addUser( User user ) {
		MONGO.insert( user );
	}

	public void saveUser( User user ) {
		MONGO.save( user );
		logger.log( Level.INFO, "saved user: {0}", new Object[] { findUserByEmail( user.getEmail() ) } );
	}

	public User findUserByClientId( String clientId ) {
		Query query = new Query( Criteria.where( Shared.USER_CLIENTID ).is( clientId ) );
		return MONGO.findOne( query, User.class );
	}

	public User findUserByEmail( String email ) {
		Query query = new Query( Criteria.where( Shared.USER_EMAIL ).is( email ) );
		return MONGO.findOne( query, User.class );
	}

	public Boolean findUserByEmailAndPassword( String email, String password ) {
		Query query = new Query( Criteria.where( Shared.USER_EMAIL ).is( email ).and( Shared.USER_PASSWORD ).is( password ) );
		return MONGO.findOne( query, User.class ) != null;
	}

	public static Date setDate( int year, int month, int day ) throws ParseException {
		String date = year + "/" + month + "/" + day;
		java.util.Date utilDate = null;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat( "yyyy/MM/dd" );
			utilDate = formatter.parse( date );
		} catch( ParseException e ) {
			logger.info( e.toString() );
		}
		return utilDate;
	}

}
