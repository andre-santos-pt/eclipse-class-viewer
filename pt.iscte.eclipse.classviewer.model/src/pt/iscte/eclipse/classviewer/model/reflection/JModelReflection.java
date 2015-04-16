package pt.iscte.eclipse.classviewer.model.reflection;

import java.lang.reflect.Method;

import pt.iscte.eclipse.classviewer.model.JClass;
import pt.iscte.eclipse.classviewer.model.JInterface;
import pt.iscte.eclipse.classviewer.model.JOperation;

public class JModelReflection {

	public static JClass createClass(Class<?> c) {
		if(c.isInterface())
			throw new IllegalArgumentException("not a class - " + c.getName());
		
		JClass jc = new JClass(c.getName());

		for(Method m : c.getDeclaredMethods()) {
			new JOperation(jc, m.getName());
		}
		return jc;
	}
	
	
	public static JInterface createInterface(Class<?> c) {
		if(!c.isInterface())
			throw new IllegalArgumentException("not an interface - " + c.getName());
		
		JInterface ji = new JInterface(c.getName());
		for(Method m : c.getDeclaredMethods()) {
			new JOperation(ji, m.getName());
		}
		
		return ji;
	}
	
	
}
