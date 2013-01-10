package de.compart.app.bruteforce;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

import java.util.regex.Pattern;

/**
 *
 * User: torsten
 * Date: 2013/01
 * Time: 22:53
 *
 */
public class VersionUnitTest {
	//============================== CLASS VARIABLES ================================//
	//=============================== CLASS METHODS =================================//
	//===============================  VARIABLES ====================================//
	//==============================  CONSTRUCTORS ==================================//
	//=============================  PUBLIC METHODS =================================//

	@Test
	public void getPropertyOfVersion() {
		assertThat( Version.getVersionString() ).startsWith( "0.0.1" ).matches( Pattern.compile("\\d\\.\\d\\.\\d-\\d{4}") );
	}

	//======================  PROTECTED/PACKAGE METHODS =============================//
	//============================  PRIVATE METHODS =================================//
	//=============================  INNER CLASSES ==================================//
}