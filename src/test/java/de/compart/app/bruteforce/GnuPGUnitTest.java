package de.compart.app.bruteforce;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

import de.compart.common.command.Command.ExecutionException;
import org.fest.assertions.core.Condition;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *
 * User: torsten
 * Date: 2012/11
 * Time: 23:11
 *
 */
public class GnuPGUnitTest {
	//============================== CLASS VARIABLES ================================//
	private static final Logger LOG = LoggerFactory.getLogger( GnuPGUnitTest.class );

	private static final String MOCK_TEXT = "my text to be encrypted and decrypted.";
	private static final String ENCRYPTED_TEXT = "-----BEGIN PGP MESSAGE-----\n" +
														 "Version: GnuPG v1.4.12 (Darwin)\n" +
														 "\n" +
														 "jA0EAwMCXnTNg9T4r+lgyVNowpqN3ymR3yc/1yDeCbeAqJEKBaljzq2N2xvK76lZ\n" +
														 "P3zjD42hodFgTsz5MxLZuXSq3cy+WxRcgBYVyrjMVpfPTcQS5WNb5ToMZn56ssvs\n" +
														 "ryDQyQ==\n" +
														 "=RNP7\n" +
														 "-----END PGP MESSAGE-----";

	private static final String PASS_PHRASE = "abc";

	//=============================== CLASS METHODS =================================//
	//===============================  VARIABLES ====================================//

	public final TestRule ruleChain = RuleChain.outerRule( new Timeout( 250 ) );

	//==============================  CONSTRUCTORS ==================================//
	//=============================  PUBLIC METHODS =================================//

	@Test( expected = NullPointerException.class )
	public void encrypt_with_nullValue() throws IOException {
		GnuPG.encrypt( null );
	}

	@Test( expected = IllegalArgumentException.class )
	public void encrypt_without_passPhrase() throws IOException {
		GnuPG.encrypt( MOCK_TEXT ).execute();
	}

	@Test
	public void encrypt_with_passPhrase() throws IOException {
		final GnuPGResult encryptionResult = GnuPG.encrypt( MOCK_TEXT ).withPassPhrase( PASS_PHRASE ).execute();
		assertThat( encryptionResult.isSuccessful() ).isTrue();
		assertThat( encryptionResult.getValue() ).contains( "BEGIN PGP MESSAGE" ).contains( "END PGP MESSAGE" );
	}

	@Test( expected = NullPointerException.class )
	public void decrypt_with_nullValue() throws IOException {
		GnuPG.decrypt( null );
	}

	@Test( expected = IllegalArgumentException.class )
	public void decrypt_without_passPhrase() throws IOException {
		GnuPG.decrypt( MOCK_TEXT ).execute();
	}

	@Test
	public void decrypt_with_passPhrase_and_invalid_text() throws IOException {
		try {
			GnuPG.decrypt( MOCK_TEXT ).withPassPhrase( PASS_PHRASE ).execute();
			fail( "This point of code should not have been reached. See the assertion." );
		} catch ( ExecutionException ex ) {
			/*
			 * Expecting some error like
			 * 'gpg: Keine gültigen OpenPGP-Daten gefunden.'
			 * 'gpg: decrypt_message failed: eof'
			 */
			assertThat( ex.getCause().getMessage() ).contains( "OpenPGP" ).contains( "gpg: decrypt_message failed: eof" );
		}
	}

	@Ignore
	@Test
	public void decrypt_with_invalid_passPhrase_and_valid_text() throws IOException {
		try {
			GnuPG.decrypt( ENCRYPTED_TEXT ).withPassPhrase( "abcdef" ).execute();
			fail( "This point of code should not have been reached. See the assertion." );
		} catch ( ExecutionException ex ) {
			/*
			 * Expecting some error like
			 * 'gpg: Keine gültigen OpenPGP-Daten gefunden.'
			 * 'gpg: decrypt_message failed: eof'
			 */
			assertThat( ex.getCause().getMessage() ).is( new Condition<String>() {
				@Override
				public boolean matches( final String value ) {
					return value.contains( "gpg" ) &&
								   value.contains( "Entschlüsselung fehlgeschlagen: Falscher Schlüssel" ) ||
								   value.contains( "Decryption failed: wrong secret key used" );
				}
			} );
		}
	}

	@Ignore
	@Test
	public void decrypt_with_passPhrase_and_valid_text() throws IOException {
		final GnuPGResult decryptionResult = GnuPG.decrypt( ENCRYPTED_TEXT ).withPassPhrase( PASS_PHRASE ).execute();
		assertThat( decryptionResult.isSuccessful() ).isTrue();
		assertThat( decryptionResult.getValue() ).isEqualTo( MOCK_TEXT );
	}

	//======================  PROTECTED/PACKAGE METHODS =============================//
	//============================  PRIVATE METHODS =================================//
	//=============================  INNER CLASSES ==================================//
}