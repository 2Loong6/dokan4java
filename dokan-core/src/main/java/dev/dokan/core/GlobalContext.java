package dev.dokan.core;

//TODO: idea for DokanOptions Global context. We can even parameterize the type!
public abstract class GlobalContext<T> {

	public T to() {
		return null;
	}

	public static <T> GlobalContext<T> from(T obj) {
		//add 64bit check
		return new GlobalContext<T>() {
		};
	}


}
