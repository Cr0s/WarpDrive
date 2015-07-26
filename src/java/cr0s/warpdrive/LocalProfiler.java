package cr0s.warpdrive;

import java.util.Stack;

public class LocalProfiler {
	private static class StackElement {
		public long start;
		public long internal;
		public String name;
	}

	private static Stack<StackElement> stack = new Stack<StackElement>();

	public static void start(String name) {
		StackElement stackElement = new StackElement();
		stackElement.start = System.nanoTime();
		stackElement.internal = 0;
		stackElement.name = name;
		stack.push(stackElement);
	}

	public static void stop() {
		if (stack.isEmpty()) {
			return;
		}

		StackElement stackElement = stack.pop();
		long end = System.nanoTime();
		long dt = end - stackElement.start;

		if (!stack.isEmpty()) {
			StackElement nextStackElement = stack.peek();
			nextStackElement.internal += dt;
		}

		long self = (dt - stackElement.internal) / 1000; // in microseconds
		long total = dt / 1000;
		WarpDrive.logger.fine("Profiling '" + stackElement.name + "': " + (self / 1000.0F) + " ms, total: " + (total / 1000.0F) + " ms");
	}
}
