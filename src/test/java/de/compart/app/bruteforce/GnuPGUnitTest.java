package de.compart.app.bruteforce;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
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
	private static final Logger LOG = LoggerFactory.getLogger(GnuPGUnitTest.class);

	private static final String MOCK_TEXT = "my text to be encrypted and decrypted.";

	//=============================== CLASS METHODS =================================//
	//===============================  VARIABLES ====================================//
	//==============================  CONSTRUCTORS ==================================//
	//=============================  PUBLIC METHODS =================================//

	@Test
	public void encrypt_and_decrypt_text() throws IOException {
		LOG.info("Starting encryption.");
		final GnuPGResult encryptionGnuPGResult = GnuPG.encrypt( MOCK_TEXT ).withPassPhrase( "abcdef" ).execute();
		LOG.info( "Finished encryption: " + encryptionGnuPGResult.getValue() );

		final String decryptedText = encryptionGnuPGResult.getValue();
		LOG.info("Starting decryption.");

		final GnuPGResult decryptionGnuPGResult = GnuPG.decrypt( decryptedText ).withPassPhrase( "abcdef" ).execute();

		assertThat( decryptionGnuPGResult.getValue() ).isEqualTo( MOCK_TEXT );
		LOG.info("Finished decryption: "+decryptionGnuPGResult.getValue());
	}


	//======================  PROTECTED/PACKAGE METHODS =============================//
	//============================  PRIVATE METHODS =================================//
	//=============================  INNER CLASSES ==================================//
}