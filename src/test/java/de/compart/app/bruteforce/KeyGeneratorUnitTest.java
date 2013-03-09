package de.compart.app.bruteforce;

import static org.fest.assertions.api.Assertions.assertThat;

import de.compart.app.bruteforce.key.Builders;
import de.compart.app.bruteforce.key.KeyGenerator;
import de.compart.common.event.Event;
import de.compart.common.event.Event.EventType;
import de.compart.common.event.EventListener;
import org.fest.assertions.core.Condition;
import org.junit.Test;

/**
 *
 * User: torsten
 * Date: 2012/11
 * Time: 22:54
 *
 */
public class KeyGeneratorUnitTest {
	//============================== CLASS VARIABLES ================================//
	private static final String MOCK_ALPHABET = "abcdef0123456";
	//=============================== CLASS METHODS =================================//
	//===============================  VARIABLES ====================================//
	//==============================  CONSTRUCTORS ==================================//
	//=============================  PUBLIC METHODS =================================//

	@Test
	public void firstKeyMeansTheFirstLetter() {
		final KeyGenerator keyGenerator = Builders.createKeyGeneratorBuilder().withAlphabet( MOCK_ALPHABET ).build();
		assertThat( keyGenerator.generate() ).isEqualTo( "a" );
	}

	@Test
	public void secondKeyMeansTheFirstLetter() {
		final KeyGenerator keyGenerator = Builders.createKeyGeneratorBuilder().withAlphabet( MOCK_ALPHABET ).build();
		for ( int i = 0; i <= 10; i++ ) {
			assertThat( keyGenerator.generate() ).isNotNull().isNotEmpty();
		}
		assertThat( keyGenerator.generate() ).isEqualTo( "5" );
	}

	@Test
	public void moreThanTheLengthOfAlphabetMeansAFlippingChar() {
		final KeyGenerator keyGenerator = Builders.createKeyGeneratorBuilder().withAlphabet( MOCK_ALPHABET ).build();
		for ( int i = 0; i < MOCK_ALPHABET.length(); i++ ) {
			assertThat( keyGenerator.generate() ).isNotNull().isNotEmpty();
		}
		assertThat( keyGenerator.generate() ).isEqualTo( "aa" );
	}

	@Test
	public void threeLoopsResultInFourTimesFirstLetter() {
		final KeyGenerator keyGenerator = Builders.createKeyGeneratorBuilder().withAlphabet( MOCK_ALPHABET ).withMinimumLength( 3 ).build();
		for ( int i = 0; i < Math.pow( MOCK_ALPHABET.length(), 3 ); i++ ) {
			assertThat( keyGenerator.generate() ).isNotNull().isNotEmpty();
		}
		assertThat( keyGenerator.generate() ).isEqualTo( "aaaa" );
	}

	@Test
	public void maximumLengthResultsInAFinishedGenerator() {
		final KeyGenerator keyGenerator = Builders.createKeyGeneratorBuilder().withAlphabet( MOCK_ALPHABET ).withMinimumLength( 3 ).withMaximumLength( 3 ).build();

		final Event[] events = new Event[]{null};
		EventListener myFinishListener = new EventListener() {
			@Override
			public void listen( final Event event ) {
				events[ 0 ] = event;
			}
		};
		keyGenerator.addFinishListener( myFinishListener );
		for ( int i = 0; i < Math.pow( MOCK_ALPHABET.length(), 3 ) - 1; i++ ) {
			assertThat( keyGenerator.generate() ).isNotNull().isNotEmpty();
		}
		assertThat( keyGenerator.generate() ).isEqualTo( "666" );
		assertThat( events ).doesNotContainNull().hasSize( 1 ).has( new Condition<Event[]>() {
			@Override
			public boolean matches( final Event[] value ) {
				return value[ 0 ].getSource() == keyGenerator && value[ 0 ].getType() == EventType.EXIT;
			}
		} );
	}

	@Test
	public void prefixWithMinimumLengthResultsInALongerString() {
		final String MOCK_PREFIX = "myPrefix";
		final String ALPHABET = "123";
		final KeyGenerator keyGenerator = Builders.createKeyGeneratorBuilder().withAlphabet( ALPHABET ).withMinimumLength( 3 ).withPrefix( MOCK_PREFIX ).build();
		for ( int i = 0; i < Math.pow( ALPHABET.length(), 3 ); i++ ) {
			assertThat( keyGenerator.generate() ).isNotNull().startsWith( MOCK_PREFIX ).hasSize( MOCK_PREFIX.length() + ALPHABET.length() );
		}
	}

	@Test
	public void suffixWithMinimumLengthResultsInALongerString() {
		final String MOCK_SUFFIX = "mySuffix";
		final String ALPHABET = "123";
		final KeyGenerator keyGenerator = Builders.createKeyGeneratorBuilder().withAlphabet( ALPHABET ).withMinimumLength( 3 ).withSuffix( MOCK_SUFFIX ).build();
		for ( int i = 0; i < Math.pow( ALPHABET.length(), 3 ); i++ ) {
			assertThat( keyGenerator.generate() ).isNotNull().endsWith( MOCK_SUFFIX ).hasSize( MOCK_SUFFIX.length() + ALPHABET.length() );
		}
	}

	//======================  PROTECTED/PACKAGE METHODS =============================//
	//============================  PRIVATE METHODS =================================//
	//=============================  INNER CLASSES ==================================//
}