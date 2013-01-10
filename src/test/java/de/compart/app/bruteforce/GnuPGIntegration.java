package de.compart.app.bruteforce;

import static org.fest.assertions.api.Assertions.assertThat;

import de.compart.common.test.ConcurrentTestRule;
import de.compart.common.test.ConcurrentTestRule.Concurrent;
import de.compart.common.test.RepeatableTestRule;
import de.compart.common.test.RepeatableTestRule.Repeatable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.IOException;

/**
 *
 * User: torsten
 * Date: 2012/12
 * Time: 16:11
 *
 */
public class GnuPGIntegration {
	//============================== CLASS VARIABLES ================================//
	//=============================== CLASS METHODS =================================//
	//===============================  VARIABLES ====================================//
	@Rule
	public final TestRule ruleChain = RuleChain.outerRule( new RepeatableTestRule() ).around( new ConcurrentTestRule() );

	//==============================  CONSTRUCTORS ==================================//
	//=============================  PUBLIC METHODS =================================//

	@Repeatable(10000)
	@Test
	public void repeatable_Encrypt_And_Decrypt() throws IOException {
		new GnuPGUnitTest().decrypt_with_passPhrase_and_valid_text();
	}

	@Concurrent(10000)
	@Test
	public void parallel_Encrypt_And_Decrypt() throws IOException {
		this.repeatable_Encrypt_And_Decrypt();
	}

	@Repeatable( 10000 )
	@Test
	public void repeatable_Error_While_Encrypt_And_Decrypt() throws IOException {
		new GnuPGUnitTest().decrypt_with_invalid_passPhrase_and_valid_text();
	}

	@Concurrent(10000)
	@Test
	public void parallel_Error_while_Encrypt_And_Decrypt() throws IOException {
		this.repeatable_Error_While_Encrypt_And_Decrypt();
	}

	//======================  PROTECTED/PACKAGE METHODS =============================//
	//============================  PRIVATE METHODS =================================//
	//=============================  INNER CLASSES ==================================//
}