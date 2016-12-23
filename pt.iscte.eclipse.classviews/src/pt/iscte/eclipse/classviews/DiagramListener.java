package pt.iscte.eclipse.classviews;

import pt.iscte.eclipse.classviews.model.JAssociation;
import pt.iscte.eclipse.classviews.model.VModel;
import pt.iscte.eclipse.classviews.model.VOperation;
import pt.iscte.eclipse.classviews.model.VType;

public interface DiagramListener {
	
	void diagramEvent(VModel model, Event event);
	
	void classEvent(VType type, Event event);
	
	void operationEvent(VOperation operation, Event event);
	
	void dependencyEvent(JAssociation association, Event event);
	
	enum Event {
		ADD,
		SELECT,
		DOUBLE_CLICK;
	}
	
	class Adapter implements DiagramListener {
		public void diagramEvent(VModel model, Event event) { }
		public void classEvent(VType type, Event event) { }
		public void operationEvent(VOperation operation, Event event) { }
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
		public void diagramEvent(VModel model, Event event) {
			if(event.equals(this.event))
				listener.diagramEvent(model, event);
		}

		@Override
		public void classEvent(VType type, Event event) {
			if(event.equals(this.event))
				listener.classEvent(type, event);
			
		}

		@Override
		public void operationEvent(VOperation operation, Event event) {
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
		public void diagramEvent(VModel model, Event event) {
			
		}

		@Override
		public void classEvent(VType type, Event event) {
			
		}

		@Override
		public void operationEvent(VOperation operation, Event event) {
		
		}

		public void operationEvent(VOperation operation) {
			
		}

		@Override
		public void dependencyEvent(JAssociation association, Event event) {
			
		}
	}
}
