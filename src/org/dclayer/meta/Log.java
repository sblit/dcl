package org.dclayer.meta;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

/**
 * logging class used for message output
 */
public class Log {
	public static String PART_MAIN = "main",
			PART_NET_UDPSOCKET = "net/udpsocket",
			PART_NET_TCPSOCKET = "net/tcpsocket",
			PART_NET_TCPSOCKETCONNECTION = "net/tcpsocketconnection",
			PART_NET_PARSE = "net/parse",
			PART_NET_SERVICE_RECEIVES2S = "net/service/receiveS2S",
			PART_NET_SERVICE_RECEIVEA2S = "net/service/receiveA2S",
			PART_NET_SERVICE_SEND_UDP = "net/service/send/udp",
			PART_NET_SERVICE_SEND_TCP = "net/service/send/tcp",
			PART_NET_SERVICE_PROCESS_ADD = "net/service/process/add",
			PART_NET_SERVICE_PROCESS_REMOVE = "net/service/process/remove",
			PART_NET_SERVICE_PROCESS_INIT = "net/service/process/init",
			PART_NET_SERVICE_PROCESS_EXEC = "net/service/process/exec",
			PART_NET_SERVICE_PROCESS_FINALIZE = "net/service/process/finalize",
			PART_PROCESS = "process",
			PART_PROCESSRECEIVEQUEUE_RECEIVE = "processreceivequeue/receive",
			PART_PROCESSRECEIVEQUEUE_ADD = "processreceivequeue/add",
			PART_PROCESSRECEIVEQUEUE_DELIVERPACKET = "processreceivequeue/deliverpacket",
			PART_PROCESSRECEIVEQUEUE_REMOVE_SUCC = "processreceivequeue/remove/succ",
			PART_PROCESSRECEIVEQUEUE_REMOVE_FAIL = "processreceivequeue/remove/fail",
			PART_A2SPROCESSDELIVERYAGENT_RECEIVE = "A2Sprocessdeliveryagent/receive",
			PART_A2SPROCESSDELIVERYAGENT_ADD = "A2Sprocessdeliveryagent/add",
			PART_A2SPROCESSDELIVERYAGENT_DELIVERPACKET = "A2Sprocessdeliveryagent/deliverpacket",
			PART_A2SPROCESSDELIVERYAGENT_REMOVE_SUCC = "A2Sprocessdeliveryagent/remove/succ",
			PART_A2SPROCESSDELIVERYAGENT_REMOVE_FAIL = "A2Sprocessdeliveryagent/remove/fail",
			PART_ADDRESSCACHE_SETSTATUS = "addresscache/setstatus";

	/**
	 * log levels
	 */
	private enum Level {
		DEBUG, MSG, WARNING, ERROR, FATAL
	};

	/**
	 * returns the stack trace of an Exception as String
	 * @param e the Exception
	 * @return the stack trace of the given Exception as String
	 */
	private static String getStackTraceAsString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}

	/**
	 * logs a fatal error message
	 * @param part the part in which the error occurred
	 * @param o the Object in which the error occurred
	 * @param s the error message
	 */
	public static synchronized void fatal(String part, Object o, String s) {
		log(Level.FATAL, part, o, s);
	}

	/**
	 * logs a fatal Exception
	 * @param part the part in which the Exception was caught
	 * @param o the Object in which the Exception was caught
	 * @param e the Exception
	 */
	public static synchronized void fatal(String part, Object o, Exception e) {
		log(Level.FATAL, part, o, getStackTraceAsString(e));
	}

	/**
	 * logs a fatal Exception
	 * @param part the part in which the Exception was caught
	 * @param e the Exception
	 */
	public static synchronized void fatal(String part, Exception e) {
		log(Level.FATAL, part, getStackTraceAsString(e));
	}

	/**
	 * logs an error message
	 * @param part the part in which the error occurred
	 * @param o the Object in which the error occurred
	 * @param s the error message
	 */
	public static synchronized void error(String part, Object o, String s) {
		log(Level.ERROR, part, o, s);
	}

	/**
	 * logs an Exception
	 * @param part the part in which the Exception was caught
	 * @param o the Object in which the Exception was caught
	 * @param e the Exception
	 */
	public static synchronized void exception(String part, Object o, Exception e) {
		log(Level.ERROR, part, o, getStackTraceAsString(e));
	}

	/**
	 * logs a warning message
	 * @param part the part in which the warning was issued
	 * @param o the Object in which the warning was issued
	 * @param s the warning message
	 */
	public static synchronized void warning(String part, Object o, String s) {
		log(Level.WARNING, part, o, s);
	}

	/**
	 * logs a message
	 * @param part the part in which the message was issued
	 * @param o the Object in which the message was issued
	 * @param s the message
	 */
	public static synchronized void msg(String part, Object o, String s) {
		log(Level.MSG, part, o, s);
	}

	/**
	 * logs a debug message
	 * @param part the part in which the debug message was issued
	 * @param o the Object in which the debug message was issued
	 * @param s the debug message
	 */
	public static synchronized void debug(String part, Object o, String s) {
		log(Level.DEBUG, part, o, s);
	}

	/**
	 * logs a message with the given {@link Level}
	 * @param l the {@link Level}
	 * @param part the part in which the message was issued
	 * @param o the Object in which the message was issued
	 * @param s the message
	 */
	private static synchronized void log(Level l, String part, Object o, String s) {
		System.out.println(String.format("[%s] %s (%s): %s", l.name(), part, o != null ? o.getClass().getSimpleName() : null, s));
	}

	/**
	 * logs a message with the given {@link Level}
	 * @param l the {@link Level}
	 * @param part the part in which the message was issued
	 * @param s the message
	 */
	private static synchronized void log(Level l, String part, String s) {
		System.out.println(String.format("[%s] %s: %s", l.name(), part, s));
	}
	
	//
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
	
	private static synchronized void println(String s) {
		System.out.println(s);
	}
	
	private static String buildPath(HierarchicalLevel hierarchicalLevel) {
		LinkedList<String> levelPath = new LinkedList<String>();
		while(hierarchicalLevel != null) {
			levelPath.addFirst(hierarchicalLevel.toString());
			hierarchicalLevel = hierarchicalLevel.getParentHierarchicalLevel();
		}
		StringBuilder stringBuilder = new StringBuilder();
		for(String level : levelPath) {
			stringBuilder.append("/");
			stringBuilder.append(level);
		}
		return stringBuilder.toString();
	}
	
	private static void log(Level l, HierarchicalLevel hierarchicalLevel, String format, Object... args) {
		println(String.format("%s [%s] %s: %s",
				DATE_FORMAT.format(Calendar.getInstance().getTime()),
				l.name(),
				buildPath(hierarchicalLevel),
				String.format(format, args)));
	}
	
	public static void debug(HierarchicalLevel hierarchicalLevel, String format, Object... args) {
		log(Level.DEBUG, hierarchicalLevel, format, args);
	}
}