package io.macgyver.mongodb;

import io.macgyver.core.CollaboratorRegistrationCallback;
import io.macgyver.core.ConfigurationException;
import io.macgyver.core.ServiceFactoryBean;

import java.net.UnknownHostException;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MongoDBFactoryBean extends ServiceFactoryBean<MongoClient> {

	public MongoDBFactoryBean() {
		super(MongoClient.class);
	}


	public MongoClient createObject() {
		try {
			return new ExtendedMongoClient(new MongoClientURI(
					injectCredentials(getProperties().getProperty("uri"), getProperties().getProperty("username"), getProperties().getProperty("password"))));
		} catch (UnknownHostException e) {
			throw new ConfigurationException(e);
		}
	}

	public DB createDBConnection() {
		ExtendedMongoClient c = (ExtendedMongoClient) createObject();
		return c.getDB(c.getDatabaseName());
		
	}
	
	
	@Override
	public CollaboratorRegistrationCallback getCollaboratorRegistrationCallback() {
		CollaboratorRegistrationCallback cb = new CollaboratorRegistrationCallback() {

			@Override
			public void registerCollaborators(RegistrationDetail detail) {

				
				String name = detail.getPrimaryBeanName() + "DB";
				logger.info("registering {}");
				BeanDefinition bd = BeanDefinitionBuilder
						.rootBeanDefinition(DBFactoryShim.class).addConstructorArgReference(detail.getPrimaryBeanName()).getBeanDefinition();
				
				detail.getRegistry().registerBeanDefinition(name, bd);

			}
		};

		return cb;
	}


	public class ExtendedMongoClient extends MongoClient {

		MongoClientURI mongoClientUri;

		public ExtendedMongoClient(MongoClientURI uri)
				throws UnknownHostException {
			super(uri);
			this.mongoClientUri = uri;

		}

		public String getDatabaseName() {
			return mongoClientUri.getDatabase();
		}
	}





	public DB getDB() {
		return createDBConnection();
	}
	/*
	 * public DB getDB(String name) { ExtendedMongoClient c =
	 * (ExtendedMongoClient) get(name); String dbName =
	 * c.mongoClientUri.getDatabase(); if (dbName==null) { throw new
	 * ConfigurationException("db name not specified in URL"); } return
	 * c.getDB(dbName);
	 * 
	 * }
	 */

	public String injectCredentials(String uri, String username, String password) {
		Preconditions.checkNotNull(uri);
		Preconditions.checkArgument(uri.startsWith("mongodb://"),
				"mongo uri must start with mongodb://");
		if (!Strings.isNullOrEmpty(username)
				&& !Strings.isNullOrEmpty(password)) {
			if (uri.contains("@")) {
				throw new IllegalArgumentException(
						"mongo uri must not contain credentials if you are injecting them");
			}
			uri = uri.replace("mongodb://", "mongodb://" + username + ":"
					+ password + "@");
			return uri;

		} else {
			return uri;
		}
	}
	
	public static class DBFactoryShim implements FactoryBean<DB>  {

		MongoClient client;
		
		DBFactoryShim(MongoClient client) {
			this.client = client;
		}
		@Override
		public DB getObject() throws Exception {
			
			
			ExtendedMongoClient extendedMongoClient = (ExtendedMongoClient) client;
			return extendedMongoClient.getDB(extendedMongoClient.getDatabaseName());
		}

		@Override
		public Class<?> getObjectType() {
			return DB.class;
		}

		@Override
		public boolean isSingleton() {
			return true;
		}


	}
}