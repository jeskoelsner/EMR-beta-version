package org.zlwima.emurgency.webapp;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.vaadin.server.VaadinServlet;

import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.zlwima.emurgency.backend.model.EmrCaseData;

//@WebServlet(value = "/*", asyncSupported = true, loadOnStartup = 1)
//@VaadinServletConfiguration(productionMode = true, ui = VaadinUI.class)
public class StartupServlet extends VaadinServlet {

	private static Timer caseTimeoutTimer = null;

	public static Publisher PUBLISHER = null;
	public static Profiler PROFILER = null;
	public static Set<EmrCaseData> CASES = Collections.newSetFromMap( new ConcurrentHashMap<EmrCaseData, Boolean>() );

	@Override
	public void init( ServletConfig servletConfig ) throws ServletException {
		super.init( servletConfig );
		System.out.println( "********** MyVaadinServlet.init() **********" );

		if( PROFILER == null ) {
			PROFILER = Profiler.getProfiler();
		}

		if( PUBLISHER == null ) {
			PUBLISHER = new Publisher( "tcp://137.226.188.142:50026", "EMR-" );
			PUBLISHER.start();
		}

		startCaseTimeoutTimer();
	}

	@Override
	public void destroy() {
		System.out.println( "********** VaadinServlet.destroy() **********" );
		if( PUBLISHER != null ) {
			PUBLISHER.stop();
		}
		caseTimeoutTimer.cancel();
	}

	public static void checkForCaseTimeouts() {
		for( EmrCaseData aCase : CASES ) {
			aCase.setCaseRunningTimeMillis( System.currentTimeMillis() - aCase.getCaseStartTimeMillis() );
			if( aCase.getCaseRunningTimeMillis() > aCase.getCaseTimeOutValue() ) {
				System.out.println( "TIMING OUT CASE AND REMOVING FROM UI " + aCase.getCaseId() );
				PUBLISHER.refreshUIListeners( Publisher.CaseReply.CLOSE_CASE, aCase );
				CASES.remove( aCase );
			} else {
				PUBLISHER.refreshUIListeners( Publisher.CaseReply.UPDATE_CASE, aCase );
			}
		}
	}

	private void startCaseTimeoutTimer() {
		TimerTask refreshCasesTimerTask = new TimerTask() {
			@Override
			public void run() {
				checkForCaseTimeouts();
			}
		};
		if( caseTimeoutTimer != null ) {
			caseTimeoutTimer.cancel();
		}

		caseTimeoutTimer = new Timer();
		caseTimeoutTimer.schedule( refreshCasesTimerTask, 5000, 5000 );
	}

}
