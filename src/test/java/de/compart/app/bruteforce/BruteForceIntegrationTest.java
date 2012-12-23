package de.compart.app.bruteforce;

import static org.fest.assertions.api.Assertions.assertThat;

import de.compart.common.test.ConcurrentTestRule;
import de.compart.common.test.ConcurrentTestRule.Concurrent;
import de.compart.common.test.RepeatableTestRule;
import de.compart.common.test.RepeatableTestRule.Repeatable;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.*;

/**
 *
 * User: torsten
 * Date: 2012/11
 * Time: 22:09
 *
 */
public class BruteForceIntegrationTest {
	//============================== CLASS VARIABLES ================================//
	private static final Logger LOG = LoggerFactory.getLogger( BruteForceIntegrationTest.class );

	@NotNull
	private static String DECRYPTED_TEXT;

	private static final String UNENCRYPTED_TEXT = "meinUnencryptedTextIsReallyLong";

	@NotNull
	private static final String ENCRYPTION_KEY = "aaa";
	private static final int TIMEOUT_LIMIT_IN_MILLIS = 10000;

	@Rule
	public final TestRule timeOut = new Timeout( TIMEOUT_LIMIT_IN_MILLIS );

	@Rule
	public final TestRule concurrentTestRule = new ConcurrentTestRule();

	@Rule
	public final TestRule repeatableTestRule = new RepeatableTestRule();


	//=============================== CLASS METHODS =================================//
	//===============================  VARIABLES ====================================//
	@NotNull
	private final KeyGenerator generator = Builders.createKeyGeneratorBuilder().withAlphabet( "abcdef" ).withMaximumLength( 6 ).build();

	@NotNull
	private GnuPGKeyAccess accessor;

	@NotNull
	private final BlockingQueue<Future<String>> resultQueue = new LinkedBlockingQueue<Future<String>>();

	@NotNull
	private ExecutorCompletionService<String> executorService;


	//==============================  CONSTRUCTORS ==================================//
	//=============================  PUBLIC METHODS =================================//

	@BeforeClass
	public static void createKey() throws IOException {
		final GnuPGResult gnuPGResult = GnuPG.encrypt( UNENCRYPTED_TEXT ).withPassPhrase( ENCRYPTION_KEY ).execute();
		assertThat( gnuPGResult.isSuccessful() ).isTrue();
		DECRYPTED_TEXT = gnuPGResult.getValue();

		LOG.info( "Encrypted Text: " + UNENCRYPTED_TEXT );
		LOG.info( "Decrypted Text: " + DECRYPTED_TEXT );
		org.apache.log4j.Logger.getRootLogger().setLevel( Level.INFO );
	}

	@Before
	public void setUp() {
		executorService = new ExecutorCompletionService<String>( Executors.newCachedThreadPool(), resultQueue );
	}

	@Repeatable(4)
	@Test
	public void decrypt_and_encrypt_text_by_brute_force_repeatable() throws InterruptedException, ExecutionException {
		buildUpTestEnvironment(false);
		assertThat(resultQueue.take().get() ).isNotNull().isNotEmpty();
	}

	@Concurrent(4)
	@Test
	public void decrypt_Encrypted_Text_By_Brute_Force_concurrent() throws InterruptedException, ExecutionException {
		buildUpTestEnvironment(true);
		assertThat( resultQueue.take().get() ).isNotNull().isNotEmpty();
	}

	private void buildUpTestEnvironment(final boolean withSynchronization) {
		final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
		executorService.submit( new Runnable() {
			@Override
			public void run() {
				while ( true ) {
					try {
						final String generatedString = generator.generate();
						if (!queue.offer( generatedString, 1, TimeUnit.SECONDS )) {
							break;
						}
					} catch ( InterruptedException e ) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}, null );
		final GnuPGKeyAccess keyAccess = new GnuPGKeyAccess( DECRYPTED_TEXT, queue );
		if (withSynchronization) {
			keyAccess.synchronize();
		}
		executorService.submit( keyAccess );
	}

	//======================  PROTECTED/PACKAGE METHODS =============================//
	//============================  PRIVATE METHODS =================================//
	//=============================  INNER CLASSES ==================================//
}