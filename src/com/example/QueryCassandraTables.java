package com.example;

import java.util.HashMap;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

public class QueryCassandraTables {
	private static SparkSession spark = null;
	
	public static void registerView(String keyspace, String table) {
		
		Map<String, String> options = new HashMap<String, String>();
		options.put("keyspace", keyspace);
		options.put("table", table);
		
		spark
			.read()
			.format("org.apache.spark.sql.cassandra")
			.options(options)
			.load()
			.createOrReplaceTempView(table);
	}
	
	
	public static void main(String[] args) {

		
		SparkConf conf = new SparkConf()
				.setAppName(QueryCassandraTables.class.getName())
				.setIfMissing("spark.master", "local[*]");
		
		spark = SparkSession.builder().config(conf).getOrCreate();
		
		registerView("demo", "movies");
		registerView("demo", "ratings");
		
		Dataset<Row> df = spark.sql("select t1.movieid, t1.title, avg(t2.rating) avg_rating"
				+ " from movies t1 join ratings t2 on t1.movieId = t2.movieid " 
				+ "group by t1.movieid, t1.title having count(1) > 100 order by avg_rating desc");
		
		df.show();
		
		df.coalesce(1).write().format("csv").mode(SaveMode.Overwrite).save("/tmp/movie-rating");
		
	}

}
