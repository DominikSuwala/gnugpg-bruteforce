package de.compart.app.bruteforce;

import de.compart.common.Maybe;
import de.compart.common.command.Task;
import de.compart.common.mvc.DataModel;
import de.compart.common.mvc.DefaultDataModel;
import de.compart.gui.cli.impl.CommandLineCommand;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GnuPG {
	//============================== CLASS VARIABLES ================================//
	private static final String EXECUTABLE = "/usr/local/bin/gpg --armor --yes --always-trust";

	private static final Logger LOG = LoggerFactory.getLogger( GnuPG.class );
	private static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

	//=============================== CLASS METHODS =================================//
	public static GnuPG encrypt( final String inStr ) throws IOException {
		GnuPG encrypt = new GnuPG( GnuPGProcesses.SYMMETRIC_ENCRYPT ).withText( inStr );
		return encrypt.withOutput( encrypt.createTempFile( false ) );
	}

	public static GnuPG encrypt( final String inStr, final String recipient ) throws IOException {
		GnuPG encrypt = new GnuPG( GnuPGProcesses.ENCRYPT ).withText( inStr ).withRecipient( recipient );
		return encrypt.withOutput( encrypt.createTempFile( false ) );
	}

	public static GnuPG decrypt( final String inStr ) throws IOException {
		GnuPG decrypt = new GnuPG( GnuPGProcesses.DECRYPT ).withText( inStr );
		return decrypt.withOutput( decrypt.createTempFile( false ) );
	}

	public static GnuPG sign( final String inStr ) {
		return new GnuPG( GnuPGProcesses.SIGN ).withText( inStr );
	}

	public static GnuPG clearSign( final String inStr ) {
		return new GnuPG( GnuPGProcesses.CLEAR_SIGN ).withText( inStr );
	}

	public static GnuPG signAndEncrypt( final String inStr, final String recipient ) {
		return new GnuPG( GnuPGProcesses.SIGN_ENCRYPT ).withText( inStr ).withRecipient( recipient );
	}

	//===============================  VARIABLES ====================================//
	private final GnuPGProcesses type;
	private final DataModel<GnuPGDataModelKeys> dataModel = new DefaultDataModel<GnuPGDataModelKeys>();

	//==============================  CONSTRUCTORS ==================================//
	protected GnuPG( final GnuPGProcesses processType ) {
		this.type = processType;
	}

	//=============================  PUBLIC METHODS =================================//
	public GnuPG withPassPhrase( final String passPhrase ) {
		this.dataModel.set( GnuPGDataModelKeys.PASSPHRASE, passPhrase );
		return this;
	}

	@NotNull
	public synchronized GnuPGResult execute() throws IOException {
		final CommandLineCommand command = this.createCommandLineCommand( dataModel );
		final Task task = new Task( command );
		task.doTask();
		if ( task.finished() && task.successful() ) {
			final String output = command.getOutput();
			if ( output != null && !output.isEmpty() ) {
				return new SimpleGnuPGResult( true, output );
			} else {
				final Maybe<String> outputFile = dataModel.get( GnuPGDataModelKeys.OUTPUT, String.class );
				if ( outputFile.isJust() ) {
					return new SimpleGnuPGResult( true, this.writeFileToText( new File( outputFile.get() ) ) );
				}
			}
		}

		return new SimpleGnuPGResult( false, command.getError() );
	}

	//======================  PROTECTED/PACKAGE METHODS =============================//
	//============================  PRIVATE METHODS =================================//
	private GnuPG withText( final String textToWorkOn ) {
		try {
			dataModel.set( GnuPGDataModelKeys.INPUT, this.writeTextToFile( textToWorkOn ).getAbsolutePath() );
		} catch ( IOException e ) {
			LOG.error( "Unable to write text to a temporary file: '" + textToWorkOn + "'", e );
			dataModel.set( GnuPGDataModelKeys.INPUT, textToWorkOn );
		}
		return this;
	}

	private GnuPG withRecipient( final String recipient ) {
		this.dataModel.set( GnuPGDataModelKeys.RECIPIENT, recipient );
		return this;
	}

	private GnuPG withOutput( final File outputFile ) {
		if ( outputFile.exists() && !outputFile.delete() ) {
			outputFile.deleteOnExit();
		}
		this.dataModel.set( GnuPGDataModelKeys.OUTPUT, outputFile.getAbsolutePath() );
		return this;
	}

	private String writeFileToText( final File fileToBeRead ) throws IOException {
		final StringBuilder builder = new StringBuilder();
		if ( fileToBeRead.exists() ) {
			LineNumberReader reader = new LineNumberReader( new FileReader( fileToBeRead ) );
			String line = null;
			try {
				while ( ( line = reader.readLine() ) != null ) {
					if ( reader.getLineNumber() > 1 ) { // if you compare to >0: then this means the first line already...
						builder.append( LINE_SEPARATOR );
					}
					builder.append( line );
				}
			} finally {
				reader.close();
				fileToBeRead.deleteOnExit();
			}
		}

		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Read file {}: with content with a length of {}: " + builder.toString(), fileToBeRead.getAbsolutePath(), builder.length() );
		}

		return builder.toString();
	}

	private File writeTextToFile( final String text ) throws IOException {
		File tmpFile = this.createTempFile( true );
		Writer writer = null;
		try {
			writer = new BufferedWriter( new FileWriter( tmpFile ) );
			writer.write( text );
			writer.flush();
		} finally {
			if ( writer != null ) {
				writer.close();
			}
		}
		tmpFile.deleteOnExit();
		return tmpFile;
	}

	private File createTempFile( final boolean existing ) throws IOException {
		File temporaryFile = File.createTempFile( "GnuPG", ".tmp" );

		if ( !existing ) {
			while ( temporaryFile.exists() ) {
				if ( !temporaryFile.delete() ) {
					temporaryFile.deleteOnExit();
				}
			}
		}
		return temporaryFile;
	}

	private CommandLineCommand createCommandLineCommand( final DataModel<GnuPGDataModelKeys> dataModel ) {
		if ( dataModel.get( GnuPGDataModelKeys.INPUT, String.class ).isNothing() ) {
			throw new IllegalArgumentException( "Requiring a valid input value of class " + String.class.getSimpleName() );
		}
		final StringBuilder builder = new StringBuilder().append( EXECUTABLE ).append( " " ).append( type.executionSpecificArguments );
		for ( GnuPGDataModelKeys key : this.type.modelKeys ) {
			this.appendToStringBuilder( builder, dataModel, key );
		}
		this.appendToStringBuilder( builder, dataModel, GnuPGDataModelKeys.PASSPHRASE );
		this.appendToStringBuilder( builder, dataModel, GnuPGDataModelKeys.OUTPUT );
		this.appendToStringBuilder( builder, dataModel, GnuPGDataModelKeys.INPUT );
		return new CommandLineCommand( builder.toString(), dataModel.get( GnuPGDataModelKeys.ARGUMENT, String.class ).get() );
	}

	private void appendToStringBuilder( StringBuilder builder, DataModel<GnuPGDataModelKeys> dataModel, GnuPGDataModelKeys key ) {
		if ( dataModel.get( key ).isJust() ) {
			builder.append( " " ).append( key.argumentName ).append( " " ).append( dataModel.get( key ).get() );
		} else if ( !key.isRequiringArgument() ) {
			builder.append( " " ).append( key.argumentName );
		} else {
			throw new IllegalArgumentException( String.format( "[%s]: Argument '%s' requires a valid value.", GnuPG.class.getSimpleName(), key.argumentName ) );
		}
	}

	//=============================  INNER CLASSES ==================================//
	private static enum GnuPGDataModelKeys {
		OUTPUT( "--output", true ),
		/**
		 * This argument should be used, if you want to provide an argument, which is passed during execution
		 * in the process output stream.
		 */
		ARGUMENT,

		/**
		 * This key is required for all non-symmetric encryptions, like 'encrypt' and 'sign_and_encrypt (se)'
		 */
		RECIPIENT( "--recipient", true ),

		/**
		 * This produces a readable encrypted text, which can be processed further
		 */
		ARMOR( "--armor" ),
		/**
		 * This key helps in enabling the batch modus, which suppress every debug or info output from
		 * gpg, however, there will be no more prompt of passwords
		 */
		BATCH( "--batch" ),

		/**
		 * If you want to suppress errors, because encrypted files may already exist with the same name,
		 * then you should call this key
		 */
		OVERRIDE( "--yes" ),

		/**
		 * If you want to express your passphrase with the command line command, then use this key, which
		 * helps in a better to understand function
		 */
		PASSPHRASE( "--passphrase", true ),

		/**
		 * Standard output redirection
		 */
		STDOUT( "--passphrase-fd 0" ),

		/**
		 * Well, this option disables actually the whole process, because with this option, you will be
		 * unable to create a valid process. This option can be called only, while running as a daemon
		 * process without any text typed.
		 */
		NO_TTY( "--no-tty" ),

		/**
		 * This enum value is meant to describe the input file or text provided by the command line
		 */
		INPUT;

		@NotNull
		private final String argumentName;
		private final boolean needsArgument;

		private GnuPGDataModelKeys() {
			this( "" );
		}

		private GnuPGDataModelKeys( final String argumentName ) {
			this( argumentName, false );
		}

		private GnuPGDataModelKeys( final String argumentName, final boolean requiresArgument ) {
			this.argumentName = argumentName;
			this.needsArgument = requiresArgument;
		}

		public boolean isRequiringArgument() {
			return this.needsArgument;
		}
	}

	//=============================  INNER CLASSES ==================================//
	private static enum GnuPGProcesses {
		SIGN( "--sign", GnuPGDataModelKeys.STDOUT, GnuPGDataModelKeys.ARMOR, GnuPGDataModelKeys.BATCH ),
		CLEAR_SIGN( "--clearsign", GnuPGDataModelKeys.STDOUT, GnuPGDataModelKeys.ARMOR, GnuPGDataModelKeys.BATCH ),
		SIGN_ENCRYPT( "-se", GnuPGDataModelKeys.STDOUT, GnuPGDataModelKeys.RECIPIENT, GnuPGDataModelKeys.BATCH, GnuPGDataModelKeys.ARMOR ),
		ENCRYPT( "--encrypt", GnuPGDataModelKeys.RECIPIENT, GnuPGDataModelKeys.BATCH, GnuPGDataModelKeys.ARMOR ),
		DECRYPT( "--decrypt" ),
		SYMMETRIC_ENCRYPT( "--symmetric", GnuPGDataModelKeys.ARMOR );

		@NotNull
		private final String executionSpecificArguments;

		@NotNull
		private final List<GnuPGDataModelKeys> modelKeys;

		private GnuPGProcesses( final String gnuPGSpecificCommandArguments, final GnuPGDataModelKeys... modelKeys ) {
			this.executionSpecificArguments = gnuPGSpecificCommandArguments;
			if ( modelKeys == null ) {
				this.modelKeys = Collections.emptyList();
			} else {
				this.modelKeys = Arrays.asList( modelKeys );
			}
		}


	}

	//=============================  INNER CLASSES ==================================//
	public static class SimpleGnuPGResult implements GnuPGResult {

		private final String output;
		private final boolean successful;

		public SimpleGnuPGResult( final boolean successful, final String output ) {
			this.successful = successful;
			this.output = output;
		}

		@Override
		public boolean isSuccessful() {
			return successful;
		}

		@Override
		public String getValue() {
			return output;
		}
	}

	//=============================  INNER CLASSES ==================================//
}