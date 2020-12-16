package org.easyone.examples.javaagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

public class MyTransformer implements ClassFileTransformer {

	Set<ClassLoader> classloaders = new HashSet<>();
	Set<String> classNames = new HashSet<String>();
	
	ClassPool classPool = ClassPool.getDefault();
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		
		byte[] byteCode = classfileBuffer;
		
		if(!classloaders.contains(loader)) {
			classloaders.add(loader);
			classPool.appendClassPath(new LoaderClassPath(loader));
		}
		
		String normalizeClassNames = className.replaceAll("/", ".");
		if(classNames.contains(normalizeClassNames)) {
			System.out.println("duplication class:" + normalizeClassNames);
		}else {
			classNames.add(normalizeClassNames);			
		}
		
		try {
			if("javax.servlet.http.HttpServlet".equalsIgnoreCase(normalizeClassNames)) {
				System.out.println("captured javax.servlet.http.HttpServlet");
                CtClass ctClass = classPool.get("javax.servlet.http.HttpServlet");
                CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
                System.out.println(Arrays.toString(declaredMethods));
                
                for(CtMethod ctMethod: declaredMethods) {
                	String methodName = ctMethod.getName();
                	String methodSignature = ctMethod.getSignature();
                	if("service".equals(methodName)) {
                		if("(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V".equals(methodSignature)) {
                			ctMethod.addLocalVariable("startTime", CtClass.longType);
                			ctMethod.insertBefore("startTime = System.currentTimeMillis();");
                			
                			StringBuilder endBlock = new StringBuilder();
                			
                			ctMethod.addLocalVariable("endTime", CtClass.longType);
                			ctMethod.addLocalVariable("opTime", CtClass.longType);
                			endBlock.append("endTime = System.currentTimeMillis();");
                			endBlock.append("opTime = endTime-startTime;");
                			
                			endBlock.append("System.out.println(\"[Application] javax.servlet.http.HttpServlet.service operation completed in:\" + opTime + \" Millis!\");");
                			
                			ctMethod.insertAfter(endBlock.toString());                			
                		}
                	}
                }
                byteCode = ctClass.toBytecode();
                ctClass.detach();
			}else if("org.springframework.web.servlet.DispatcherServlet".equalsIgnoreCase(normalizeClassNames)) {
				System.out.println("captured org.springframework.web.servlet.DispatcherServlet");
	            CtClass ctClass = classPool.get("org.springframework.web.servlet.DispatcherServlet");
	            CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
	            System.out.println(Arrays.toString(declaredMethods));
			}
		}catch (Exception e) {
			System.out.println("Exception:" + e);
		}
		
		return byteCode;
	}
}
