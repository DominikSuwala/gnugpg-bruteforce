package de.compart.app.bruteforce;

import de.compart.common.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * User: torsten
 * Date: 2012/11
 * Time: 07:51
 *
 */
public interface Finishable {
	//===============================  VARIABLES ====================================//
	//=============================  PUBLIC METHODS =================================//

	/**
	 * Add a listener, to receive notification about a reached end of execution.
	 *
	 * @param listener - add listener, to be notified about the current state.
	 */
	void addFinishListener(@NotNull final EventListener listener );

	/**
	 * Removes a listener, and returns <code>true</code> if, and only if, the listener was not null and was existing in the listener list.
	 * @param listener - the listener to be removed
	 * @return
	 */
	boolean removeFinishListener(@Nullable final EventListener listener );

	//=============================  INNER CLASSES ==================================//

}