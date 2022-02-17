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

	private static final String host = "localhost";
	private static final Integer port = 5432;
	private static final String database = "test";
	private static final String table = "reproducer";
	private static final String user = "postgres";
	private static final String password = "";

	private static final Integer nbEntities = 65536;

	private Single<PgPool> createPool() {
		return SingleHelper.toSingle(handler -> {

			PgConnectOptions connectOptions = new PgConnectOptions()
				.setPort(MainVerticle.port)
				.setHost(MainVerticle.host)
				.setDatabase(MainVerticle.database)
				.setUser(MainVerticle.user)
				.setPassword(MainVerticle.password);

			PoolOptions poolOptions = new PoolOptions();

			PgPool pool = PgPool.pool(vertx, connectOptions, poolOptions);
			handler.handle(Future.succeededFuture(pool));
		});
	}

	private Completable runTest(PgPool pool) {
		Tuple tuple = Tuple.tuple();
		String valuesString = IntStream.range(0, MainVerticle.nbEntities)
			.mapToObj(index -> {
				tuple.addString(String.format("entity_%d", index));
				return String.format("($%d)", tuple.size());
			})
			.collect(Collectors.joining(", "));
		String query = String.format("INSERT into %s (name) VALUES %s", MainVerticle.table, valuesString);
		return pool.preparedQuery(query)
			.rxExecute(tuple)
			.ignoreElement();
	}

	@Override
	public Completable rxStart() {
		return createPool()
			.flatMapCompletable(this::runTest);
	}
}
