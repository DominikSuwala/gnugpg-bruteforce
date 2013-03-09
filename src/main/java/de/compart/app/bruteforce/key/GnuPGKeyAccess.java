package de.compart.app.bruteforce.key;

import de.compart.app.bruteforce.gpg.GnuPG;
import de.compart.app.bruteforce.gpg.GnuPGResult;
import de.compart.common.Generator;
import de.compart.common.command.Command.ExecutionException;
import de.compart.common.event.DefaultEvent;
import de.compart.common.event.Event;
import de.compart.common.event.Event.EventType;
import de.compart.common.event.EventListener;
import de.compart.common.event.EventManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * User: torsten
 * Date: 2012/11
 * Time: 00:01
 *
 */
public class GnuPGKeyAccess implements Generator<String>, Callable<String>, Finishable, EventListener {
	//============================== CLASS VARIABLES ================================//
	private static final Logger LOG = LoggerFactory.getLogger( GnuPGKeyAccess.class );

	private static final EventManager mainEventManager = new EventManager( new Object() );

	private static final AtomicInteger counter = new AtomicInteger( 0 );

	//=============================== CLASS METHODS =================================//
	//===============================  VARIABLES ====================================//
	@NotNull
	private final EventManager eventManager = new EventManager( this );

	@NotNull
	private final BlockingQueue<String> queue;

	@NotNull
	private final String text;

	@NotNull
	private final String name;

	@NotNull
	private volatile boolean close = false;

	//==============================  CONSTRUCTORS ==================================//
	public GnuPGKeyAccess( @NotNull final String text, @NotNull final BlockingQueue<String> inputQueue ) {
		this.queue = inputQueue;
		this.text = text;
		this.name = String.format( "%s_%d", GnuPGKeyAccess.class.getSimpleName(), counter.incrementAndGet() );
		LOG.info( "[{}]: up, and running.", name );
	}

	public GnuPGKeyAccess( @NotNull final File keyFile, @NotNull final BlockingQueue<String> inputQueue ) throws IOException {
		this( loadTextFromFile( keyFile ), inputQueue );
	}

	//=============================  PUBLIC METHODS =================================//
	@Override
	public String call() throws Exception {
		String possibleResult = null;
		while ( !this.close ) {
			possibleResult = this.generate();
			if (possibleResult != null && !possibleResult.isEmpty()) {
				break;
			}
		}
		return possibleResult;
	}

	@Nullable
	@Override
	public String generate() {
		@Nullable final String currentKey;
		try {
			currentKey = this.queue.poll( 1, TimeUnit.SECONDS );
		} catch ( InterruptedException e ) {
			throw new RuntimeException( e );
		}
		if ( currentKey != null ) {
			final GnuPG gnuPG;
			try {
				gnuPG = GnuPG.decrypt( text );
			} catch ( IOException e ) {
				throw new RuntimeException( "While creating the GNUPG process, an error occurred.", e );
			}

			GnuPGResult gnuPGResult = null;
			try {
				LOG.info( "[{}]: Trying key '{}'", name, currentKey );
				gnuPGResult = gnuPG.withPassPhrase( currentKey ).execute();
				if ( gnuPGResult.isSuccessful() && gnuPGResult.getValue() != null && !gnuPGResult.getValue().isEmpty() ) {
					this.eventManager.notifyListener( createInfoEvent( currentKey ) );
					mainEventManager.notifyListener( createCloseEvent( currentKey ) );
					return currentKey;
				}
			} catch ( IOException ex ) {
				this.handleError( gnuPGResult, ex );
			} catch ( ExecutionException ex ) {
				this.handleError( gnuPGResult, ex );
			}
		}
		return null;
	}

	@Override
	public void addFinishListener( @NotNull final EventListener listener ) {
		eventManager.registerListener( listener );
	}

	@Override
	public boolean removeFinishListener( @Nullable final EventListener listener ) {
		return eventManager.removeListener( listener );
	}

	public GnuPGKeyAccess synchronize() {
		mainEventManager.registerListener( this );
		return this;
	}

	@Override
	public void listen( final Event event ) {
		if (event.getType() == EventType.EXIT) {
			LOG.info( "[{}]: Received signal to stop.", this.name );
			this.close = true;
		}
	}

	//======================  PROTECTED/PACKAGE METHODS =============================//
	protected static String loadTextFromFile( final File file ) throws IOException {
		final StringBuilder builder = new StringBuilder();
		final BufferedReader reader = new BufferedReader( new FileReader( file ) );
		String line;
		try {
			while ( ( line = reader.readLine() ) != null ) {
				builder.append( line );
			}
		} finally {
			reader.close();
		}
		return builder.toString();
	}

	//============================  PRIVATE METHODS =================================//
	private void handleError( @Nullable final GnuPGResult gnuPGResult, final Exception ex ) {
		if ( gnuPGResult != null && !gnuPGResult.isSuccessful() ) {
			LOG.error( "[{}]: Unable to receive a valid result of gnuPG executable: {} (EXCEPTION: {})", new Object[]{name, gnuPGResult.getValue(), ex.getClass().getSimpleName()} );
		}
	}

	private Event<String> createInfoEvent( final String currentKey ) {
		return new DefaultEvent<String>( EventType.INFO, this, currentKey );
	}

	private Event<String> createCloseEvent( final String currentKey ) {
		return new DefaultEvent<String>( EventType.EXIT, this, currentKey );
	}

	//=============================  INNER CLASSES ==================================//
}