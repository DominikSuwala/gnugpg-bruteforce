package de.compart.app.bruteforce.key;

/**
 *
 * This interface is meant to abstract a possible cache for entries.
 * This interface supports the assignment of a last (cached) entry.
 *
 * User: torsten
 * Date: 2012/11
 * Time: 21:07
 *
 */
public interface Cache<T> {
	//===============================  VARIABLES ====================================//
	//=============================  PUBLIC METHODS =================================//

	/**
	 *
	 * @return
	 */
	T getCachedEntry();

	//=============================  INNER CLASSES ==================================//

}