package aic.bigdata.enrichment;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.List;

import aic.bigdata.database.GraphDatabase;
import aic.bigdata.database.MongoDatabase;
import aic.bigdata.database.SqlDatabase;
import aic.bigdata.server.ServerConfig;

/**
 * Determines topics that the user is interested in and stores them in Neo4J.
 */
public class TopicAnalyzer implements Runnable {

	private MongoDatabase mongodb;
	private GraphDatabase graphDB;
	private SqlDatabase sqldb;

	private boolean running = false;

	/**
	 * Maximum amount of users that are analyzed.
	 */
	private int userLimit = 1500000;

	/**
	 * Analyze only the most recent tweets to keep performance manageable.
	 */
	private int latestTweetsLimit = 100;

	public TopicAnalyzer(ServerConfig config, GraphDatabase graphDB) {
		this.mongodb = new MongoDatabase(config);
		try {
			this.sqldb = new SqlDatabase(config);
		} catch (SQLException e) {
			System.err.println("could not create sqldatabase connection");
			e.printStackTrace();
		}
		this.graphDB = graphDB;
	}

	/**
	 * Analyze tweets and store topics that are seen as interesting in Neo4J db
	 * 
	 * @throws UnknownHostException
	 * @throws SQLException
	 */
	public void analyzeTweets() throws UnknownHostException, SQLException {
		this.running = true;
		long time = System.currentTimeMillis();
		List<String> topics = mongodb.readAllTopicsInLowercase();
		List<Long> userIds = sqldb.getUserIds(userLimit);
		TopicTweetsMiner miner = new TopicTweetsMiner(topics);
		String bigTweet = null;
		for (int i = 0; i < userIds.size() && running; i++) {
			bigTweet = mongodb.readLatestTweetsAsOneString(userIds.get(i), latestTweetsLimit);

			List<String> interests = miner.getInterestedTopics(bigTweet);

			for (int j = 0; j < interests.size(); j++) {
				graphDB.addMentionsRelationship(userIds.get(i), interests.get(j));
			}
		}

		System.out.println("mining done. processing time: " + (System.currentTimeMillis() - time) + "ms for "
				+ userIds.size() + " users.");
	}

	@Override
	public void run() {
		try {
			this.analyzeTweets();
		} catch (UnknownHostException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stopAnalyze() {
		this.running = false;
	}
}
