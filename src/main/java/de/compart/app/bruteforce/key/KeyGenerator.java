package de.compart.app.bruteforce.key;

import de.compart.common.Generator;
import de.compart.common.event.Event;
import de.compart.common.event.EventListener;
import de.compart.common.event.EventManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * User: torsten
 * Date: 2012/11
 * Time: 23:18
 *
 */
public class KeyGenerator implements Generator<String>, Finishable {
	//============================== CLASS VARIABLES ================================//
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( KeyGenerator.class );

	private final EventManager eventManager = new EventManager( this );

	private final String alphabet;
	private final String prefix;
	private final String suffix;
	private final int minimumLength;
	private final int maximumLength;

	private Cache<char[]> cache = new DefaultCache();

	//=============================== CLASS METHODS =================================//
	//===============================  VARIABLES ====================================//
	//==============================  CONSTRUCTORS ==================================//
	public KeyGenerator( final String alphabet, final String prefix, final String suffix, final int minimumLength, final int maximumLength ) {
		log.debug( "Initialized default instance: {}", KeyGenerator.class.getSimpleName() );
		this.alphabet = alphabet;
		this.prefix = prefix;
		this.suffix = suffix;
		this.minimumLength = ( minimumLength > 0 ? minimumLength : -1 );
		this.maximumLength = ( maximumLength > 0 && maximumLength >= minimumLength ? maximumLength : -1 );
	}

	//=============================  PUBLIC METHODS =================================//
	public KeyGenerator setCache( final Cache<char[]> stringCache ) {
		this.cache = stringCache;
		return this;
	}

	public boolean containsOnly( final char[] array, final char character ) {
		for ( char arrayChar : array ) {
			if ( arrayChar != character ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String generate() {

		final char[] before = cache.getCachedEntry();
		final char firstChar = this.alphabet.charAt( 0 );
		final char lastChar = this.alphabet.charAt( this.alphabet.length() - 1 );


		String resultingString = "";
		if ( before == null ) {
			if ( minimumLength != -1 ) {
				final char[] temporaryArray = new char[ minimumLength ];
				Arrays.fill( temporaryArray, 0, minimumLength, firstChar );
				resultingString = String.valueOf( temporaryArray );
			} else {
				resultingString = String.valueOf( this.alphabet.charAt( 0 ) );
			}
		} else {
			final int length = before.length;
			for ( int i = length - 1; i >= 0; i-- ) {
				final char currentChar = before[ i ];

				if ( currentChar == lastChar ) {
					before[ i ] = firstChar;

					if ( i == 0 ) {
						final char[] extendedResultSet = new char[ length + 1 ];
						extendedResultSet[ length ] = firstChar;
						System.arraycopy( before, 0, extendedResultSet, 0, length );
						resultingString = String.valueOf( extendedResultSet );
						break;
					} else {
						continue;
					}
				}
				final int positionOfCurrentChar = alphabet.indexOf( currentChar );
				before[ i ] = alphabet.charAt( positionOfCurrentChar + 1 );
				resultingString = String.valueOf( before );
				break;
			}
		}

		final char[] resultSet = resultingString.toCharArray();

		if ( resultSet.length == maximumLength && containsOnly( resultSet, lastChar ) ) {
			eventManager.notifyListener( new Event<String>() {
				@Override
				public EventType getType() {
					return EventType.EXIT;
				}

				@Override
				public Object getSource() {
					return KeyGenerator.this;
				}

				@Override
				public String getMessage() {
					return "Maximum number of entries reached: " + String.valueOf( resultSet );
				}
			} );
		}

		cache = new DefaultCache( resultSet );
		return this.addSuffixOrNothing( this.addPrefixOrNothing( resultingString ) );
	}

	private String addSuffixOrNothing( final String resultingString ) {
		return ( this.suffix != null ? resultingString + suffix : resultingString );
	}

	private String addPrefixOrNothing( final String resultingString ) {
		return ( this.prefix != null ? prefix + resultingString : resultingString );
	}

	@Override
	public void addFinishListener( @NotNull final EventListener listener ) {
		this.eventManager.registerListener( listener );
	}

	@Override
	public boolean removeFinishListener( @Nullable final EventListener listener ) {
		return this.eventManager.removeListener( listener );
	}

	//======================  PROTECTED/PACKAGE METHODS =============================//
	//============================  PRIVATE METHODS =================================//
	//=============================  INNER CLASSES ==================================//

	private static class DefaultCache implements Cache<char[]> {

		@NotNull
		private Queue<char[]> cache = new LinkedBlockingQueue<char[]>();

		public DefaultCache() {
			// default constructor is needed
		}

		public DefaultCache( final char[] inputChars ) {
			this.cache.offer( inputChars );
		}

		public Cache<char[]> addCachedEntry( final String cachedEntry ) {
			this.cache.offer( cachedEntry.toCharArray() );
			return this;
		}

		@Override
		public char[] getCachedEntry() {
			return this.cache.poll();
		}

	}
}