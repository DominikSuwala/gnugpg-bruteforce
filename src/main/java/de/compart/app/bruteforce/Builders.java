package de.compart.app.bruteforce;

import de.compart.common.Builder;
import de.compart.common.Generator;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * User: torsten
 * Date: 2012/11
 * Time: 23:40
 *
 */
public class Builders {
	//============================== CLASS VARIABLES ================================//
	//=============================== CLASS METHODS =================================//

	public static KeyGeneratorBuilder createKeyGeneratorBuilder() {
		return new KeyGeneratorBuilder();
	}

	public static KeyAccessBuilder createKeyAccessBuilder() {
		return new KeyAccessBuilder();
	}

	//===============================  VARIABLES ====================================//
	//==============================  CONSTRUCTORS ==================================//
	//=============================  PUBLIC METHODS =================================//
	//======================  PROTECTED/PACKAGE METHODS =============================//
	//============================  PRIVATE METHODS =================================//
	//=============================  INNER CLASSES ==================================//
	@SuppressWarnings( "MethodReturnOfConcreteClass" ) // the inspection cannot be full-filled with a builder-pattern
	public static class KeyGeneratorBuilder implements Builder<Generator<String>> {

		private String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		private String prefix = "";
		private String suffix = "";

		private int minimumLength = 0;
		private int maximumLength = 99;

		public KeyGeneratorBuilder withAlphabet( final String alphabet ) {
			this.alphabet = alphabet;
			return this;
		}

		public KeyGeneratorBuilder withPrefix( final String prefix ) {
			this.prefix = prefix;
			return this;
		}

		public KeyGeneratorBuilder withSuffix( final String suffix ) {
			this.suffix = suffix;
			return this;
		}

		public KeyGeneratorBuilder withMinimumLength( final int minimumLength ) {
			this.minimumLength = minimumLength;
			return this;
		}

		public KeyGeneratorBuilder withMaximumLength( final int maximumLength ) {
			this.maximumLength = maximumLength;
			return this;
		}

		@Override
		public KeyGenerator build() {
			return new KeyGenerator( this.alphabet, this.prefix, this.suffix, this.minimumLength, this.maximumLength );
		}
	}

	//=============================  INNER CLASSES ==================================//
	public static class KeyAccessBuilder implements Builder<GnuPGKeyAccess> {

		@NotNull
		private String text = "";

		@NotNull
		private BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

		public KeyAccessBuilder withText( final String text ) {
			this.text = text;
			return this;
		}

		public KeyAccessBuilder withText( final File file ) throws IOException {
			final BufferedReader reader = new BufferedReader( new FileReader( file ) );
			final StringBuilder builder = new StringBuilder();

			try {
				String line = null;
				while ( ( line = reader.readLine() ) != null ) {
					builder.append( line );
				}
			} finally {
				reader.close();
			}
			this.text = builder.toString();
			return this;
		}

		public KeyAccessBuilder withQueue( final BlockingQueue<String> inputQueue) {
			this.queue = inputQueue;
			return this;
		}

		@Override
		public GnuPGKeyAccess build() {
			return new GnuPGKeyAccess(this.text, this.queue);
		}
	}

}