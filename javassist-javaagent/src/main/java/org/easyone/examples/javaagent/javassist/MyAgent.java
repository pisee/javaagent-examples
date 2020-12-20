package org.easyone.examples.javaagent.javassist;

import java.lang.instrument.Instrumentation;

public class MyAgent {

	public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("start Javassist premain");
		inst.addTransformer(new MyTransformer());
	}
	
	public static void agentmain(String agentArgs, Instrumentation inst) {
		System.out.println("start Javassist agentmain");
		inst.addTransformer(new MyTransformer());
	}
}
