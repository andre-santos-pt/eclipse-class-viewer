package pt.iscte.eclipse.classviewer;

import pt.iscte.eclipse.classviewer.model.JAssociation;
import pt.iscte.eclipse.classviewer.model.JModel;
import pt.iscte.eclipse.classviewer.model.JOperation;
import pt.iscte.eclipse.classviewer.model.JType;

public interface DiagramListener {
	
	void diagramEvent(JModel model, Event event);
	
	void classEvent(JType type, Event event);
	
	void operationEvent(JOperation operation, Event event);
	
	void dependencyEvent(JAssociation association, Event event);
	
	enum Event {
		ADD,
		SELECT,
		DOUBLE_CLICK;
	}
	
	class Adapter implements DiagramListener {
		public void diagramEvent(JModel model, Event event) { }
		public void classEvent(JType type, Event event) { }
		public void operationEvent(JOperation operation, Event event) { }
		public void dependencyEvent(JAssociation association, Event event) { }
	}
	
	public static class EventFilter implements DiagramListener {
		private DiagramListener listener;
		private Event event;
		
		public EventFilter(DiagramListener listener, Event event) {
			if(listener == null || event == null)
				throw new NullPointerException("args cannot be null");
			this.event = event;
			this.listener = listener;
		}
		
		@Override
		public void diagramEvent(JModel model, Event event) {
			if(event.equals(this.event))
				listener.diagramEvent(model, event);
		}

		@Override
		public void classEvent(JType type, Event event) {
			if(event.equals(this.event))
				listener.classEvent(type, event);
			
		}

		@Override
		public void operationEvent(JOperation operation, Event event) {
			if(event.equals(this.event))
				listener.operationEvent(operation, event);
		}

		@Override
		public void dependencyEvent(JAssociation association, Event event) {
			if(event.equals(this.event))
				listener.dependencyEvent(association, event);
		}
		
	}
	
	public static class EventAdapter implements DiagramListener {
		
		@Override
		public void diagramEvent(JModel model, Event event) {
			
		}

		@Override
		public void classEvent(JType type, Event event) {
			
		}

		@Override
		public void operationEvent(JOperation operation, Event event) {
		
		}

		public void operationEvent(JOperation operation) {
			
		}

		@Override
		public void dependencyEvent(JAssociation association, Event event) {
			
		}
	}
}
