package org.easyone.examples.javaagent.asm;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class MyTransformer implements ClassFileTransformer {

	public static final String SERVLET_METHOD = "service";
	public static final String JAVAX_SERVLET_HTTP_HTTP_SERVLET = "javax.servlet.http.HttpServlet";
	Set<String> classNames = new HashSet<String>();
	
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		
		byte[] byteCode = classfileBuffer;
		
		String normalizeClassNames = className.replaceAll("/", ".");
		if(classNames.contains(normalizeClassNames)) {
			System.out.println("duplication class:" + normalizeClassNames);
		}else {
			classNames.add(normalizeClassNames);			
		}
		
		if(JAVAX_SERVLET_HTTP_HTTP_SERVLET.equalsIgnoreCase(normalizeClassNames)) {
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
            ClassPrinter visitor = new ClassPrinter(writer);
            reader.accept(visitor, 0);
            return writer.toByteArray();
		}	
		
		return byteCode;
	}
}
