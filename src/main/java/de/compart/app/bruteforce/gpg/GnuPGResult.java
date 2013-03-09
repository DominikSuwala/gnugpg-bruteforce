package de.compart.app.bruteforce.gpg;

/**
 *
 * User: torsten
 * Date: 2012/12
 * Time: 22:57
 *
 */
public interface GnuPGResult {
	//===============================  VARIABLES ====================================//
	//=============================  PUBLIC METHODS =================================//
	boolean isSuccessful();

	String getValue();

	//=============================  INNER CLASSES ==================================//
}