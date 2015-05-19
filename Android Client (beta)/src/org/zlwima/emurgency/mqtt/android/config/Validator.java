package org.zlwima.emurgency.mqtt.android.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.zlwima.emurgency.mqtt.R;

import android.content.Context;
import android.content.res.Resources;

/**
 * weird...
 *
 * @author tom
 *
 */
public class Validator {

	private String[] patternStrings;
	private List<Pattern> patternList;
	private String[] errorList;

	public Validator( Context context ) {
		Resources ressources = context.getResources();
		patternStrings = ressources.getStringArray( R.array.validation_patterns );
		errorList = ressources.getStringArray( R.array.validation_errors );
		patternList = new ArrayList<Pattern>();
		for( String pattern : patternStrings ) {
			patternList.add( Pattern.compile( pattern ) );
		}
	}

	public String wrongEmail() {
		return errorList[0];
	}

	public String wrongFirstName() {
		return errorList[1];
	}

	public String wrongLastName() {
		return errorList[2];
	}

	public String wrongPassword() {
		return errorList[3];
	}

	public String wrongPasswordCheck() {
		return errorList[4];
	}

	public boolean checkEmail( String input ) {
		return patternList.get( 0 ).matcher( input ).matches();
	}

	public boolean checkFirstName( String input ) {
		return patternList.get( 1 ).matcher( input ).matches();
	}

	public boolean checkLastName( String input ) {
		return patternList.get( 2 ).matcher( input ).matches();
	}

	public boolean checkPassword( String input ) {
		return patternList.get( 3 ).matcher( input ).matches();
	}

	public boolean samePasswords( String pass1, String pass2 ) {
		return pass1.equals( pass2 );
	}
}
