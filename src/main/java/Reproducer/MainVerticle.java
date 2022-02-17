package Reproducer;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;
import io.vertx.sqlclient.PoolOptions;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainVerticle extends AbstractVerticle {

	/**
	 * Static attributes, change them to configure the connection to your PostgreSQL database
	 */
	private static final String host = "localhost";
	private static final Integer port = 5432;
	private static final String database = "test";
	private static final String table = "reproducer";
	private static final String user = "postgres";
	private static final String password = "";

	private static final Integer nbEntities = 65536;

	/**
	 * Create the connection pool to the PostgreSQL database
	 * 
	 * @return The pool as a Single
	 */
	private Single<PgPool> createPool() {
		return SingleHelper.toSingle(handler -> {

			PgConnectOptions connectOptions = new PgConnectOptions()
				.setPort(MainVerticle.port)
				.setHost(MainVerticle.host)
				.setDatabase(MainVerticle.database)
				.setUser(MainVerticle.user)
				.setPassword(MainVerticle.password);

			PoolOptions poolOptions = new PoolOptions();

			// Create the pool
			PgPool pool = PgPool.pool(vertx, connectOptions, poolOptions);
			handler.handle(Future.succeededFuture(pool));
		});
	}

	/**
	 * Run the test creating MainVerticle.nbEntities
	 * 
	 * @param pool The pool to use
	 * @return The Completable result
	 */
	private Completable runTest(PgPool pool) {
		// The tuple we'll use with the preparedQuery
		Tuple tuple = Tuple.tuple();
		// Create the value string, should looks like ($1), ($2), ($3) etc...
		String valuesString = IntStream.range(0, MainVerticle.nbEntities)
			.mapToObj(index -> {
				tuple.addString(String.format("entity_%d", index));
				return String.format("($%d)", tuple.size());
			})
			.collect(Collectors.joining(", "));
		// The query as a string, we're adding the table name and values here
		String query = String.format("INSERT into %s (name) VALUES %s", MainVerticle.table, valuesString);
		return pool.preparedQuery(query)
			.rxExecute(tuple)
			.ignoreElement();
	}

	/**
	 * Start this verticle
	 * 
	 * @Return The completable
	 */
	@Override
	public Completable rxStart() {
		return createPool()
			.flatMapCompletable(this::runTest);
	}
}
